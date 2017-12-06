#!/bin/bash
cd modelchecker 
if gradle build ; then
     cd .. && java -jar ModelChecker.jar
 else 
     cd ..
 fi
