class ZipUtils {
    public static void writeShort(OutputStream os, int v) throws IOException {
        os.write(v & 0xff);
        os.write((v >>> 8) & 0xff);
    }
    public static void writeInt(OutputStream os, long v) throws IOException {
        os.write((int)(v & 0xff));
        os.write((int)((v >>>  8) & 0xff));
        os.write((int)((v >>> 16) & 0xff));
        os.write((int)((v >>> 24) & 0xff));
    }
    public static void writeLong(OutputStream os, long v) throws IOException {
        os.write((int)(v & 0xff));
        os.write((int)((v >>>  8) & 0xff));
        os.write((int)((v >>> 16) & 0xff));
        os.write((int)((v >>> 24) & 0xff));
        os.write((int)((v >>> 32) & 0xff));
        os.write((int)((v >>> 40) & 0xff));
        os.write((int)((v >>> 48) & 0xff));
        os.write((int)((v >>> 56) & 0xff));
    }
    public static void writeBytes(OutputStream os, byte[] b)
        throws IOException
    {
        os.write(b, 0, b.length);
    }
    public static void writeBytes(OutputStream os, byte[] b, int off, int len)
        throws IOException
    {
        os.write(b, off, len);
    }
    public static byte[] toDirectoryPath(byte[] dir) {
        if (dir.length != 0 && dir[dir.length - 1] != '/') {
            dir = Arrays.copyOf(dir, dir.length + 1);
            dir[dir.length - 1] = '/';
        }
        return dir;
    }
    public static long dosToJavaTime(long dtime) {
        Date d = new Date((int)(((dtime >> 25) & 0x7f) + 80),
                          (int)(((dtime >> 21) & 0x0f) - 1),
                          (int)((dtime >> 16) & 0x1f),
                          (int)((dtime >> 11) & 0x1f),
                          (int)((dtime >> 5) & 0x3f),
                          (int)((dtime << 1) & 0x3e));
        return d.getTime();
    }
    public static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return (1 << 21) | (1 << 16);
        }
        return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
               d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
               d.getSeconds() >> 1;
    }
    private static final long WINDOWS_EPOCH_IN_MICROSECONDS = -11644473600000000L;
    public static final long winToJavaTime(long wtime) {
        return TimeUnit.MILLISECONDS.convert(
               wtime / 10 + WINDOWS_EPOCH_IN_MICROSECONDS, TimeUnit.MICROSECONDS);
    }
    public static final long javaToWinTime(long time) {
        return (TimeUnit.MICROSECONDS.convert(time, TimeUnit.MILLISECONDS)
               - WINDOWS_EPOCH_IN_MICROSECONDS) * 10;
    }
    public static final long unixToJavaTime(long utime) {
        return TimeUnit.MILLISECONDS.convert(utime, TimeUnit.SECONDS);
    }
    public static final long javaToUnixTime(long time) {
        return TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
    }
    private static final String regexMetaChars = ".^$+{[]|()";
    private static final String globMetaChars = "\\*?[{";
    private static boolean isRegexMeta(char c) {
        return regexMetaChars.indexOf(c) != -1;
    }
    private static boolean isGlobMeta(char c) {
        return globMetaChars.indexOf(c) != -1;
    }
    private static char EOL = 0;  
    private static char next(String glob, int i) {
        if (i < glob.length()) {
            return glob.charAt(i);
        }
        return EOL;
    }
    public static String toRegexPattern(String globPattern) {
        boolean inGroup = false;
        StringBuilder regex = new StringBuilder("^");
        int i = 0;
        while (i < globPattern.length()) {
            char c = globPattern.charAt(i++);
            switch (c) {
                case '\\':
                    if (i == globPattern.length()) {
                        throw new PatternSyntaxException("No character to escape",
                                globPattern, i - 1);
                    }
                    char next = globPattern.charAt(i++);
                    if (isGlobMeta(next) || isRegexMeta(next)) {
                        regex.append('\\');
                    }
                    regex.append(next);
                    break;
                case '/':
                    regex.append(c);
                    break;
                case '[':
                    regex.append("[[^/]&&[");
                    if (next(globPattern, i) == '^') {
                        regex.append("\\^");
                        i++;
                    } else {
                        if (next(globPattern, i) == '!') {
                            regex.append('^');
                            i++;
                        }
                        if (next(globPattern, i) == '-') {
                            regex.append('-');
                            i++;
                        }
                    }
                    boolean hasRangeStart = false;
                    char last = 0;
                    while (i < globPattern.length()) {
                        c = globPattern.charAt(i++);
                        if (c == ']') {
                            break;
                        }
                        if (c == '/') {
                            throw new PatternSyntaxException("Explicit 'name separator' in class",
                                    globPattern, i - 1);
                        }
                        if (c == '\\' || c == '[' ||
                                c == '&' && next(globPattern, i) == '&') {
                            regex.append('\\');
                        }
                        regex.append(c);
                        if (c == '-') {
                            if (!hasRangeStart) {
                                throw new PatternSyntaxException("Invalid range",
                                        globPattern, i - 1);
                            }
                            if ((c = next(globPattern, i++)) == EOL || c == ']') {
                                break;
                            }
                            if (c < last) {
                                throw new PatternSyntaxException("Invalid range",
                                        globPattern, i - 3);
                            }
                            regex.append(c);
                            hasRangeStart = false;
                        } else {
                            hasRangeStart = true;
                            last = c;
                        }
                    }
                    if (c != ']') {
                        throw new PatternSyntaxException("Missing ']", globPattern, i - 1);
                    }
                    regex.append("]]");
                    break;
                case '{':
                    if (inGroup) {
                        throw new PatternSyntaxException("Cannot nest groups",
                                globPattern, i - 1);
                    }
                    regex.append("(?:(?:");
                    inGroup = true;
                    break;
                case '}':
                    if (inGroup) {
                        regex.append("))");
                        inGroup = false;
                    } else {
                        regex.append('}');
                    }
                    break;
                case ',':
                    if (inGroup) {
                        regex.append(")|(?:");
                    } else {
                        regex.append(',');
                    }
                    break;
                case '*':
                    if (next(globPattern, i) == '*') {
                        regex.append(".*");
                        i++;
                    } else {
                        regex.append("[^/]*");
                    }
                    break;
                case '?':
                   regex.append("[^/]");
                   break;
                default:
                    if (isRegexMeta(c)) {
                        regex.append('\\');
                    }
                    regex.append(c);
            }
        }
        if (inGroup) {
            throw new PatternSyntaxException("Missing '}", globPattern, i - 1);
        }
        return regex.append('$').toString();
    }
}
