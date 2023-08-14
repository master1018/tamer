public class ConnectedVMs extends TestScaffold {
    static int failCount = 0;;
    static String passName;
    public static void main(String args[]) throws Exception {
        new ConnectedVMs(args[0]).startTests();
        if (failCount > 0) {
            throw new RuntimeException(
             "VirtualMachineManager.connectedVirtualMachines() " +
             failCount + " tests failed");
        } else {
            System.out.println(
          "VirtualMachineManager.connectedVirtualMachines() tests passed");
        }
    }
    ConnectedVMs(String name) throws Exception {
        super(new String[0]);
        passName = name;
        System.out.println("create " + passName);
    }
    void vms(int expected) {
        List vms = Bootstrap.virtualMachineManager().
            connectedVirtualMachines();
        if (vms.size() != expected) {
            System.out.println("FAILURE! " + passName +
                               " - expected: " + expected +
                               ", got: " + vms.size());
            ++failCount;
        }
    }
    protected void runTests() throws Exception {
        System.out.println("Testing " + passName);
        vms(0);
        startToMain("InstTarg");
        ThreadReference thread = waitForVMStart();
        StepEvent stepEvent = stepIntoLine(thread);
        vms(1);
        if (passName.equals("Kill")) {
            vm().process().destroy();
        } else if (passName.equals("Resume to exit")) {
            vm().resume();
        } else if (passName.equals("dispose()")) {
            vm().dispose();
        } else if (passName.equals("exit()")) {
            vm().exit(1);
        }
        resumeToVMDisconnect();
        vms(0);
    }
}
