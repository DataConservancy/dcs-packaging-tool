/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.util.droid;

import org.junit.BeforeClass;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by hvu on 3/17/15.
 */
public class BaseFileSetUpTest {

    protected static File JPG_FILE_NO_EXTENSION;
    protected static File PNG_FILE;
    protected static File TAR_FILE;
    protected static File TAR_GZ_FILE;
    protected static File TEXT_FILE;
    protected static File ZIP_FILE;
    protected static File JPG_FILE;
    protected static File PNG_TXT_FILE;
    protected static File WAV_FILE;
    protected static File DOTINNAME_FILE;
    protected static File MALFORMED_XML_FILE;

    protected static final String PNG_MIMETYPE = "image/png";
    protected static final String JPG_MIMETYPE = "image/jpeg";
    protected static final String ZIP_MIMETYPE = "application/zip";
    protected static final String TEXT_MIMETYPE = "text/plain";
    protected static final String XML_MIMETYPE = "text/xml";
    protected static final String WAV_MIMETYPE = "audio/x-wav";
    protected static final String XGZIP_MIMETYPE = "application/x-gzip";

    @BeforeClass
    public static void setUpFile() throws URISyntaxException {
        PNG_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/baboon.png").toURI());
        TAR_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/CowGrass.tar").toURI());
        TAR_GZ_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/CowGrass.tar.gz").toURI());
        ZIP_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/CowGrass.zip").toURI());
        TEXT_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/CowGrass.txt").toURI());
        JPG_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/DogToy.jpg").toURI());
        PNG_TXT_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/really-a-png.txt").toURI());
        WAV_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/8-Bit-Noise-1.wav").toURI());
        DOTINNAME_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/Cow.Grass.tar.gz").toURI());
        MALFORMED_XML_FILE = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/dcp.xml").toURI());
        JPG_FILE_NO_EXTENSION = new File(BaseFileSetUpTest.class.getResource("/SampleFiles/jpgFileNoExtension").toURI());
    }
}
