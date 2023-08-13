public class Pack200Test {
    private static ArrayList <File> jarList = new ArrayList<File>();
    static final MemoryMXBean mmxbean = ManagementFactory.getMemoryMXBean();
    static final long m0 = getUsedMemory();
    static final int LEAK_TOLERANCE = 20000; 
    private Pack200Test() {}
    static long getUsedMemory() {
        mmxbean.gc();
        mmxbean.gc();
        mmxbean.gc();
        return mmxbean.getHeapMemoryUsage().getUsed()/1024;
    }
    private static void leakCheck() throws Exception {
        long diff = getUsedMemory() - m0;
        System.out.println("  Info: memory diff = " + diff + "K");
        if ( diff  > LEAK_TOLERANCE) {
            throw new Exception("memory leak detected " + diff);
        }
    }
    private static void doPackUnpack() {
        for (File in : jarList) {
            JarOutputStream javaUnpackerStream = null;
            JarOutputStream nativeUnpackerStream = null;
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(in);
                File packFile = new File(in.getName() + Utils.PACK_FILE_EXT);
                System.out.println("Packing [" + in.toString() + "]");
                Utils.pack(jarFile, packFile);
                jarFile.close();
                leakCheck();
                System.out.println("  Unpacking using java unpacker");
                File javaUnpackedJar = new File("java-" + in.getName());
                javaUnpackerStream = new JarOutputStream(
                        new FileOutputStream(javaUnpackedJar));
                Utils.unpackj(packFile, javaUnpackerStream);
                javaUnpackerStream.close();
                System.out.println("  Testing...java unpacker");
                leakCheck();
                Utils.doCompareVerify(in.getAbsoluteFile(), javaUnpackedJar);
                System.out.println("  Unpacking using native unpacker");
                File nativeUnpackedJar = new File("native-" + in.getName());
                nativeUnpackerStream = new JarOutputStream(
                        new FileOutputStream(nativeUnpackedJar));
                Utils.unpackn(packFile, nativeUnpackerStream);
                nativeUnpackerStream.close();
                System.out.println("  Testing...native unpacker");
                leakCheck();
                Utils.doCompareBitWise(javaUnpackedJar, nativeUnpackedJar);
                System.out.println("Done.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Utils.close(nativeUnpackerStream);
                Utils.close(javaUnpackerStream);
                Utils.close((Closeable) jarFile);
            }
        }
    }
    public static void main(String[] args) {
        jarList.add(Utils.locateJar("tools.jar"));
        jarList.add(Utils.locateJar("rt.jar"));
        jarList.add(Utils.locateJar("golden.jar"));
        System.out.println(jarList);
        doPackUnpack();
    }
}
