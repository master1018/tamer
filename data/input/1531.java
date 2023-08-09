public class DayTimeServerImpl
    extends Activatable
    implements DayTimeInterface {
    private static final String PROG_NAME       = "DayTimeServerImpl";
    private static final String SERVER_OBJECT   = "DayTimeServer";
    private static final String CLASS_NAME      = "activation.DayTimeServerImpl";
    private static final String POLICY_FILE   = "policy_file";
    private static final String USER_DIR      =
                        System.getProperty("user.dir").replace('\\', '/');
    private static final String CODE_LOCATION = "file:"+USER_DIR+"/";
    private static final MarshalledObject DATA = null;
    private static ActivationDesc ACTIVATION_DESC = null;
    private TestInterface ref;
    public void ping() throws RemoteException {}
    public ActivationID getActivationID() throws RemoteException {
        return super.getID();
    }
    public DayTimeServerImpl(ActivationID id, MarshalledObject data)
        throws RemoteException {
        super(id, 0);
        if (data != null) {
            try {
                ref = (TestInterface)data.get();
                ref.ping(SERVER_OBJECT);
            }
            catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
    }
    public Date getDayTime() throws RemoteException {
        return new Date();
    }
    public void exit() throws RemoteException {
        System.exit(0);
    }
    public void inactive()
        throws RemoteException, ActivationException, UnknownObjectException {
    }
    public void unregister()
        throws RemoteException, ActivationException, UnknownObjectException {
        unregister(super.getID());
    }
    public void register()
        throws RemoteException, ActivationException, UnknownObjectException {
        register(ACTIVATION_DESC);
    }
    public ActivationGroupID getCurrentGroupID() throws RemoteException {
        return ActivationGroup.currentGroupID();
    }
    private static void setup() {
        try {
          DayTimeInterface rsi; 
          System.setSecurityManager(new RMISecurityManager());
          rsi = (DayTimeInterface)Activatable.register(ACTIVATION_DESC);
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
