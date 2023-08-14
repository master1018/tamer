public class IPv4Formats {
    public static void main(String[] args) {
        InetAddress ad1, ad2;
        String adds[][] = {
            {"0", "0.0.0.0"},
            {"126.1", "126.0.0.1"},
            {"128.50.65534", "128.50.255.254"},
            {"192.168.1.2", "192.168.1.2"},
            {"hello.foo.bar", null},
            {"1024.1.2.3", null},
            {"128.14.66000", null }
        };
        for (int i = 0; i < adds.length; i++) {
            if (adds[i][1] != null) {
                try {
                    ad1 = InetAddress.getByName(adds[i][0]);
                    ad2 = InetAddress.getByName(adds[i][1]);
                } catch (UnknownHostException ue) {
                    throw new RuntimeException("Wrong conversion: " + adds[i][0] + " should be " + adds[i][1] + " But throws " + ue);
                }
                if (! ad1.equals(ad2))
                    throw new RuntimeException("Wrong conversion: " + adds[i][0] + " should be " + adds[i][1] + " But is " + ad1);
            } else {
                try {
                    ad1 = InetAddress.getByName(adds[i][0]);
                    throw new RuntimeException(adds[i][0] + " should throw UnknownHostException!");
                } catch (UnknownHostException e) {
                }
            }
        }
    }
}
