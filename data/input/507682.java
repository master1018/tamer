class WithFramework {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        Class<?> mainClass = Class.forName(args[0]);
        System.loadLibrary("android_runtime");
        if (registerNatives() < 0) {
            throw new RuntimeException("Error registering natives.");
        }
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[] { newArgs });
    }
    private static void printUsage() {
        System.err.println("Usage: dalvikvm " + WithFramework.class.getName()
                + " [main class] [args]");
    }
    static native int registerNatives();
}
