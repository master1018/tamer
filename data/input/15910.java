class WindowsPathParser {
    private WindowsPathParser() { }
    static class Result {
        private final WindowsPathType type;
        private final String root;
        private final String path;
        Result(WindowsPathType type, String root, String path) {
            this.type = type;
            this.root = root;
            this.path = path;
        }
        WindowsPathType type() {
            return type;
        }
        String root() {
            return root;
        }
        String path() {
            return path;
        }
    }
    static Result parse(String input) {
        return parse(input, true);
    }
    static Result parseNormalizedPath(String input) {
        return parse(input, false);
    }
    private static Result parse(String input, boolean requireToNormalize) {
        String root = "";
        WindowsPathType type = null;
        int len = input.length();
        int off = 0;
        if (len > 1) {
            char c0 = input.charAt(0);
            char c1 = input.charAt(1);
            char c = 0;
            int next = 2;
            if (isSlash(c0) && isSlash(c1)) {
                type = WindowsPathType.UNC;
                off = nextNonSlash(input, next, len);
                next = nextSlash(input, off, len);
                if (off == next)
                    throw new InvalidPathException(input, "UNC path is missing hostname");
                String host = input.substring(off, next);  
                off = nextNonSlash(input, next, len);
                next = nextSlash(input, off, len);
                if (off == next)
                    throw new InvalidPathException(input, "UNC path is missing sharename");
                root = "\\\\" + host + "\\" + input.substring(off, next) + "\\";
                off = next;
            } else {
                if (isLetter(c0) && c1 == ':') {
                    root = input.substring(0, 2);
                    if (len > 2 && isSlash(input.charAt(2))) {
                        off = 3;
                        root += "\\";
                        type = WindowsPathType.ABSOLUTE;
                    } else {
                        off = 2;
                        type = WindowsPathType.DRIVE_RELATIVE;
                    }
                }
            }
        }
        if (off == 0) {
            if (len > 0 && isSlash(input.charAt(0))) {
                type = WindowsPathType.DIRECTORY_RELATIVE;
                root = "\\";
            } else {
                type = WindowsPathType.RELATIVE;
            }
        }
        if (requireToNormalize) {
            StringBuilder sb = new StringBuilder(input.length());
            sb.append(root);
            return new Result(type, root, normalize(sb, input, off));
        } else {
            return new Result(type, root, input);
        }
    }
    private static String normalize(StringBuilder sb, String path, int off) {
        int len = path.length();
        off = nextNonSlash(path, off, len);
        int start = off;
        char lastC = 0;
        while (off < len) {
            char c = path.charAt(off);
            if (isSlash(c)) {
                if (lastC == ' ')
                    throw new InvalidPathException(path,
                                                   "Trailing char <" + lastC + ">",
                                                   off - 1);
                sb.append(path, start, off);
                off = nextNonSlash(path, off, len);
                if (off != len)   
                    sb.append('\\');
                start = off;
            } else {
                if (isInvalidPathChar(c))
                    throw new InvalidPathException(path,
                                                   "Illegal char <" + c + ">",
                                                   off);
                lastC = c;
                off++;
            }
        }
        if (start != off) {
            if (lastC == ' ')
                throw new InvalidPathException(path,
                                               "Trailing char <" + lastC + ">",
                                               off - 1);
            sb.append(path, start, off);
        }
        return sb.toString();
    }
    private static final boolean isSlash(char c) {
        return (c == '\\') || (c == '/');
    }
    private static final int nextNonSlash(String path, int off, int end) {
        while (off < end && isSlash(path.charAt(off))) { off++; }
        return off;
    }
    private static final int nextSlash(String path, int off, int end) {
        char c;
        while (off < end && !isSlash(c=path.charAt(off))) {
            if (isInvalidPathChar(c))
                throw new InvalidPathException(path,
                                               "Illegal character [" + c + "] in path",
                                               off);
            off++;
        }
        return off;
    }
    private static final boolean isLetter(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }
    private static final String reservedChars = "<>:\"|?*";
    private static final boolean isInvalidPathChar(char ch) {
        return ch < '\u0020' || reservedChars.indexOf(ch) != -1;
    }
}
