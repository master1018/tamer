public final class Pattern implements Serializable {
    private static final long serialVersionUID = 5073258162644648461L;
    public static final int UNIX_LINES = 0x01;
    public static final int CASE_INSENSITIVE = 0x02;
    public static final int COMMENTS = 0x04;
    public static final int MULTILINE = 0x08;
    public static final int LITERAL = 0x10;
    public static final int DOTALL = 0x20;
    public static final int UNICODE_CASE = 0x40;
    public static final int CANON_EQ = 0x80;
    private String pattern;
    private int flags;
    transient int mNativePattern;
    transient int mGroupCount;
    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }
    public String[] split(CharSequence inputSeq, int limit) {
        if (inputSeq.length() == 0) {
            return new String[] { "" };
        }
        int maxLength = limit <= 0 ? Integer.MAX_VALUE : limit;
        String input = inputSeq.toString();
        ArrayList<String> list = new ArrayList<String>();
        Matcher matcher = new Matcher(this, inputSeq);
        int savedPos = 0;
        while(matcher.find() && list.size() + 1 < maxLength) {
            list.add(input.substring(savedPos, matcher.start()));
            savedPos = matcher.end();
        }
        if (list.size() < maxLength) {
            if (savedPos < input.length()) {
                list.add(input.substring(savedPos));
            } else {
                list.add("");
            }
        }
        if (limit == 0) {
            int i = list.size() - 1;
            while (i >= 0 && "".equals(list.get(i))) {
                list.remove(i);
                i--;
            }
        }
        return list.toArray(new String[list.size()]);
    }
    public String[] split(CharSequence input) {
        return split(input, 0);
    }
    public String pattern() {
        return pattern;
    }
    @Override
    public String toString() {
        return pattern;
    }
    public int flags() {
        return flags;
    }
    public static Pattern compile(String pattern, int flags) throws PatternSyntaxException {
        return new Pattern(pattern, flags);
    }
    private Pattern(String pattern, int flags) throws PatternSyntaxException {
        if ((flags & CANON_EQ) != 0) {
            throw new UnsupportedOperationException("CANON_EQ flag not supported");
        }
        this.pattern = pattern;
        this.flags = flags;
        compileImpl(pattern, flags);
    }
    public static Pattern compile(String pattern) {
        return new Pattern(pattern, 0);
    }
    private void compileImpl(String pattern, int flags) throws PatternSyntaxException {
        if (pattern == null) {
            throw new NullPointerException();
        }
        if ((flags & LITERAL) != 0) {
            pattern = quote(pattern);
        }
        flags = flags & (CASE_INSENSITIVE | COMMENTS | MULTILINE | DOTALL | UNIX_LINES);
        mNativePattern = NativeRegEx.open(pattern, flags);
        mGroupCount = NativeRegEx.groupCount(mNativePattern);
    }
    public static boolean matches(String regex, CharSequence input) {
        return new Matcher(new Pattern(regex, 0), input).matches();
    }
    public static String quote(String s) {
        StringBuilder sb = new StringBuilder().append("\\Q"); 
        int apos = 0;
        int k;
        while ((k = s.indexOf("\\E", apos)) >= 0) { 
            sb.append(s.substring(apos, k + 2)).append("\\\\E\\Q"); 
            apos = k + 2;
        }
        return sb.append(s.substring(apos)).append("\\E").toString(); 
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativePattern != 0) {
                NativeRegEx.close(mNativePattern);
            }
        }
        finally {
            super.finalize();
        }
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        compileImpl(pattern, flags);
    }
}
