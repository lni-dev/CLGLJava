# TODO
this file contains todos and notes about this project.

## Building the Project
currently the Project has to be built in several steps. Ideally, these would be automatically run
through gradle. (That means cmake build should be executed by the gradle build script). Native libs
should not be copied to resources, but instead into generated sources by that cmake task and then included
like we are doing with the spir-v shader binary.

## Project Name
Rename the cmake project to CVG4J.

## Native Objects
Some native objects (for example NullTerminatedUtf8String) may be set as pointer in a buffer,
but the JVM could garbage collect the string, which would invalidate the data the pointer
is pointing to. this could happen to any struct :(
<br><br>
Another problem is, that the constant allocating and freeing of small heap memory is bad for the 
performance.
<br><br>
A solution for both of these problems, would be a self-managed stack. Basically a pretty
large bytebuffer, in which we store the different small structs and then release them from
the stack once we don't need them anymore (e.g. at the end of a function call). This stack could
be stored on a per-thread basis utilising a custom Thread subclass.

## Proper Logging using llog
Add Logging (DEBUG, WARN and ERROR)

### llog missing some docs
e.g. How to activate logger, which properties are supported, what do they do.