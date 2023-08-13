public class SADebugServerAttachingConnector extends ConnectorImpl implements AttachingConnector {
    static final String ARG_DEBUG_SERVER_NAME = "debugServerName";
    private Transport transport;
    public SADebugServerAttachingConnector(com.sun.tools.jdi.VirtualMachineManagerService ignored) {
        this();
    }
    public SADebugServerAttachingConnector() {
        addStringArgument(
                ARG_DEBUG_SERVER_NAME,
                "Debug Server",                      
                "Name of a remote SA Debug Server",  
                "",
                true);
        transport = new Transport() {
                   public String name() {
                       return "RMI";
                   }
                 };
    }
    private VirtualMachine createVirtualMachine(Class vmImplClass,
                                                String debugServerName)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method connectByServerMethod =
                            vmImplClass.getMethod(
                                   "createVirtualMachineForServer",
                                   new Class[] {
                                       VirtualMachineManager.class,
                                       String.class,
                                       Integer.TYPE
                                   });
        return (VirtualMachine) connectByServerMethod.invoke(null,
                                   new Object[] {
                                       Bootstrap.virtualMachineManager(),
                                       debugServerName,
                                       new Integer(0)
                                   });
    }
    public VirtualMachine attach(Map arguments) throws IOException,
                                      IllegalConnectorArgumentsException {
        String debugServerName = argument(ARG_DEBUG_SERVER_NAME, arguments).value();
        if (debugServerName == null || debugServerName.equals("")) {
            throw new IllegalConnectorArgumentsException("debugServerName should be non-null and non-empty",
                                                         ARG_DEBUG_SERVER_NAME);
        }
        VirtualMachine myVM;
        try {
            try {
                Class vmImplClass = loadVirtualMachineImplClass();
                myVM = createVirtualMachine(vmImplClass, debugServerName);
            } catch (InvocationTargetException ite) {
                Class vmImplClass = handleVMVersionMismatch(ite);
                if (vmImplClass != null) {
                    return createVirtualMachine(vmImplClass, debugServerName);
                } else {
                    throw ite;
                }
            }
        } catch (Exception ee) {
            if (DEBUG) {
                System.out.println("VirtualMachineImpl() got an exception:");
                ee.printStackTrace();
                System.out.println("debug server name = " + debugServerName);
            }
            throw (IOException) new IOException().initCause(ee);
        }
        setVMDisposeObserver(myVM);
        return myVM;
    }
    public String name() {
        return "sun.jvm.hotspot.jdi.SADebugServerAttachingConnector";
    }
    public String description() {
        return getString("This connector allows you to attach to a Java Process via a debug server with the Serviceability Agent");
    }
    public Transport transport() {
        return transport;
    }
}
