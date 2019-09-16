This directory should contain  both the jar and some directories with the native binaries for z3. You should keep this directory as it is.

SWEN324HandBuild 👽  ls -l
total 173336
-rw-r--r--  1 dstr  staff  86979478  4 Sep 11:45 ModelChecker.jar
drwxr-xr-x  5 dstr  staff       160  4 Sep 11:03 amd64-linux
drwxr-xr-x  7 dstr  staff       224  4 Sep 11:03 amd64-windows
-rw-r--r--  1 dstr  staff        88  4 Sep 11:45 recentfiles.conf
drwxr-xr-x  5 dstr  staff       160  4 Sep 11:42 x86-linux
drwxr-xr-x  7 dstr  staff       224  4 Sep 11:41 x86-windows
drwxr-xr-x  5 dstr  staff       160  4 Sep 11:38 x86_64-macosx
SWEN324HandBuild 👽  java -jar ModelChecker.jar


To run the program cd into the directory and type

java -jar ModelChecker.jar


It appears that MacOS may attach ACL information to the binaries and this will mean that the jar will crash with class not found error

your native files in x86_64-macosx should look like

total 122568
-rwxr-xr-x  1 dstr  staff  41099960  5 Sep 11:05 libz3.a
-rwxr-xr-x  1 dstr  staff  21464892  5 Sep 11:05 libz3.dylib
-rwxr-xr-x  1 dstr  staff    182792  5 Sep 11:05 libz3java.dylib
Model-Checker 👽

if you see -rwxr-xr-x@  then this is the problem.

chmod -N removes all access control entries for a file or folder.

chmod -a allows one to remove access control entries individually

have a look at
https://backdrift.org/fixing-mac-osx-file-permissions-and-acls-from-the-command-line
