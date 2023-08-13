public class ResourceRecord {
    static final int TYPE_A     =  1;
    static final int TYPE_NS    =  2;
    static final int TYPE_CNAME =  5;
    static final int TYPE_SOA   =  6;
    static final int TYPE_PTR   = 12;
    static final int TYPE_HINFO = 13;
    static final int TYPE_MX    = 15;
    static final int TYPE_TXT   = 16;
    static final int TYPE_AAAA  = 28;
    static final int TYPE_SRV   = 33;
    static final int TYPE_NAPTR = 35;
    static final int QTYPE_AXFR = 252;          
    static final int QTYPE_STAR = 255;          
    static final String rrTypeNames[] = {
        null, "A", "NS", null, null,
        "CNAME", "SOA", null, null, null,
        null, null, "PTR", "HINFO", null,
        "MX", "TXT", null, null, null,
        null, null, null, null, null,
        null, null, null, "AAAA", null,
        null, null, null, "SRV", null,
        "NAPTR"
    };
    static final int CLASS_INTERNET = 1;
    static final int CLASS_HESIOD   = 2;
    static final int QCLASS_STAR    = 255;      
    static final String rrClassNames[] = {
        null, "IN", null, null, "HS"
    };
    byte[] msg;                 
    int msgLen;                 
    boolean qSection;           
    int offset;                 
    int rrlen;                  
    DnsName name;               
    int rrtype;                 
    String rrtypeName;          
    int rrclass;                
    String rrclassName;         
    int ttl = 0;                
    int rdlen = 0;              
    Object rdata = null;        
    ResourceRecord(byte[] msg, int msgLen, int offset,
                   boolean qSection, boolean decodeRdata)
            throws InvalidNameException {
        this.msg = msg;
        this.msgLen = msgLen;
        this.offset = offset;
        this.qSection = qSection;
        decode(decodeRdata);
    }
    public String toString() {
        String text = name + " " + rrclassName + " " + rrtypeName;
        if (!qSection) {
            text += " " + ttl + " " +
                ((rdata != null) ? rdata : "[n/a]");
        }
        return text;
    }
    public DnsName getName() {
        return name;
    }
    public int size() {
        return rrlen;
    }
    public int getType() {
        return rrtype;
    }
    public int getRrclass() {
        return rrclass;
    }
    public Object getRdata() {
        return rdata;
    }
    public static String getTypeName(int rrtype) {
        return valueToName(rrtype, rrTypeNames);
    }
    public static int getType(String typeName) {
        return nameToValue(typeName, rrTypeNames);
    }
    public static String getRrclassName(int rrclass) {
        return valueToName(rrclass, rrClassNames);
    }
    public static int getRrclass(String className) {
        return nameToValue(className, rrClassNames);
    }
    private static String valueToName(int val, String[] names) {
        String name = null;
        if ((val > 0) && (val < names.length)) {
            name = names[val];
        } else if (val == QTYPE_STAR) {         
            name = "*";
        }
        if (name == null) {
            name = Integer.toString(val);
        }
        return name;
    }
    private static int nameToValue(String name, String[] names) {
        if (name.equals("")) {
            return -1;                          
        } else if (name.equals("*")) {
            return QTYPE_STAR;                  
        }
        if (Character.isDigit(name.charAt(0))) {
            try {
                return Integer.parseInt(name);
            } catch (NumberFormatException e) {
            }
        }
        for (int i = 1; i < names.length; i++) {
            if ((names[i] != null) &&
                    name.equalsIgnoreCase(names[i])) {
                return i;
            }
        }
        return -1;                              
    }
    public static int compareSerialNumbers(long s1, long s2) {
        long diff = s2 - s1;
        if (diff == 0) {
            return 0;
        } else if ((diff > 0 &&  diff <= 0x7FFFFFFF) ||
                   (diff < 0 && -diff >  0x7FFFFFFF)) {
            return -1;
        } else {
            return 1;
        }
    }
    private void decode(boolean decodeRdata) throws InvalidNameException {
        int pos = offset;       
        name = new DnsName();                           
        pos = decodeName(pos, name);
        rrtype = getUShort(pos);                        
        rrtypeName = (rrtype < rrTypeNames.length)
            ? rrTypeNames[rrtype]
            : null;
        if (rrtypeName == null) {
            rrtypeName = Integer.toString(rrtype);
        }
        pos += 2;
        rrclass = getUShort(pos);                       
        rrclassName = (rrclass < rrClassNames.length)
            ? rrClassNames[rrclass]
            : null;
        if (rrclassName == null) {
            rrclassName = Integer.toString(rrclass);
        }
        pos += 2;
        if (!qSection) {
            ttl = getInt(pos);                          
            pos += 4;
            rdlen = getUShort(pos);                     
            pos += 2;
            rdata = (decodeRdata ||                     
                     (rrtype == TYPE_SOA))
                ? decodeRdata(pos)
                : null;
            if (rdata instanceof DnsName) {
                rdata = rdata.toString();
            }
            pos += rdlen;
        }
        rrlen = pos - offset;
        msg = null;     
    }
    private int getUByte(int pos) {
        return (msg[pos] & 0xFF);
    }
    private int getUShort(int pos) {
        return (((msg[pos] & 0xFF) << 8) |
                (msg[pos + 1] & 0xFF));
    }
    private int getInt(int pos) {
        return ((getUShort(pos) << 16) | getUShort(pos + 2));
    }
    private long getUInt(int pos) {
        return (getInt(pos) & 0xffffffffL);
    }
    private DnsName decodeName(int pos) throws InvalidNameException {
        DnsName n = new DnsName();
        decodeName(pos, n);
        return n;
    }
    private int decodeName(int pos, DnsName n) throws InvalidNameException {
        if (msg[pos] == 0) {                            
            n.add(0, "");
            return (pos + 1);
        } else if ((msg[pos] & 0xC0) != 0) {            
            decodeName(getUShort(pos) & 0x3FFF, n);
            return (pos + 2);
        } else {                                        
            int len = msg[pos++];
            try {
                n.add(0, new String(msg, pos, len, "ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException e) {
            }
            return decodeName(pos + len, n);
        }
    }
    private Object decodeRdata(int pos) throws InvalidNameException {
        if (rrclass == CLASS_INTERNET) {
            switch (rrtype) {
            case TYPE_A:
                return decodeA(pos);
            case TYPE_AAAA:
                return decodeAAAA(pos);
            case TYPE_CNAME:
            case TYPE_NS:
            case TYPE_PTR:
                return decodeName(pos);
            case TYPE_MX:
                return decodeMx(pos);
            case TYPE_SOA:
                return decodeSoa(pos);
            case TYPE_SRV:
                return decodeSrv(pos);
            case TYPE_NAPTR:
                return decodeNaptr(pos);
            case TYPE_TXT:
                return decodeTxt(pos);
            case TYPE_HINFO:
                return decodeHinfo(pos);
            }
        }
        byte[] rd = new byte[rdlen];
        System.arraycopy(msg, pos, rd, 0, rdlen);
        return rd;
    }
    private String decodeMx(int pos) throws InvalidNameException {
        int preference = getUShort(pos);
        pos += 2;
        DnsName name = decodeName(pos);
        return (preference + " " + name);
    }
    private String decodeSoa(int pos) throws InvalidNameException {
        DnsName mname = new DnsName();
        pos = decodeName(pos, mname);
        DnsName rname = new DnsName();
        pos = decodeName(pos, rname);
        long serial = getUInt(pos);
        pos += 4;
        long refresh = getUInt(pos);
        pos += 4;
        long retry = getUInt(pos);
        pos += 4;
        long expire = getUInt(pos);
        pos += 4;
        long minimum = getUInt(pos);    
        pos += 4;
        return (mname + " " + rname + " " + serial + " " +
                refresh + " " + retry + " " + expire + " " + minimum);
    }
    private String decodeSrv(int pos) throws InvalidNameException {
        int priority = getUShort(pos);
        pos += 2;
        int weight =   getUShort(pos);
        pos += 2;
        int port =     getUShort(pos);
        pos += 2;
        DnsName target = decodeName(pos);
        return (priority + " " + weight + " " + port + " " + target);
    }
    private String decodeNaptr(int pos) throws InvalidNameException {
        int order = getUShort(pos);
        pos += 2;
        int preference = getUShort(pos);
        pos += 2;
        StringBuffer flags = new StringBuffer();
        pos += decodeCharString(pos, flags);
        StringBuffer services = new StringBuffer();
        pos += decodeCharString(pos, services);
        StringBuffer regexp = new StringBuffer(rdlen);
        pos += decodeCharString(pos, regexp);
        DnsName replacement = decodeName(pos);
        return (order + " " + preference + " " + flags + " " +
                services + " " + regexp + " " + replacement);
    }
    private String decodeTxt(int pos) {
        StringBuffer buf = new StringBuffer(rdlen);
        int end = pos + rdlen;
        while (pos < end) {
            pos += decodeCharString(pos, buf);
            if (pos < end) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }
    private String decodeHinfo(int pos) {
        StringBuffer buf = new StringBuffer(rdlen);
        pos += decodeCharString(pos, buf);
        buf.append(' ');
        pos += decodeCharString(pos, buf);
        return buf.toString();
    }
    private int decodeCharString(int pos, StringBuffer buf) {
        int start = buf.length();       
        int len = getUByte(pos++);      
        boolean quoted = (len == 0);    
        for (int i = 0; i < len; i++) {
            int c = getUByte(pos++);
            quoted |= (c == ' ');
            if ((c == '\\') || (c == '"')) {
                quoted = true;
                buf.append('\\');
            }
            buf.append((char) c);
        }
        if (quoted) {
            buf.insert(start, '"');
            buf.append('"');
        }
        return (len + 1);       
    }
    private String decodeA(int pos) {
        return ((msg[pos] & 0xff) + "." +
                (msg[pos + 1] & 0xff) + "." +
                (msg[pos + 2] & 0xff) + "." +
                (msg[pos + 3] & 0xff));
    }
    private String decodeAAAA(int pos) {
        int[] addr6 = new int[8];  
        for (int i = 0; i < 8; i++) {
            addr6[i] = getUShort(pos);
            pos += 2;
        }
        int curBase = -1;
        int curLen = 0;
        int bestBase = -1;
        int bestLen = 0;
        for (int i = 0; i < 8; i++) {
            if (addr6[i] == 0) {
                if (curBase == -1) {    
                    curBase = i;
                    curLen = 1;
                } else {                
                    ++curLen;
                    if ((curLen >= 2) && (curLen > bestLen)) {
                        bestBase = curBase;
                        bestLen = curLen;
                    }
                }
            } else {                    
                curBase = -1;
            }
        }
        if (bestBase == 0) {
            if ((bestLen == 6) ||
                    ((bestLen == 7) && (addr6[7] > 1))) {
                return ("::" + decodeA(pos - 4));
            } else if ((bestLen == 5) && (addr6[5] == 0xffff)) {
                return ("::ffff:" + decodeA(pos - 4));
            }
        }
        boolean compress = (bestBase != -1);
        StringBuffer buf = new StringBuffer(40);
        if (bestBase == 0) {
            buf.append(':');
        }
        for (int i = 0; i < 8; i++) {
            if (!compress || (i < bestBase) || (i >= bestBase + bestLen)) {
                buf.append(Integer.toHexString(addr6[i]));
                if (i < 7) {
                    buf.append(':');
                }
            } else if (compress && (i == bestBase)) {  
                buf.append(':');
            }
        }
        return buf.toString();
    }
}
