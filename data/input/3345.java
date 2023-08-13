class InterruptHangTarg {
    public static String sync = "sync";
    public static void main(String[] args){
        int answer = 0;
        System.out.println("Howdy!");
        Interruptor interruptorThread = new Interruptor(Thread.currentThread());
        synchronized(sync) {
            interruptorThread.start();
            try {
                sync.wait();
            } catch (InterruptedException ee) {
                System.out.println("Debuggee interruptee: interrupted before starting loop");
            }
        }
        for (int ii = 0; ii < 200; ii++) {
            answer++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ee) {
                System.out.println("Debuggee interruptee: interrupted at iteration: "
                                   + ii);
            }
        }
        interruptorThread.interrupt();
        System.out.println("Goodbye from InterruptHangTarg!");
    }
}
class Interruptor extends Thread {
    Thread interruptee;
    Interruptor(Thread interruptee) {
        this.interruptee = interruptee;
    }
    public void run() {
        synchronized(InterruptHangTarg.sync) {
            InterruptHangTarg.sync.notify();
        }
        int ii = 0;
        while(true) {
            ii++;
            interruptee.interrupt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ee) {
                System.out.println("Debuggee Interruptor: finished after " +
                                   ii + " iterrupts");
                break;
            }
        }
    }
}
public class InterruptHangTest extends TestScaffold {
    ThreadReference mainThread;
    Thread timerThread;
    String sync = "sync";
    static int nSteps = 0;
    InterruptHangTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new InterruptHangTest(args).startTests();
    }
    public void stepCompleted(StepEvent event) {
        synchronized(sync) {
            nSteps++;
        }
        println("Got StepEvent " + nSteps + " at line " +
                event.location().method() + ":" +
                event.location().lineNumber());
        if (nSteps == 1) {
            timerThread.start();
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("InterruptHangTarg");
        mainThread = bpe.thread();
        EventRequestManager erm = vm().eventRequestManager();
        StepRequest request = erm.createStepRequest(mainThread,
                                                    StepRequest.STEP_LINE,
                                                    StepRequest.STEP_OVER);
        request.enable();
        timerThread = new Thread("test timer") {
                public void run() {
                    int mySteps = 0;
                    while (true) {
                        try {
                            Thread.sleep(20000);
                            synchronized(sync) {
                                System.out.println("steps = " + nSteps);
                                if (mySteps == nSteps) {
                                    failure("failure: Debuggee appears to be hung");
                                    vm().exit(-1);
                                    break;
                                }
                            }
                            mySteps = nSteps;
                        } catch (InterruptedException ee) {
                            break;
                        }
                    }
                }
            };
        listenUntilVMDisconnect();
        timerThread.interrupt();
        if (!testFailed) {
            println("InterruptHangTest: passed");
        } else {
            throw new Exception("InterruptHangTest: failed");
        }
    }
}
