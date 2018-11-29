
set -e

export GRADLE_HOME=/usr/local/gradle-4.4
export PATH=$PATH:$GRADLE_HOME/bin

# android
export ANDROID_NDK=$HOME/Android/Sdk/ndk-bundle
export ADB_HOME=$HOME/android-sdk/platform-tools/

export PATH=$PATH:$ANDROID_NDK:$ANDROID_NDK/toolchains/x86_64-4.9/prebuilt/linux-x86_64/bin:/usr/local/gradle/bin:$ADB_HOME

ndk-build

mkdir -p ../include && cp test.h ../include/

#cp ../libs/arm64-v8a/libtest.so ../../libs/arm64-v8a/
#cp ../libs/armeabi-v7a/libtest.so ../../libs/armeabi-v7a/

#mkdir -p ../../../distribution/libs/ && cp -r ../libs/*  ../../../distribution/libs/
