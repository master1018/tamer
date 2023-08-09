class URIEncoderDecoder {
    static final String digits = "0123456789ABCDEF"; 
    static final String encoding = "UTF8"; 
    static void validate(String s, String legal) throws URISyntaxException {
        for (int i = 0; i < s.length();) {
            char ch = s.charAt(i);
            if (ch == '%') {
                do {
                    if (i + 2 >= s.length()) {
                        throw new URISyntaxException(s, Msg.getString("K0313"), 
                                i);
                    }
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1) {
                        throw new URISyntaxException(s, Msg.getString("K0314", 
                                s.substring(i, i + 3)), i);
                    }
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                continue;
            }
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9') || legal.indexOf(ch) > -1 || (ch > 127
                    && !Character.isSpaceChar(ch) && !Character
                    .isISOControl(ch)))) {
                throw new URISyntaxException(s, Msg.getString("K00c1"), i); 
            }
            i++;
        }
    }
    static void validateSimple(String s, String legal)
            throws URISyntaxException {
        for (int i = 0; i < s.length();) {
            char ch = s.charAt(i);
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9') || legal.indexOf(ch) > -1)) {
                throw new URISyntaxException(s, Msg.getString("K00c1"), i); 
            }
            i++;
        }
    }
    static String quoteIllegal(String s, String legal)
            throws UnsupportedEncodingException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || legal.indexOf(ch) > -1
                    || (ch > 127 && !Character.isSpaceChar(ch) && !Character
                            .isISOControl(ch))) {
                buf.append(ch);
            } else {
                byte[] bytes = new String(new char[] { ch }).getBytes(encoding);
                for (int j = 0; j < bytes.length; j++) {
                    buf.append('%');
                    buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
                    buf.append(digits.charAt(bytes[j] & 0xf));
                }
            }
        }
        return buf.toString();
    }
    static String encodeOthers(String s) throws UnsupportedEncodingException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 127) {
                buf.append(ch);
            } else {
                byte[] bytes = new String(new char[] { ch }).getBytes(encoding);
                for (int j = 0; j < bytes.length; j++) {
                    buf.append('%');
                    buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
                    buf.append(digits.charAt(bytes[j] & 0xf));
                }
            }
        }
        return buf.toString();
    }
    static String decode(String s) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= s.length()) {
                        throw new IllegalArgumentException(Msg.getString(
                                "K01fe", i)); 
                    }
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException(Msg.getString(
                                "K01ff", s.substring(i, i + 3), 
                                String.valueOf(i)));
                    }
                    out.write((byte) ((d1 << 4) + d2));
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                result.append(out.toString(encoding));
                continue;
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }
}
