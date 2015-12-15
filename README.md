## Data Conservancy Packaging tool ##

This is a description of the DCS packaging tool

### Quick start ###

#### Prerequisites ####
 - Oracle JDK 8 with Java FX

####To build the entire tool ####

 `mvn clean install`
  
Some tests require creating symbolic linksx which is a priviliged operation on Windows.  To run tests without tests requiring escalated privileges:
`mvn clean install -P unprivileged`

#### To run the package tool GUI ####

    cd dcs-packaging-tool-gui
    mvn jfx:run