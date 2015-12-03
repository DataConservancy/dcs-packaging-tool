/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;
import org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil;
import org.dataconservancy.packaging.tool.ser.StreamMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.PropertyDescriptor;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * Responsible for (de)serializing <em>annotated</em> fields of {@link PackageState}.  Callers may request that all
 * annotated fields be serialized by calling {@code serialize(PackageState, OutputStream)}, or callers may specify a
 * particular annotated field to be serialized by calling {@code serialize(PackageState, StreamId, OutputStream)}
 * (i.e. by specifying the annotated field's stream identifier).
 * <p>
 * In order for fields of {@code PackageState} to be serialized, they must be annotated with
 * {@link org.dataconservancy.packaging.tool.model.ser.Serialize}, and have a so-called "stream identifier" assigned.
 * If a field has not been annotated, then it cannot be serialized by this implementation.  This implementation
 * <em>does not serialize the entirety</em> of a {@code PackageState} instance; it will only serialize fields that are
 * annotated.  Annotated fields must have standard JavaBean accessors and mutators (getXXX and setXXX methods).
 * </p>
 * <p>
 * <strong>Examples</strong>
 * </p>
 * <p>
 * Serializing package metadata to a file
 * </p>
 * <pre>
 *     // Assume the existence of a populated PackageState instance
 *     PackageState state = ...
 *
 *     // Instantiate and configure the serializer, or have it dependency injected
 *     AnnotationDrivenPackageStateSerializer serializer = new AnnotationDrivenPackageStateSerializer();
 *     serializer.setArchive(false);  // we're just serializing a single stream,
 *                                    // so we don't need a zip archive
 *
 *     File packageMd = new File("packageMetadata.out");
 *     serializer.serialize(state, StreamId.PACKAGE_METADATA, new FileOutputStream(packageMd));
 * </pre>
 * <p>
 * Serializing package state before closing the Package Tool GUI
 * </p>
 * <pre>
 *     // Assume the existence of a populated PackageState instance
 *     PackageState state = ...
 *
 *     // Instantiate and configure the serializer, or have it dependency injected
 *     AnnotationDrivenPackageStateSerializer serializer = new AnnotationDrivenPackageStateSerializer();
 *     serializer.setArchive(true);  // we're serializing all annotated fields,
 *                                   // so we'll put all the streams in a
 *                                   // zip archive
 *
 *     File myPackage = new File("mypackage.zip");
 *     serializer.serialize(state, new FileOutputStream(packageMd));
 * </pre>
 * <p>
 * <strong>Configuration</strong>
 * </p>
 * <p>
 * This class has two main sets of configuration properties.  Those that pertain to creating archives, and
 * (un)marshallers used for the fields on the {@code PackageState}.
 * </p>
 * <p>
 * <strong>Archive-related configuration:</strong>
 * </p>
 * <dl>
 * <dt>archive</dt>
 * <dd>Flag used during serialization, controlling whether or not the output produces an archive (zip or tar).
 * When this flag is false, other archive-related properties are not consulted.</dd>
 * <dt>arxStreamFactory</dt>
 * <dd>This is the abstraction used to create {@link ArchiveOutputStream} and {@link ArchiveEntry} objects</dd>
 * <dt>failOnChecksumMismatch</dt>
 * <dd>Flag used during deserialization, controlling whether or not an {@link StreamChecksumMismatch exception} is
 * thrown when encountering an invalid checksum when reading streams from a package state</dd>
 * </dl>
 * <p>
 * <strong>Marshalling-related configuration:</strong>
 * </p>
 * <dl>
 * <dt>marshallerMap</dt>
 * <dd>Maps the (un)marshallers to the streams that they (de)serialize.  Typically this will be
 * dependency injected.  In order for this implementation to function properly, each {@link StreamId}
 * should be represented in this {@code Map} with associated {@link StreamMarshaller}s</dd>
 * </dl>
 */
public class AnnotationDrivenPackageStateSerializer implements PackageStateSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationDrivenPackageStateSerializer.class);

    /**
     * Placeholders: requested stream id, PackageState class name, list of possible stream ids from the PackageState
     * class (obtained at runtime)
     */
    private static final String ERR_INVALID_STREAMID = "Unable to obtain streamId '%s' from the '%s' class.  The " +
            "streamId may be invalid, or the expected JavaBean accessor method is missing or not accessible.  " +
            "Allowed stream identifiers are: %s.";

    /**
     * Placeholders: StreamId
     */
    private static final String ERR_MISSING_DESCRIPTOR = "No PropertyDescriptor found for streamId '%s'";

    /**
     * Placeholders: method name, class name, error message
     */
    private static final String ERR_INVOKING_METHOD = "Error invoking the method named '%s' on an instance of '%s': %s";

    private static final String ERR_MISSING_MARSHALLINGMAP = "No marshalling map has been specified.  Please call 'setMarshallerMap(...) first.";

    /**
     * Placeholders: streamId
     */
    private static final String ERR_MISSING_STREAMMARSHALLER = "No StreamMarshaller found for streamId '%s'.  Check " +
            "the 'marshallerMap' supplied to setMarshallerMap(...).";

    /**
     * Placeholders: streamId, streamId
     */
    private static final String ERR_MISSING_SPRINGMARSHALLER = "No Spring Marshaller found for streamId '%s'.  Check " +
            "the StreamMarshaller for streamId '%s' in the 'marshallerMap' supplied to setMarshallerMap(...).";

    /**
     * Placeholders: streamId, error message
     */
    private static final String ERR_MARSHALLING_STREAM = "Error marshalling streamId '%s': %s";


    /**
     * Placeholders: streamId, error message
     */
    private static final String ERR_UNMARSHALLING_ARCHIVE = "Error unmarshalling package state stream from archive: %s";

    /**
     * Placeholders: streamId, error message
     */
    private static final String ERR_UNMARSHALLING_STREAMID_ARCHIVE = "Error unmarshalling streamId '%s' from archive: %s";

    /**
     * Placeholders: streamId, error message
     */
    private static final String ERR_UNMARSHALLING_STREAM = "Error unmarshalling streamId '%s': %s";

    /**
     * Placeholders: streamid, expected crc, actual crc
     */
    private static final String WARN_CRC_MISMATCH = "CRC for stream %s did not match.  Expected: %s Actual: %s";

    /**
     * Placeholders: streamid
     */
    private static final String WARN_UNKNOWN_STREAM = "Encountered unknown stream identifer '%s'.  Skipping it.";

    /**
     * Whether or not we are serializing streams into an archive (zip or tar)
     */
    private boolean archive = true;

    /**
     * Abstraction used to support the creation of ArchiveOutputStream and ArchiveEntry instances for
     * a particular archive format (when {@link #archive} is {@code true}). Because we are not supporting multiple
     * archive formats at this time (the requirement is to support ZIP only), this field is set to the
     * ZipArchiveStreamFactory.
     */
    private ArchiveStreamFactory arxStreamFactory = new ZipArchiveStreamFactory();

    /**
     * A Map containing the Marshaller and Unmarshaller implementation for each field in PackageState that
     * may be (de)serialized by this implementation.
     */
    private Map<StreamId, StreamMarshaller> marshallerMap;

    /**
     * A Map containing PropertyDescriptors for each field in PackageState that may be (de)serialized by this
     * implementation.  The PropertyDescriptor is used to access the field in the PackageState using reflection.
     */
    private Map<StreamId, PropertyDescriptor> propertyDescriptors =
            SerializationAnnotationUtil.getStreamDescriptors(PackageState.class);

    /**
     * A flag controlling whether or not an exception will be thrown when a stream's calculated checksum does
     * not match its actual checksum.
     */
    private boolean failOnChecksumMismatch = false;

    /**
     * {@inheritDoc}
     * <p>
     * Behaves as
     * </p>
     * <pre>
     * deserialize(state, null, in)
     * </pre>
     *
     * @param state {@inheritDoc}
     * @param in {@inheritDoc}
     * @throws RuntimeException when there are errors accessing fields of the state using reflection
     * @throws StreamChecksumMismatch when {@code failOnChecksumMismatch} is {@code true} and a checksum mismatch is
     *                                encountered
     */
    @Override
    public void deserialize(PackageState state, InputStream in) {
        deserialize(state, null, in);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will serialize all fields in the {@link PackageState} annotated with {@link Serialize} to the supplied
     * {@code OutputStream}.  It is recommended that when calling this method, the caller also set the {@link
     * #setArchive(boolean) archive flag} to {@code true}.  Because {@code PackageState} contains multiple fields, and
     * because each field is serialized as a distinct stream, this method will result in multiple streams being
     * serialized to the same {@code OutputStream}.  If {@code archive} is {@code true}, each stream will have a
     * checksum calculated and included in the output.
     * </p>
     *
     * @param state the package state containing the annotated fields to be serialized
     * @param out   the output stream to serialize to
     */
    @Override
    public void serialize(PackageState state, OutputStream out) {

        // If we are archiving, create an ArchiveOutputStream and serialize each stream from the PackageState
        // to the ArchiveOutputStream.
        //
        // If we aren't archiving, simply serialize each stream from the PackageState to the supplied OutputStream.

        if (archive) {
            try (ArchiveOutputStream aos = arxStreamFactory.newArchiveOutputStream(out)) {
                propertyDescriptors.keySet().stream().forEach(streamId -> serializeToArchive(state, streamId, aos));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            propertyDescriptors.keySet().stream().forEach(streamId -> {
                StreamResult result = new StreamResult(out);
                serializeToResult(state, streamId, result);
            });
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will serialize the specified field in the {@link PackageState} annotated with {@link Serialize} to the
     * supplied {@code OutputStream}.  If {@code archive} is {@code true}, the stream will have a checksum calculated
     * and included in the output.
     * </p>
     *
     * @param state    the package state containing the stream identified by {@code streamId}
     * @param streamId the stream within the package state to serialize
     * @param out      the output stream to serialize to
     */
    @Override
    public void serialize(PackageState state, StreamId streamId, OutputStream out) {

        // If we are archiving, create an ArchiveOutputStream and serialize the specified stream from the PackageState
        // to the ArchiveOutputStream.
        //
        // If we aren't archiving, simply serialize the specified stream from the PackageState to the supplied
        // OutputStream.

        if (archive) {
            try (ArchiveOutputStream aos = arxStreamFactory.newArchiveOutputStream(out)) {
                serializeToArchive(state, streamId, aos);
            } catch (IOException e) {
                throw new RuntimeException(String.format(ERR_MARSHALLING_STREAM, streamId, e.getMessage()), e);
            }
        } else {
            StreamResult result = new StreamResult(out);
            serializeToResult(state, streamId, result);
        }
    }

    /**
     * Serializes the identified stream from the package state to the supplied archive output stream.
     *
     * @param state    the package state object containing the identified stream
     * @param streamId the stream identifier for the content being serialized
     * @param aos      the archive output stream to serialize the stream to
     */
    void serializeToArchive(PackageState state, StreamId streamId, ArchiveOutputStream aos) {

        // when writing to an archive file:
        //   1. read the stream to be serialized, to get its properties
        //   2. create the archive entry using the properties and write it to the archive stream
        //   3. write the stream to be serialized to the archive stream
        //   4. close the entry

        final FileTime now = FileTime.fromMillis(Calendar.getInstance().getTimeInMillis());

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 4);
        CRC32CalculatingOutputStream crc = new CRC32CalculatingOutputStream(baos);
        StreamResult result = new StreamResult(crc);
        serializeToResult(state, streamId, result);
        ArchiveEntry arxEntry = arxStreamFactory
                .newArchiveEntry(streamId.name(), baos.size(), now, now, 0644, crc.resetCrc());

        try {
            aos.putArchiveEntry(arxEntry);
            baos.writeTo(aos);
            aos.closeArchiveEntry();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Serializes the identified stream from the package state to the supplied result.
     *
     * @param state    the package state object containing the identified stream
     * @param streamId the stream identifier for the content being serialized
     * @param result   holds the output stream for the serialization result
     */
    void serializeToResult(PackageState state, StreamId streamId, StreamResult result) {

        if (marshallerMap == null) {
            throw new IllegalStateException(ERR_MISSING_MARSHALLINGMAP);
        }

        PropertyDescriptor pd = propertyDescriptors.get(streamId);

        if (pd == null) {
            throw new IllegalArgumentException(String.format(ERR_INVALID_STREAMID, streamId.name(),
                    PackageState.class.getName(),
                    propertyDescriptors.keySet().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }

        Object toSerialize;

        try {
            toSerialize = pd.getReadMethod().invoke(state);
            if (toSerialize == null) {
                // The field on the package state had a null value, which is OK.  We have nothing to serialize.
                return;
            }
        } catch (Exception e) {
            String err = String.format(ERR_INVOKING_METHOD,
                    pd.getReadMethod(), state.getClass().getName(), e.getMessage());
            throw new RuntimeException(err, e);
        }

        try {
            StreamMarshaller streamMarshaller = marshallerMap.get(streamId);
            if (streamMarshaller == null) {
                throw new RuntimeException(String.format(ERR_MISSING_STREAMMARSHALLER, streamId));
            }

            Marshaller marshaller = streamMarshaller.getMarshaller();
            if (marshaller == null) {
                throw new RuntimeException(String.format(ERR_MISSING_SPRINGMARSHALLER, streamId, streamId));
            }

            marshaller.marshal(toSerialize, result);
        } catch (Exception e) {
            throw new RuntimeException(String.format(ERR_MARSHALLING_STREAM, streamId, e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Deserializes the identified stream ({@code streamId}) from the supplied input stream into the package state.
     * If {@code streamId} is {@code null}, then all streams found in the supplied input stream are deserialized to
     * {@code state}.
     * </p>
     * <p>
     * If the specified stream is not found, this method will do nothing.  If an unrecognized stream is encountered, it
     * will be skipped.
     * </p>
     * <p>
     * An unrecognized stream is a Zip entry with a name that is not present in the {@code StreamId enum}.  Unrecognized
     * streams can be encountered when the supplied {@code streamId} is {@code null}.
     * </p>
     *
     * @param state the package state to be populated
     * @param streamId the stream to be deserialized, may be {@code null} to specify all streams found in the supplied
     *                 input stream
     * @param in the input stream containing the identified {@code streamId}
     * @throws RuntimeException when there are errors accessing fields of the state using reflection
     * @throws StreamChecksumMismatch when {@code failOnChecksumMismatch} is {@code true} and a checksum mismatch is
     *                                encountered
     */
    @Override
    public void deserialize(PackageState state, StreamId streamId, InputStream in) {
        if (isArchiveStream(in)) {
            in = new ZipArchiveInputStream(in);
            deserialize(state, streamId, (ZipArchiveInputStream) in);
            return;
        }

        if (streamId == null) {
            throw new UnsupportedOperationException("Cannot deserialize a PackageState object from a non-archive " +
                    "(i.e. not a zip file) input stream.");
        }

        try {
            Object result = marshallerMap.get(streamId).getUnmarshaller().unmarshal(new StreamSource(in));
            if (result != null) {
                propertyDescriptors.get(streamId).getWriteMethod().invoke(state, result);
            } else {
                throw new RuntimeException(String.format(ERR_UNMARSHALLING_STREAM, streamId,
                        "Inexplicable 'null' result from unmarshalling!"));
            }
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format(ERR_UNMARSHALLING_STREAM, streamId, e.getMessage()), e);
        }
    }

    /**
     * Deserializes the identified stream ({@code streamId}) from the supplied input stream into the package state.
     * If {@code streamId} is {@code null}, then all streams found in the supplied input stream are deserialized to
     * {@code state}.
     * <p>
     * If the specified stream is not found, this method will do nothing.  If an unrecognized stream is encountered, it
     * will be skipped.
     * </p>
     * <p>
     * An unrecognized stream is a Zip entry with a name that is not present in the {@code StreamId enum}.  Unrecognized
     * streams can be encountered when the supplied {@code streamId} is {@code null}.
     * </p>
     *
     * @param state the package state to be populated
     * @param streamId the stream to be deserialized, may be {@code null} to specify all streams found in the supplied
     *                 input stream
     * @param in the input stream containing the identified {@code streamId}
     * @throws RuntimeException when there are errors accessing fields of the state using reflection
     * @throws StreamChecksumMismatch when {@code failOnChecksumMismatch} is {@code true} and a checksum mismatch is
     *                                encountered
     */
    void deserialize(PackageState state, StreamId streamId, ZipArchiveInputStream in) {
        ArchiveEntry entry;
        Object deserializedStream;
        boolean all = streamId == null;

        // deserialize the specified stream identifier (or all stream identifiers if streamId is null) from the
        // inputStream to the package state

        try {
            while ((entry = in.getNextEntry()) != null) {
                if (entry.getSize() == 0) {
                    // Skip empty entries
                    continue;
                }
                if (all || streamId.name().equals(entry.getName())) {
                    if (all) {
                        try {
                            streamId = StreamId.valueOf(entry.getName().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            LOG.warn(String.format(WARN_UNKNOWN_STREAM, entry.getName().toUpperCase()));
                            continue;
                        }
                    }

                    if (!hasPropertyDescription(streamId)) {
                        throw new NullPointerException(String.format(ERR_MISSING_DESCRIPTOR, streamId));
                    }
                    if (!hasUnmarshaller(streamId)) {
                        throw new NullPointerException(String.format(ERR_MISSING_SPRINGMARSHALLER, streamId, streamId));
                    }

                    CRC32CalculatingInputStream crcIn = new CRC32CalculatingInputStream(in);
                    long expectedCrc = ((ZipArchiveEntry) entry).getCrc();

                    deserializedStream = marshallerMap.get(streamId).getUnmarshaller().unmarshal(new StreamSource(crcIn));

                    long actualCrc = crcIn.resetCrc();
                    if (actualCrc != expectedCrc) {
                        if (failOnChecksumMismatch) {
                            throw new StreamChecksumMismatch(
                                    String.format(
                                            WARN_CRC_MISMATCH,
                                            streamId, Long.toHexString(expectedCrc), Long.toHexString(actualCrc)));
                        } else {
                            LOG.warn(String.format(WARN_CRC_MISMATCH,
                                streamId, Long.toHexString(expectedCrc), Long.toHexString(actualCrc)));
                        }
                    }
                    propertyDescriptors.get(streamId).getWriteMethod().invoke(state, deserializedStream);
                }
            }
        } catch (StreamChecksumMismatch e) {
            throw e; // don't wrap this exception, throw it as-is per Javadoc
        } catch (Exception e) {
            if (streamId == null) {
                throw new RuntimeException(String.format(ERR_UNMARSHALLING_ARCHIVE, e.getMessage()), e);
            } else {
                throw new RuntimeException(
                        String.format(ERR_UNMARSHALLING_STREAMID_ARCHIVE, streamId, e.getMessage()), e);
            }
        }
    }

    boolean isArchiveStream(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        if (!in.markSupported()) {
            throw new IllegalArgumentException("Mark is not supported.");
        }

        final byte[] signature = new byte[12];
        in.mark(signature.length);
        int signatureLength;
        try {
            signatureLength = IOUtils.readFully(in, signature);
            in.reset();
        } catch (IOException e) {
            throw new RuntimeException(String.format(ERR_UNMARSHALLING_STREAM, "<unknown>", e.getMessage()), e);
        }
        return ZipArchiveInputStream.matches(signature, signatureLength);
    }

    /**
     * A Map containing the Marshaller and Unmarshaller implementation for each field in PackageState that
     * may be (de)serialized by this implementation.
     */
    public Map<StreamId, StreamMarshaller> getMarshallerMap() {
        return marshallerMap;
    }

    /**
     * A Map containing the Marshaller and Unmarshaller implementation for each field in PackageState that
     * may be (de)serialized by this implementation.
     */
    public void setMarshallerMap(Map<StreamId, StreamMarshaller> marshallerMap) {
        this.marshallerMap = marshallerMap;
    }

    /**
     * Abstraction used to support the creation of ArchiveOutputStream and ArchiveEntry instances for
     * a particular archive format (currently zip is supported).  The implementation only
     * consults the factory when the {@link #archive archive flag} is {@code true}.  Package-private for unit test
     * access.
     *
     * @return the ArchiveStreamFactory
     */
    ArchiveStreamFactory getArxStreamFactory() {
        return arxStreamFactory;
    }

    /**
     * Abstraction used to support the creation of ArchiveOutputStream and ArchiveEntry instances for
     * a particular archive format (currently zip is supported).  The implementation only
     * consults the factory when the {@link #archive archive flag} is {@code true}.  Package-private for unit test
     * access.
     *
     * @param arxStreamFactory the ArchiveStreamFactory
     */
    void setArxStreamFactory(ArchiveStreamFactory arxStreamFactory) {
        this.arxStreamFactory = arxStreamFactory;
    }

    /**
     * Whether or not we are serializing streams into an archive (zip or tar)
     *
     * @return a flag indicating whether or not we are producing an archive
     */
    public boolean isArchive() {
        return archive;
    }

    /**
     * Whether or not we are serializing streams into an archive (zip or tar)
     *
     * @param archive a flag indicating whether or not we are producing an archive
     */
    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    /**
     * A flag controlling if an exception will be thrown when a stream's actual checksum does not match its
     * expected checksum.
     *
     * @return when true, a {@code StreamChecksumMismatch} will be thrown when checksums do not match
     */
    public boolean isFailOnChecksumMismatch() {
        return failOnChecksumMismatch;
    }

    /**
     * A flag controlling if an exception will be thrown when a stream's actual checksum does not match its
     * expected checksum.
     *
     * @param failOnChecksumMismatch when true, a {@code StreamChecksumMismatch} will be thrown when checksums do not
     *                               match
     */
    public void setFailOnChecksumMismatch(boolean failOnChecksumMismatch) {
        this.failOnChecksumMismatch = failOnChecksumMismatch;
    }

    private boolean hasPropertyDescription(StreamId streamId) {
        return propertyDescriptors.get(streamId) != null;
    }

    private boolean hasStreamMarshaller(StreamId streamId) {
        return marshallerMap.get(streamId) != null;
    }

    private boolean hasMarshaller(StreamId streamId) {
        if (!hasStreamMarshaller(streamId)) {
            throw new NullPointerException("StreamId '" + streamId + "' does not have a StreamMarshaller configured.");
        }
        return marshallerMap.get(streamId).getMarshaller() != null;
    }

    private boolean hasUnmarshaller(StreamId streamId) {
        if (!hasStreamMarshaller(streamId)) {
            throw new NullPointerException("StreamId '" + streamId + "' does not have a StreamMarshaller configured.");
        }
        return marshallerMap.get(streamId).getUnmarshaller() != null;
    }

    /**
     * Calculates a CRC32 checksum as bytes are written to the wrapped {@code OutputStream}
     */
    private class CRC32CalculatingOutputStream extends FilterOutputStream {

        private CRC32 crc32 = new CRC32();

        public CRC32CalculatingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void write(int b) throws IOException {
            crc32.update(b);
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            crc32.update(b);
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            crc32.update(b, off, len);
            out.write(b, off, len);
        }

        /**
         * Resets the checksum calculation and returns its value.
         *
         * @return the checksum value for the bytes written thus far
         */
        long resetCrc() {
            long crc = crc32.getValue();
            crc32.reset();
            return crc;
        }

    }

    /**
     * Calculates a CRC32 checksum as bytes are written to the wrapped {@code OutputStream}
     */
    private class CRC32CalculatingInputStream extends FilterInputStream {

        private CRC32 crc32 = new CRC32();

        public CRC32CalculatingInputStream(InputStream in) {
            super(in);
        }


        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read() throws IOException {
            int read = in.read();
            if (read >= 0) {
                crc32.update(read);
            }

            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = in.read(b, off, len);
            if (read >= 0) {
                crc32.update(b, off, read);
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            // Can't skip, we have to hash everything to verify the checksum
            if (read() >= 0) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * Resets the checksum calculation and returns its value.
         *
         * @return the checksum value for the bytes written thus far
         */
        long resetCrc() {
            long crc = crc32.getValue();
            crc32.reset();
            return crc;
        }

    }

}
