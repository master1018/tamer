public class SurrogatePairs {
    static String[] testStrings = {"\uD800\uDC00",
                                   "\uD800\uDFFF",
                                   "\uDBFF\uDC00",
                                   "\uDBFF\uDFFF",
                                   "1\uDBFF\uDC00",
                                   "@\uDBFF\uDC00",
                                   "\uDBFF\uDC001",
                                   "\uDBFF\uDC00@",
                                   "\u0101\uDBFF\uDC00",
                                   "\uDBFF\uDC00\u0101"
    };
    static String[] correctEncodings = {"%F0%90%80%80",
                                        "%F0%90%8F%BF",
                                        "%F4%8F%B0%80",
                                        "%F4%8F%BF%BF",
                                        "1%F4%8F%B0%80",
                                        "%40%F4%8F%B0%80",
                                        "%F4%8F%B0%801",
                                        "%F4%8F%B0%80%40",
                                        "%C4%81%F4%8F%B0%80",
                                        "%F4%8F%B0%80%C4%81"
    };
    public static void main(String[] args) throws Exception {
        for (int i=0; i < testStrings.length; i++) {
            test(testStrings[i], correctEncodings[i]);
        }
    }
    private static void test(String str, String correctEncoding)
        throws Exception {
        System.out.println("Unicode bytes of test string are: "
                           + getHexBytes(str));
        String encoded = URLEncoder.encode(str, "UTF-8");
        System.out.println("URLEncoding is: " + encoded);
        if (encoded.equals(correctEncoding))
            System.out.println("The encoding is correct!");
        else {
            throw new Exception("The encoding is incorrect!" +
                                " It should be " + correctEncoding);
        }
        String decoded = URLDecoder.decode(encoded, "UTF-8");
        System.out.println("Unicode bytes for URLDecoding are: "
                           + getHexBytes(decoded));
        if (str.equals(decoded))
            System.out.println("The decoding is correct");
        else {
            throw new Exception("The decoded is not equal to the original");
        }
        System.out.println("---");
    }
    private static String getHexBytes(String s) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            int a = s.charAt(i);
            int b1 = (a >>8) & 0xff;
            int b2 = (byte)a;
            int b11 = (b1>>4) & 0x0f;
            int b12 = b1 & 0x0f;
            int b21 = (b2 >>4) & 0x0f;
            int b22 = b2 & 0x0f;
            sb.append(Integer.toHexString(b11));
            sb.append(Integer.toHexString(b12));
            sb.append(Integer.toHexString(b21));
            sb.append(Integer.toHexString(b22));
            sb.append(' ');
        }
        return sb.toString();
    }
}
