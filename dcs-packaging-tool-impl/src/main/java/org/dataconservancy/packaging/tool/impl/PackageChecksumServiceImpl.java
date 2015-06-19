/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl;

import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.dcs.model.Checksum;
import org.dataconservancy.dcs.model.ChecksumImpl;
import org.dataconservancy.packaging.tool.api.PackageChecksumService;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Implementation of the PackageChecksumService interface
 */
public class PackageChecksumServiceImpl implements PackageChecksumService {


    public Map<File, List<Checksum>> generatePackageFileChecksums(
            Set<File> packageFiles, List<String> checksumAlgorithms) throws PackageToolException {

        Map<File, List<Checksum>> packageChecksums = new HashMap<>();
        for(File file : packageFiles){
            List<Checksum> fileChecksums = new ArrayList<>();
            for(String algorithm : checksumAlgorithms){
                try{
                    FileInputStream fis = new FileInputStream(file);
                    ChecksumImpl checksum = new ChecksumImpl(algorithm, ChecksumGeneratorVerifier.generateChecksum(algorithm, fis));
                    fileChecksums.add(checksum);
                } catch (FileNotFoundException fnfe) {
                   throw new PackageToolException(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION, fnfe, file.getPath());
                } catch (NoSuchAlgorithmException nsae){
                   throw new PackageToolException(PackagingToolReturnInfo.PKG_NO_SUCH_CHECKSUM_ALGORITHM_EXCEPTION, nsae, algorithm);
                }
                packageChecksums.put(file, fileChecksums);
            }

        }
        return packageChecksums;
    }

}
