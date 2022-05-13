package dev.thihup.holy.agent;


import dev.thihup.holy.agent.Premain.SetupUIFix;
import java.lang.reflect.Modifier;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

public class PatchesVisitor extends ClassVisitor {

    private final String className;
    private static final int API_VERSION;

    static {
        int apiVersion;
        Class<Opcodes> opcodesClass = Opcodes.class;
        try {
            apiVersion = opcodesClass.getDeclaredField("ASM9").getInt(null);
        } catch (Throwable var1) {
            try {
                apiVersion = opcodesClass.getDeclaredField("ASM8").getInt(null);
            } catch (Throwable var2) {
                try {
                    apiVersion = opcodesClass.getDeclaredField("ASM7").getInt(null);
                } catch (Throwable var3) {
                    try {
                        apiVersion = opcodesClass.getDeclaredField("ASM6").getInt(null);
                    } catch (Throwable var4) {
                        try {
                            apiVersion = opcodesClass.getDeclaredField("ASM5").getInt(null);
                        } catch (Throwable var5) {
                            try {
                                apiVersion = opcodesClass.getDeclaredField("ASM4").getInt(null);
                            } catch (Throwable var6) {
                                apiVersion = 0;
                            }
                        }
                    }
                }
            }
        }

        API_VERSION = apiVersion;
    }

    protected PatchesVisitor(String className, ClassVisitor classWriter) {
        super(API_VERSION, classWriter);
        this.className = className;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature,
        Object value) {

        if (className.equals("sun/font/FontDesignMetrics") && name.equals("metricsCache")) {
            return cv.visitField(Modifier.FINAL ^ access, name, descriptor, signature, value);
        }

        if (className.equals("javax/swing/text/html/HTMLEditorKit") && name.equals(
            "defaultFactory")) {
            return cv.visitField(Modifier.FINAL ^ access, name, descriptor, signature, value);
        }

        return cv.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
        String[] exceptions) {

        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature,
            exceptions);

        if (className.equals("jdk/internal/reflect/Reflection") && name.equals("filterFields")) {
            return new MethodVisitor(API_VERSION) {
                @Override
                public void visitCode() {
                    // Field[] filterFields(Class<?> containingClass, Field[] fields) {return fields; }
                    methodVisitor.visitCode();
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitInsn(Opcodes.ARETURN);

                    methodVisitor.visitMaxs(0, 0);
                    methodVisitor.visitEnd();
                }
            };
        }

        if (className.equals("com/alee/utils/ProprietaryUtils") && name.equals("setupUIDefaults")) {
            return new MethodVisitor(API_VERSION) {
                @Override
                public void visitCode() {
                    methodVisitor.visitCode();

                    if (Premain.UI_FIX_TYPE == SetupUIFix.RENDERING_HINTS) {
                        // table.put(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        // table.put(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints",
                            "KEY_ANTIALIASING", "Ljava/awt/RenderingHints$Key;");
                        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints",
                            "VALUE_ANTIALIAS_ON", "Ljava/lang/Object;");
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map",
                            "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                        methodVisitor.visitInsn(Opcodes.POP);
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints",
                            "KEY_TEXT_ANTIALIASING", "Ljava/awt/RenderingHints$Key;");
                        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints",
                            "VALUE_TEXT_ANTIALIAS_ON", "Ljava/lang/Object;");
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map",
                            "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                        methodVisitor.visitInsn(Opcodes.POP);

                    } else if (Premain.UI_FIX_TYPE == SetupUIFix.AA_TEXT_INFO) {
                        // sun.swing.SwingUtilities2.putAATextInfo(true, table);

                        methodVisitor.visitInsn(Opcodes.ICONST_1);
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "sun/swing/SwingUtilities2",
                            "putAATextInfo",
                            "(ZLjava/util/Map;)V", false);
                    }
                    methodVisitor.visitInsn(Opcodes.RETURN);
                    methodVisitor.visitMaxs(0, 0);
                    methodVisitor.visitEnd();
                }
            };
        }
        if (className.equals("sun/management/RuntimeImpl") && name.equals("getInputArguments")) {
            return new MethodVisitor(API_VERSION) {
                @Override
                public void visitCode() {
                    methodVisitor.visitCode();

                    // Util.checkMonitorAccess();
                    // var 0 = new ArrayList(jvm.getVmArguments());
                    // 0.removeIf("-javaagent"::startWith);
                    // return Collections.unmodifiableList(0);

                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "sun/management/Util",
                        "checkMonitorAccess", "()V", false);

                    methodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
                    methodVisitor.visitInsn(Opcodes.DUP);

                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "sun/management/RuntimeImpl",
                        "jvm", "Lsun/management/VMManagement;");

                    methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        "sun/management/VMManagement",
                        "getVmArguments", "()Ljava/util/List;", true);

                    methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList",
                        "<init>", "(Ljava/util/Collection;)V", false);

                    methodVisitor.visitVarInsn(Opcodes.ASTORE, 1);
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitLdcInsn("-javaagent:patches.jar");

                    methodVisitor.visitLdcInsn("-javaagent");
                    methodVisitor.visitInvokeDynamicInsn("test",
                        "(Ljava/lang/String;)Ljava/util/function/Predicate;",
                        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                            "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                            false),
                        Type.getMethodType(Type.BOOLEAN_TYPE, Type.getType(Object.class)),
                        new Handle(Opcodes.H_INVOKEVIRTUAL, "java/lang/String", "startsWith",
                            "(Ljava/lang/String;)Z", false),
                        Type.getMethodType(Type.BOOLEAN_TYPE, Type.getType(String.class))
                    );

                    methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
                        "removeIf",
                        "(Ljava/util/function/Predicate;)Z", true);

                    methodVisitor.visitInsn(Opcodes.POP);
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Collections",
                        "unmodifiableList", "(Ljava/util/List;)Ljava/util/List;", false);

                    methodVisitor.visitInsn(Opcodes.ARETURN);
                    methodVisitor.visitMaxs(0, 0);
                    methodVisitor.visitEnd();
                }
            };
        }

        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        if (className.equals("com/alee/utils/system/JavaVersion")) {
            // JavaVersion(String var) { this(); }
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "com/alee/utils/system/JavaVersion", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // int compareVersion(double var0, int var1, int var2) { return compareTo(var0, var1, var2); }
            MethodVisitor mv2 = cv.visitMethod(Opcodes.ACC_PUBLIC,
                "compareVersion", "(DII)I", null, null);
            mv2.visitCode();
            mv2.visitVarInsn(Opcodes.ALOAD, 0);
            mv2.visitVarInsn(Opcodes.DLOAD, 1);
            mv2.visitVarInsn(Opcodes.ILOAD, 3);
            mv2.visitVarInsn(Opcodes.ILOAD, 4);
            mv2.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "com/alee/utils/system/JavaVersion", "compareTo", "(DII)I", false);
            mv2.visitInsn(Opcodes.IRETURN);
            mv2.visitMaxs(0, 0);
            mv2.visitEnd();
        }

        cv.visitEnd();
    }
}
