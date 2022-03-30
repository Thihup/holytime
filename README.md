# holyrics-newer-java

Steps to make [Holyrics](https://holyrics.com.br) work with newer Java versions (11/17+).
Overall, it works just fine. However, as much of its components were removed from standard JDKs, you must put it back to it work as it worked with JDK 8.

(Note that these commands were tried with Linux, for Windows some of them may change a little bit, like from ":" to ";")

## Download
- [Nashorn](https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/15.3/nashorn-core-15.3.jar) 
- [ASM](https://repo1.maven.org/maven2/org/ow2/asm/asm/9.2/asm-9.2.jar)
- [ASM Commons](https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.2/asm-commons-9.2.jar)
- [ASM Tree](https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.2/asm-tree-9.2.jar)
- [ASM Analysis](https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/9.2/asm-analysis-9.2.jar)
- [ASM Utilities](https://repo1.maven.org/maven2/org/ow2/asm/asm-util/9.2/asm-util-9.2.jar)
- [JAXB Bundle](https://search.maven.org/remotecontent?filepath=com/sun/xml/bind/jaxb-ri/2.3.6/jaxb-ri-2.3.6.zip)
- [JAXWS](https://search.maven.org/remotecontent?filepath=com/sun/xml/ws/jaxws-ri/2.3.5/jaxws-ri-2.3.5.zip)
- [JavaFX SDK Linux](https://download2.gluonhq.com/openjfx/18/openjfx-18_linux-x64_bin-sdk.zip)
- [JavaFX SDK Windows](https://download2.gluonhq.com/openjfx/18/openjfx-18_windows-x64_bin-sdk.zip)
(You can also use a JDK that already bundles the JavaFX, like "Full JDK" from [Bellsoft](https://bell-sw.com/pages/downloads), in this case, ignore all options related to JavaFX)

## Extract the bundles
1. Create a folder `mods`
2. Move the ASM jars (`asm-9.2.jar,asm-commons-9.2.jar,asm-tree-9.2.jar,asm-analysis-9.2.jar,asm-util-9.2.jar`) and the Nashorn (`nashorn-core-15.3.jar`) to the `mods` folder. 
3. Extract the bundles (`jaxb-ri-2.3.6.zip,jaxws-ri-2.3.5.zip,openjfx-18_linux-x64_bin-sdk.zip`) inside the `mods` folder.

In the end, you should have a structure similar to
```
mods
├── javafx-sdk-18
├── jaxb-ri
├── jaxws-ri
├── asm-9.2.jar
├── asm-analysis-9.2.jar
├── asm-commons-9.2.jar
├── asm-tree-9.2.jar
├── asm-util-9.2.jar
└── nashorn-core-15.3.jar
```

## Command line

You need to include these folders in the module-path, and add all the modules in the root set with:
```
--module-path mods:mods/javafx-sdk-18/lib:mods/jaxb-ri/mod:mods/jaxws-ri/lib --add-modules ALL-MODULE-PATH
```

After that, you need to specify the `--add-opens`:
```
--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.table=ALL-UNNAMED --add-opens=java.desktop/sun.font=ALL-UNNAMED --add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED --add-exports=javafx.web/com.sun.webkit.dom=ALL-UNNAMED
```


## Full command line

Linux:
```shell
java -Dsun.java2d.d3d=false --module-path mods:mods/javafx-sdk-18/lib:mods/jaxb-ri/mod:mods/jaxws-ri/lib --add-modules ALL-MODULE-PATH --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.table=ALL-UNNAMED --add-opens=java.desktop/sun.font=ALL-UNNAMED --add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED --add-exports=javafx.web/com.sun.webkit.dom=ALL-UNNAMED -jar Holyrics.jar
```

Windows:
```shell
java -Dsun.java2d.d3d=false --module-path mods;mods/javafx-sdk-18/lib;mods/jaxb-ri/mod;mods/jaxws-ri/lib --add-modules ALL-MODULE-PATH --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.table=ALL-UNNAMED --add-opens=java.desktop/sun.font=ALL-UNNAMED --add-exports=javafx.web/com.sun.webkit.dom=ALL-UNNAMED -jar Holyrics.exe
```

## Issues
Currently, there are some minor issues:
- Antialiasing does not work correctly
  - It seems to be related to: java.lang.NoSuchFieldException: AA_TEXT_PROPERTY_KEY
- The File icon shows either a broken image or the text "File"


# Did someone say Modules?
If your are feeling adventures, you can try to use the Holyrics in the module-path:

Linux:
```
java -Dsun.java2d.d3d=false --module-path mods:mods/javafx-sdk-18/lib:mods/jaxb-ri/mod:mods/jaxws-ri/lib:lib/AbsoluteLayout.jar:lib/beansbinding-1.2.1.jar:lib/Bible-1.3.0.jar:lib/bridj-0.7.0.jar:lib/commons-imaging-1.0-alpha1.jar:lib/dom4j-1.6.1.jar:lib/google-zxing-3.2.1.jar:lib/gson-2.4.jar:lib/jna-3.5.2.jar:lib/jna-platform-4.1.0.jar:lib/xmlbeans-2.6.0.jar:lib/webcam-capture-0.3.12.jar:lib/vlcj-2.4.1.jar:lib/VagalumeAPI_LG-1.0.0.jar:lib/sqlite-jdbc-3.8.11.2.jar:lib/SpellCheck-1.4.1.jar:lib/SevenZip.jar:lib/PopupWorker-1.0.0.jar:lib/poi-3.9-20121203.jar:lib/i18n-1.0.0.jar:lib/weblaf-1.29.jar:Holyrics.jar:lib/poi-ooxml-schemas-3.9-20121203.jar --patch-module=poi=lib/poi-ooxml-3.9-20121203.jar:lib/poi-scratchpad-3.9.jar --patch-module=weblaf=lib/holyrics-lib-fix-1.0.0.jar --add-modules ALL-MODULE-PATH,java.sql --add-opens=java.base/java.util=weblaf --add-opens=java.base/java.lang.reflect=weblaf --add-opens=java.base/java.lang=weblaf --add-opens=java.base/java.text=weblaf --add-opens=java.desktop/java.awt=weblaf --add-opens=java.desktop/java.awt.font=weblaf --add-opens=java.desktop/java.awt.event=weblaf --add-opens=java.desktop/javax.swing=weblaf --add-opens=java.desktop/javax.swing.table=weblaf --add-opens=java.desktop/sun.font=weblaf --add-opens=java.desktop/sun.awt.X11=weblaf --add-exports=javafx.web/com.sun.webkit.dom=Holyrics -m Holyrics
```
