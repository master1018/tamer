public class RequestPropertyValues {
    public static void main(String[] args) throws Exception {
        part1();
        part2();
    }
    public static void part1() throws Exception {
        URL[] urls = { new URL("http:
                        new URL("file:/etc/passwd"),
                        new URL("ftp:
                        new URL("jar:http:
                    };
        boolean failed = false;
        for (int proto = 0; proto < urls.length; proto++) {
            URLConnection uc = (URLConnection) urls[proto].openConnection();
            try {
                uc.setRequestProperty("TestHeader", null);
            } catch (NullPointerException npe) {
                System.out.println("setRequestProperty is throwing NPE" +
                                " for url: " + urls[proto]);
                failed = true;
            }
            try {
                uc.addRequestProperty("TestHeader", null);
            } catch (NullPointerException npe) {
                System.out.println("addRequestProperty is throwing NPE" +
                                " for url: " + urls[proto]);
                failed = true;
            }
        }
        if (failed) {
            throw new Exception("RequestProperty setting/adding is" +
                                " throwing NPE for null values");
        }
    }
    public static void part2() throws Exception {
        URL url = null;
        String[] goodKeys = {"", "$", "key", "Key", " ", "    "};
        String[] goodValues = {"", "$", "value", "Value", " ", "    "};
        URLConnection conn = getConnection(url);
        for (int i = 0; i < goodKeys.length; ++i) {
            for (int j = 0; j < goodValues.length; ++j) {
                conn.setRequestProperty(goodKeys[i], goodValues[j]);
                String value = conn.getRequestProperty(goodKeys[i]);
                if (!((goodValues[j] == null && value == null) || (value != null && value.equals(goodValues[j]))))
                    throw new RuntimeException("Method setRequestProperty(String,String) works incorrectly");
            }
        }
    }
    static URLConnection getConnection(URL url) {
        return new DummyURLConnection(url);
    }
    static class DummyURLConnection extends URLConnection {
        DummyURLConnection(URL url) {
            super(url);
        }
        public void connect() {
            connected = true;
        }
    }
}
