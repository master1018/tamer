public class FindASCIIReplBugs {
    private static int failures = 0;
    public static void main(String[] args) throws Exception {
        Charset ascii = Charset.forName("ASCII");
        for (Map.Entry<String,Charset> e
                 : Charset.availableCharsets().entrySet()) {
            String csn = e.getKey();
            Charset cs = e.getValue();
            if (!cs.contains(ascii) ||
                csn.matches(".*2022.*") ||             
                csn.matches(".*UTF-[16|32].*"))        
                continue;
            if (! cs.canEncode()) continue;
            byte[] sc_subs = { 'A'};
            byte[] mc_subs = { 'A', 'S'};
            if (!cs.newEncoder().isLegalReplacement (sc_subs) ||
                !cs.newEncoder().isLegalReplacement (mc_subs)) {
                System.out.printf(" %s: isLegalReplacement failed!%n", csn);
                failures++;
            }
        }
        if (failures > 0)
            throw new Exception(failures + "tests failed");
    }
}
