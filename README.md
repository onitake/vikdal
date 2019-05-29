# vikdal

A Dalvik bytecode disassembler, code graph generator and decompiler.

It can read .dex and .apk files.

:warning: This is a work in progress. In particular, decompilation does not
work yet.

## Build

Compile all Java sources in src/ and test/ .

You need the JgraphT, JGraphX, AntLR and JavaParser libraries.

A Maven build file is provided, but JGraphX is missing because upstream does
not upload builds to Maven Central. The .jar must be obtained elsewhere.

## Run

There are a few example applications in test/ .

To get started, run ch.seto.vikdal.browser.Browser - this is a graphical code
browser that nicely visualises code graphs of individual functions.

## License and Copyright

vikdal is copyright © 2014-2019 by Gregor Riepl.

vikdal is released under the GNU General Public License (GPL) version 3.

The file test/org/jgrapht/ext/JGraphXAdapter.java is released under the
GNU Lesser General Public License (LGPL) version 2.1 and is copyright
2013 by JeanYves Tinevez.

The icons in test/imaes are released under the Apache 2.0 license and copyright
2017-2018 by The Apache Software Foundation.
