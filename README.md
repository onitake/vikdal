# vikdal

A Dalvik bytecode disassembler, code graph generator and decompiler.

It can read .dex and .apk files.

:warning: This is a work in progress. In particular, decompilation does not
fully work yet.

## Overview

vikdal is dalvik in reverse. More precisely: With its syllables reversed.

Dalvik was the virtual machine that executed applications in the Android
operating system until version 4.4 (KitKat), where it was replaced by
Android Runtime (ART).

Application code for Android is delivered in Dalvik EXecution (DEX) format,
which is a packed application binary that contains bytecode for the Dalvik
virtual machine. This DEX format is still used in the Android Runtime.

## Build

The project is split into a core library and code graph browser.

Both can be built with Maven:

```
mvn package
```

This will produce .jar files in `core/target` and `browser/target`.
Unfortunately, the JGraphX dependency used by the browser is not actively
maintained and not available on Maven Central. Version 2.1.0.7 is included
with vikdal and will be compiled into the standalone jar automatically.

Possible replacements are being investigated.

## Run

The class browser can be run with:

```
java -jar browser/target/vikdal-browser-0.0.1-jar-with-dependencies.jar
```

It allows browsing the class hierarchy and displaying code graphs of
individual functions, with instructions presented in a Java-like
pseudocode form.

Work on a full decompiler is still in progress.

## Framework

The disassembler and decompiler code can be found in `core/src/java`.

`core/test/java` contains unit tests and some examples. These make use of
the main decompiler frontend in `ch.seto.vikdal.java.transformers.Decompiler`.

Decompilation is a five-step process:

1. Dalvik disassembly - a DEX or APK file is deserialised and the Davik
   bytecode is transformed into an instruction list
2. Code graph construction - the instruction list is transformed into a
   code graph, which is then run through multiple optimisation and
   augmentation steps. These can be extended by creating a transformer
   class that implements the `ch.seto.vikdal.java.transformers.Transformer`
   interface and adding an instance to the transformer list.
3. Abstract Syntax Tree generation - the code graph is walked through,
   with each node generating an AST branch that is attached to its parent.
4. Optimisation - the AST is optimised to represent a functional program
5. The AST is converted into Java code

In the present state, a large part of this process is still missing:

- Dalvik is a register-based virtual machine, with no concept of local
  variables. Registers can be repurposed for different types of data.
  An additional transformer needs to be implemented to resolve registers
  into local variables.
- Loops and blocks must be untangled before they can be converted into AST.
- The AST transformation is only implemented on a per-instruction level.
- AST optimisation and code generation is missing.

## License and Copyright

vikdal is copyright © 2014-2019 by Gregor Riepl.

vikdal is released under the GNU General Public License (GPL) version 3.

The file `browser/src/main/java/org/jgrapht/ext/JGraphXAdapter.java` is released under the
GNU Lesser General Public License (LGPL) version 2.1 and is copyright
2013 by JeanYves Tinevez.

The icons in `browser/src/main/resources/images` are released under the Apache 2.0 license and copyright
2017-2018 by The Apache Software Foundation. They were taken from the Netbeans IDE.

The JGraphX library in `browser/lib/repo/com/mxgraph/jgraphx/2.1.0.7/jgraphx-2.1.0.7.jar`
is copyright 2006-2009 Gaudenz Alder and 2008-2009 JGraph Ltd and released
under the GNU Lesser General Public License (LGPL) version 2.1.
