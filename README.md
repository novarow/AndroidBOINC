AndroidBOINC
============

CAUTION: this project is under open development and is not considered stable!

porting Berkeley's Open Infrastructure for Network Computing to Android-powered devices.

The intention of this project is to port the exisiting BOINC client to ARM based Android devices. This can vastly increase the potential devices participating in BOINC based scientific projects. In order to achieve this goal, the client has to be cross compiled for ARM architecutre and adapted to accomondate with Android specific behaviour. To offer the user a graphical interface, a BOINC manager has to be developed for Android using the common Android app development methods (Java, Android SDK).

This repository contains all code to build the fully-fledged BOINC client for Android:

android/:
	edu.berkeley.boinc.AndroidBOINCActivity:
	The Android application (BOINC manager for Android)

native/:
	android-4-tc
	standalone toolchain for cross compilation of native source code for Android ARM (v5 ABI) devices. It contains cross compilation tool binaries as well as the SYSROOT for Android devices with NDK level 4 (Android 1.6)

	boinc-7.0.9
	The adapted version of the BOINC client, forked from BOINC repo tag 7.0.9. See /samples/example_app for Android specific "upper case" example application.

	curl-x.x.x
	curl library source, used by the client

	openssl-x
	openSSL library source, used by the client

	build.sh
	build script to build the required libraries, the BOINC client and BOINC applications for Android devices, using the cross compiler binaries offered by the Android NDK and the corresponding SYSROOT.


	 
