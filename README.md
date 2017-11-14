# Model-Checker [![Build Status](https://jenkins.tangentmc.net/buildStatus/icon?job=Model-Checker)](https://jenkins.tangentmc.net/job/Model-Checker/) [![GitHub release](https://img.shields.io/github/release/DavidSheridan/Model-Checker.svg)](https://github.com/DavidSheridan/Model-Checker/releases)
[Currently hosted application](http://modelchecker.tangentmc.net/).     
NB: URL may change and this link will be useless.   
Streader's Raiders SWEN302 Group Project

[Click for the latest jar build from master](https://jenkins.tangentmc.net/job/Model-Checker)

## Overview

Automata Concocter is developed using the [Bootstrap](http://getbootstrap.com/) framework.  

### Building / Distributing

-----------------------
>cd .../Model-Checker
Run the build task using `./gradlew build` inside modelchecker. This will take care of web dependencies and will give you a single executable jar.

### Main Application

-----------------------
The Automata Concocter is a web based application that constructs finite state automata based on text input of the
user and was designed as an educational tool for students studying software engineering. The AC allows the user to
define multiple automata and navigate through diffirent edges to reach different states within the user defined state
machines. The user can save defined automata as a txt file and can upload a previously saved txt file with defined
state machines.

###Styles

-----------------------

Main application css files can be found in the styles directory (Model-Checker/app/styles/).

