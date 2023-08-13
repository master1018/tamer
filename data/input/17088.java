public abstract class Alignment {
    private static int nextOrdinal = 0;
    private static HashMap<String, Alignment> map = new HashMap<String, Alignment>();
    private static final String blanks = "                                                                                                                                                               ";
    private final String name;
    private final int value = nextOrdinal++;
    protected abstract String align(String s, int width);
    public static final Alignment CENTER = new Alignment("center") {
        protected String align(String s, int width) {
            int length = s.length();
            if (length >= width) {
                return s;
            }
            int pad = width - length;
            int pad2 = pad / 2;
            int padr = pad % 2;
            if (pad2 == 0) {
              return s + blanks.substring(0, padr);
            } else {
              return  blanks.substring(0, pad2) + s +
                      blanks.substring(0, pad2 + padr);
            }
        }
    };
    public static final Alignment LEFT = new Alignment("left") {
        protected String align(String s, int width) {
            int length = s.length();
            if (length >= width) {
                return s;
            }
            int pad = width - length;
            return s+blanks.substring(0, pad);
        }
    };
    public static final Alignment RIGHT = new Alignment("right") {
        protected String align(String s, int width) {
            int length = s.length();
            if (length >= width) {
                return s;
            }
            int pad = width - length;
            return blanks.substring(0, pad) + s;
        }
    };
    public static Alignment toAlignment(String s) {
        return map.get(s);
    }
    public static Set keySet() {
        return map.keySet();
    }
    public String toString() {
        return name;
    }
    private Alignment(String name) {
        this.name = name;
        map.put(name, this);
    }
}
