# decoder.java

## Overview
This code is designed to parse malicious Banload JAR files which employ a DES-based crypto routine to mask important network indicators from malware analysts. The code is often difficult to manually examine due to the length of variables and functions (often several thousand characters long) and moreover the samples often will not execute in environments outside of the intended victims, making behavioural analysis difficult. This code helps analysts extract key network and other indicators which may be needed to protect your environment and eradication of malicious infrastructure.

## Compiling
The code is written in Java. This isn't my favourite language, but since the bad guys used Java to write their code, it was easier for me to implement some of their decryption routines in the same language. You'll need to compile the java file using javac like:

    $ javac decoder.java
    
This will produce the decoder.class file, also included in this repo.

## Running
The code can be invoke from the command line, taking one command line argument which is the filename to be examined:

    $ java decoder file.name
    
This will then output the decoded strings to your console window. 

## Licence
Released under MIT Licence. Enjoy.

## Contact
You can contact me on cdh dot cert @ gmail dot com.


