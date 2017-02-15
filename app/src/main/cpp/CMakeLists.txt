cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(NATIVE_LIBS_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)

include_directories(${ANDROID_NDK}/sources/android/native_app_glue)

add_library(pio-test SHARED pio-test.cpp)

add_library(native-app-glue STATIC
            ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c)

add_library(androidthings SHARED IMPORTED)
set_target_properties(androidthings PROPERTIES IMPORTED_LOCATION
    ${NATIVE_LIBS_DIR}/${ANDROID_ABI}/libandroidthings.so)

add_library(tensorflow SHARED IMPORTED)
set_target_properties(tensorflow PROPERTIES IMPORTED_LOCATION
    ${NATIVE_LIBS_DIR}/${ANDROID_ABI}/libtensorflow_inference.so)

target_link_libraries(pio-test
                      android
                      log
                      native-app-glue
                      androidthings)