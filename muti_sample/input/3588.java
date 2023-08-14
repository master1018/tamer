public class NotActivatableServerImpl
    extends UnicastRemoteObject
    implements NotActivatableInterface {
    private static final String PROG_NAME       = "NotActivatableServerImpl";
    private static final String SERVER_OBJECT   = "NotActivatableServer";
    private static final String CLASS_NAME      = "activation.NotActivatableServerImpl";
    private static final String POLICY_FILE   = "policy_file";
    private static final String USER_DIR      =
                        System.getProperty("user.dir").replace('\\', '/');
    private static final String CODE_LOCATION = "file:"+USER_DIR+"/";
    private static final MarshalledObject DATA = null;
    private static ActivationDesc ACTIVATION_DESC = null;
    public NotActivatableServerImpl() throws RemoteException {}
    public void ping() throws RemoteException {}
    public void exit() throws RemoteException {
        System.exit(0);
    }
    private static void setup() {
        try {
          NotActivatableInterface rsi;  
          System.setSecurityManager(new RMISecurityManager());
          rsi = (NotActivatableInterface)Activatable.register(ACTIVATION_DESC);
          System.out.println("Got stub for "+SERVER_OBJECT+" implementation");
          Naming.rebind(SERVER_OBJECT, rsi);
          System.out.println("Exported "+SERVER_OBJECT+" implementation");
        } catch (Exception e) {
            System.err.println("Exception: " + e);
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("java.security.policy", POLICY_FILE);
            ActivationGroupDesc agd = new ActivationGroupDesc(props, null);
            ActivationGroupID agid = ActivationGroup.getSystem().registerGroup(agd);
            ACTIVATION_DESC = new ActivationDesc(agid,
                        CLASS_NAME, CODE_LOCATION, DATA, false);
        }
        catch (Exception e) {
            System.err.println("Exception: " + e);
            e.printStackTrace();
        }
        setup();
    }
}
