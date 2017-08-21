# macros
macro(create_source_groups curdir)
    file(GLOB children RELATIVE ${PROJECT_SOURCE_DIR}/${curdir} ${PROJECT_SOURCE_DIR}/${curdir}/*)
    foreach(child ${children})
    if(IS_DIRECTORY ${PROJECT_SOURCE_DIR}/${curdir}/${child})
        create_source_groups(${curdir}/${child})
    else()
        string(REPLACE "/" "\\" groupname ${curdir})
        source_group(${groupname} FILES ${PROJECT_SOURCE_DIR}/${curdir}/${child})
    endif()
    endforeach()
endmacro()

# body
cmake_minimum_required (VERSION 3.2)
project (ImageScanner)

list(APPEND CMAKE_CXX_FLAGS "-std=c++1y -Wall -Werror -Wno-sign-compare -Wno-char-subscripts  -Wno-error=char-subscripts -Wno-unused-const-variable")


set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS} -g")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS} -O3")


set(BinDir ${PROJECT_SOURCE_DIR}/bin)
set(LibDir ${PROJECT_SOURCE_DIR}/lib)

# archive is for static libraries
set( CMAKE_RUNTIME_OUTPUT_DIRECTORY ${BinDir} )
set( CMAKE_LIBRARY_OUTPUT_DIRECTORY ${LibDir} )
set( CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${LibDir} )
foreach( OUTPUTCONFIG ${CMAKE_CONFIGURATION_TYPES} )
    string( TOUPPER ${OUTPUTCONFIG} OUTPUTCONFIG )
    set( CMAKE_RUNTIME_OUTPUT_DIRECTORY_${OUTPUTCONFIG} ${BinDir} )
    set( CMAKE_LIBRARY_OUTPUT_DIRECTORY_${OUTPUTCONFIG} ${LibDir} )
    set( CMAKE_ARCHIVE_OUTPUT_DIRECTORY_${OUTPUTCONFIG} ${LibDir} )
endforeach( OUTPUTCONFIG CMAKE_CONFIGURATION_TYPES )


include_directories("/usr/local/include")
include_directories("/usr/include")
include_directories("./include")

find_library(ANT_LIBRARY ant)
if (NOT ANT_LIBRARY)
    message(FATAL_ERROR "ant library not found")
endif()

# submissions
add_executable(v_1 "submit/v_1.cpp" "app/main_template.cpp")

# methods


# others
add_executable(nika others/nika.cpp app/main_template.cpp)
set_target_properties(nika PROPERTIES COMPILE_FLAGS "-Wno-unused-variable -Wno-error=unused-variable -Wno-error=unused-value -Wno-return-type -Wno-parentheses -Wno-error=parentheses")

add_executable(romka others/romka.cpp app/main_template.cpp)
set_target_properties(romka PROPERTIES COMPILE_FLAGS "-Wno-unused-variable -Wno-error=unused-variable -Wno-error=unused-value -Wno-return-type -Wno-parentheses -Wno-error=parentheses")