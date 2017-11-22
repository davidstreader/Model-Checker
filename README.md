# Model-Checker 

[![Build Status](https://img.shields.io/travis/davidstreader/Model-Checker.svg)](https://jenkins.tangentmc.net/job/Model-Checker/)
[![GitHub release](https://img.shields.io/github/release/davidstreader/Model-Checker.svg)](https://github.com/davidstreader/Model-Checker/releases)
[Click for the latest jar build from master](https://jenkins.tangentmc.net/job/Model-Checker)

## Overview

### Main Application

----------------------- 

The Automata Concocter is a web based application that
constructs finite state automata based on text input of the user and was
designed as an educational tool for students studying software engineering. The
AC allows the user to define multiple automata and navigate through diffirent
edges to reach different states within the user defined state machines. The user
can save defined automata as a txt file and can upload a previously saved txt
file with defined state machines.

### Building / Distributing

-----------------------

```bash 
$cd modelchecker
$./gradlew build
$cd ..
```

This will build a jar file `ModelChecker.jar` in the root folder of the
repository


### Styles

-----------------------

Main application css files can be found in the styles directory
(Model-Checker/app/styles/).


### Overall Structure

-----------------------

The program is roughly broken into the `Lexer`, `Abstract Syntax Tree`,
`Interpreter` and the `automata` itself, which may have `operations` performed
upon it.
