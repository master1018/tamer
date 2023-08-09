class UnixUriUtils {
    private UnixUriUtils() { }
    static Path fromUri(UnixFileSystem fs, URI uri) {
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        if (uri.isOpaque())
            throw new IllegalArgumentException("URI is not hierarchical");
        String scheme = uri.getScheme();
        if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
            throw new IllegalArgumentException("URI scheme is not \"file\"");
        if (uri.getAuthority() != null)
            throw new IllegalArgumentException("URI has an authority component");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("URI has a fragment component");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("URI has a query component");
        if (!uri.toString().startsWith("file:
            return new File(uri).toPath();
        String p = uri.getRawPath();
        int len = p.length();
        if (len == 0)
            throw new IllegalArgumentException("URI path component is empty");
        if (p.endsWith("/") && len > 1)
            len--;
        byte[] result = new byte[len];
        int rlen = 0;
        int pos = 0;
        while (pos < len) {
            char c = p.charAt(pos++);
            byte b;
            if (c == '%') {
                assert (pos+2) <= len;
                char c1 = p.charAt(pos++);
                char c2 = p.charAt(pos++);
                b = (byte)((decode(c1) << 4) | decode(c2));
                if (b == 0)
                    throw new IllegalArgumentException("Nul character not allowed");
            } else {
                assert c < 0x80;
                b = (byte)c;
            }
            result[rlen++] = b;
        }
        if (rlen != result.length)
            result = Arrays.copyOf(result, rlen);
        return new UnixPath(fs, result);
    }
    static URI toUri(UnixPath up) {
        byte[] path = up.toAbsolutePath().asByteArray();
        StringBuilder sb = new StringBuilder("file:
        assert path[0] == '/';
        for (int i=1; i<path.length; i++) {
            char c = (char)(path[i] & 0xff);
            if (match(c, L_PATH, H_PATH)) {
                sb.append(c);
            } else {
               sb.append('%');
               sb.append(hexDigits[(c >> 4) & 0x0f]);
               sb.append(hexDigits[(c) & 0x0f]);
            }
        }
        if (sb.charAt(sb.length()-1) != '/') {
            try {
                 if (UnixFileAttributes.get(up, true).isDirectory())
                     sb.append('/');
            } catch (UnixException x) {
            }
        }
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException x) {
            throw new AssertionError(x);  
        }
    }
    private static long lowMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c < 64)
                m |= (1L << c);
        }
        return m;
    }
    private static long highMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if ((c >= 64) && (c < 128))
                m |= (1L << (c - 64));
        }
        return m;
    }
    private static long lowMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 63), 0);
        int l = Math.max(Math.min(last, 63), 0);
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }
    private static long highMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 127), 64) - 64;
        int l = Math.max(Math.min(last, 127), 64) - 64;
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }
    private static boolean match(char c, long lowMask, long highMask) {
        if (c < 64)
            return ((1L << c) & lowMask) != 0;
        if (c < 128)
            return ((1L << (c - 64)) & highMask) != 0;
        return false;
    }
    private static int decode(char c) {
        if ((c >= '0') && (c <= '9'))
            return c - '0';
        if ((c >= 'a') && (c <= 'f'))
            return c - 'a' + 10;
        if ((c >= 'A') && (c <= 'F'))
            return c - 'A' + 10;
        throw new AssertionError();
    }
    private static final long L_DIGIT = lowMask('0', '9');
    private static final long H_DIGIT = 0L;
    private static final long L_UPALPHA = 0L;
    private static final long H_UPALPHA = highMask('A', 'Z');
    private static final long L_LOWALPHA = 0L;
    private static final long H_LOWALPHA = highMask('a', 'z');
    private static final long L_ALPHA = L_LOWALPHA | L_UPALPHA;
    private static final long H_ALPHA = H_LOWALPHA | H_UPALPHA;
    private static final long L_ALPHANUM = L_DIGIT | L_ALPHA;
    private static final long H_ALPHANUM = H_DIGIT | H_ALPHA;
    private static final long L_MARK = lowMask("-_.!~*'()");
    private static final long H_MARK = highMask("-_.!~*'()");
    private static final long L_UNRESERVED = L_ALPHANUM | L_MARK;
    private static final long H_UNRESERVED = H_ALPHANUM | H_MARK;
    private static final long L_PCHAR
        = L_UNRESERVED | lowMask(":@&=+$,");
    private static final long H_PCHAR
        = H_UNRESERVED | highMask(":@&=+$,");
   private static final long L_PATH = L_PCHAR | lowMask(";/");
   private static final long H_PATH = H_PCHAR | highMask(";/");
   private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
}
