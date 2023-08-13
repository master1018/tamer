public class TestDynamicPolicy {
    public static void main(String args[]) throws Exception {
        try {
            TestDynamicPolicy jstest = new TestDynamicPolicy();
            jstest.doit();
        } catch(Exception e)  {
            System.out.println("Failed. Unexpected exception:" + e);
            throw e;
        }
        System.out.println("Passed. OKAY");
    }
    private void doit() throws Exception {
        SecurityManager sm=System.getSecurityManager();
        if (sm==null)
            throw new
                Exception("Test must be run with a security manager installed");
        DynamicPolicy dp = new DynamicPolicy();
        Policy.setPolicy(dp);
        if (dp != Policy.getPolicy())
            throw new Exception("Policy was not set!!");
        String usr = getUserName();
        if (usr != null) {
            System.out.println("Test was able to read user.name prior to refresh!");
            throw new
                Exception("Test was able to read user.name prior to refresh!");
        }
        dp.refresh();
        usr = getUserName();
        if (usr == null) {
            System.out.println("Test was unable to read user.name after refresh!");
            throw new
                Exception("Test was unable to read user.name after refresh!");
        }
        dp.refresh();
        usr = getUserName();
        if (usr != null) {
            System.out.println("Test was able to read user.name following 2nd refresh!");
            throw new
                Exception("Test was able to read user.name following 2nd refresh!");
        }
    }
    private String getUserName() {
        String usr = null;
        try {
            usr = System.getProperty("user.name");
        } catch (Exception e) {
        }
        return usr;
    }
}
