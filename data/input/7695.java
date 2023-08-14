public final class DnsName implements Name {
    private String domain = "";
    private ArrayList labels = new ArrayList();
    private short octets = 1;
    public DnsName() {
    }
    public DnsName(String name) throws InvalidNameException {
        parse(name);
    }
    private DnsName(DnsName n, int beg, int end) {
        int b = n.size() - end;
        int e = n.size() - beg;
        labels.addAll(n.labels.subList(b, e));
        if (size() == n.size()) {
            domain = n.domain;
            octets = n.octets;
        } else {
            Iterator iter = labels.iterator();
            while (iter.hasNext()) {
                String label = (String) iter.next();
                if (label.length() > 0) {
                    octets += (short) (label.length() + 1);
                }
            }
        }
    }
    public String toString() {
        if (domain == null) {
            StringBuffer buf = new StringBuffer();
            Iterator iter = labels.iterator();
            while (iter.hasNext()) {
                String label = (String) iter.next();
                if (buf.length() > 0 || label.length() == 0) {
                    buf.append('.');
                }
                escape(buf, label);
            }
            domain = buf.toString();
        }
        return domain;
    }
    public boolean isHostName() {
        Iterator iter = labels.iterator();
        while (iter.hasNext()) {
            if (!isHostNameLabel((String) iter.next())) {
                return false;
            }
        }
        return true;
    }
    public short getOctets() {
        return octets;
    }
    public int size() {
        return labels.size();
    }
    public boolean isEmpty() {
        return (size() == 0);
    }
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < size(); i++) {
            h = 31 * h + getKey(i).hashCode();
        }
        return h;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof Name) || (obj instanceof CompositeName)) {
            return false;
        }
        Name n = (Name) obj;
        return ((size() == n.size()) &&         
                (compareTo(obj) == 0));
    }
    public int compareTo(Object obj) {
        Name n = (Name) obj;
        return compareRange(0, size(), n);      
    }
    public boolean startsWith(Name n) {
        return ((size() >= n.size()) &&
                (compareRange(0, n.size(), n) == 0));
    }
    public boolean endsWith(Name n) {
        return ((size() >= n.size()) &&
                (compareRange(size() - n.size(), size(), n) == 0));
    }
    public String get(int pos) {
        if (pos < 0 || pos >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int i = size() - pos - 1;       
        return (String) labels.get(i);
    }
    public Enumeration getAll() {
        return new Enumeration() {
            int pos = 0;
            public boolean hasMoreElements() {
                return (pos < size());
            }
            public Object nextElement() {
                if (pos < size()) {
                    return get(pos++);
                }
                throw new java.util.NoSuchElementException();
            }
        };
    }
    public Name getPrefix(int pos) {
        return new DnsName(this, 0, pos);
    }
    public Name getSuffix(int pos) {
        return new DnsName(this, pos, size());
    }
    public Object clone() {
        return new DnsName(this, 0, size());
    }
    public Object remove(int pos) {
        if (pos < 0 || pos >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int i = size() - pos - 1;     
        String label = (String) labels.remove(i);
        int len = label.length();
        if (len > 0) {
            octets -= (short) (len + 1);
        }
        domain = null;          
        return label;
    }
    public Name add(String comp) throws InvalidNameException {
        return add(size(), comp);
    }
    public Name add(int pos, String comp) throws InvalidNameException {
        if (pos < 0 || pos > size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int len = comp.length();
        if ((pos > 0 && len == 0) ||
            (pos == 0 && hasRootLabel())) {
                throw new InvalidNameException(
                        "Empty label must be the last label in a domain name");
        }
        if (len > 0) {
            if (octets + len + 1 >= 256) {
                throw new InvalidNameException("Name too long");
            }
            octets += (short) (len + 1);
        }
        int i = size() - pos;   
        verifyLabel(comp);
        labels.add(i, comp);
        domain = null;          
        return this;
    }
    public Name addAll(Name suffix) throws InvalidNameException {
        return addAll(size(), suffix);
    }
    public Name addAll(int pos, Name n) throws InvalidNameException {
        if (n instanceof DnsName) {
            DnsName dn = (DnsName) n;
            if (dn.isEmpty()) {
                return this;
            }
            if ((pos > 0 && dn.hasRootLabel()) ||
                (pos == 0 && hasRootLabel())) {
                    throw new InvalidNameException(
                        "Empty label must be the last label in a domain name");
            }
            short newOctets = (short) (octets + dn.octets - 1);
            if (newOctets > 255) {
                throw new InvalidNameException("Name too long");
            }
            octets = newOctets;
            int i = size() - pos;       
            labels.addAll(i, dn.labels);
            if (isEmpty()) {
                domain = dn.domain;
            } else if (domain == null || dn.domain == null) {
                domain = null;
            } else if (pos == 0) {
                domain += (dn.domain.equals(".") ? "" : ".") + dn.domain;
            } else if (pos == size()) {
                domain = dn.domain + (domain.equals(".") ? "" : ".") + domain;
            } else {
                domain = null;
            }
        } else if (n instanceof CompositeName) {
            n = (DnsName) n;            
        } else {                
            for (int i = n.size() - 1; i >= 0; i--) {
                add(pos, n.get(i));
            }
        }
        return this;
    }
    boolean hasRootLabel() {
        return (!isEmpty() &&
                get(0).equals(""));
    }
    private int compareRange(int beg, int end, Name n) {
        if (n instanceof CompositeName) {
            n = (DnsName) n;                    
        }
        int minSize = Math.min(end - beg, n.size());
        for (int i = 0; i < minSize; i++) {
            String label1 = get(i + beg);
            String label2 = n.get(i);
            int j = size() - (i + beg) - 1;     
            int c = compareLabels(label1, label2);
            if (c != 0) {
                return c;
            }
        }
        return ((end - beg) - n.size());        
    }
    String getKey(int i) {
        return keyForLabel(get(i));
    }
    private void parse(String name) throws InvalidNameException {
        StringBuffer label = new StringBuffer();        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\\') {                    
                c = getEscapedOctet(name, i++);
                if (isDigit(name.charAt(i))) {  
                    i += 2;                     
                }
                label.append(c);
            } else if (c != '.') {              
                label.append(c);
            } else {                            
                add(0, label.toString());       
                label.delete(0, i);             
            }
        }
        if (!name.equals("") && !name.equals(".")) {
            add(0, label.toString());
        }
        domain = name;          
    }
    private static char getEscapedOctet(String name, int pos)
                                                throws InvalidNameException {
        try {
            char c1 = name.charAt(++pos);
            if (isDigit(c1)) {          
                char c2 = name.charAt(++pos);
                char c3 = name.charAt(++pos);
                if (isDigit(c2) && isDigit(c3)) {
                    return (char)
                        ((c1 - '0') * 100 + (c2 - '0') * 10 + (c3 - '0'));
                } else {
                    throw new InvalidNameException(
                            "Invalid escape sequence in " + name);
                }
            } else {                    
                return c1;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidNameException(
                    "Invalid escape sequence in " + name);
        }
    }
    private static void verifyLabel(String label) throws InvalidNameException {
        if (label.length() > 63) {
            throw new InvalidNameException(
                    "Label exceeds 63 octets: " + label);
        }
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if ((c & 0xFF00) != 0) {
                throw new InvalidNameException(
                        "Label has two-byte char: " + label);
            }
        }
    }
    private static boolean isHostNameLabel(String label) {
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (!isHostNameChar(c)) {
                return false;
            }
        }
        return !(label.startsWith("-") || label.endsWith("-"));
    }
    private static boolean isHostNameChar(char c) {
        return (c == '-' ||
                c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9');
    }
    private static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }
    private static void escape(StringBuffer buf, String label) {
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c == '.' || c == '\\') {
                buf.append('\\');
            }
            buf.append(c);
        }
    }
    private static int compareLabels(String label1, String label2) {
        int min = Math.min(label1.length(), label2.length());
        for (int i = 0; i < min; i++) {
            char c1 = label1.charAt(i);
            char c2 = label2.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 'a' - 'A';                        
            }
            if (c2 >= 'A' && c2 <= 'Z') {
                c2 += 'a' - 'A';                        
            }
            if (c1 != c2) {
                return (c1 - c2);
            }
        }
        return (label1.length() - label2.length());     
    }
    private static String keyForLabel(String label) {
        StringBuffer buf = new StringBuffer(label.length());
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 'a' - 'A';                         
            }
            buf.append(c);
        }
        return buf.toString();
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeObject(toString());
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        try {
            parse((String) s.readObject());
        } catch (InvalidNameException e) {
            throw new java.io.StreamCorruptedException(
                    "Invalid name: " + domain);
        }
    }
    private static final long serialVersionUID = 7040187611324710271L;
}
