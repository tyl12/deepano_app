# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


#add_library(
#        lib_test
#        SHARED
#        IMPORTED
#        )
#set_target_properties(
#        lib_test
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/distribution/libs/${ANDROID_ABI}/libtest.so
#        )



add_library(
        lib_dpnusb
        SHARED
        IMPORTED
        )
set_target_properties(
        lib_dpnusb
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/distribution/libs/${ANDROID_ABI}/libdpnusb.so
        )


add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/dpnJniEntry.cpp
        )


target_include_directories(
        native-lib
        PRIVATE
        ${CMAKE_SOURCE_DIR}/src/main/cpp
        )

#add_library(avformat-57 SHARED IMPORTED)
#set_target_properties(avformat-57 PROPERTIES IMPORTED_LOCATION
#        ${ffmpeg_DIR}/lib/${ANDROID_ABI}/libavformat-57.so)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib


        # Links the target library to the log library
        # included in the NDK.
        lib_dpnusb
        ${log-lib}
        )