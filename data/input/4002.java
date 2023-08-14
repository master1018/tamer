public class InvokeDynamicPrintArgs {
    public static void main(String... av) throws Throwable {
        if (av.length > 0 && av[0].equals("--check-output"))  openBuf();
        if (av.length > 0 && av[0].equals("--security-manager"))  setSM();
        System.out.println("Printing some argument lists, starting with a empty one:");
        INDY_nothing().invokeExact();                 
        INDY_bar().invokeExact("bar arg", 1);         
        INDY_bar2().invokeExact("bar2 arg", 222);     
        INDY_baz().invokeExact("baz arg", 2, 3.14);   
        INDY_foo().invokeExact("foo arg");            
        System.out.println("Done printing argument lists.");
        closeBuf();
        checkConstantRefs();
    }
    private static void checkConstantRefs() throws Throwable {
        assertEquals(MT_bsm(), MH_bsm().type());
        assertEquals(MT_bsm2(), MH_bsm2().type());
        try {
            assertEquals(MT_bsm(), non_MH_bsm().type());
            assertEquals(false, System.getSecurityManager() != null);
        } catch (SecurityException ex) {
            assertEquals(true, System.getSecurityManager() != null);
        }
    }
    private static void assertEquals(Object exp, Object act) {
        if (exp == act || (exp != null && exp.equals(act)))  return;
        throw new AssertionError("not equal: "+exp+", "+act);
    }
    private static void setSM() {
        class SM extends SecurityManager {
            public void checkPackageAccess(String pkg) {
                if (pkg.startsWith("test."))
                    throw new SecurityException("checkPackageAccess "+pkg);
            }
            public void checkMemberAccess(Class<?> clazz, int which) {
                if (clazz == InvokeDynamicPrintArgs.class)
                    throw new SecurityException("checkMemberAccess "+clazz.getName()+" #"+which);
            }
            public void checkPermission(java.security.Permission perm) {
            }
        }
        System.setSecurityManager(new SM());
    }
    @Test
    public void testInvokeDynamicPrintArgs() throws IOException {
        System.err.println(System.getProperties());
        String testClassPath = System.getProperty("build.test.classes.dir");
        if (testClassPath == null)  throw new RuntimeException();
        String[] args = new String[]{
            "--verify-specifier-count=3",
            "--verbose",
            "--expand-properties", "--classpath", testClassPath,
            "--java", "test.java.lang.invoke.InvokeDynamicPrintArgs", "--check-output"
        };
        System.err.println("Indify: "+Arrays.toString(args));
        indify.Indify.main(args);
    }
    private static PrintStream oldOut;
    private static ByteArrayOutputStream buf;
    private static void openBuf() {
        oldOut = System.out;
        buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
    }
    private static void closeBuf() {
        if (buf == null)  return;
        System.out.flush();
        System.setOut(oldOut);
        String[] haveLines = new String(buf.toByteArray()).split("[\n\r]+");
        for (String line : haveLines)  System.out.println(line);
        Iterator<String> iter = Arrays.asList(haveLines).iterator();
        for (String want : EXPECT_OUTPUT) {
            String have = iter.hasNext() ? iter.next() : "[EOF]";
            if (want.equals(have))  continue;
            System.err.println("want line: "+want);
            System.err.println("have line: "+have);
            throw new AssertionError("unexpected output: "+have);
        }
        if (iter.hasNext())
            throw new AssertionError("unexpected output: "+iter.next());
    }
    private static final String[] EXPECT_OUTPUT = {
        "Printing some argument lists, starting with a empty one:",
        "[test.java.lang.invoke.InvokeDynamicPrintArgs, nothing, ()void][]",
        "[test.java.lang.invoke.InvokeDynamicPrintArgs, bar, (String,int)void, class java.lang.Void, void type!, 1, 234.5, 67.5, 89][bar arg, 1]",
        "[test.java.lang.invoke.InvokeDynamicPrintArgs, bar2, (String,int)void, class java.lang.Void, void type!, 1, 234.5, 67.5, 89][bar2 arg, 222]",
        "[test.java.lang.invoke.InvokeDynamicPrintArgs, baz, (String,int,double)void, 1234.5][baz arg, 2, 3.14]",
        "[test.java.lang.invoke.InvokeDynamicPrintArgs, foo, (String)void][foo arg]",
        "Done printing argument lists."
    };
    private static boolean doPrint = true;
    private static void printArgs(Object bsmInfo, Object... args) {
        String message = bsmInfo+Arrays.deepToString(args);
        if (doPrint)  System.out.println(message);
    }
    private static MethodHandle MH_printArgs() throws ReflectiveOperationException {
        shouldNotCallThis();
        return lookup().findStatic(lookup().lookupClass(),
                                   "printArgs", methodType(void.class, Object.class, Object[].class));
    }
    private static CallSite bsm(Lookup caller, String name, MethodType type) throws ReflectiveOperationException {
        Object bsmInfo = Arrays.asList(caller, name, type);
        return new ConstantCallSite(MH_printArgs().bindTo(bsmInfo).asCollector(Object[].class, type.parameterCount()).asType(type));
    }
    private static MethodType MT_bsm() {
        shouldNotCallThis();
        return methodType(CallSite.class, Lookup.class, String.class, MethodType.class);
    }
    private static MethodHandle MH_bsm() throws ReflectiveOperationException {
        shouldNotCallThis();
        return lookup().findStatic(lookup().lookupClass(), "bsm", MT_bsm());
    }
    private static MethodHandle non_MH_bsm() throws ReflectiveOperationException {
        return lookup().findStatic(lookup().lookupClass(), "bsm", MT_bsm());
    }
    public static class PrintingCallSite extends ConstantCallSite {
        final Lookup caller;
        final String name;
        final Object[] staticArgs;
        PrintingCallSite(Lookup caller, String name, MethodType type, Object... staticArgs) throws Throwable {
            super(type, MH_createTarget());
            this.caller = caller;
            this.name = name;
            this.staticArgs = staticArgs;
        }
        public MethodHandle createTarget() {
            try {
                return lookup().bind(this, "runTarget", genericMethodType(0, true)).asType(type());
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        public Object runTarget(Object... dynamicArgs) {
            List<Object> bsmInfo = new ArrayList<>(Arrays.asList(caller, name, type()));
            bsmInfo.addAll(Arrays.asList(staticArgs));
            printArgs(bsmInfo, dynamicArgs);
            return null;
        }
        private static MethodHandle MH_createTarget() throws ReflectiveOperationException {
            shouldNotCallThis();
            return lookup().findVirtual(lookup().lookupClass(), "createTarget", methodType(MethodHandle.class));
        }
    }
    private static CallSite bsm2(Lookup caller, String name, MethodType type, Object... arg) throws Throwable {
        return new PrintingCallSite(caller, name, type, arg);
    }
    private static MethodType MT_bsm2() {
        shouldNotCallThis();
        return methodType(CallSite.class, Lookup.class, String.class, MethodType.class, Object[].class);
    }
    private static MethodHandle MH_bsm2() throws ReflectiveOperationException {
        shouldNotCallThis();
        return lookup().findStatic(lookup().lookupClass(), "bsm2", MT_bsm2());
    }
    private static MethodHandle INDY_nothing() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm().invoke(lookup(),
                                                  "nothing", methodType(void.class)
                                                  )).dynamicInvoker();
    }
    private static MethodHandle INDY_foo() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm().invoke(lookup(),
                                                  "foo", methodType(void.class, String.class)
                                                  )).dynamicInvoker();
    }
    private static MethodHandle INDY_bar() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm2().invoke(lookup(),
                                                  "bar", methodType(void.class, String.class, int.class)
                                                  , Void.class, "void type!", 1, 234.5F, 67.5, (long)89
                                                  )).dynamicInvoker();
    }
    private static MethodHandle INDY_bar2() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm2().invoke(lookup(),
                                                  "bar2", methodType(void.class, String.class, int.class)
                                                  , Void.class, "void type!", 1, 234.5F, 67.5, (long)89
                                                  )).dynamicInvoker();
    }
    private static MethodHandle INDY_baz() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm2().invoke(lookup(),
                                                  "baz", methodType(void.class, String.class, int.class, double.class)
                                                  , 1234.5
                                                  )).dynamicInvoker();
    }
    private static void shouldNotCallThis() {
        if (System.getProperty("InvokeDynamicPrintArgs.allow-untransformed") != null)  return;
        throw new AssertionError("this code should be statically transformed away by Indify");
    }
}
