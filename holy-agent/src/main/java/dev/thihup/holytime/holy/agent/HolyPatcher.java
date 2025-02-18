package dev.thihup.holytime.holy.agent;

import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import jdk.internal.classfile.components.ClassRemapper;

import static java.util.Objects.requireNonNull;

class HolyPatcher implements ClassFileTransformer {

    private static final ClassRemapper CLASS_REMAPPER = ClassRemapper.of(oldClassName -> {
        if (oldClassName.packageName().startsWith("jdk.nashorn")) {
            return ClassDesc.ofDescriptor(oldClassName.descriptorString().replace("jdk/nashorn/", "org/openjdk/nashorn/"));
        }
        return oldClassName;
    });

    static ClassTransform matchingFieldName(String fieldName, FieldTransform fieldTransform) {
        return (builder, element) -> {
            if (element instanceof FieldModel fieldModel && fieldModel.fieldName().equalsString(fieldName)) {
                builder.transformField(fieldModel, fieldTransform);
                return;
            }
            builder.with(element);
        };
    }

    static ClassTransform matchingMethodName(String methodName, MethodTransform methodTransform) {
        return (builder, element) -> {
            if (element instanceof MethodModel methodModel
                    && methodModel.methodName().equalsString(methodName)) {
                builder.transformMethod(methodModel, methodTransform);
                return;
            }
            builder.with(element);
        };
    }

    static ClassTransform matchingAnyMethodName(List<String> methodName, MethodTransform methodTransform) {
        return (builder, element) -> {
            if (element instanceof MethodModel methodModel
                    && methodName.stream().anyMatch(methodModel.methodName()::equalsString)) {
                builder.transformMethod(methodModel, methodTransform);
                return;
            }
            builder.with(element);
        };
    }

    @Override
    public byte @Nullable [] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                       ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        return switch (className) {

            // WebLaf
            case "com/alee/utils/system/JavaVersion" -> loadNewJavaVersionClass(className);
            case "com/alee/utils/ProprietaryUtils" ->
                    patchClass(classfileBuffer, matchingMethodName("setupUIDefaults", Patches.callSwingUtilities2()));
            case "com/alee/utils/XmlUtils" -> patchClass(classfileBuffer, matchingMethodName("initializeXStream", Patches.enableAnyTypePermissionXStream()));

            // JDK
            case "sun/management/RuntimeImpl" ->
                    patchClass(classfileBuffer, matchingMethodName("getInputArguments", Patches.hideAgentFromCommandLine()));
            case "jdk/internal/reflect/Reflection" ->
                    patchClass(classfileBuffer, matchingMethodName("filterFields", Patches.returnArgumentWithoutFiltering()));
            case "sun/font/FontDesignMetrics" ->
                    patchClass(classfileBuffer, matchingFieldName("metricsCache", Patches.finalFlagRemover()));
            case "javax/swing/text/html/HTMLEditorKit" ->
                    patchClass(classfileBuffer, matchingFieldName("defaultFactory", Patches.finalFlagRemover()));

            // VLCJ
            case "uk/co/caprica/vlcj/player/NativeString" ->
                    patchClass(classfileBuffer,
                        matchingAnyMethodName(List.of("getNativeString", "copyNativeString"), Patches.removeDeprecatedCallJNA()));
            case "uk/co/caprica/vlcj/player/direct/DefaultDirectMediaPlayer" ->
                    patchClass(classfileBuffer, matchingMethodName("format", Patches.removeDeprecatedCallJNA()));

            // Holyrics
            case String e when e.startsWith("com/limagiran/js/") || e.startsWith("com/limagiran/holyrics/js/") ->
                 patchClass(classfileBuffer, CLASS_REMAPPER);

            case String _ -> null;
        };
    }

    private static byte @Nullable [] loadNewJavaVersionClass(String className) {
        try (InputStream inputStream = Premain.class.getResourceAsStream(
                "/dev/thihup/holytime/holy/agent/JavaVersion.class")) {
            return requireNonNull(inputStream).readAllBytes();
        } catch (Exception e) {
            System.out.println("[Holyrics Patcher] Failed to patch " + className);
            return null;
        }
    }

    public static byte[] patchClass(byte[] classfileBuffer,
                                     ClassTransform classTransform) {
        ClassFile classfile = ClassFile.of();
        ClassModel classModel = classfile.parse(classfileBuffer);
        return classfile.transformClass(classModel, classTransform);
    }

}
