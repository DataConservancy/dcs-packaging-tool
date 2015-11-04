package org.dataconservancy.packaging.tool.ser;

import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public interface PackageStateSerializer {

    /**
     * Serialize objects in the state to the output stream.  Because the state normally contains multiple
     * objects, multiple streams will be written to the output stream.  How this occurs is implementation dependant.
     *
     * @param state
     * @param out
     */
    public void serialize(PackageState state, OutputStream out);

    /**
     * Obtain the object specified by {@link StreamId} from the state and serialize it to the output stream.  Because
     * the state should only contain one object with the specified {@code StreamId}, a single stream will be written
     * to the output stream.
     *
     * @param state
     * @param streamId
     * @param out
     */
    public void serialize(PackageState state, StreamId streamId, OutputStream out);

    /**
     * Deserialize the stream or streams found in the input stream, and place the object form of each deserialized
     * stream on the supplied state.
     *
     * @param state
     * @param in
     */
    public void deserialize(PackageState state, InputStream in);

    /**
     * Deserialize the specified stream from the input stream, and place the object form of the deserialized stream on
     * the supplied state.
     *
     * @param state
     * @param in
     * @param streamId
     */
    public void deserialize(PackageState state, InputStream in, StreamId streamId);

}
