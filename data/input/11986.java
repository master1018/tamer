public final class LdapName implements Name {
    private transient String unparsed;  
    private transient Vector rdns;      
    private transient boolean valuesCaseSensitive = false;
    public LdapName(String name) throws InvalidNameException {
        unparsed = name;
        parse();
    }
    private LdapName(String name, Vector rdns) {
        unparsed = name;
        this.rdns = (Vector)rdns.clone();
    }
    private LdapName(String name, Vector rdns, int beg, int end) {
        unparsed = name;
        this.rdns = new Vector();
        for (int i = beg; i < end; i++) {
            this.rdns.addElement(rdns.elementAt(i));
        }
    }
    public Object clone() {
        return new LdapName(unparsed, rdns);
    }
    public String toString() {
        if (unparsed != null) {
            return unparsed;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = rdns.size() - 1; i >= 0; i--) {
            if (i < rdns.size() - 1) {
                buf.append(',');
            }
            Rdn rdn = (Rdn)rdns.elementAt(i);
            buf.append(rdn);
        }
        unparsed = new String(buf);
        return unparsed;
    }
    public boolean equals(Object obj) {
        return ((obj instanceof LdapName) &&
                (compareTo(obj) == 0));
    }
    public int compareTo(Object obj) {
        LdapName that = (LdapName)obj;
        if ((obj == this) ||                    
            (unparsed != null && unparsed.equals(that.unparsed))) {
            return 0;
        }
        int minSize = Math.min(rdns.size(), that.rdns.size());
        for (int i = 0 ; i < minSize; i++) {
            Rdn rdn1 = (Rdn)rdns.elementAt(i);
            Rdn rdn2 = (Rdn)that.rdns.elementAt(i);
            int diff = rdn1.compareTo(rdn2);
            if (diff != 0) {
                return diff;
            }
        }
        return (rdns.size() - that.rdns.size());        
    }
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < rdns.size(); i++) {
            Rdn rdn = (Rdn)rdns.elementAt(i);
            hash += rdn.hashCode();
        }
        return hash;
    }
    public int size() {
        return rdns.size();
    }
    public boolean isEmpty() {
        return rdns.isEmpty();
    }
    public Enumeration getAll() {
        final Enumeration enum_ = rdns.elements();
        return new Enumeration () {
            public boolean hasMoreElements() {
                return enum_.hasMoreElements();
            }
            public Object nextElement() {
                return enum_.nextElement().toString();
            }
        };
    }
    public String get(int pos) {
        return rdns.elementAt(pos).toString();
    }
    public Name getPrefix(int pos) {
        return new LdapName(null, rdns, 0, pos);
    }
    public Name getSuffix(int pos) {
        return new LdapName(null, rdns, pos, rdns.size());
    }
    public boolean startsWith(Name n) {
        int len1 = rdns.size();
        int len2 = n.size();
        return (len1 >= len2 &&
                matches(0, len2, n));
    }
    public boolean endsWith(Name n) {
        int len1 = rdns.size();
        int len2 = n.size();
        return (len1 >= len2 &&
                matches(len1 - len2, len1, n));
    }
     public void setValuesCaseSensitive(boolean caseSensitive) {
         toString();
         rdns = null;   
         try {
             parse();
         } catch (InvalidNameException e) {
             throw new IllegalStateException("Cannot parse name: " + unparsed);
         }
         valuesCaseSensitive = caseSensitive;
     }
    private boolean matches(int beg, int end, Name n) {
        for (int i = beg; i < end; i++) {
            Rdn rdn;
            if (n instanceof LdapName) {
                LdapName ln = (LdapName)n;
                rdn = (Rdn)ln.rdns.elementAt(i - beg);
            } else {
                String rdnString = n.get(i - beg);
                try {
                    rdn = (new DnParser(rdnString, valuesCaseSensitive)).getRdn();
                } catch (InvalidNameException e) {
                    return false;
                }
            }
            if (!rdn.equals(rdns.elementAt(i))) {
                return false;
            }
        }
        return true;
    }
    public Name addAll(Name suffix) throws InvalidNameException {
        return addAll(size(), suffix);
    }
    public Name addAll(int pos, Name suffix) throws InvalidNameException {
        if (suffix instanceof LdapName) {
            LdapName s = (LdapName)suffix;
            for (int i = 0; i < s.rdns.size(); i++) {
                rdns.insertElementAt(s.rdns.elementAt(i), pos++);
            }
        } else {
            Enumeration comps = suffix.getAll();
            while (comps.hasMoreElements()) {
                DnParser p = new DnParser((String)comps.nextElement(),
                    valuesCaseSensitive);
                rdns.insertElementAt(p.getRdn(), pos++);
            }
        }
        unparsed = null;                                
        return this;
    }
    public Name add(String comp) throws InvalidNameException {
        return add(size(), comp);
    }
    public Name add(int pos, String comp) throws InvalidNameException {
        Rdn rdn = (new DnParser(comp, valuesCaseSensitive)).getRdn();
        rdns.insertElementAt(rdn, pos);
        unparsed = null;                                
        return this;
    }
    public Object remove(int pos) throws InvalidNameException {
        String comp = get(pos);
        rdns.removeElementAt(pos);
        unparsed = null;                                
        return comp;
    }
    private void parse() throws InvalidNameException {
        rdns = (new DnParser(unparsed, valuesCaseSensitive)).getDn();
    }
    private static boolean isWhitespace(char c) {
        return (c == ' ' || c == '\r');
    }
    public static String escapeAttributeValue(Object val) {
        return TypeAndValue.escapeValue(val);
    }
    public static Object unescapeAttributeValue(String val) {
        return TypeAndValue.unescapeValue(val);
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeObject(toString());
        s.writeBoolean(valuesCaseSensitive);
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        unparsed = (String)s.readObject();
        valuesCaseSensitive = s.readBoolean();
        try {
            parse();
        } catch (InvalidNameException e) {
            throw new java.io.StreamCorruptedException(
                    "Invalid name: " + unparsed);
        }
    }
    static final long serialVersionUID = -1595520034788997356L;
    static class DnParser {
        private final String name;      
        private final char[] chars;     
        private final int len;          
        private int cur = 0;            
        private boolean valuesCaseSensitive;
        DnParser(String name, boolean valuesCaseSensitive)
            throws InvalidNameException {
            this.name = name;
            len = name.length();
            chars = name.toCharArray();
            this.valuesCaseSensitive = valuesCaseSensitive;
        }
        Vector getDn() throws InvalidNameException {
            cur = 0;
            Vector rdns = new Vector(len / 3 + 10);  
            if (len == 0) {
                return rdns;
            }
            rdns.addElement(parseRdn());
            while (cur < len) {
                if (chars[cur] == ',' || chars[cur] == ';') {
                    ++cur;
                    rdns.insertElementAt(parseRdn(), 0);
                } else {
                    throw new InvalidNameException("Invalid name: " + name);
                }
            }
            return rdns;
        }
        Rdn getRdn() throws InvalidNameException {
            Rdn rdn = parseRdn();
            if (cur < len) {
                throw new InvalidNameException("Invalid RDN: " + name);
            }
            return rdn;
        }
        private Rdn parseRdn() throws InvalidNameException {
            Rdn rdn = new Rdn();
            while (cur < len) {
                consumeWhitespace();
                String attrType = parseAttrType();
                consumeWhitespace();
                if (cur >= len || chars[cur] != '=') {
                    throw new InvalidNameException("Invalid name: " + name);
                }
                ++cur;          
                consumeWhitespace();
                String value = parseAttrValue();
                consumeWhitespace();
                rdn.add(new TypeAndValue(attrType, value, valuesCaseSensitive));
                if (cur >= len || chars[cur] != '+') {
                    break;
                }
                ++cur;          
            }
            return rdn;
        }
        private String parseAttrType() throws InvalidNameException {
            final int beg = cur;
            while (cur < len) {
                char c = chars[cur];
                if (Character.isLetterOrDigit(c) ||
                      c == '.' ||
                      c == '-' ||
                      c == ' ') {
                    ++cur;
                } else {
                    break;
                }
            }
            while ((cur > beg) && (chars[cur - 1] == ' ')) {
                --cur;
            }
            if (beg == cur) {
                throw new InvalidNameException("Invalid name: " + name);
            }
            return new String(chars, beg, cur - beg);
        }
        private String parseAttrValue() throws InvalidNameException {
            if (cur < len && chars[cur] == '#') {
                return parseBinaryAttrValue();
            } else if (cur < len && chars[cur] == '"') {
                return parseQuotedAttrValue();
            } else {
                return parseStringAttrValue();
            }
        }
        private String parseBinaryAttrValue() throws InvalidNameException {
            final int beg = cur;
            ++cur;                      
            while (cur < len &&
                   Character.isLetterOrDigit(chars[cur])) {
                ++cur;
            }
            return new String(chars, beg, cur - beg);
        }
        private String parseQuotedAttrValue() throws InvalidNameException {
            final int beg = cur;
            ++cur;                      
            while ((cur < len) && chars[cur] != '"') {
                if (chars[cur] == '\\') {
                    ++cur;              
                }
                ++cur;
            }
            if (cur >= len) {   
                throw new InvalidNameException("Invalid name: " + name);
            }
            ++cur       ;       
            return new String(chars, beg, cur - beg);
        }
        private String parseStringAttrValue() throws InvalidNameException {
            final int beg = cur;
            int esc = -1;       
            while ((cur < len) && !atTerminator()) {
                if (chars[cur] == '\\') {
                    ++cur;              
                    esc = cur;
                }
                ++cur;
            }
            if (cur > len) {            
                throw new InvalidNameException("Invalid name: " + name);
            }
            int end;
            for (end = cur; end > beg; end--) {
                if (!isWhitespace(chars[end - 1]) || (esc == end - 1)) {
                    break;
                }
            }
            return new String(chars, beg, end - beg);
        }
        private void consumeWhitespace() {
            while ((cur < len) && isWhitespace(chars[cur])) {
                ++cur;
            }
        }
        private boolean atTerminator() {
            return (cur < len &&
                    (chars[cur] == ',' ||
                     chars[cur] == ';' ||
                     chars[cur] == '+'));
        }
    }
    static class Rdn {
        private final Vector tvs = new Vector();
        void add(TypeAndValue tv) {
            int i;
            for (i = 0; i < tvs.size(); i++) {
                int diff = tv.compareTo(tvs.elementAt(i));
                if (diff == 0) {
                    return;             
                } else if (diff < 0) {
                    break;
                }
            }
            tvs.insertElementAt(tv, i);
        }
        public String toString() {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < tvs.size(); i++) {
                if (i > 0) {
                    buf.append('+');
                }
                buf.append(tvs.elementAt(i));
            }
            return new String(buf);
        }
        public boolean equals(Object obj) {
            return ((obj instanceof Rdn) &&
                    (compareTo(obj) == 0));
        }
        public int compareTo(Object obj) {
            Rdn that = (Rdn)obj;
            int minSize = Math.min(tvs.size(), that.tvs.size());
            for (int i = 0; i < minSize; i++) {
                TypeAndValue tv = (TypeAndValue)tvs.elementAt(i);
                int diff = tv.compareTo(that.tvs.elementAt(i));
                if (diff != 0) {
                    return diff;
                }
            }
            return (tvs.size() - that.tvs.size());      
        }
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < tvs.size(); i++) {
                hash += tvs.elementAt(i).hashCode();
            }
            return hash;
        }
        Attributes toAttributes() {
            Attributes attrs = new BasicAttributes(true);
            TypeAndValue tv;
            Attribute attr;
            for (int i = 0; i < tvs.size(); i++) {
                tv = (TypeAndValue) tvs.elementAt(i);
                if ((attr = attrs.get(tv.getType())) == null) {
                    attrs.put(tv.getType(), tv.getUnescapedValue());
                } else {
                    attr.add(tv.getUnescapedValue());
                }
            }
            return attrs;
        }
    }
    static class TypeAndValue {
        private final String type;
        private final String value;             
        private final boolean binary;
        private final boolean valueCaseSensitive;
        private String comparable = null;
        TypeAndValue(String type, String value, boolean valueCaseSensitive) {
            this.type = type;
            this.value = value;
            binary = value.startsWith("#");
            this.valueCaseSensitive = valueCaseSensitive;
        }
        public String toString() {
            return (type + "=" + value);
        }
        public int compareTo(Object obj) {
            TypeAndValue that = (TypeAndValue)obj;
            int diff = type.toUpperCase().compareTo(that.type.toUpperCase());
            if (diff != 0) {
                return diff;
            }
            if (value.equals(that.value)) {     
                return 0;
            }
            return getValueComparable().compareTo(that.getValueComparable());
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeAndValue)) {
                return false;
            }
            TypeAndValue that = (TypeAndValue)obj;
            return (type.equalsIgnoreCase(that.type) &&
                    (value.equals(that.value) ||
                     getValueComparable().equals(that.getValueComparable())));
        }
        public int hashCode() {
            return (type.toUpperCase().hashCode() +
                    getValueComparable().hashCode());
        }
        String getType() {
            return type;
        }
        Object getUnescapedValue() {
            return unescapeValue(value);
        }
        private String getValueComparable() {
            if (comparable != null) {
                return comparable;      
            }
            if (binary) {
                comparable = value.toUpperCase();
            } else {
                comparable = (String)unescapeValue(value);
                if (!valueCaseSensitive) {
                    comparable = comparable.toUpperCase(); 
                }
            }
            return comparable;
        }
        static String escapeValue(Object val) {
            return (val instanceof byte[])
                ? escapeBinaryValue((byte[])val)
                : escapeStringValue((String)val);
        }
        private static String escapeStringValue(String val) {
            final String escapees = ",=+<>#;\"\\";
            char[] chars = val.toCharArray();
            StringBuffer buf = new StringBuffer(2 * val.length());
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
                    buf.append('\\');
                }
                buf.append(c);
            }
            return new String(buf);
        }
        private static String escapeBinaryValue(byte[] val) {
            StringBuffer buf = new StringBuffer(1 + 2 * val.length);
            buf.append("#");
            for (int i = 0; i < val.length; i++) {
                byte b = val[i];
                buf.append(Character.forDigit(0xF & (b >>> 4), 16));
                buf.append(Character.forDigit(0xF & b, 16));
            }
            return (new String(buf)).toUpperCase();
        }
        static Object unescapeValue(String val) {
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
            StringBuffer buf = new StringBuffer(end - beg);
            int esc = -1; 
            for (int i = beg; i < end; i++) {
                if ((chars[i] == '\\') && (i + 1 < end)) {
                    if (!Character.isLetterOrDigit(chars[i + 1])) {
                        ++i;                    
                        buf.append(chars[i]);   
                        esc = i;
                    } else {
                        byte[] utf8 = getUtf8Octets(chars, i, end);
                        if (utf8.length > 0) {
                            try {
                                buf.append(new String(utf8, "UTF8"));
                            } catch (java.io.UnsupportedEncodingException e) {
                            }
                            i += utf8.length * 3 - 1;
                        } else {
                            throw new IllegalArgumentException(
                                "Not a valid attribute string value:" +
                                val +", improper usage of backslash");
                        }
                    }
                } else {
                    buf.append(chars[i]);       
                }
            }
            int len = buf.length();
            if (isWhitespace(buf.charAt(len - 1)) && esc != (end - 1)) {
                buf.setLength(len - 1);
            }
            return new String(buf);
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
                        "Illegal attribute value: #" + new String(chars));
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
    }
}
