public final class RuntimePermission extends BasicPermission {
    private static final long serialVersionUID = 7399184964622342223L;
    static final RuntimePermission permissionToSetSecurityManager = new RuntimePermission(
            "setSecurityManager"); 
    static final RuntimePermission permissionToCreateSecurityManager = new RuntimePermission(
            "createSecurityManager"); 
    static final RuntimePermission permissionToGetProtectionDomain = new RuntimePermission(
            "getProtectionDomain"); 
    static final RuntimePermission permissionToGetClassLoader = new RuntimePermission(
            "getClassLoader"); 
    static final RuntimePermission permissionToCreateClassLoader = new RuntimePermission(
            "createClassLoader"); 
    static final RuntimePermission permissionToModifyThread = new RuntimePermission(
            "modifyThread"); 
    static final RuntimePermission permissionToModifyThreadGroup = new RuntimePermission(
            "modifyThreadGroup"); 
    static final RuntimePermission permissionToExitVM = new RuntimePermission(
            "exitVM"); 
    static final RuntimePermission permissionToReadFileDescriptor = new RuntimePermission(
            "readFileDescriptor"); 
    static final RuntimePermission permissionToWriteFileDescriptor = new RuntimePermission(
            "writeFileDescriptor"); 
    static final RuntimePermission permissionToQueuePrintJob = new RuntimePermission(
            "queuePrintJob"); 
    static final RuntimePermission permissionToSetFactory = new RuntimePermission(
            "setFactory"); 
    static final RuntimePermission permissionToSetIO = new RuntimePermission(
            "setIO"); 
    static final RuntimePermission permissionToStopThread = new RuntimePermission(
            "stopThread"); 
    static final RuntimePermission permissionToSetContextClassLoader = new RuntimePermission(
            "setContextClassLoader"); 
    public RuntimePermission(String permissionName) {
        super(permissionName);
    }
    public RuntimePermission(String name, String actions) {
        super(name, actions);
    }
}
