public class SpecialCaseMap implements Comparable {
    SpecialCaseMap() {
        chSource = 0xFFFF;
    }
    public static SpecialCaseMap[] readSpecFile(File file, int plane) throws FileNotFoundException {
        ArrayList caseMaps = new ArrayList(150);
        int count = 0;
        BufferedReader f = new BufferedReader(new FileReader(file));
                String line = null;
        loop:
        while(true) {
            try {
                line = f.readLine();
            }
            catch (IOException e) { break loop; }
                if (line == null) break loop;
                SpecialCaseMap item = parse(line.trim());
                if (item != null) {
                                if(item.getCharSource() >> 16 < plane) continue;
                                if((int)(item.getCharSource() >> 16) > plane) break;
                                caseMaps.add(item);
                ++count;
            }
        }
        caseMaps.trimToSize();
        SpecialCaseMap[] result = new SpecialCaseMap[caseMaps.size()];
        caseMaps.toArray(result);
        Arrays.sort(result);
        return result;
    }
    public static SpecialCaseMap parse(String s) {
        SpecialCaseMap spec = null;
        String[] tokens = new String[REQUIRED_FIELDS];
        if ( s != null && s.length() != 0 && s.charAt(0) != '#') {
            try {
                int x = 0, tokenStart = 0, tokenEnd = 0;
                for (x=0; x<REQUIRED_FIELDS-1; x++) {
                    tokenEnd = s.indexOf(';', tokenStart);
                    tokens[x] = s.substring(tokenStart, tokenEnd);
                    tokenStart = tokenEnd+1;
                }
                tokens[x] = s.substring(tokenStart);
                if(tokens[FIELD_CONDITIONS].indexOf(';') == -1) {
                    spec = new SpecialCaseMap();
                    spec.setCharSource(parseChar(tokens[FIELD_SOURCE]));
                    spec.setUpperCaseMap(parseCaseMap(tokens[FIELD_UPPERCASE]));
                    spec.setLowerCaseMap(parseCaseMap(tokens[FIELD_LOWERCASE]));
                    spec.setTitleCaseMap(parseCaseMap(tokens[FIELD_TITLECASE]));
                    spec.setLocale(parseLocale(tokens[FIELD_CONDITIONS]));
                    spec.setContext(parseContext(tokens[FIELD_CONDITIONS]));
                }
            }
            catch(Exception e) {
                spec = null;
                System.out.println("Error parsing spec line.");
            }
        }
        return spec;
    }
    static int parseChar(String token) throws NumberFormatException {
        return Integer.parseInt(token, 16);
    }
    static char[] parseCaseMap(String token ) throws NumberFormatException {
        int pos = 0;
        StringBuffer buff = new StringBuffer();
        int start = 0, end = 0;
        while(pos < token.length() ){
            while(Character.isSpaceChar(token.charAt(pos++)));
            --pos;
            start = pos;
            while(pos < token.length() && !Character.isSpaceChar(token.charAt(pos))) pos++;
            end = pos;
            int ch = parseChar(token.substring(start,end));
                        if (ch > 0xFFFF) {
                                buff.append(getHighSurrogate(ch));
                                buff.append(getLowSurrogate(ch));
                        } else {
                                buff.append((char)ch);
                        }
        }
        char[] map = new char[buff.length()];
        buff.getChars(0, buff.length(), map, 0);
        return map;
    }
    static Locale parseLocale(String token) {
        return null;
    }
    static String[] parseContext(String token) {
        return null;
    }
    static  int find(int ch, SpecialCaseMap[] map) {
        if ((map == null) || (map.length == 0)) {
            return -1;
        }
        int top, bottom, current;
        bottom = 0;
        top = map.length;
        current = top/2;
        while (top - bottom > 1) {
            if (ch >= map[current].getCharSource()) {
                bottom = current;
            } else {
                top = current;
            }
            current = (top + bottom) / 2;
        }
        if (ch == map[current].getCharSource()) return current;
        else return -1;
    }
        static char getHighSurrogate(int codePoint) {
                char high = (char)codePoint;
                if (codePoint > 0xFFFF) {
                    high = (char)((codePoint - 0x10000)/0x0400 + 0xD800);
                }
                return high;
        }
        static char getLowSurrogate(int codePoint) {
                char low = (char)codePoint;
                if(codePoint > 0xFFFF) {
                        low = (char)((codePoint - 0x10000)%0x0400 + 0xDC00);
                }
                return low;
        }
        static String hex6(int n) {
                String str = Integer.toHexString(n & 0xFFFFFF).toUpperCase();
                return "000000".substring(Math.min(6, str.length())) + str;
        }
        static String hex6(char[] map){
                StringBuffer buff = new StringBuffer();
                int x=0;
                buff.append(hex6(map[x++]));
                while(x<map.length) {
                        buff.append(" " + hex6(map[x++]));
                }
                return buff.toString();
        }
    void setCharSource(int ch) {
        chSource = ch;
    }
    void setLowerCaseMap(char[] map) {
        lowerCaseMap = map;
    }
    void setUpperCaseMap(char[] map) {
        upperCaseMap = map;
    }
    void setTitleCaseMap(char[] map) {
        titleCaseMap = map;
    }
    void setLocale(Locale locale) {
        this.locale = locale;
    }
    void setContext(String[] context) {
        this.context = context;
    }
    public int getCharSource() {
        return chSource;
    }
    public char[] getLowerCaseMap() {
        return lowerCaseMap;
    }
    public char[] getUpperCaseMap() {
        return upperCaseMap;
    }
    public char[] getTitleCaseMap() {
        return titleCaseMap;
    }
    public Locale getLocale() {
        return locale;
    }
    public String[] getContext() {
        return context;
    }
    int chSource;
    Locale locale;
    char[] lowerCaseMap;
    char[] upperCaseMap;
    char[] titleCaseMap;
    String[] context;
    static int REQUIRED_FIELDS = 5;
    static int FIELD_SOURCE = 0;
    static int FIELD_LOWERCASE = 1;
    static int FIELD_TITLECASE = 2;
    static int FIELD_UPPERCASE = 3;
    static int FIELD_CONDITIONS = 4;
    static String CONTEXT_FINAL = "FINAL";
    static String CONTEXT_NONFINAL = "NON_FINAL";
    static String CONTEXT_MODERN = "MODERN";
    static String CONTEXT_NONMODERN = "NON_MODERN";
    public int compareTo(Object otherObject) {
                SpecialCaseMap other = (SpecialCaseMap)otherObject;
        if (chSource < other.chSource) {
            return -1;
        }
        else if (chSource > other.chSource) {
            return 1;
        }
        else return 0;
    }
    public boolean equals(Object o1) {
                boolean bEqual = false;
                if (0 == compareTo(o1)) {
                        bEqual = true;
                }
        return bEqual;
    }
        public String toString() {
                StringBuffer buff = new StringBuffer();
                buff.append(hex6(getCharSource()));
                buff.append("|" + hex6(lowerCaseMap));
                buff.append("|" + hex6(upperCaseMap));
                buff.append("|" + hex6(titleCaseMap));
                buff.append("|" + context);
                return buff.toString();
        }
        public int hashCode() {
                return (int)chSource;
        }
        public static void main(String[] args) {
                SpecialCaseMap[] spec = null;
                if (args.length == 2 ) {
                        try {
                                File file = new File(args[0]);
                                int plane = Integer.parseInt(args[1]);
                                spec = SpecialCaseMap.readSpecFile(file, plane);
                                System.out.println("SpecialCaseMap[" + spec.length + "]:");
                                for (int x=0; x<spec.length; x++) {
                                        System.out.println(spec[x].toString());
                                }
                        }
                        catch(Exception e) {
                                e.printStackTrace();
                        }
                }
        }
}
