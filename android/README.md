This directory contains Android specific application code:

- (deprecated) edu.berkeley.boinc.AndroidBOINCActivity:
generic BOINC applications that bundles BOINC Client and BOINC Manager in the same Android application. This architecutre was used for the proof-of-concept, but has limitations supporting  multiple project-branded Android applications.

- BOINCClient
generic BOINC Client.
According to an architecture, whith generic and Android-wide unique BOINC Client. This architecture supports multiple project-branded Android applications that represent branded Managers.

- edu.berkeley.boinc.manager
generic BOINC Manager. Can be adapted/branded for individual project. Requires BOINCClient on the Android system.
