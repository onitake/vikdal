# vikdal

A Dalvik bytecode disassembler, code graph generator and decompiler.

It can read .dex and .apk files.

:warning: This is a work in progress. In particular, decompilation does not
work yet.

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

The browser can then be run with:

```
java -jar browser/target/vikdal-browser-0.0.1-jar-with-dependencies.jar
```

Aside from unit tests, there are also some examples in `core/test/java`.

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
