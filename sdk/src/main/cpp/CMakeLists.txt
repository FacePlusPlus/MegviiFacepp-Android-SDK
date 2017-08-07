cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_AUTOMOC ON)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(MEGVII_FACEPP_VERSION "0.4.7")

include_directories(${CMAKE_SOURCE_DIR}/include)
message (${CMAKE_SOURCE_DIR})
set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${jnilibs}/${ANDROID_ABI})

add_library(MegviiFacepp-jni-${MEGVII_FACEPP_VERSION}
             SHARED
             megvii_facepp_jni.cpp)

add_library(facepp SHARED IMPORTED )
set_target_properties(facepp PROPERTIES
    IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libMegviiFacepp-${MEGVII_FACEPP_VERSION}.so")

add_library(face_new SHARED IMPORTED )
set_target_properties(face_new PROPERTIES
    IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libmegface-new.so")

find_library( # Sets the name of the path variable.
              log-lib
              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries(MegviiFacepp-jni-${MEGVII_FACEPP_VERSION} facepp face_new ${log-lib})

