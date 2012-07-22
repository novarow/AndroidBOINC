#!/bin/sh
#script to compile BOINC for Android

#===please adapt the following paths============================
#Android toolchain
ANDROIDTC="/home/novarow/Documents/AndroidBOINC/android-4-tc"

#sources
#OpenSSL
OPENSSL="/home/novarow/Documents/AndroidBOINC/openssl-1.0.0d"
#Curl
CURL="/home/novarow/Documents/AndroidBOINC/curl-7.21.4"
#BOINC
BOINC="/home/novarow/Documents/AndroidBOINC/boinc-7.0.9j"
#BOINC Application
BAPP="$BOINC/samples/example_app"

#destination
#build output
BOINCINSTALL="/home/novarow/Documents/AndroidBOINC/nativebuild"
#APK assets
TARGETAPK="/home/novarow/VBshare/ews/edu.berkeley.boinc.AndroidBOINCActivity/assets"
#=============================================================

#===and mark with 'yes' what to compile=======================
CONFIGURE="yes"
#COMPILEOPENSSL="yes" #compiling required libraries. ONLY NECESSARY if libssl.a and libcurl.a DO NOT EXIST in $TCINCLUDES
#COMPILECURL="yes"
#COMPILEBOINC="yes"
COMPILEAPP="yes"
#=============================================================



#convenience vars
TCBINARIES="$ANDROIDTC/bin" #cross compiler location
TCINCLUDES="$ANDROIDTC/arm-linux-androideabi" #libraries to include in build
TCSYSROOT="$ANDROIDTC/sysroot" #SYSROOT of Android device
STDCPPTC="$TCINCLUDES/lib/libstdc++.a" #stdc++ library

#compiler setup
export PATH="$PATH:$TCBINARIES" #add location of compiler binaries to PATH
export CC=arm-linux-androideabi-gcc #C compiler
export CXX=arm-linux-androideabi-g++ #C++ compiler
export LD=arm-linux-androideabi-ld #LD tool
export CFLAGS="--sysroot=$TCSYSROOT -DANDROID -DDECLARE_TIMEZONE -Wall -I$TCINCLUDES/include -O3 -fomit-frame-pointer" #DECLARE_TIMEZONE for boinc_zip library, ANDROID for compilation of curl
export CXXFLAGS="--sysroot=$TCSYSROOT -DANDROID -Wall -funroll-loops -fexceptions -O3 -fomit-frame-pointer" #ANDROID for special handling in BOINC client
export LDFLAGS="-L$TCSYSROOT/usr/lib -L$TCINCLUDES/lib -llog" #log is Logcat
export GDB_CFLAGS="--sysroot=$TCSYSROOT -Wall -g -I$TCINCLUDES/include"



#cleaning target directory
echo "===================removing files from $BOINCINSTALL==========================="
cd $BOINCINSTALL
rm -r *
echo "==========================================================================="

if [ -n "$COMPILEOPENSSL" ]; then
echo "================building openssl from $OPENSSL============================="
cd $OPENSSL
make clean
./Configure linux-generic32 no-shared no-dso -DL_ENDIAN --openssldir="$TCINCLUDES/ssl"
#override flags in Makefile
sed -e "s/^CFLAG=.*$/`grep -e \^CFLAG= Makefile` \$(CFLAGS)/g
s%^INSTALLTOP=.*%INSTALLTOP=$TCINCLUDES%g" Makefile > Makefile.out
mv Makefile.out Makefile
make
make install_sw
echo "====================building openssl done=================================="
fi
if [ -n "$COMPILECURL" ]; then
echo "==================building curl from $CURL================================="
cd $CURL
make clean
./configure --host=arm-linux --prefix=$TCINCLUDES --libdir="$TCINCLUDES/lib" --disable-shared --enable-static --with-random=/dev/urandom
make
make install
echo "===================building curl done============================="
fi
if [ -n "$COMPILEBOINC" ]; then
echo "==================building BOINC from $BOINC=========================="
cd $BOINC
make clean
if [ -n "$CONFIGURE" ]; then
./_autosetup
./configure --host=arm-linux --prefix=$BOINCINSTALL --with-boinc-platform=arm-android-linux-gnu --with-boinc-alt-platform=arm-android --disable-server --disable-manager --disable-shared --enable-static
sed -e "s%^CLIENTLIBS *= *.*$%CLIENTLIBS = -lm $STDCPPTC%g" client/Makefile > client/Makefile.out #override libstdc++ in Makefile
mv client/Makefile.out client/Makefile
fi
make

cd api
make install
cd ../client
make install
cd ../lib
make install
echo "===================building BOINC done============================="
echo "===================copy binary to Android assets direcotry at $TARGETAPK========================="
sudo cp "$BOINCINSTALL/bin/boinc_client" "$TARGETAPK/boinc_client"
fi
if [ -n "$COMPILEAPP" ]; then
echo "=================building application from $BAPP=============="
cd "$BAPP"
make clean
ln -s "$STDCPPTC" #create symbolic link to stdc++ library
make #Makefile got manually modified
echo "===================building example app done============================="
echo "===================copy binary to Android assets direcotry at $TARGETAPK========================="
sudo cp "$BOINC/samples/example_app/uc2" "$TARGETAPK/uc2"
fi
echo "===================script done==================="
