class Java_gTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from Java_gTarg!");
    }
}
public class Java_gTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    Java_gTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        String mslibName = System.mapLibraryName("msvcrtd");
        if (mslibName.equals("msvcrtd.dll")) {
            try {
                System.loadLibrary("msvcrtd");
            } catch (Throwable ee) {
                System.out.println("Exception looking for msvcrtd.dll: " + ee);
                System.out.println("msvcrtd.dll does not exist.  Let the test pass");
                return;
            }
        }
        String specialExec = "java";
        String sep = System.getProperty("file.separator");
        String jhome =  System.getProperty("java.home");
        String jbin = jhome + sep + "bin";
        File binDir = new File(jbin);
        if ((new File(binDir, specialExec).exists()) ||
            (new File(binDir, specialExec + ".exe").exists())) {
            args = new String[2];
            args[0] = "-connect";
            args[1] = "com.sun.jdi.CommandLineLaunch:vmexec=" + specialExec;
            new Java_gTest(args).startTests();
        } else {
            System.out.println("No java executable exists.  Let the test pass.");
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("Java_gTarg");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("Java_gTest: passed");
        } else {
            throw new Exception("Java_gTest: failed");
        }
    }
}
