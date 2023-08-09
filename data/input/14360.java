public class SurrogateTestEUCTW {
    private static final String testRootDir
        = System.getProperty("test.src", ".");
    public static void main(String[] args) throws Exception {
        char[] surrogatePair = new char[2];
        int[] expectBytes = new int[4];
        String[] testPlane = { "3", "4", "5", "6" ,"7", "15" };
        for (int i = 0 ; i < testPlane.length; i++) {
            FileReader f = new FileReader(testRootDir +
                                          System.getProperty("file.separator")
                                          + "SurrogateTestEUCTW.plane"
                                          + testPlane[i]
                                          + ".surrogates");
            BufferedReader r = new BufferedReader(f);
            String line;
            while ((line = r.readLine()) != null) {
                int charValue = Integer.parseInt(line.substring(9,14), 16);
                surrogatePair[0] = (char) ((charValue - 0x10000) / 0x400
                                    + 0xd800);
                surrogatePair[1] = (char) ((charValue - 0x10000) % 0x400
                                    + 0xdc00);
                expectBytes[0] = 0x8E;
                expectBytes[1] = 0xA0 + Integer.parseInt(testPlane[i]);
                expectBytes[2] = 0x80 | Integer.parseInt(line.substring(2,4), 16);
                expectBytes[3] = 0x80 | Integer.parseInt(line.substring(4,6), 16);
                String testStr = new String(surrogatePair);
                byte[] encodedBytes = testStr.getBytes("EUC-TW");
                for (int x = 0 ; x < 4 ; x++) {
                    if (encodedBytes[x] != (byte)(expectBytes[x] & 0xff)) {
                        throw new Exception("EUC_TW Surrogate Encoder error");
                    }
                }
                String decoded = new String(encodedBytes, "EUC-TW");
                if (!decoded.equals(testStr)) {
                    throw new Exception("EUCTW Decoder error");
                }
            }
            r.close();
            f.close();
        }
    }
}
