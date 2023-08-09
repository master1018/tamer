public class RMIConnector_NPETest  {
    public static void main(String argv[]) throws Exception {
        boolean testFailed = false;
        String rmidCmd = System.getProperty("java.home") + File.separator +
            "bin" + File.separator + "rmid -port 3333";
        String stopRmidCmd = System.getProperty("java.home") + File.separator +
                "bin" + File.separator + "rmid -stop -port 3333";
    try {
        System.out.println("Starting rmid");
        Runtime.getRuntime().exec(rmidCmd);
        Thread.sleep(5000);
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        RMIJRMPServerImpl rmiserver = new RMIJRMPServerImpl(3333, null, null, null);
        rmiserver.setMBeanServer(mbs);
        RMIConnector agent = new RMIConnector(rmiserver, null);
        agent.connect();
    } catch(NullPointerException npe) {
        npe.printStackTrace();
        testFailed = true;
    } catch (Exception e) {
    } finally {
        System.out.println("Stopping rmid");
        Runtime.getRuntime().exec(stopRmidCmd);
        }
    if(testFailed)
        throw new Exception("Test failed");
    }
}
