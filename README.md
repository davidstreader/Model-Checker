# Model-Checker [![Build Status](https://teamcity.tangentmc.net/app/rest/builds/buildType(id:ModelChecker_Build)/statusIcon)](https://teamcity.tangentmc.net/viewType.html?buildTypeId=ModelChecker_Build&guest=1)

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

Lexer -> Parser -> Expander> Replacer > Interpreter -> Evaluator -> Graphical Display

The lexer produces a list of Tokens from the input code given via the interface,
this list is then passed to the Parser.  The Parser produces an AST (Abstract
syntax tree)  The Expander replaces indexes with finite state approximations and outputs a new AST. 
The Replacer removes references to Locally defined processes building a final AST.

The final AST is then used by the interperator to build process models, automata diagrams or  petri Nets.

Functions parallel composition, choice and sequential composition map pairs of automata/ Petri Nets to a resulting automata/ Petri Net.
Finally the evaluator first tests the operations (Which are similar to tests),
then it moves onto testing equations which is done by generating different
automata and the applying the user created automata to test if it works over a
given set space.
