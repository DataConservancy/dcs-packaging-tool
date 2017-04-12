<!--
Copyright 2015 Johns Hopkins University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
## Data Conservancy Packaging tool ##

The Data Conservancy Packaging Tool is a JavaFX GUI used to
describe and package digital content.

The packaging tool produces packages that are compliant with
the following specifications:

* [BagIt][bagit]
* [Data Conservancy BagIt Profile][bagit-profile]
* [Data Conservancy Packaging Specification][package-spec].

> Note: the Data Conservancy specifications are managed 
> separately, [here][spec-home].

Full documentation is available on our [wiki][dc-wiki].

### Releases ###

Releases are cataloged on the [GitHub Releases page][gh-releases].

### Developer Quick Start ###

#### Prerequisites ####
 - Oracle JDK 8 with Java FX

####To build the entire tool ####

Clone this repository.

 `mvn clean install`
  
Some tests require creating symbolic links which is a privileged operation on Windows.  To run all tests _except_ those requiring escalated privileges:
`mvn clean install -P unprivileged`

Some tests also create large, temporary, files for testing (~8 GB) which may not be accommodated by machines with limited resources.  To run all tests _except_ those requiring large amounts of resources:
 `mvn clean install -P constrained` 
 
 You can skip both kinds of tests by running:
 `mvn clean install -Pconstrained,unprivileged`


#### To run the package tool GUI ####

    cd dcs-packaging-tool-gui
    mvn jfx:run
    
### Creating a release ###

#### Requirements

* Oracle Java 8 >= 1.8.0-40
* Maven 3.3.9 or greater

#### Preparation

* Releases are cut from the `master` branch
* The `HEAD` commit hash of `master` for your local git repository should match the `HEAD` commit hash of your `https://github.com/DataConservancy/dcs-packaging-tool.git` remote (named `upstream`, in these instructions) 
  * e.g. the commit shown by `git show master` and `git show upstream/master` should be the same
* There should be no modified, unversioned, or staged files in your local git repository (check with `git status`)
* The Travis build for `master` should be green
* Know what version you are releasing (e.g. `1.0.4`)
* Know what the next development version should be (e.g. `1.0.5-SNAPSHOT`)
* Note the commit hash of `HEAD`, useful if you need to roll back

#### Perform the release

* `mvn release:prepare`
  * Bumps the version in the POMs to the release version (e.g. from `1.0.4-SNAPSHOT` to `1.0.4`)
  * Tags the release
  * Checks out the newly created tag
  * Builds the tag

At this point, you can look at your git commit log to see if everything looks ok.  Nothing has been pushed yet, so if you see a mistake, it is easy to roll back.  To roll back:
 * `mvn release:clean`
 * `git reset --hard HEAD^`
 * `git tag -D <newly created tag>` (use `git tag` to list the tags)
 
 If all goes well with `mvn release:prepare`, then you're ready to _perform_ the release.
 
 * `mvn release:perform`
   * Deploys release artifacts to the Maven repository
   * Bumps the version in the POMs to the next development version (e.g. from `1.0.4` to `1.0.5-SNAPSHOT`)
   
At this point, you can _still_ look at your git commit log to see if everything looks ok!  Nothing has been pushed yet, so if you see a mistake, it is _mostly_ easy to roll back.  To roll back:

 * `mvn release:clean`
 * `git reset --hard HEAD^^`
 * `git tag -D <newly created tag>` (use `git tag` to list the tags)
 * Depending on the situation, you may need to delete deployed release artifacts from the Maven repository
 
If all went well with `mvn release:perform`, push your changes to `https://github.com/DataConservancy/dcs-packaging-tool.git`, making the release official.  Alternately, open a PR, which will make the release official upon merging.

Finally, push the tags to `https://github.com/DataConservancy/dcs-packaging-tool.git` (`git push --tags upstream`).

#### Create native installers

After the release has been pushed to `master`, native installers for MacOS, Windows, and Linux need to be created.

They _must_ be created from the release tag.  This insures that build metadata like the commit hash, the tag, and version number of the tool are consistent across the three platforms, and of course, insures that they are all cut from identical code.

* `git checkout <tag>`

##### MacOS

To create the MacOS native installer, cd into `dcs-package-tool-gui`, and run:
 
 * `mvn clean jfx:native`
 
 The installer will be located at `dcs-packaging-tool-gui/target/jfx/native/DC Package Tool-1.0.4.dmg`
 
 ##### Linux
 
 * TODO
 
 ##### Windows
 
 * TODO


#### Create GitHub release

* A release should be created on the GitHub releases [page][gh-releases]
* Some release notes should be prepared, summarizing major changes
* The native installers should be uploaded to the release page, and their checksums noted


[bagit]: http://www.ietf.org/id/draft-kunze-bagit-12.txt "BagIt 0.97"
[bagit-profile]: http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html "Data Conservancy BagIt Profile"
[package-spec]: http://dataconservancy.github.io/dc-packaging-spec/dc-packaging-spec-1.0.html "Data Conservancy Packaging Specification"
[spec-home]: http://dataconservancy.github.io/dc-packaging-spec/ "Data Conservancy Packaging Specification Home"
[gh-releases]: https://github.com/DataConservancy/dcs-packaging-tool/releases "Package Tool Release Page"
[dc-wiki]: https://wiki.library.jhu.edu/display/DCSDOCPKG/Package+Tools+Documentation+Home "Data Conservancy Documentation"
