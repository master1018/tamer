public class ClassAttrs {
    ClassAttrs() {
        class ConsInnerNamed {
            public void showMe() {
                printClassAttrs(this.getClass());
            }
        }
        ConsInnerNamed cinner = new ConsInnerNamed();
        cinner.showMe();
    }
    public static void main() {
        printClassAttrs(ClassAttrs.class);
        printClassAttrs(OtherClass.class);
        printClassAttrs(OtherPackageClass.class);
        class InnerNamed {
            public void showMe() {
                printClassAttrs(this.getClass());
            }
        }
        InnerNamed inner = new InnerNamed();
        inner.showMe();
        ClassAttrs attrs = new ClassAttrs();
        printClassAttrs((new OtherClass() { int i = 5; }).getClass());
        printClassAttrs(MemberClass.class);
        try {
            Constructor cons;
            cons = MemberClass.class.getConstructor(
                    new Class[] { MemberClass.class });
            System.out.println("constructor signature: "
                    + getSignatureAttribute(cons));
            Method meth;
            meth = MemberClass.class.getMethod("foo", (Class[]) null);
            System.out.println("method signature: "
                    + getSignatureAttribute(meth));
            Field field;
            field = MemberClass.class.getField("mWha");
            System.out.println("field signature: "
                    + getSignatureAttribute(field));
        } catch (NoSuchMethodException nsme) {
            System.err.println("FAILED: " + nsme);
        } catch (NoSuchFieldException nsfe) {
            System.err.println("FAILED: " + nsfe);
        } catch (RuntimeException re) {
            System.err.println("FAILED: " + re);
            re.printStackTrace();
        }
    }
    public static String getSignatureAttribute(Object obj) {
        Method method;
        try {
            if (obj instanceof AccessibleObject) {
                method = AccessibleObject.class.getDeclaredMethod(
                        "getSignatureAttribute");
            } else {
                method = Class.class.getDeclaredMethod(
                        "getSignatureAttribute");
            }
            method.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            System.err.println("getSignatureAttribute() not defined.");
            ex.printStackTrace();
            return "<unknown>";
        }
        try {
            return (String) method.invoke(obj);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    static class MemberClass<XYZ> {
        public MemberClass<XYZ> mWha;
        public MemberClass(MemberClass<XYZ> memb) {
            mWha = memb;
        }
        public Class<XYZ> foo() throws NoSuchMethodException {
            return null;
        }
    }
    static public class PublicMemberClass {
        float mBlah;
    }
    public static void printClassAttrs(Class clazz) {
        final boolean WORKING = false;
        Class clazz2;
        System.out.println("***** " + clazz + ":");
        System.out.println("  name: "
            + clazz.getName());
        System.out.println("  canonical: "
            + clazz.getCanonicalName());
        System.out.println("  simple: "
            + clazz.getSimpleName());
        System.out.println("  genericSignature: "
            + getSignatureAttribute(clazz));
        System.out.println("  super: "
            + clazz.getSuperclass());
        if (WORKING) System.out.println("  genericSuperclass: "
            + clazz.getGenericSuperclass());
        System.out.println("  declaring: "
            + clazz.getDeclaringClass());
        System.out.println("  enclosing: "
            + clazz.getEnclosingClass());
        System.out.println("  enclosingCon: "
            + clazz.getEnclosingConstructor());
        System.out.println("  enclosingMeth: "
            + clazz.getEnclosingMethod());
        System.out.println("  modifiers: "
            + clazz.getModifiers());
        System.out.println("  package: "
            + clazz.getPackage());
        System.out.println("  declaredClasses: "
            + stringifyTypeArray(clazz.getDeclaredClasses()));
        System.out.println("  member classes: "
            + stringifyTypeArray(clazz.getClasses()));
        System.out.println("  isAnnotation: "
            + clazz.isAnnotation());
        System.out.println("  isAnonymous: "
            + clazz.isAnonymousClass());
        System.out.println("  isArray: "
            + clazz.isArray());
        System.out.println("  isEnum: "
            + clazz.isEnum());
        System.out.println("  isInterface: "
            + clazz.isInterface());
        System.out.println("  isLocalClass: "
            + clazz.isLocalClass());
        System.out.println("  isMemberClass: "
            + clazz.isMemberClass());
        System.out.println("  isPrimitive: "
            + clazz.isPrimitive());
        System.out.println("  isSynthetic: "
            + clazz.isSynthetic());
        if (WORKING) System.out.println("  genericInterfaces: "
            + stringifyTypeArray(clazz.getGenericInterfaces()));
    }
    private static String stringifyTypeArray(Type[] types) {
        StringBuilder stb = new StringBuilder();
        boolean first = true;
        stb.append("[" + types.length + "]");
        for (Type t: types) {
            if (first) {
                stb.append(" ");
                first = false;
            } else {
                stb.append(", ");
            }
            stb.append(t.toString());
        }
        return stb.toString();
    }
}
