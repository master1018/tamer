class FramesTarg {
    static void foo3() {
        System.out.println("executing foo3");
    }
    static void foo2() {
        foo3();
    }
    static void foo1() {
        foo2();
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        foo1();
    }
}
public class FramesTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    static String[] expectedNames = {"foo3", "foo2", "foo1", "main"};
    FramesTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new FramesTest(args).startTests();
    }
    void exceptionTest(int start, int length) {
        boolean gotException = false;
        try {
            mainThread.frames(start, length);
        } catch (IndexOutOfBoundsException exc) {
            gotException = true;
        } catch (Exception exc) {
            failure("unexpected exception thrown for: " +
                    "start = " + start + ", length = " + length +
                    " - " + exc);
            gotException = true;
        }
        if (!gotException) {
            failure("expected IndexOutOfBoundsException " +
                    "not thrown for: " +
                    "start = " + start + ", length = " + length);
        }
    }
    void nameTest(int start, int length) {
        try {
            List fs = mainThread.frames(start, length);
            if (fs.size() != length) {
                failure("wrong length for: " +
                        "start = " + start + ", length = " + length);
            }
            for (int i = 0; i < length; ++i) {
                StackFrame sf = (StackFrame)(fs.get(i));
                String name = sf.location().method().name();
                String expected = expectedNames[start+i];
                if (!name.equals(expected)) {
                    failure("bad frame entry (" + start + "," + length +
                            ") - expected " + expected +
                            ", got " + name);
                }
            }
        } catch (Exception exc) {
            failure("unexpected exception thrown for: " +
                    "start = " + start + ", length = " + length +
                    " - " + exc);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("FramesTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        int initialSize = mainThread.frames().size();
        resumeTo("FramesTarg", "foo3", "()V");
        if (!mainThread.frame(0).location().method().name()
                        .equals("foo3")) {
            failure("frame failed");
        }
        if (mainThread.frames().size() != (initialSize + 3)) {
            failure("frames size failed");
        }
        if (mainThread.frames().size() != mainThread.frameCount()) {
            failure("frames size not equal to frameCount");
        }
        exceptionTest(-1, 1);
        exceptionTest(mainThread.frameCount(), 1);
        exceptionTest(0, -1);
        exceptionTest(0, -2);
        exceptionTest(0, mainThread.frameCount()+1);
        nameTest(0, 0);
        nameTest(0, 1);
        nameTest(0, 4);
        nameTest(2, 2);
        nameTest(1, 1);
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("FramesTest: passed");
        } else {
            throw new Exception("FramesTest: failed");
        }
    }
}
