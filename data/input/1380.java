public class RedefineAgent implements ClassFileTransformer {
    static byte[] classfilebytes;
    static final String targetName = "java.math.BigInteger";
    static final String targetNameSlashes = "java/math/BigInteger";
    static boolean gotRedefineTransform = false;
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain    protectionDomain,
                            byte[] classfileBuffer) {
        if (className.equals(targetNameSlashes)) {
            if (classBeingRedefined == null) {
                System.out.println("Got bytes for: " + className);
                classfilebytes = Arrays.copyOf(classfileBuffer, classfileBuffer.length);
            } else {
                gotRedefineTransform = true;
            }
        }
        return null;
    }
    public static void testRedefine(Instrumentation inst) throws Exception {
        Class[] classes = inst.getAllLoadedClasses();
        for (Class k : classes) {
            if (k.getName().equals(targetName)) {
                throw new Exception("RedefineAgent Test error: class " + targetName + " has already been loaded.");
            }
        }
        inst.addTransformer(new RedefineAgent());
        ClassLoader.getSystemClassLoader().loadClass(targetName);
        classes = inst.getAllLoadedClasses();
        Class targetClass = null;
        for (Class k : classes) {
            if (k.getName().equals(targetName)) {
                targetClass = k;
                break;
            }
        }
        if (targetClass == null) {
            throw new Exception("RedefineAgent Test error: class " + targetName + " not loaded.");
        }
        if (classfilebytes == null) {
            throw new Exception("RedefineAgent Error(6439234): no transform call for class " + targetName);
        }
        ClassDefinition cd = new ClassDefinition(targetClass, classfilebytes);
        inst.redefineClasses(cd);
        System.out.println("RedefineAgent did redefine.");
        if (gotRedefineTransform) {
            System.out.println("RedefineAgent got redefine transform.");
        } else {
            throw new Exception("RedefineAgent Error(6439234): no transform call for redefine " + targetName);
        }
    }
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        System.out.println("RedefineAgent running...");
        System.out.println("RedefineAgent redefine supported: " + inst.isRedefineClassesSupported());
        int port = Integer.parseInt(args);
        System.out.println("RedefineAgent connecting back to Tool....");
        Socket s = new Socket();
        s.connect( new InetSocketAddress(port) );
        System.out.println("RedefineAgent connected to Tool.");
        testRedefine(inst);
        s.close();
    }
}
