public class ModifyDescriptor
        implements ActivateMe, Runnable
{
    private ActivationID id;
    private String message;
    private static final String MESSAGE1 = "hello";
    private static final String MESSAGE2 = "hello, again";
    public ModifyDescriptor(ActivationID id, MarshalledObject mobj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        Activatable.exportObject(this, id, 0);
        try {
            message = (String) mobj.get();
        } catch (Exception e) {
            System.err.println("unable to get message from marshalled object");
        }
    }
    public String getMessage() {
        return message;
    }
    public String getProperty(String name) {
        return TestLibrary.getProperty(name, null);
    }
    public ActivationID getID() {
        return id;
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"ModifyDescriptor")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4127754\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            System.err.println("Creating group descriptor");
            Properties props = new Properties();
            props.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            props.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            props.put("test.message", MESSAGE1);
            ActivationGroupDesc initialGroupDesc =
                new ActivationGroupDesc(props, null);
            System.err.println("Registering group");
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(initialGroupDesc);
            System.err.println("Creating descriptor");
            ActivationDesc initialDesc =
                new ActivationDesc(groupID, "ModifyDescriptor", null,
                                   new MarshalledObject(MESSAGE1), false);
            System.err.println("Registering descriptor");
            ActivateMe obj = (ActivateMe) Activatable.register(initialDesc);
            System.err.println("Ping object");
            String message1 = obj.getMessage();
            System.err.println("message = " + message1);
            if (message1.equals(MESSAGE1)) {
                System.err.println("Test1a passed: initial MarshalledObject " +
                                   "correct");
            } else {
                TestLibrary.bomb("Test1 failed: unexpected MarshalledObject passed to " +
                     "constructor", null);
            }
            message1 = obj.getProperty("test.message");
            if (message1.equals(MESSAGE1)) {
                System.err.println("Test1b passed: initial group property " +
                                   "correct");
            } else {
                TestLibrary.bomb("Test1 failed: unexpected property passed to " +
                     "group", null);
            }
            System.err.println("Update activation descriptor");
            ActivationDesc newDesc =
                new ActivationDesc(groupID, "ModifyDescriptor", null,
                               new MarshalledObject(MESSAGE2), false);
            ActivationID id = obj.getID();
            ActivationDesc oldDesc = system.setActivationDesc(id, newDesc);
            if (oldDesc.equals(initialDesc)) {
                System.err.println("Test2a passed: desc returned from " +
                                   "setActivationDesc is okay");
            } else {
                TestLibrary.bomb("Test2a failed: desc returned from setActivationDesc " +
                     "is not the initial descriptor!", null);
            }
            Properties props2 = new Properties();
            props2.put("test.message", MESSAGE2);
            props2.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            props2.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            ActivationGroupDesc newGroupDesc =
                new ActivationGroupDesc(props2, null);
            ActivationGroupDesc oldGroupDesc =
                system.setActivationGroupDesc(groupID, newGroupDesc);
            if (oldGroupDesc.equals(initialGroupDesc)) {
                System.err.println("Test2b passed: group desc returned from " +
                                   "setActivationGroupDesc is okay");
            } else {
                TestLibrary.bomb("Test2b failed: group desc returned from " +
                     "setActivationGroupDesc is not the initial descriptor!",
                     null);
            }
            rmid.restart();
            System.err.println("Ping object after restart");
            String message2 = obj.getMessage();
            if (message2.equals(MESSAGE2)) {
                System.err.println("Test3a passed: setActivationDesc takes " +
                                   "effect after a restart");
            } else {
                TestLibrary.bomb("Test3a failed: setActivationDesc did not take effect " +
                     "after a restart", null);
            }
            message2 = obj.getProperty("test.message");
            if (message2.equals(MESSAGE2)) {
                System.err.println("Test3b passed: setActivationGroupDesc " +
                                   "takes effect after a restart");
            } else {
                TestLibrary.bomb("Test3b failed: setActivationGroupDesc did not take " +
                     "effect after a restart", null);
            }
            System.err.println("Get activation descriptor");
            ActivationDesc latestDesc = system.getActivationDesc(id);
            if (latestDesc.equals(newDesc)) {
                System.err.println("Test4a passed: desc is same as latest");
            } else {
                TestLibrary.bomb("Test4a failed: there is no way this would happen", null);
            }
            System.err.println("Get activation group descriptor");
            ActivationGroupDesc latestGroupDesc =
                system.getActivationGroupDesc(groupID);
            if (latestGroupDesc.equals(newGroupDesc)) {
                System.err.println("Test4b passed: group desc is same as " +
                                   "latest");
            } else {
                TestLibrary.bomb("Test4b failed: there is no way this would happen", null);
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
