public class ICCBasher {
    static final int TEST_SIZE = 20;
    static final int STRING_SIZE = 5;
    static final int CHAR_VALUE_LIMIT = 128;
    public static void main(String[] args) throws Exception {
        LinkedList L1 = new LinkedList();
        LinkedList L2 = new LinkedList();
        LinkedList L3 = new LinkedList();
        LinkedList L4 = new LinkedList();
        Random generator = new Random();
        int achar=0;
        StringBuffer entryBuffer = new StringBuffer(10);
        String snippet = null;
        for (int x=0; x<TEST_SIZE * 2; x++) {
            for(int y=0; y<STRING_SIZE; y++) {
                achar = generator.nextInt(CHAR_VALUE_LIMIT);
                char test = (char)(achar);
                entryBuffer.append(test);
            }
            snippet = entryBuffer.toString();
            snippet.toLowerCase();
            if (x < TEST_SIZE)
                L1.add(snippet);
            else
                L2.add(snippet);
        }
        for (int x=0; x<TEST_SIZE; x++) {
            String entry = (String)L1.get(x) + (String)L2.get(x);
            L3.add(entry);
        }
        for (int x=0; x<TEST_SIZE; x++) {
            achar = generator.nextInt();
            if (achar > 0) {
                String mod = (String)L1.get(x);
                mod = mod.toUpperCase();
                L1.set(x, mod);
            }
            achar = generator.nextInt();
            if (achar > 0) {
                String mod = (String)L2.get(x);
                mod = mod.toUpperCase();
                L2.set(x, mod);
            }
        }
        for (int x=0; x<TEST_SIZE; x++) {
            String entry = (String)L1.get(x) + (String)L2.get(x);
            L4.add(entry);
        }
        Collections.sort(L3, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(L4, String.CASE_INSENSITIVE_ORDER);
        for (int x=0; x<TEST_SIZE; x++) {
            String one = (String)L3.get(x);
            String two = (String)L4.get(x);
            if (!one.equalsIgnoreCase(two))
                throw new RuntimeException("Case Insensitive Sort Failure.");
        }
    }
}
