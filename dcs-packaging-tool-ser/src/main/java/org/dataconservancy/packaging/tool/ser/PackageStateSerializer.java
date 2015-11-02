package org.dataconservancy.packaging.tool.ser;

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.packaging.tool.model.PackageState;

import java.io.File;

/**
 *
 */
public class PackageStateSerializer {

    private boolean archive;

    private boolean compress;

    private XStream xStream;

    public PackageStateSerializer() {

    }

    public PackageStateSerializer(XStream xStream) {
        this.xStream = xStream;
    }

    public void serialize(PackageState state) {

    }

    public void serialize(PackageState state, File destination) {

    }

    public PackageState deserialize(File source) {
        return null;
    }

}
