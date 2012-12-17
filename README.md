iAndroidBOINC
============

Porting Berkeley's Open Infrastructure for Network Computing -BOINC- to Android-powered devices.

To see current development status of AndroidBOINC on your device, download and install BOINC_Manager.apk from AndroidBOINC-binaries repository.

The intention of this project is to port the exisiting BOINC client to ARM based Android devices. This can vastly increase the potential devices participating in BOINC based scientific projects. In order to achieve this goal, the client has to be cross compiled for ARM architecutre and adapted to accomondate with Android specific behaviour. To offer the user a graphical interface, a BOINC manager has to be developed for Android using the common Android app development methods (Java, Android SDK).

This repository holds code and scripts to build AndroidBOINC:

android/ :
contains Android applications developed within this project.

native/ :
The Android application requires the BOINC Client binaries for ARM. If you are interested in building the BOINC Client for ARM, you might find the files here helpful.

for documentation see BOINC developer wiki at http://boinc.berkeley.edu/trac/wiki/SoftwareDevelopment	 
