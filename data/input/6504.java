public class Bootstrap extends Object {
    static public synchronized VirtualMachineManager virtualMachineManager() {
        return com.sun.tools.jdi.VirtualMachineManagerImpl.virtualMachineManager();
    }
}
