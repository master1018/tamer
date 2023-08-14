public class SAPIDAttachingConnector extends ConnectorImpl implements AttachingConnector {
    static final String ARG_PID = "pid";
    private Transport transport;
    public SAPIDAttachingConnector(com.sun.tools.jdi.VirtualMachineManagerService ignored) {
         this();
    }
    public SAPIDAttachingConnector() {
         super();
        addStringArgument(
                ARG_PID,
                "PID",                     
                "PID of a Java process",   
                "",
                true);
        transport = new Transport() {
                   public String name() {
                       return "local process";
                       }
                };
    }
    private void checkProcessAttach(int pid) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String os = System.getProperty("os.name");
            try {
                checkNativeLink(sm, os);
                if (os.equals("SunOS") || os.equals("Linux")) {
                    sm.checkRead("/proc/" + pid);
                }
            } catch (SecurityException se) {
                throw new SecurityException("permission denied to attach to " + pid);
            }
        }
    }
    private VirtualMachine createVirtualMachine(Class virtualMachineImplClass, int pid)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method createByPIDMethod
                  = virtualMachineImplClass.getMethod("createVirtualMachineForPID",
                     new Class[] {
                         VirtualMachineManager.class,
                         Integer.TYPE, Integer.TYPE
                     });
        return (VirtualMachine) createByPIDMethod.invoke(null,
                     new Object[] {
                         Bootstrap.virtualMachineManager(),
                         new Integer(pid),
                         new Integer(0)
                     });
    }
    public VirtualMachine attach(Map arguments) throws IOException,
                                      IllegalConnectorArgumentsException {
        int pid = 0;
        try {
            pid = Integer.parseInt(argument(ARG_PID, arguments).value());
        } catch (NumberFormatException nfe) {
            throw (IllegalConnectorArgumentsException) new IllegalConnectorArgumentsException
                                                  (nfe.getMessage(), ARG_PID).initCause(nfe);
        }
        checkProcessAttach(pid);
        VirtualMachine myVM = null;
        try {
            try {
                Class vmImplClass = loadVirtualMachineImplClass();
                myVM = createVirtualMachine(vmImplClass, pid);
            } catch (InvocationTargetException ite) {
                Class vmImplClass = handleVMVersionMismatch(ite);
                if (vmImplClass != null) {
                    return createVirtualMachine(vmImplClass, pid);
                } else {
                    throw ite;
                }
            }
        } catch (Exception ee) {
            if (DEBUG) {
                System.out.println("VirtualMachineImpl() got an exception:");
                ee.printStackTrace();
                System.out.println("pid = " + pid);
            }
            throw (IOException) new IOException().initCause(ee);
        }
        setVMDisposeObserver(myVM);
        return myVM;
    }
    public String name() {
        return "sun.jvm.hotspot.jdi.SAPIDAttachingConnector";
    }
    public String description() {
        return getString("This connector allows you to attach to a Java process using the Serviceability Agent");
    }
    public Transport transport() {
        return transport;
    }
}
