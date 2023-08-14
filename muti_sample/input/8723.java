public class SACoreAttachingConnector extends ConnectorImpl implements AttachingConnector {
    static final String ARG_COREFILE = "core";
    static final String ARG_JAVA_EXECUTABLE = "javaExecutable";
    private Transport transport;
    public SACoreAttachingConnector(com.sun.tools.jdi.VirtualMachineManagerService ignored) {
        this();
    }
    public SACoreAttachingConnector() {
        super();
        addStringArgument(
                ARG_JAVA_EXECUTABLE,
                "Java Executable",              
                "Pathname of Java Executable",  
                "",
                true);
        addStringArgument(
                ARG_COREFILE,
                "Corefile",                                    
                "Pathname of a corefile from a Java Process",  
                "core",
                false);
        transport = new Transport() {
                   public String name() {
                       return "filesystem";
                   }
               };
    }
    private void checkCoreAttach(String corefile) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                checkNativeLink(sm, System.getProperty("os.name"));
                sm.checkRead(corefile);
            } catch (SecurityException se) {
                throw new SecurityException("permission denied to attach to " + corefile);
            }
        }
    }
    private VirtualMachine createVirtualMachine(Class vmImplClass,
                                                String javaExec, String corefile)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method connectByCoreMethod = vmImplClass.getMethod(
                                 "createVirtualMachineForCorefile",
                                  new Class[] {
                                      VirtualMachineManager.class,
                                      String.class, String.class,
                                      Integer.TYPE
                                  });
        return (VirtualMachine) connectByCoreMethod.invoke(null,
                                  new Object[] {
                                      Bootstrap.virtualMachineManager(),
                                      javaExec,
                                      corefile,
                                      new Integer(0)
                                  });
    }
    public VirtualMachine attach(Map arguments) throws IOException,
                                      IllegalConnectorArgumentsException {
        String javaExec = argument(ARG_JAVA_EXECUTABLE, arguments).value();
        if (javaExec == null || javaExec.equals("")) {
            throw new IllegalConnectorArgumentsException("javaExec should be non-null and non-empty",
                                                         ARG_JAVA_EXECUTABLE);
        }
        String corefile = argument(ARG_COREFILE, arguments).value();
        if (corefile == null || corefile.equals("")) {
            throw new IllegalConnectorArgumentsException("corefile should be non-null and non-empty",
                                                         ARG_COREFILE);
        }
        checkCoreAttach(corefile);
        VirtualMachine myVM = null;
        try {
            try {
                Class vmImplClass = loadVirtualMachineImplClass();
                myVM = createVirtualMachine(vmImplClass, javaExec, corefile);
            } catch (InvocationTargetException ite) {
                Class vmImplClass = handleVMVersionMismatch(ite);
                if (vmImplClass != null) {
                    return createVirtualMachine(vmImplClass, javaExec, corefile);
                } else {
                    throw ite;
                }
            }
        } catch (Exception ee) {
            if (DEBUG) {
                System.out.println("VirtualMachineImpl() got an exception:");
                ee.printStackTrace();
                System.out.println("coreFile = " + corefile + ", javaExec = " + javaExec);
            }
            throw (IOException) new IOException().initCause(ee);
        }
        setVMDisposeObserver(myVM);
        return myVM;
    }
    public String name() {
        return "sun.jvm.hotspot.jdi.SACoreAttachingConnector";
    }
    public String description() {
        return getString("This connector allows you to attach to a core file using the Serviceability Agent");
    }
    public Transport transport() {
        return transport;
    }
}
