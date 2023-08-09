public class Rdn implements Serializable, Comparable<Object> {
    private transient ArrayList entries;
    private static final int DEFAULT_SIZE = 1;
    private static final long serialVersionUID = -5994465067210009656L;
    public Rdn(Attributes attrSet) throws InvalidNameException {
        if (attrSet.size() == 0) {
            throw new InvalidNameException("Attributes cannot be empty");
        }
        entries = new ArrayList(attrSet.size());
        NamingEnumeration attrs = attrSet.getAll();
        try {
            for (int nEntries = 0; attrs.hasMore(); nEntries++) {
                RdnEntry entry = new RdnEntry();
                Attribute attr = (Attribute) attrs.next();
                entry.type = attr.getID();
                entry.value = attr.get();
                entries.add(nEntries, entry);
            }
        } catch (NamingException e) {
            InvalidNameException e2 = new InvalidNameException(
                                        e.getMessage());
            e2.initCause(e);
            throw e2;
        }
        sort(); 
    }
    public Rdn(String rdnString) throws InvalidNameException {
        entries = new ArrayList(DEFAULT_SIZE);
        (new Rfc2253Parser(rdnString)).parseRdn(this);
    }
    public Rdn(Rdn rdn) {
        entries = new ArrayList(rdn.entries.size());
        entries.addAll(rdn.entries);
    }
    public Rdn(String type, Object value) throws InvalidNameException {
        if (value == null) {
            throw new NullPointerException("Cannot set value to null");
        }
        if (type.equals("") || isEmptyValue(value)) {
            throw new InvalidNameException(
                "type or value cannot be empty, type:" + type +
                " value:" + value);
        }
        entries = new ArrayList(DEFAULT_SIZE);
        put(type, value);
    }
    private boolean isEmptyValue(Object val) {
        return ((val instanceof String) && val.equals("")) ||
        ((val instanceof byte[]) && (((byte[]) val).length == 0));
    }
    Rdn() {
        entries = new ArrayList(DEFAULT_SIZE);
    }
    Rdn put(String type, Object value) {
        RdnEntry newEntry = new RdnEntry();
        newEntry.type =  type;
        if (value instanceof byte[]) {  
            newEntry.value = ((byte[]) value).clone();
        } else {
            newEntry.value = value;
        }
        entries.add(newEntry);
        return this;
    }
    void sort() {
        if (entries.size() > 1) {
            Collections.sort(entries);
        }
    }
    public Object getValue() {
        return ((RdnEntry) entries.get(0)).getValue();
    }
    public String getType() {
        return ((RdnEntry) entries.get(0)).getType();
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = entries.size();
        if (size > 0) {
            builder.append(entries.get(0));
        }
        for (int next = 1; next < size; next++) {
            builder.append('+');
            builder.append(entries.get(next));
        }
        return builder.toString();
    }
    public int compareTo(Object obj) {
        if (!(obj instanceof Rdn)) {
            throw new ClassCastException("The obj is not a Rdn");
        }
        if (obj == this) {
            return 0;
        }
        Rdn that = (Rdn) obj;
        int minSize = Math.min(entries.size(), that.entries.size());
        for (int i = 0; i < minSize; i++) {
            int diff = ((RdnEntry) entries.get(i)).compareTo(
                                        that.entries.get(i));
            if (diff != 0) {
                return diff;
            }
        }
        return (entries.size() - that.entries.size());  
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Rdn)) {
            return false;
        }
        Rdn that = (Rdn) obj;
        if (entries.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (!entries.get(i).equals(that.entries.get(i))) {
                return false;
            }
        }
        return true;
    }
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < entries.size(); i++) {
            hash += entries.get(i).hashCode();
        }
        return hash;
    }
    public Attributes toAttributes() {
        Attributes attrs = new BasicAttributes(true);
        for (int i = 0; i < entries.size(); i++) {
            RdnEntry entry = (RdnEntry) entries.get(i);
            Attribute attr = attrs.put(entry.getType(), entry.getValue());
            if (attr != null) {
                attr.add(entry.getValue());
                attrs.put(attr);
            }
        }
        return attrs;
    }
    private static class RdnEntry implements Comparable {
        private String type;
        private Object value;
        private String comparable = null;
        String getType() {
            return type;
        }
        Object getValue() {
            return value;
        }
        public int compareTo(Object obj) {
            RdnEntry that = (RdnEntry) obj;
            int diff = type.toUpperCase().compareTo(
                        that.type.toUpperCase());
            if (diff != 0) {
                return diff;
            }
            if (value.equals(that.value)) {     
                return 0;
            }
            return getValueComparable().compareTo(
                        that.getValueComparable());
        }
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RdnEntry)) {
                return false;
            }
            RdnEntry that = (RdnEntry) obj;
            return (type.equalsIgnoreCase(that.type)) &&
                        (getValueComparable().equals(
                        that.getValueComparable()));
        }
        public int hashCode() {
            return (type.toUpperCase().hashCode() +
                getValueComparable().hashCode());
        }
        public String toString() {
            return type + "=" + escapeValue(value);
        }
        private String getValueComparable() {
            if (comparable != null) {
                return comparable;              
            }
            if (value instanceof byte[]) {
                comparable = escapeBinaryValue((byte[]) value);
            } else {
                comparable = ((String) value).toUpperCase();
            }
            return comparable;
        }
    }
    public int size() {
        return entries.size();
    }
    public static String escapeValue(Object val) {
        return (val instanceof byte[])
                ? escapeBinaryValue((byte[])val)
                : escapeStringValue((String)val);
    }
    private static final String escapees = ",=+<>#;\"\\";
    private static String escapeStringValue(String val) {
            char[] chars = val.toCharArray();
            StringBuilder builder = new StringBuilder(2 * val.length());
            int lead;   
            for (lead = 0; lead < chars.length; lead++) {
                if (!isWhitespace(chars[lead])) {
                    break;
                }
            }
            int trail;  
            for (trail = chars.length - 1; trail >= 0; trail--) {
                if (!isWhitespace(chars[trail])) {
                    break;
                }
            }
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if ((i < lead) || (i > trail) || (escapees.indexOf(c) >= 0)) {
                    builder.append('\\');
                }
                builder.append(c);
            }
            return builder.toString();
    }
    private static String escapeBinaryValue(byte[] val) {
        StringBuilder builder = new StringBuilder(1 + 2 * val.length);
        builder.append("#");
        for (int i = 0; i < val.length; i++) {
            byte b = val[i];
            builder.append(Character.forDigit(0xF & (b >>> 4), 16));
            builder.append(Character.forDigit(0xF & b, 16));
        }
        return builder.toString();
    }
    public static Object unescapeValue(String val) {
            char[] chars = val.toCharArray();
            int beg = 0;
            int end = chars.length;
            while ((beg < end) && isWhitespace(chars[beg])) {
                ++beg;
            }
            while ((beg < end) && isWhitespace(chars[end - 1])) {
                --end;
            }
            if (end != chars.length &&
                    (beg < end) &&
                    chars[end - 1] == '\\') {
                end++;
            }
            if (beg >= end) {
                return "";
            }
            if (chars[beg] == '#') {
                return decodeHexPairs(chars, ++beg, end);
            }
            if ((chars[beg] == '\"') && (chars[end - 1] == '\"')) {
                ++beg;
                --end;
            }
            StringBuilder builder = new StringBuilder(end - beg);
            int esc = -1; 
            for (int i = beg; i < end; i++) {
                if ((chars[i] == '\\') && (i + 1 < end)) {
                    if (!Character.isLetterOrDigit(chars[i + 1])) {
                        ++i;                            
                        builder.append(chars[i]);       
                        esc = i;
                    } else {
                        byte[] utf8 = getUtf8Octets(chars, i, end);
                        if (utf8.length > 0) {
                            try {
                                builder.append(new String(utf8, "UTF8"));
                            } catch (java.io.UnsupportedEncodingException e) {
                            }
                            i += utf8.length * 3 - 1;
                        } else { 
                            throw new IllegalArgumentException(
                                "Not a valid attribute string value:" +
                                val + ",improper usage of backslash");
                        }
                    }
                } else {
                    builder.append(chars[i]);   
                }
            }
            int len = builder.length();
            if (isWhitespace(builder.charAt(len - 1)) && esc != (end - 1)) {
                builder.setLength(len - 1);
            }
            return builder.toString();
        }
        private static byte[] decodeHexPairs(char[] chars, int beg, int end) {
            byte[] bytes = new byte[(end - beg) / 2];
            for (int i = 0; beg + 1 < end; i++) {
                int hi = Character.digit(chars[beg], 16);
                int lo = Character.digit(chars[beg + 1], 16);
                if (hi < 0 || lo < 0) {
                    break;
                }
                bytes[i] = (byte)((hi<<4) + lo);
                beg += 2;
            }
            if (beg != end) {
                throw new IllegalArgumentException(
                        "Illegal attribute value: " + new String(chars));
            }
            return bytes;
        }
        private static byte[] getUtf8Octets(char[] chars, int beg, int end) {
            byte[] utf8 = new byte[(end - beg) / 3];    
            int len = 0;        
            while ((beg + 2 < end) &&
                   (chars[beg++] == '\\')) {
                int hi = Character.digit(chars[beg++], 16);
                int lo = Character.digit(chars[beg++], 16);
                if (hi < 0 || lo < 0) {
                   break;
                }
                utf8[len++] = (byte)((hi<<4) + lo);
            }
            if (len == utf8.length) {
                return utf8;
            } else {
                byte[] res = new byte[len];
                System.arraycopy(utf8, 0, res, 0, len);
                return res;
            }
        }
    private static boolean isWhitespace(char c) {
        return (c == ' ' || c == '\r');
    }
    private void writeObject(ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();
        s.writeObject(toString());
    }
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        entries = new ArrayList(DEFAULT_SIZE);
        String unparsed = (String) s.readObject();
        try {
            (new Rfc2253Parser(unparsed)).parseRdn(this);
        } catch (InvalidNameException e) {
            throw new java.io.StreamCorruptedException(
                    "Invalid name: " + unparsed);
        }
    }
}
