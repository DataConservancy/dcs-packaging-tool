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
> separately, [here][spechome].

Full documentation is available on our [wiki][dc-wiki].

### Releases ###

Releases are cataloged on the [GitHub Releases page][gh-releases].

### Developer Quick Start ###

#### Prerequisites ####
 - Oracle JDK 8 with Java FX

####To build the entire tool ####

Clone this repository.

 `mvn clean install`
  
Some tests require creating symbolic linksx which is a priviliged operation on Windows.  To run tests without tests requiring escalated privileges:
`mvn clean install -P unprivileged`

#### To run the package tool GUI ####

    cd dcs-packaging-tool-gui
    mvn jfx:run

[bagit]: http://www.ietf.org/id/draft-kunze-bagit-12.txt "BagIt 0.97"
[bagit-profile]: http://dataconservancy.github.io/dc-packaging-spec/dc-bagit-profile-1.0.html "Data Conservancy BagIt Profile"
[package-spec]: http://dataconservancy.github.io/dc-packaging-spec/dc-packaging-spec-1.0.html "Data Conservancy Packaging Specification"
[spec-home]: http://dataconservancy.github.io/dc-packaging-spec/ "Data Conservancy Packaging Specification Home"
[gh-releases]: https://github.com/DataConservancy/dcs-packaging-tool/releases "Package Tool Release Page"
[dc-wiki]: https://wiki.library.jhu.edu/display/DCSDOCPKG/Package+Tools+Documentation+Home "Data Conservancy Documentation"
