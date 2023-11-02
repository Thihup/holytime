source dependencies.properties

mkdir mods
cd mods

echo "Downloading dependencies"

wget -q https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/$nashorn_version/nashorn-core-$nashorn_version.jar
wget -q https://repo1.maven.org/maven2/org/ow2/asm/asm/$asm_version/asm-$asm_version.jar
wget -q https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/$asm_version/asm-commons-$asm_version.jar
wget -q https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/$asm_version/asm-tree-$asm_version.jar
wget -q https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/$asm_version/asm-analysis-$asm_version.jar
wget -q https://repo1.maven.org/maven2/org/ow2/asm/asm-util/$asm_version/asm-util-$asm_version.jar
wget -q -O jaxws-ri.zip https://repo1.maven.org/maven2/com/sun/xml/ws/jaxws-ri/$jaxws_version/jaxws-ri-$jaxws_version.zip
wget -q -O openjfx_linux-x64_bin-jmods.zip https://download2.gluonhq.com/openjfx/$openjfx_version/openjfx-${openjfx_version}_linux-x64_bin-jmods.zip
    
echo "Extracting"

unzip -d . -q jaxws-ri.zip
unzip -d . -q openjfx_linux-x64_bin-jmods.zip

echo "Adding missing module-info to jakarta.annotation-api.jar"

jdeps --generate-module-info tmp ./jaxws-ri/lib/jakarta.annotation-api.jar
javac --patch-module=java.annotation=jaxws-ri/lib/jakarta.annotation-api.jar ./tmp/java.annotation/module-info.java
jar uf ./jaxws-ri/lib/jakarta.annotation-api.jar -C ./tmp/java.annotation/ module-info.class

echo "Generating runtime"
cd ..
jlink --module-path mods:mods/javafx-jmods-$openjfx_version:mods/jaxws-ri/lib --add-modules $modules --output "holytime" --compress zip-6 --generate-cds-archive --strip-debug
tar -czf holytime-linux.tar.gz holytime && echo "Generated runtime --> $PWD/holytime-linux.tar.gz"
