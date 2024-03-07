/open TOOLING

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.nio.file.*;

void downloadFile(String urlStr, String outputFileStr) throws IOException {
    URL url = new URL(urlStr);
    Path outputFile = Paths.get(outputFileStr);

    Files.createDirectories(outputFile.getParent());

    try (InputStream in = new BufferedInputStream(url.openStream())) {
        Files.copy(in, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }
}

Properties loadProperties(String propertiesFile) throws Throwable {
    var properties = new Properties();
    try (InputStream in = new FileInputStream(propertiesFile)) {
        properties.load(in);
    }
    return properties;
}


String osName = System.getProperty("os.name").toLowerCase().contains("win") ? "windows" : "linux";
String osArch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";

var dependencies = loadProperties("dependencies.properties");

System.out.println("Downloading dependencies");
downloadFile(STR."https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/\{dependencies.get("nashorn_version")}/nashorn-core-\{dependencies.get("nashorn_version")}.jar", "mods/nashorn-core.jar");
downloadFile(STR."https://repo1.maven.org/maven2/org/ow2/asm/asm/\{dependencies.get("asm_version")}/asm-\{dependencies.get("asm_version")}.jar", "mods/asm.jar");
downloadFile(STR."https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/\{dependencies.get("asm_version")}/asm-commons-\{dependencies.get("asm_version")}.jar", "mods/asm-commons.jar");
downloadFile(STR."https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/\{dependencies.get("asm_version")}/asm-tree-\{dependencies.get("asm_version")}.jar", "mods/asm-tree.jar");
downloadFile(STR."https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/\{dependencies.get("asm_version")}/asm-analysis-\{dependencies.get("asm_version")}.jar", "mods/asm-analysis.jar");
downloadFile(STR."https://repo1.maven.org/maven2/org/ow2/asm/asm-util/\{dependencies.get("asm_version")}/asm-util-\{dependencies.get("asm_version")}.jar", "mods/asm-util.jar");
downloadFile(STR."https://repo1.maven.org/maven2/com/sun/xml/ws/jaxws-ri/\{dependencies.get("jaxws_version")}/jaxws-ri-\{dependencies.get("jaxws_version")}.zip", "mods/jaxws-ri.zip");
downloadFile(STR."https://download2.gluonhq.com/openjfx/\{dependencies.get("openjfx_version")}/openjfx-\{dependencies.get("openjfx_version")}_\{osName}-\{osArch}_bin-jmods.zip", "mods/openjfx-jmods.zip");

System.out.println("Extracting");

String jaxwsFolder = "mods/jaxws-ri";
String openjfxFolder = "mods/javafx-jmods";

jar("xf", "mods/jaxws-ri.zip"); 
jar("xf", "mods/openjfx-jmods.zip"); 
Files.move(Paths.get("jaxws-ri"), Paths.get(jaxwsFolder), StandardCopyOption.REPLACE_EXISTING);
Files.move(Paths.get(STR."javafx-jmods-\{dependencies.get("openjfx_version")}"), Paths.get(openjfxFolder), StandardCopyOption.REPLACE_EXISTING);

System.out.println("Adding missing module-info to jakarta.annotation-api.jar");
jdeps("--generate-module-info", "mods/tmp", STR."\{jaxwsFolder}/lib/jakarta.annotation-api.jar");
javac(STR."--patch-module=java.annotation=\{jaxwsFolder}/lib/jakarta.annotation-api.jar", "mods/tmp/java.annotation/module-info.java");
jar("uf", STR."\{jaxwsFolder}/lib/jakarta.annotation-api.jar", "-C", "mods/tmp/java.annotation/", "module-info.class");

System.out.println("Generating runtime");
jlink("--module-path", STR."mods\{File.pathSeparator}\{openjfxFolder}\{File.pathSeparator}\{jaxwsFolder}/lib", "--add-modules", (String)dependencies.get("modules"), "--output", "holytime", "--compress", "zip-6", "--generate-cds-archive", "--strip-debug");

System.out.println("Compressing")
try (FileSystem fs = FileSystems.newFileSystem(Paths.get(STR."holytime-\{osName}.zip"), Map.of("create", "true", "enablePosixFileAttributes", "true"));
    Stream<Path> runtimeFiles = Files.walk(Paths.get("holytime"))) {
    for (Path path : (Iterable<Path>) () -> runtimeFiles.iterator()) {
        Files.copy(path, fs.getPath("/").resolve(path.toString()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
}


/exit