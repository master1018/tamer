public class FindCanEncodeBugs {
    static boolean encodable1(CharsetEncoder enc, char c) {
        enc.reset();
        return enc.canEncode(c);
    }
    static boolean encodable2(CharsetEncoder enc, char c) {
        enc.reset();
        try { enc.encode(CharBuffer.wrap(new char[]{c})); return true; }
        catch (CharacterCodingException e) { return false; }
    }
    public static void main(String[] args) throws Exception {
        int failures = 0;
        for (Map.Entry<String,Charset> e
                 : Charset.availableCharsets().entrySet()) {
            String csn = e.getKey();
            Charset cs = e.getValue();
            if (! cs.canEncode() || csn.matches("x-COMPOUND_TEXT"))
                continue;
            CharsetEncoder enc = cs.newEncoder();
            for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
                boolean encodable1 = encodable1(enc, (char)i);
                boolean encodable2 = encodable2(enc, (char)i);
                if (encodable1 != encodable2) {
                    int start = i;
                    int end = i;
                    for (int j = i;
                         j <= '\uffff' &&
                             encodable1(enc, (char)j) == encodable1 &&
                             encodable2(enc, (char)j) == encodable2;
                         j++)
                        end = j;
                    System.out.printf("charset=%-18s canEncode=%-5b ",
                                      csn, encodable1);
                    if (start == end)
                        System.out.printf("\'\\u%04x\'%n", start);
                    else
                        System.out.printf("\'\\u%04x\' - \'\\u%04x\'%n",
                                          start, end);
                    i = end;
                    failures++;
                }
            }
        }
        if (failures > 0)
            throw new Exception(failures + " failures");
    }
}
