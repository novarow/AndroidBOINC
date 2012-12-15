This directory contains Android specific application code:

1. edu.berkeley.boinc.AndroidBOINCActivity
generic BOINC applications that bundles BOINC Client and BOINC Manager in the same Android application. This architecutre was used for the proof-of-concept, but has limitations supporting  multiple project-branded Android applications.

2. BOINCClient
generic BOINC Client.
According to an architecture, whith generic and Android-wide unique BOINC Client. This architecture supports multiple project-branded Android applications that represent branded Managers.

3. edu.uwm.phys.einstein.MainActivity
EINSTEINatHOME branded BOINC Manager. Requires BOINCClient on the Android system.
