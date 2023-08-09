public class DdmRegister {
    private DdmRegister() {}
    public static void registerHandlers() {
        if (Config.LOGV)
            Log.v("ddm", "Registering DDM message handlers");
        DdmHandleHello.register();
        DdmHandleThread.register();
        DdmHandleHeap.register();
        DdmHandleNativeHeap.register();
        DdmHandleProfiling.register();
        DdmHandleExit.register();
        DdmServer.registrationComplete();
    }
}
