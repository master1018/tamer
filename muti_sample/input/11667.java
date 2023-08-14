public class CacheTest {
    public static void main(String args[]) throws Exception {
        String ttlProp = "networkaddress.cache.negative.ttl";
        int ttl = 0;
        String policy = Security.getProperty(ttlProp);
        if (policy != null) {
            ttl = Integer.parseInt(policy);
        }
        if (ttl <= 0  || ttl > 15) {
            System.err.println("Security property " + ttlProp + " needs to " +
                " in 1-15 second range to execute this test");
            return;
        }
        SimpleNameService.put("theclub", "129.156.220.219");
        InetAddress.getByName("theclub");
        try {
            InetAddress.getByName("luster");
            throw new RuntimeException("Test internal error " +
                " - luster is bring resolved by name service");
        } catch (UnknownHostException x) {
        }
        SimpleNameService.put("luster", "10.5.18.21");
        Thread.currentThread().sleep(ttl*1000 + 1000);
        InetAddress.getByName("luster");
    }
}
