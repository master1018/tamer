public class RedefineClassWithNativeMethodAgent {
    static Class clz;
    public static void premain(String agentArgs, final Instrumentation inst) throws Exception {
        String s = agentArgs.substring(0, agentArgs.indexOf(".class"));
        clz = Class.forName(s.replace('/', '.'));
        ClassLoader loader =
            RedefineClassWithNativeMethodAgent.class.getClassLoader();
        URL classURL = loader.getResource(agentArgs);
        if (classURL == null) {
            throw new Exception("Cannot find class: " + agentArgs);
        }
        int         redefineLength;
        InputStream redefineStream;
        System.out.println("Reading test class from " + classURL);
        if (classURL.getProtocol().equals("file")) {
            File f = new File(classURL.getFile());
            redefineStream = new FileInputStream(f);
            redefineLength = (int) f.length();
        } else {
            URLConnection conn = classURL.openConnection();
            redefineStream = conn.getInputStream();
            redefineLength = conn.getContentLength();
        }
        final byte[] buffer = new byte[redefineLength];
        new BufferedInputStream(redefineStream).read(buffer);
        new Timer(true).schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("Instrumenting");
                    ClassDefinition cld = new ClassDefinition(clz, buffer);
                    inst.redefineClasses(new ClassDefinition[] { cld });
                }
                catch (Exception e) { e.printStackTrace(); }
            }
        }, 500);
    }
}
