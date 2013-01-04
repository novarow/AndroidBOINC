This directory contains Android specific application code:

- (deprecated) BOINC Client-Manager Bundle:
generic BOINC applications that bundles BOINC Client and BOINC Manager in the same Android application. This architecutre was used for the proof-of-concept, but has limitations supporting  multiple project-branded Android applications.

- BOINC Client:
generic BOINC Client.
According to an architecture, whith generic and Android-wide unique BOINC Client. This architecture supports multiple project-branded Android applications that represent branded Managers.
CAUTION:
if you want to run this project on your machine, download BOINC Client ARM binaries [1] and copy them to the projects assets/ directory. For an up-to-date version, download current code from official UCB BOINC repository and build it using Android NDK.

- BOINC Manager:
generic BOINC Manager. Can be adapted/branded for individual project. Requires BOINCClient on the Android system.

[1] http://fridgelike.com/boinc/boinc_client
