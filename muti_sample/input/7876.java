public class DefaultCaching {
    public static void main(String args[]) throws Exception {
        SimpleNameService.put("theclub", "129.156.220.219");
        test ("theclub", "129.156.220.219", true);      
        test ("luster", "1.16.20.2", false);            
        SimpleNameService.put("luster", "10.5.18.21");
        test ("luster", "1.16.20.2", false);            
        sleep (10+1);
        test("luster", "10.5.18.21", true, 3);          
        sleep (5);
        SimpleNameService.put("foo", "10.5.18.22");
        SimpleNameService.put("theclub", "129.156.220.1");
        test ("theclub", "129.156.220.219", true, 3);
        test ("luster", "10.5.18.21", true, 3);
        test ("bar", "10.5.18.22", false, 4);
        test ("foo", "10.5.18.22", true, 5);
        sleep (5);
        test ("foo", "10.5.18.22", true, 5);
        test ("theclub", "129.156.220.1", true, 6);
        sleep (11);
        test ("luster", "10.5.18.21", true, 7);
        test ("theclub", "129.156.220.1", true, 7);
        sleep (10+6);
        test ("theclub", "129.156.220.1", true, 8);
        test ("luster", "10.5.18.21", true, 8);
        test ("foo", "10.5.18.22", true, 9);
    }
    static void test (String host, String address,
                        boolean shouldSucceed, int count) {
        test (host, address, shouldSucceed);
        int got = SimpleNameService.lookupCalls();
        if (got != count) {
            throw new RuntimeException ("lookups exp/got: " + count+"/"+got);
        }
    }
    static void sleep (int seconds) {
        try {
            Thread.sleep (seconds * 1000);
        } catch (InterruptedException e) {}
    }
    static void test (String host, String address, boolean shouldSucceed) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName (host);
            if (!shouldSucceed) {
                throw new RuntimeException (host+":"+address+": should fail");
            }
            if (!address.equals(addr.getHostAddress())) {
                throw new RuntimeException(host+":"+address+": compare failed");
            }
        } catch (UnknownHostException e) {
            if (shouldSucceed) {
                throw new RuntimeException(host+":"+address+": should succeed");
            }
        }
    }
}
