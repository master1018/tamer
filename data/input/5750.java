public class FindASCIIRangeCodingBugs {
    private static int failures = 0;
    private static byte[] asciiBytes = new byte[0x80];
    private static char[] asciiChars = new char[0x80];
    private static String asciiString;
    private static void check(String csn) throws Exception {
        System.out.println(csn);
        if (! Arrays.equals(asciiString.getBytes(csn), asciiBytes)) {
            System.out.printf("%s -> bytes%n", csn);
            failures++;
        }
        if (! new String(asciiBytes, csn).equals(asciiString)) {
            System.out.printf("%s -> chars%n", csn);
            failures++;
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 0x80; i++) {
            asciiBytes[i] = (byte) i;
            asciiChars[i] = (char) i;
        }
        asciiString = new String(asciiChars);
        Charset ascii = Charset.forName("ASCII");
        for (Map.Entry<String,Charset> e
                 : Charset.availableCharsets().entrySet()) {
            String csn = e.getKey();
            Charset cs = e.getValue();
            if (!cs.contains(ascii) ||
                csn.matches(".*2022.*") ||             
                csn.matches("x-windows-5022[0|1]") ||  
                csn.matches(".*UTF-[16|32].*"))        
                continue;
            if (! cs.canEncode()) continue;
            try {
                check(csn);
            } catch (Throwable t) {
                t.printStackTrace();
                failures++;
            }
        }
        if (failures > 0)
            throw new Exception(failures + "tests failed");
    }
}
