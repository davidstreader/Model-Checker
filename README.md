# Model-Checker 

[![Build Status](https://jenkins.tangentmc.net/job/Model-Checker/badge/icon)](https://jenkins.tangentmc.net/job/Model-Checker/)

## Overview

### Main Application

----------------------- 

The Automata Concocter is an application that constructs finite state automata
based on text input of the user and was designed as an educational tool for
students studying software engineering. The AC allows the user to define
multiple automata and navigate through different edges to reach different states
within the user defined state machines. The user can save defined automata as a
txt file and can upload a previously saved txt file with defined state machines.

### Building / Distributing

-----------------------
Starting note that this build process has only been tested on Java JDK 8u151.

## Linux
```bash 
$cd modelchecker
$./gradlew build && java -jar ../ModelChecker.jar
```
Please be aware that if you have a web authentication proxy you will need to add arguments to gradlew.

## Windows

Ensure that the `JAVA_HOME` environmental variable is set correctly.

```bash
$cd modelchecker
$./gradlew.bat build
$java -jar ../ModelChecker.jar
```

These will build a jar file `ModelChecker.jar` in the root folder of the
repository.

### Overall Structure

-----------------------

Lexer -> Parser -> Interpreter -> Evaluator -> Graphical Display

The lexer produces a list of Tokens from the input code given via the interface,
this list is then passed to the Parser.  The Parser produces an AST (Abstract
syntax tree) which is then used by the interperator to build automata diagrams.
Finally the evaluator first tests the operations (Which are similar to tests),
then it moves onto testing equations which is done by generating different
automata and the applying the user created automata to test if it works over a
given set space.
