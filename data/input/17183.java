public class NativeInstanceFilterTarg {
    public static void main(String args[]) {
        boolean runTest = jvmSupportsJVMTI_12x();
        String s1 = "abc";
        String s2 = "def";
        latch(s1);
        s1.intern();
        if (runTest) {
            s2.intern(); 
        } else {
            System.out.println("Neutering test since JVMTI 1.2 not supported");
        }
    }
    public static String latch(String s) { return s; }
    public static boolean jvmSupportsJVMTI_12x() {
        int major = Version.jvmMajorVersion();
        int minor = Version.jvmMinorVersion();
        int micro = Version.jvmMicroVersion();
        int build = Version.jvmBuildNumber();
        return (major > 20 || major == 20 &&
                   (minor > 0 || micro > 0 || build >= 5));
    }
}
