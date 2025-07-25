package dev.thihup.holytime.holy.agent;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Premain {

    private static final System.Logger LOGGER = System.getLogger(Premain.class.getName());

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
        if (Boolean.getBoolean("holyagent.disable")) {
            System.out.println(
                "[Holyrics Patcher] Agent disabled by the system property 'holyagent.disable'.");
            return;
        }

        LOGGER.log(Level.INFO, "[Holyrics Patcher] Agent loaded");
        try {
             addOpens(inst);
        } catch (UnsupportedOperationException e) {
             LOGGER.log(Level.WARNING,
               "[Holyrics Patcher] Failed to open packages for modules. Probably during -Xshare:dump. Disabling agent.");
             return;
        }
        redefineReflection(inst);
        inst.addTransformer(new HolyPatcher());
    }

    private static void redefineReflection(Instrumentation inst) {
        try {

            byte[] classfileBuffer = requireNonNull(ClassLoader.getSystemResourceAsStream(
                    "jdk/internal/reflect/Reflection.class")).readAllBytes();
            //noinspection Java9ReflectionClassVisibility
            ClassDefinition classDefinition = new ClassDefinition(
                    Class.forName("jdk.internal.reflect.Reflection"),
                    HolyPatcher.patchClass(
                            classfileBuffer,
                            HolyPatcher.matchingMethodName("filterFields", Patches.returnArgumentWithoutFiltering())));
            inst.redefineClasses(classDefinition);
        } catch (UnmodifiableClassException | ClassNotFoundException | IOException e) {
            LOGGER.log(Level.WARNING,
                    "[Holyrics Patcher] Failed to patch jdk/internal/reflect/Reflection");
        }
    }

    private static void addOpens(Instrumentation instrumentation) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        Module unnamedModule = systemClassLoader.getUnnamedModule();
        Module xstreamModule = ModuleLayer.boot().findModule("com.thoughtworks.xstream").orElse(unnamedModule);
        Module weblafModule = ModuleLayer.boot().findModule("com.alee.weblaf").orElse(unnamedModule);

        Set<Module> modules = new HashSet<>(List.of(unnamedModule, xstreamModule, weblafModule));

        openPackagesForModule("java.base", Map.of(
                "java.util", modules,
                "java.lang", modules,
                "java.lang.reflect", modules,
                "java.text", modules,
                "jdk.internal.classfile.components", modules
                ), instrumentation);

        openPackagesForModule("java.desktop", Map.of(
                "java.awt", modules,
                "java.awt.font", modules,
                "java.awt.event", modules,
                "javax.swing", modules,
                "javax.swing.table", modules,
                "javax.swing.text", modules,
                "javax.swing.text.html", modules,
                "javax.swing.plaf.basic", modules,
                "sun.swing", modules
               ,"sun.font", modules
                ), instrumentation);

        try {
            openPackagesForModule("java.desktop", Map.of("sun.awt.X11", modules),
                    instrumentation);
        } catch (Exception ignored) {
        }

        try {
            openPackagesForModule("java.desktop", Map.of("sun.awt.shell", modules),
                    instrumentation);
        } catch (Exception ignored) {
        }

        openPackagesForModule("javafx.web", Map.of(
                "com.sun.webkit.dom", modules
        ), instrumentation);



    }

}
