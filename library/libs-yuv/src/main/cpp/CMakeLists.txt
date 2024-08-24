# For more information about using CMake with Android Studio,
# read the documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
# 设置构建本机库所需的CMake的最小版本。
cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.
# 声明并命名项目。
project("_yuv")
include_directories(libyuv/include)
add_subdirectory(libyuv ./build)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# 创建并命名一个库，将其设置为 STATIC 或 SHARED，并提供其源代码的相对路径。
# 你可以定义多个库，CMake 会为你构建它们。Gradle 会自动将共享库打包到 APK 中。
add_library(
        # Sets the name of the library.
        # 设置库的名称。
        _yuv
        # Sets the library as a shared library.
        # 将库设置为共享库。
        SHARED
        # Provides a relative path to your source file(s).
        # 提供源文件的相对路径。
        NativeYUV.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
# 搜索指定的预构建库并将路径存储为变量。
# 因为 CMake 默认在搜索路径中包含系统库，所以你只需要指定你想要添加的公共NDK库的名称。
# CMake 在完成构建之前会验证该库是否存在。
find_library(
        # Sets the name of the path variable.
        # 设置路径变量的名称。
        log-lib
        # Specifies the name of the NDK library that you want CMake to locate.
        # 指定要 CMake 定位的 NDK 库的名称。
        log
)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
# 指定 CMake 应该链接到目标库的库。
# 您可以链接多个库，例如您在此构建脚本中定义的库、预构建的第三方库或系统库。
target_link_libraries(
        # Specifies the target library.
        # 指定目标库。
        _yuv
        # Links the target library to the log library included in the NDK.
        # 将目标库链接到 NDK 中包含的日志库
        ${log-lib}
        yuv
)