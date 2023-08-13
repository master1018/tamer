public class TestCOMP {
    public static void main(String[] argv) throws CharacterCodingException {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
            return;
        try {
            String src =
                "JIS0208\u4eb0" +
                "ASCII" +
                "JIS0212\u4e74\u4e79" +
                "GB2312\u7279\u5b9a" +
                "JIS0201\uff67\uff68" +
                "Johab\uac00\uac01";
            byte[] ba = src.getBytes("COMPOUND_TEXT");
            String dst = new String(ba, "COMPOUND_TEXT");
            char[] ca = dst.toCharArray();
            if (!src.equals(dst)) {
                System.out.printf("src=<%s>\n", src);
                System.out.printf("dst=<%s>\n", dst);
                throw new CharacterCodingException();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
