package dev.thihup.holy.agent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.commons.ClassRemapper;
import jdk.internal.org.objectweb.asm.commons.SimpleRemapper;

public class Premain {

    private static final System.Logger LOGGER = System.getLogger(Premain.class.getName());
    enum SetupUIFix {
        AA_TEXT_INFO,
        RENDERING_HINTS
    }
    static SetupUIFix UI_FIX_TYPE = SetupUIFix.AA_TEXT_INFO;

    private static void openPackagesForModule(String moduleName,
        Map<String, Set<Module>> packageToModule,
        Instrumentation instrumentation) {
        ModuleLayer.boot().findModule(moduleName).ifPresentOrElse(module -> {
            instrumentation.redefineModule(module, Set.of(), Map.of(), packageToModule, Set.of(),
                Map.of());
        }, () -> LOGGER.log(Level.WARNING,
            "[Holyrics Patcher] Module " + moduleName + " not found"));
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs != null) {
            if (Integer.parseInt(agentArgs) == 1) {
                UI_FIX_TYPE = SetupUIFix.AA_TEXT_INFO;
            } else if (Integer.parseInt(agentArgs) == 2) {
                UI_FIX_TYPE = SetupUIFix.RENDERING_HINTS;
            }

        }
        LOGGER.log(Level.INFO, "[Holyrics Patcher] Agent loaded (Using " + UI_FIX_TYPE + ")");
        addOpens(inst);

        redefineReflection(inst);
        inst.addTransformer(new HolyPatcher());
    }

    private static void redefineReflection(Instrumentation inst) {
        try {
            //noinspection Java9ReflectionClassVisibility
            inst.redefineClasses(
                new ClassDefinition(Class.forName("jdk.internal.reflect.Reflection"),
                    HolyPatcher.patchClass("jdk/internal/reflect/Reflection",
                        Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(
                            "jdk/internal/reflect/Reflection.class")).readAllBytes())));
        } catch (UnmodifiableClassException | ClassNotFoundException | IOException e) {
            LOGGER.log(Level.WARNING,
                "[Holyrics Patcher] Failed to patch jdk/internal/reflect/Reflection");
        }
    }

    private static void addOpens(Instrumentation instrumentation) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        Module unnamedModule = systemClassLoader.getUnnamedModule();

        openPackagesForModule("java.desktop", Map.of(
            "java.awt", Set.of(unnamedModule),
            "java.awt.font", Set.of(unnamedModule),
            "java.awt.event", Set.of(unnamedModule),
            "javax.swing", Set.of(unnamedModule),
            "javax.swing.table", Set.of(unnamedModule),
            "javax.swing.text", Set.of(unnamedModule),
            "javax.swing.text.html", Set.of(unnamedModule),
            "javax.swing.plaf.basic", Set.of(unnamedModule),
            "sun.swing", Set.of(unnamedModule),
            "sun.font", Set.of(unnamedModule)), instrumentation);

        try {
            openPackagesForModule("java.desktop", Map.of("sun.awt.X11", Set.of(unnamedModule)),
                instrumentation);
        } catch (Exception ignored) {
        }

        openPackagesForModule("java.base", Map.of(
            "java.util", Set.of(unnamedModule),
            "java.lang", Set.of(unnamedModule),
            "java.lang.reflect", Set.of(unnamedModule),
            "java.text", Set.of(unnamedModule),
            "jdk.internal.org.objectweb.asm", Set.of(unnamedModule),
            "jdk.internal.org.objectweb.asm.commons", Set.of(unnamedModule)
        ), instrumentation);

        openPackagesForModule("javafx.web", Map.of(
            "com.sun.webkit.dom", Set.of(unnamedModule)
        ), instrumentation);
    }

    private static class HolyPatcher implements ClassFileTransformer {

        public static final Map<String, String> RENAMES = Map.of(
            "jdk/nashorn/api/scripting/NashornScriptEngineFactory",
            "org/openjdk/nashorn/api/scripting/NashornScriptEngineFactory",
            "jdk/nashorn/api/scripting/ScriptObjectMirror",
            "org/openjdk/nashorn/api/scripting/ScriptObjectMirror",
            "jdk/nashorn/api/scripting/ClassFilter",
            "org/openjdk/nashorn/api/scripting/ClassFilter"
        );
        public static final SimpleRemapper REMAPPER = new SimpleRemapper(RENAMES);

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

            switch (className) {
                case "com/alee/utils/system/JavaVersion":
                    try (InputStream inputStream = Premain.class.getResourceAsStream(
                        "/dev/thihup/holy/agent/JavaVersion.class")) {
                        byte[] bytes = Objects.requireNonNull(inputStream).readAllBytes();
                        return patchClass("com/alee/utils/system/JavaVersion", bytes);
                    } catch (Exception e) {
                        System.out.println("[Holyrics Patcher] Failed to patch " + className);
                        return null;
                    }
                case "com/alee/utils/ProprietaryUtils":
                case "sun/management/RuntimeImpl":
                case "jdk/internal/reflect/Reflection":
                case "sun/font/FontDesignMetrics":
                case "javax/swing/text/html/HTMLEditorKit":
                    return patchClass(className, classfileBuffer);
                case "com/limagiran/util/JavaScriptSecure":
                case "com/limagiran/util/MyClassFilter":
                    return remapClass(className, classfileBuffer);
                default:
                    return null;
            }
        }

        private byte[] remapClass(String className, byte[] classfileBuffer) {
            LOGGER.log(Level.DEBUG, "[Holyrics Patcher] Patching " + className);
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            ClassRemapper classRemapper = new ClassRemapper(classWriter, REMAPPER);

            classReader.accept(classRemapper, 0);

            return classWriter.toByteArray();
        }

        static byte[] patchClass(String className, byte[] classfileBuffer) {
            LOGGER.log(Level.DEBUG, "[Holyrics Patcher] Patching " + className);
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);

            classReader.accept(new PatchesVisitor(className, classWriter), 0);

            return classWriter.toByteArray();
        }
    }

}
