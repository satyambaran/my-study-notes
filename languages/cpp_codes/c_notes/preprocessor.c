// preprocessor
1. Macro
2. File Inclusion
3. Conditional Compilation
4. Other Directives 


1. Macro

#define         Used to define a macro
#undef          Used to undefine a macro
#include        Used to include a file in the source code program
#ifdef          Used to include a section of code if a certain macro is defined by #define
#ifndef         Used to include a section of code if a certain macro is not defined by #define
#if             Check for the specified condition
#else           Alternate code that executes when #if fails
#endif          Used to mark the end of #if, #ifdef, and #ifndef

2. File Inclusion

#include <file_name> // The ‘<‘ and ‘>’ brackets tell the compiler to look for the file in the standard directory
#include "filename"  // The double quotes ( ” ” ) tell the compiler to search for the header file in the source file’s directory


3. Conditional Compilation

#if Directive
#ifdef Directive
#ifndef Directive
#else Directive
#elif Directive
#endif Directive    // #endif directive is used to close off the #if, #ifdef, and #ifndef opening directives

4. Other Directives 
    Apart from the above directives, there are two more directives that are not commonly used. These are:

#undef Directive   //undefine the existing macro 
    #undef MACRO_NAME

#pragma Directive
    #pragma startup // These directives help us to specify the functions that are needed to run before program startup (before the control passes to main()).
    #pragma exit    // These directives help us to specify the functions that are needed to run just before the program exit (just before the control returns from main()).
    #pragma warn -rvl // directive hides those warnings which are raised when a function that is supposed to return a value does not return a value