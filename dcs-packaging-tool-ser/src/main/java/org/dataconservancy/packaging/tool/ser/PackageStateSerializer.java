package org.dataconservancy.packaging.tool.ser;

import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Responsible for (de)serializing <em>specified</em> fields of {@link PackageState}.
 */
public interface PackageStateSerializer {

    /**
     * Serialize objects in the state to the output stream.  Because the state normally contains multiple
     * objects, multiple streams will be written to the output stream.  How this occurs is implementation dependant.
     *
     * @param state the package state containing the streams to be serialized
     * @param out the output stream to serialize to
     */
    void serialize(PackageState state, OutputStream out);

    /**
     * Obtain the object specified by {@link StreamId} from the state and serialize it to the output stream.  Because
     * the state should only contain one object with the specified {@code StreamId}, a single stream will be written
     * to the output stream.
     *
     * @param state the package state containing the stream identified by {@code streamId}
     * @param streamId the stream within the package state to serialize
     * @param out the output stream to serialize to
     */
    void serialize(PackageState state, StreamId streamId, OutputStream out);

    /**
     * Deserialize the stream or streams found in the input stream, and place the object form of each deserialized
     * stream on the supplied state.
     *
     * @param state the package state to be populated from the input stream, {@code in}
     * @param in the input stream containing the streams to be deserialized onto {@code state}
     */
    void deserialize(PackageState state, InputStream in);

    /**
     * Deserialize the specified stream from the input stream, and place the object form of the deserialized stream on
     * the supplied state.
     *
     * @param state the package state to be populated from the input stream, {@code in}
     * @param streamId identifies an individual stream from the the input stream, {@code in}
     * @param in the input stream containing the identified stream to be deserialized onto {@code state}
     */
    void deserialize(PackageState state, StreamId streamId, InputStream in);

}
