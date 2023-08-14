public class URLDecoder {
    static Charset defaultCharset;
    @Deprecated
    public static String decode(String s) {
        if (defaultCharset == null) {
            try {
                defaultCharset = Charset.forName(
                        System.getProperty("file.encoding")); 
            } catch (IllegalCharsetNameException e) {
            } catch (UnsupportedCharsetException e) {
            }
            if (defaultCharset == null) {
                defaultCharset = Charset.forName("ISO-8859-1"); 
            }
        }
        return decode(s, defaultCharset);
    }
    public static String decode(String s, String enc)
            throws UnsupportedEncodingException {
        if (enc == null) {
            throw new NullPointerException();
        }
        if (enc.length() == 0) {
            throw new UnsupportedEncodingException(
                    Msg.getString("K00a5", "enc")); 
        }
        if (s.indexOf('%') == -1) {
            if (s.indexOf('+') == -1)
                return s;
            char str[] = s.toCharArray();
            for (int i = 0; i < str.length; i++) {
                if (str[i] == '+')
                    str[i] = ' ';
            }
            return new String(str);
        }
        Charset charset = null;
        try {
            charset = Charset.forName(enc);
        } catch (IllegalCharsetNameException e) {
            throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                    enc).initCause(e));
        } catch (UnsupportedCharsetException e) {
            throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                    enc).initCause(e));
        }
        return decode(s, charset);
    }
    private static String decode(String s, Charset charset) {
        char str_buf[] = new char[s.length()];
        byte buf[] = new byte[s.length() / 3];
        int buf_len = 0;
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (c == '+') {
                str_buf[buf_len] = ' ';
            } else if (c == '%') {
                int len = 0;
                do {
                    if (i + 2 >= s.length()) {
                        throw new IllegalArgumentException(
                                Msg.getString("K01fe", i)); 
                    }
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException(
                                Msg.getString(
                                        "K01ff", 
                                        s.substring(i, i + 3),
                                        String.valueOf(i)));
                    }
                    buf[len++] = (byte) ((d1 << 4) + d2);
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                CharBuffer cb = charset.decode(ByteBuffer.wrap(buf, 0, len));
                len = cb.length();
                System.arraycopy(cb.array(), 0, str_buf, buf_len, len);
                buf_len += len;
                continue;
            } else {
                str_buf[buf_len] = c;
            }
            i++;
            buf_len++;
        }
        return new String(str_buf, 0, buf_len);
    }
}
