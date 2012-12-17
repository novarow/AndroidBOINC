This directory contains files regarding the development/compilation of native BOINC source code for ARM/Android

If you want to build BOINC Client for ARM/Android, please obtain the source code at the official BOINC software repository. You might find the files provided here helpful in order to build the binaries for ARM/Android or to understand the changes to the BOINC Client during this project.

- android-4-tc/ :
Toolchain for cross-compilation under x86 Linux for ARM. Created using the "standalone toolchain script" of the Android NDK.

- curl/ ;  openSSL/ :
(unmodified) source code copy of required libraries. Please use this only as a fall-back, prefer current version from corresponding websites.

- diffs_android/ :
collection of changes that have been made to the BOINC repository during this project. 

- build_android.sh :
unix bash script that helps building BOINC code and dependencies for Android/ARM.
