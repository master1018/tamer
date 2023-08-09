class NativeMethodPrefixAgent {
    static ClassFileTransformer t0, t1, t2;
    static Instrumentation inst;
    static class Tr implements ClassFileTransformer {
        final String trname;
        final int transformId;
        Tr(int transformId) {
            this.trname = "tr" + transformId;
            this.transformId = transformId;
        }
        public byte[]
        transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain    protectionDomain,
            byte[] classfileBuffer) {
            boolean redef = classBeingRedefined != null;
            System.out.println(trname + ": " +
                               (redef? "Retransforming " : "Loading ") + className);
            if (className != null) {
                Options opt = new Options();
                opt.shouldInstrumentNativeMethods = true;
                opt.trackerClassName = "bootreporter/StringIdCallbackReporter";
                opt.wrappedTrackerMethodName = "tracker";
                opt.fixedIndex = transformId;
                opt.wrappedPrefix = "wrapped_" + trname + "_";
                try {
                    byte[] newcf =  Inject.instrumentation(opt, loader, className, classfileBuffer);
                    return redef? null : newcf;
                } catch (Throwable ex) {
                    System.err.println("ERROR: Injection failure: " + ex);
                    ex.printStackTrace();
                    System.err.println("Returning bad class file, to cause test failure");
                    return new byte[0];
                }
            }
            return null;
        }
    }
    static void write_buffer(String fname, byte[]buffer) {
        try {
            FileOutputStream outStream = new FileOutputStream(fname);
            outStream.write(buffer, 0, buffer.length);
            outStream.close();
        } catch (Exception ex) {
            System.err.println("EXCEPTION in write_buffer: " + ex);
        }
    }
    public static void
    premain (String agentArgs, Instrumentation instArg)
        throws IOException, IllegalClassFormatException,
        ClassNotFoundException, UnmodifiableClassException {
        inst = instArg;
        System.out.println("Premain");
        t1 = new Tr(1);
        t2 = new Tr(2);
        t0 = new Tr(0);
        inst.addTransformer(t1, true);
        inst.addTransformer(t2, false);
        inst.addTransformer(t0, true);
        instArg.setNativeMethodPrefix(t0, "wrapped_tr0_");
        instArg.setNativeMethodPrefix(t1, "wrapped_tr1_");
        instArg.setNativeMethodPrefix(t2, "wrapped_tr2_");
        instArg.retransformClasses(Runtime.class);
    }
}
