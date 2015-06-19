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
package org.dataconservancy.packaging.tool.api;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.model.Checksum;
import org.dataconservancy.packaging.tool.model.PackageToolException;

/**
 * Generate checksums for file artifacts in a package.
 * Parameters are a Set of Artifacts, and a List of checksum algorithms
 * Return a Map of Artifact Ids to  the corresponding list of Checksums for that Artifact
 */
public interface PackageChecksumService {
   Map<File, List<Checksum>> generatePackageFileChecksums(Set<File> packageFiles, List<String> checksumAlgorithms) throws
           PackageToolException;
}