/*
 * Copyright 2017 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.impl.support.validation.BlacklistedCharacterMatcher;
import org.dataconservancy.packaging.tool.impl.support.validation.InvalidUtf8CharacterMatcher;
import org.dataconservancy.packaging.tool.impl.support.validation.SubstitutionRemediation;
import org.dataconservancy.packaging.tool.impl.support.validation.TruncationRemediation;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.codec.digest.DigestUtils.shaHex;
import static org.dataconservancy.packaging.tool.impl.support.validation.TruncationRemediation.Strategy.TRAILING_SUBSTITUTION;

/**
 * Provides for remediating package resource paths.  The <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">
 * Data Conservancy BagIt profile §2.2.2.1</a> places significant restrictions on the characters and length of package
 * resource paths.  Clients may use this class to remediate package resource paths before
 * {@link org.dataconservancy.packaging.tool.api.generator.PackageAssembler#reserveResource(String, PackageResourceType)
 * reserving} or {@link org.dataconservancy.packaging.tool.api.generator.PackageAssembler#createResource(String,
 * PackageResourceType, InputStream) creating} resources.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see <a href="http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html#a2.2.2.1">Data Conservancy
 *      BagIt Profile 1.0 §2.2.2.1</a>
 */
class RemediationUtil {

    public static final String DOT_DOT = "..";
    public static final String DOT = ".";
    /**
     * Paths that must not be truncated (i.e. they must be preserved in the remediated string).
     * Note that this List must be sorted with the longest paths at the head of the List.
     */
    private static final List<String> RESERVED_PATHS = Arrays.stream(PackageResourceType.values())
            .map(PackageResourceType::getRelativePackageLocation)
            .sorted(Comparator.comparingInt(s -> -(s.length())))
            .collect(Collectors.toList());

    /**
     * Regular expression that matches file names or path components that are reserved by the Windows/DOS
     * platform.
     */
    private static final String ILLEGAL_DOS_NAMES = "^(CON|PRN|AUX|NUL|COM[0-9]*|LPT[0-9]*)($|\\..*$)";

    /**
     * Performs remediation by substituting valid characters for invalid characters in path components
     */
    private static final SubstitutionRemediation SUBST = new SubstitutionRemediation();

    /**
     * Performs remediation by shortening path components that violate length limits
     */
    private static final TruncationRemediation TRUNC = new TruncationRemediation();

    /**
     * Remediate the supplied path, correcting any irregularities that would conflict with the BagIt specification or
     * the identified packaging profile.  If no corrections are needed, the {@code packageResourcePath} will be returned
     * as-is.
     *
     * @param packageResourcePath the full path of a resource in the package, relative to the base directory of the
     *                            package
     * @param profileId the profile identifier
     * @return the remediated path
     */
    static String remediatePath(String packageResourcePath, String profileId) {
        // profileId currently ignored since there's only one profile id supported right now

        StringBuilder toRemediate = new StringBuilder(packageResourcePath);
        StringBuilder preserved = preservePrefix(toRemediate);
        int limit = 1024 - preserved.length();
        String remediatedPath = Arrays.stream(toRemediate.toString().split("/"))
                .map(StringBuilder::new)
                .map(pathComponent -> SUBST.remediateMatchingCharacters(pathComponent, 'X', Stream.of(new BlacklistedCharacterMatcher(), new InvalidUtf8CharacterMatcher())))
                .map(pathComponent -> SUBST.remediateEqualStrings(pathComponent, 'X', DOT_DOT))
                .map(pathComponent -> SUBST.remediateEqualStrings(pathComponent, 'X', DOT))
                .map(pathComponent -> SUBST.remediateMatchingStrings(pathComponent, 'X', ILLEGAL_DOS_NAMES))
                .map(pathComponent -> TRUNC.remediate(pathComponent, 255, TRAILING_SUBSTITUTION))
                .collect(Collectors.joining("/", "", packageResourcePath.endsWith("/") ? "/" : ""));

        remediatedPath = TRUNC.remediate(new StringBuilder(remediatedPath), limit, TRAILING_SUBSTITUTION);

        return preserved.append(remediatedPath).toString();
    }

    /**
     * Searches the supplied {@code packageResourcePath} for reserved path prefixes that should <em>not</em> be subject
     * to remediation, and removes them from the {@code packageResourcePath}.  The location of {@link PackageResourceType
     * package resources} are specified by the BagIt specification and the Data Conservancy BagIt profile, and
     * therefore:
     * <ol>
     *     <li>May be assumed to <em>not</em> violate the specification or profile (e.g. paths defined by the
     *     specification will never have a path component greater than 255 characters, or contain illegal
     *     characters)</li>
     *     <li>Must not be subject to remediation (e.g. truncation remediation shall not truncate a portion of the
     *     reserved path)</li>
     * </ol>
     *
     * @param packageResourcePath the full path of the resource in the package, relative to the base directory of the
     *                            package
     * @return the portion of the {@code packageResourcePath} that <em>must</em> be preserved from remediation
     *         (may be empty)
     */
    private static StringBuilder preservePrefix(StringBuilder packageResourcePath) {
        for (String reserved : RESERVED_PATHS) {
            final int index = packageResourcePath.indexOf(reserved);
            if (index < 0) {
                // nothing to preserve, as the reserved path doesn't exist in the string being remediated
                continue;
            }

            // Preserve everything from index 0 in the string being remediated to the ending of the path being preserved
            // this is a bit greedy, but it does handle the case where the reserved path doesn't start with a /, and
            // the string being remediated does.
            StringBuilder preserved = new StringBuilder(packageResourcePath.subSequence(0, index + reserved.length()));
            packageResourcePath.delete(0, index + reserved.length());

            return preserved;
        }

        return new StringBuilder();
    }

    /**
     * Generate a unique path for {@code node} based on the suggested location.
     *
     * @param node the node
     * @param locationHint the suggested location
     * @return the unique location based on the node and the hint
     */
    static String unique(Node node, String locationHint) throws URISyntaxException {
        final Path path = Paths.get(locationHint);
        final StringBuilder remediatedPath = new StringBuilder();

        if (path.isAbsolute()) {
            remediatedPath.append("/");
        }

        if (path.getNameCount() > 1) {
            remediatedPath.append(path.subpath(0, path.getNameCount() - 1).toString());
            remediatedPath.append("/");
        }

        final StringBuilder toRemediate = new StringBuilder(shaHex(node.getIdentifier().toString()));

        remediatedPath.append(toRemediate);

        if (locationHint.endsWith("/")) {
            remediatedPath.append("/");
        }

        return remediatedPath.toString();
    }

}
