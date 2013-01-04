AndroidBOINC
============

Porting Berkeley's Open Infrastructure for Network Computing -BOINC- to Android-powered devices.

This project is still under development. If you want to see the current status on your Android device, try [1] at own risk!

The intention of this project is to port the exisiting BOINC client to ARM based Android devices. This can vastly increase the potential devices participating in BOINC based scientific projects. In order to achieve this goal, the client has to be cross compiled for ARM architecutre and adapted to accomondate with Android specific behaviour. To offer the user a graphical interface, a BOINC manager has to be developed for Android using the common Android app development methods (Java, Android SDK).

This repository holds code and scripts to build AndroidBOINC:

android/ :
contains Android applications developed within this project.

native/ :
The Android application requires the BOINC Client binaries for ARM. If you are interested in building the BOINC Client for ARM, you might find the files here helpful.

for documentation see BOINC developer wiki at [2].	 

[1] http://fridgelike.com/boinc/BOINC_Client.apk
[2] http://boinc.berkeley.edu/trac/wiki/SoftwareDevelopment
