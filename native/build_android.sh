#/bin/sh
#script to compile BOINC for Android

#++++++++++++++++++++++++CONFIGURATION++++++++++++++++++++++++++++
#===BOINC platform name
BPNAME="arm-android-4"

#===locations
#Android toolchain
ANDROIDTC=""

#sources
OPENSSL="" #openSSL sources, requiered by BOINC
CURL="" #CURL sources, required by BOINC
BOINC="" #BOINC source code
BAPP="" #BOINC application ("scientific app") source

#destination
BOINCINSTALL="" #destination directory of boinc binaries
TARGETAPK="" #location of Android Manager App, which will use this BOINC build

#===script behavior
CONFIGURE="yes"
MAKECLEAN="yes"

COMPILEOPENSSL="yes" #compiling required libraries. ONLY NECESSARY if libssl.a and libcurl.a DO NOT EXIST in $TCINCLUDES
COMPILECURL="yes"
COMPILEBOINC="yes"
COMPILEAPP="yes"
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


TCBINARIES="$ANDROIDTC/bin" #cross compiler location
TCINCLUDES="$ANDROIDTC/arm-linux-androideabi" #libraries to include in build
export TCSYSROOT="$ANDROIDTC/sysroot" #SYSROOT of Android device
export STDCPPTC="$TCINCLUDES/lib/libstdc++.a" #stdc++ library

export PATH="$PATH:$TCBINARIES:$TCINCLUDES/bin" #add location of compiler binaries to PATH
export CC=arm-linux-androideabi-gcc #C compiler
export CXX=arm-linux-androideabi-g++ #C++ compiler
export LD=arm-linux-androideabi-ld #LD tool
export CFLAGS="--sysroot=$TCSYSROOT -DANDROID -DDECLARE_TIMEZONE -Wall -I$TCINCLUDES/include -O3 -fomit-frame-pointer" #DECLARE_TIMEZONE for boinc_zip library, ANDROID for compilation of curl
export CXXFLAGS="--sysroot=$TCSYSROOT -DANDROID -Wall -funroll-loops -fexceptions -O3 -fomit-frame-pointer" #ANDROID for special handling in BOINC client
export LDFLAGS="-L$TCSYSROOT/usr/lib -L$TCINCLUDES/lib -llog" #log is Logcat
export GDB_CFLAGS="--sysroot=$TCSYSROOT -Wall -g -I$TCINCLUDES/include"


if [ -n "$COMPILEOPENSSL" ]; then
echo "================building openssl from $OPENSSL============================="
cd $OPENSSL
if [ -n "$MAKECLEAN" ]; then
make clean
fi
if [ -n "$CONFIGURE" ]; then
./Configure linux-generic32 no-shared no-dso -DL_ENDIAN --openssldir="$TCINCLUDES/ssl"
#override flags in Makefile
sed -e "s/^CFLAG=.*$/`grep -e \^CFLAG= Makefile` \$(CFLAGS)/g
s%^INSTALLTOP=.*%INSTALLTOP=$TCINCLUDES%g" Makefile > Makefile.out
mv Makefile.out Makefile
fi
make
make install_sw
echo "========================openssl DONE=================================="
fi
if [ -n "$COMPILECURL" ]; then
echo "==================building curl from $CURL================================="
cd $CURL
if [ -n "$MAKECLEAN" ]; then
make clean
fi
if [ -n "$CONFIGURE" ]; then
./configure --host=arm-linux --prefix=$TCINCLUDES --libdir="$TCINCLUDES/lib" --disable-shared --enable-static --with-random=/dev/urandom
fi
make
make install
echo "========================curl done================================="
fi
if [ -n "$COMPILEBOINC" ]; then
echo "==================building BOINC from $BOINC=========================="
cd $BOINC
if [ -n "$MAKECLEAN" ]; then
make clean
fi
if [ -n "$CONFIGURE" ]; then
./_autosetup
./configure --host=arm-linux --prefix=$BOINCINSTALL --with-boinc-platform="$BPNAME" --disable-server --disable-manager --disable-shared --enable-static
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
echo "=============================BOINC done============================="
echo "===================copy binary to assets direcotry of APK at $TARGETAPK========================="
sudo cp "$BOINCINSTALL/bin/boinc_client" "$TARGETAPK/assets/boinc_client"
fi
if [ -n "$COMPILEAPP" ]; then
echo "=================building application from $BAPP=============="
cd "$BAPP"
if [ -n "$MAKECLEAN" ]; then
make clean
fi
make -f Makefile_android
echo "============================app done============================="
fi
echo "============================script done=========================="
