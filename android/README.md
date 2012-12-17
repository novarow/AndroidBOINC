This directory contains Android specific application code:

- (deprecated) BOINC Client-Manager Bundle:
generic BOINC applications that bundles BOINC Client and BOINC Manager in the same Android application. This architecutre was used for the proof-of-concept, but has limitations supporting  multiple project-branded Android applications.

- BOINC Client:
generic BOINC Client.
According to an architecture, whith generic and Android-wide unique BOINC Client. This architecture supports multiple project-branded Android applications that represent branded Managers.

- BOINC Manager:
generic BOINC Manager. Can be adapted/branded for individual project. Requires BOINCClient on the Android system.


CAUTION:
if you want to build these projects on your own machine, make sure to copy BOINC Client binaries into the projects "assets/" direcory!
Current build can be found at https://github.com/novarow/AndroidBOINC-binaries/
