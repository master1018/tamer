public class GeneralName {
    public static final int OTHER_NAME = 0;
    public static final int RFC822_NAME = 1;
    public static final int DNS_NAME = 2;
    public static final int X400_ADDR = 3;
    public static final int DIR_NAME = 4;
    public static final int EDIP_NAME = 5;
    public static final int UR_ID = 6;
    public static final int IP_ADDR = 7;
    public static final int REG_ID = 8;
    private static ASN1Type[] nameASN1 = new ASN1Type[9];
    static {
        nameASN1[OTHER_NAME] = OtherName.ASN1;
        nameASN1[RFC822_NAME] = ASN1StringType.IA5STRING;
        nameASN1[DNS_NAME] = ASN1StringType.IA5STRING;
        nameASN1[UR_ID] = ASN1StringType.IA5STRING;
        nameASN1[X400_ADDR] = ORAddress.ASN1;
        nameASN1[DIR_NAME] = Name.ASN1;
        nameASN1[EDIP_NAME] = EDIPartyName.ASN1;
        nameASN1[IP_ADDR] = ASN1OctetString.getInstance();
        nameASN1[REG_ID] = ASN1Oid.getInstance();
    }
    private int tag;
    private Object name;
    private byte[] encoding;
    private byte[] name_encoding;
    public GeneralName(int tag, String name) throws IOException {
        if (name == null) {
            throw new IOException(Messages.getString("security.28")); 
        }
        this.tag = tag;
        switch (tag) {
            case OTHER_NAME :
            case X400_ADDR :
            case EDIP_NAME :
                throw new IOException( Messages.getString("security.180", tag )); 
            case DNS_NAME :
                checkDNS(name);
                this.name = name;
                break;
            case UR_ID :
                checkURI(name);
                this.name = name;
                break;
            case RFC822_NAME :
                this.name = name;
                break;
            case REG_ID:
                this.name = oidStrToInts(name);
                break;
            case DIR_NAME :
                this.name = new Name(name);
                break;
            case IP_ADDR :
                this.name = ipStrToBytes(name);
                break;
            default:
                throw new IOException(Messages.getString("security.181", tag)); 
        }
    }
    public GeneralName(OtherName name) {
        this.tag = OTHER_NAME;
        this.name = name;
    }
    public GeneralName(ORAddress name) {
        this.tag = X400_ADDR;
        this.name = name;
    }
    public GeneralName(Name name) {
        this.tag = DIR_NAME;
        this.name = name;
    }
    public GeneralName(EDIPartyName name) {
        this.tag = EDIP_NAME;
        this.name = name;
    }
    public GeneralName(byte[] name) throws IllegalArgumentException {
        int length = name.length;
        if (length != 4 && length != 8 && length != 16 && length != 32) {
            throw new IllegalArgumentException(
                    Messages.getString("security.182")); 
        }
        this.tag = IP_ADDR;
        this.name = new byte[name.length];
        System.arraycopy(name, 0, this.name, 0, name.length);
    }
    public GeneralName(int tag, byte[] name) 
                                    throws IOException {
        if (name == null) {
            throw new NullPointerException(Messages.getString("security.28")); 
        }
        if ((tag < 0) || (tag > 8)) {
            throw new IOException(Messages.getString("security.183", tag)); 
        }
        this.tag = tag;
        this.name_encoding = new byte[name.length];
        System.arraycopy(name, 0, this.name_encoding, 0, name.length);
        this.name = nameASN1[tag].decode(this.name_encoding);
    }
    public int getTag() {
        return tag;
    }
    public Object getName() {
        return name;
    }
    public boolean equals(Object _gname) {
        if (!(_gname instanceof GeneralName)) {
            return false;
        }
        GeneralName gname = (GeneralName) _gname;
        if (this.tag != gname.tag) {
            return false;
        }
        switch(tag) {
            case RFC822_NAME:
            case DNS_NAME:
            case UR_ID:
                return ((String) name).equalsIgnoreCase(
                        (String) gname.getName());
            case REG_ID:
                return Arrays.equals((int[]) name, (int[]) gname.name);
            case IP_ADDR: 
                return Arrays.equals((byte[]) name, (byte[]) gname.name);
            case DIR_NAME: 
            case X400_ADDR:
            case OTHER_NAME:
            case EDIP_NAME:
                return Arrays.equals(getEncoded(), gname.getEncoded());
            default:
        }
        return false;
    }
	public int hashCode() {
		switch(tag) {
	        case RFC822_NAME:
	        case DNS_NAME:
	        case UR_ID:
	        case REG_ID:
	        case IP_ADDR: 
	            return name.hashCode();
	        case DIR_NAME: 
	        case X400_ADDR:
	        case OTHER_NAME:
	        case EDIP_NAME:
	            return getEncoded().hashCode();
	        default:
	            return super.hashCode();
		}
	}
    public boolean isAcceptable(GeneralName gname) {
        if (this.tag != gname.getTag()) {
            return false;
        }
        switch (this.tag) {
            case RFC822_NAME:
                return ((String) gname.getName()).toLowerCase()
                    .endsWith(((String) name).toLowerCase());
            case DNS_NAME:
                String dns = (String) name;
                String _dns = (String) gname.getName();
                if (dns.equalsIgnoreCase(_dns)) {
                    return true;
                } else {
                    return _dns.toLowerCase().endsWith("." + dns.toLowerCase()); 
                }
            case UR_ID:
                String uri = (String) name;
                int begin = uri.indexOf(":
                int end = uri.indexOf('/', begin);
                String host = (end == -1) 
                                ? uri.substring(begin)
                                : uri.substring(begin, end);
                uri = (String) gname.getName();
                begin = uri.indexOf(":
                end = uri.indexOf('/', begin);
                String _host = (end == -1) 
                                ? uri.substring(begin)
                                : uri.substring(begin, end);
                if (host.startsWith(".")) { 
                    return _host.toLowerCase().endsWith(host.toLowerCase());
                } else {
                    return host.equalsIgnoreCase(_host);
                }
            case IP_ADDR: 
                byte[] address = (byte[]) name;
                byte[] _address = (byte[]) gname.getName();
                int length = address.length;
                int _length = _address.length;
                if (length == _length) {
                    return Arrays.equals(address, _address);
                } else if (length == 2*_length) {
                    for (int i=0; i<_address.length; i++) {
                        if ((_address[i] < address[i]) 
                                || (_address[i] > address[i+_length])) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            case DIR_NAME: 
            case X400_ADDR:
            case OTHER_NAME:
            case EDIP_NAME:
            case REG_ID:
                return Arrays.equals(getEncoded(), gname.getEncoded());
            default:
        }
        return true;
    }
    public List getAsList() {
        ArrayList result = new ArrayList();
        result.add(new Integer(tag));
        switch (tag) {
            case OTHER_NAME:
                result.add(((OtherName) name).getEncoded());
                break;
            case RFC822_NAME:
            case DNS_NAME:
            case UR_ID:
                result.add(name); 
                break;
            case REG_ID:
                result.add(ObjectIdentifier.toString((int[]) name));
                break;
            case X400_ADDR:
                result.add(((ORAddress) name).getEncoded());
                break;
            case DIR_NAME: 
                result.add(((Name) name).getName(X500Principal.RFC2253));
                break;
            case EDIP_NAME:
                result.add(((EDIPartyName) name).getEncoded());
                break;
            case IP_ADDR: 
                result.add(ipBytesToStr((byte[]) name));
                break;
            default:
        }
        return Collections.unmodifiableList(result);
    }
    private String getBytesAsString(byte[] data) {
        String result = ""; 
        for (int i=0; i<data.length; i++) {
            String tail = Integer.toHexString(0x00ff & data[i]);
            if (tail.length() == 1) {
                tail = "0" + tail;  
            }
            result += tail + " "; 
        }
        return result;
    }
    public String toString() {
        String result = ""; 
        switch (tag) {
            case OTHER_NAME:
                result = "otherName[0]: "  
                         + getBytesAsString(getEncoded());
                break;
            case RFC822_NAME:
                result = "rfc822Name[1]: " + name; 
                break;
            case DNS_NAME:
                result = "dNSName[2]: " + name; 
                break;
            case UR_ID:
                result = "uniformResourceIdentifier[6]: " + name; 
                break;
            case REG_ID:
                result = "registeredID[8]: " + ObjectIdentifier.toString((int[]) name); 
                break;
            case X400_ADDR:
                result = "x400Address[3]: "  
                         + getBytesAsString(getEncoded());
                break;
            case DIR_NAME: 
                result = "directoryName[4]: "  
                         + ((Name) name).getName(X500Principal.RFC2253);
                break;
            case EDIP_NAME:
                result = "ediPartyName[5]: "  
                         + getBytesAsString(getEncoded());
                break;
            case IP_ADDR: 
                result = "iPAddress[7]: " + ipBytesToStr((byte[]) name); 
                break;
            default:
        }
        return result;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public byte[] getEncodedName() {
        if (name_encoding == null) {
            name_encoding = nameASN1[tag].encode(name);
        }
        return name_encoding;
    }
    public static void checkDNS(String dns) throws IOException {
        byte[] bytes = dns.toLowerCase().getBytes("UTF-8"); 
        boolean first_letter = true;
        for (int i=0; i<bytes.length; i++) {
            byte ch = bytes[i];
            if (first_letter) {
                if ((bytes.length > 2) && (ch == '*') && (bytes[1] == '.')) {
                    first_letter = false;
                    continue;
                }
                if ((ch > 'z' || ch < 'a') && (ch < '0' || ch > '9')) {
                    throw new IOException(Messages.getString("security.184", 
                            (char)ch, dns));
                }
                first_letter = false;
                continue;
            }
            if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || (ch == '-') || (ch == '.'))) {
                throw new IOException(Messages.getString("security.185", dns)); 
            }
            if (ch == '.') {
                if (bytes[i-1] == '-') {
                    throw new IOException(
                            Messages.getString("security.186", dns)); 
                }
                first_letter = true;
            }
        }
    }
    public static void checkURI(String uri) throws IOException {
        try {
            URI ur = new URI(uri);
            if ((ur.getScheme() == null) 
                    || (ur.getRawSchemeSpecificPart().length() == 0)) {
                throw new IOException(Messages.getString("security.187", uri)); 
            }
            if (!ur.isAbsolute()) {
                throw new IOException(Messages.getString("security.188", uri)); 
            }
        } catch (URISyntaxException e) {
            throw (IOException) new IOException(
                    Messages.getString("security.189", uri)).initCause(e);
        }
    }
    public static int[] oidStrToInts(String oid) throws IOException {
        byte[] bytes = oid.getBytes("UTF-8"); 
        if (bytes[bytes.length-1] == '.') {
            throw new IOException(Messages.getString("security.56", oid)); 
        }
        int[] result = new int[bytes.length/2+1]; 
        int number = 0; 
        for (int i=0; i<bytes.length; i++) {
            int value = 0;
            int pos = i;
            while ((i < bytes.length) && (bytes[i] >= '0')
                        && (bytes[i] <= '9')) {
                value = 10 * value + (bytes[i++] - 48);
            }
            if (i == pos) {
                throw new IOException(Messages.getString("security.56", oid)); 
            }
            result[number++] = value;
            if (i >= bytes.length) {
                break;
            }
            if (bytes[i] != '.') {
                throw new IOException(Messages.getString("security.56", oid)); 
            }
        }
        if (number < 2) {
            throw new IOException(Messages.getString("security.18A", oid));
        }
        int[] res = new int[number];
        for (int i=0; i<number; i++) {
            res[i] = result[i];
        }
        return res;
    }
    public static byte[] ipStrToBytes(String ip) throws IOException {
        boolean isIPv4 = (ip.indexOf('.') > 0);
        int num_components = (isIPv4) ? 4 : 16;
        if (ip.indexOf('/') > 0) {
            num_components *= 2; 
        }
        byte[] result = new byte[num_components];
        byte[] ip_bytes = ip.getBytes("UTF-8"); 
        int component = 0;
        boolean reading_second_bound = false;
        if (isIPv4) {
            int i = 0;
            while (i < ip_bytes.length) {
                int digits = 0;
                int value = 0;
                while ((i < ip_bytes.length) && (ip_bytes[i] >= '0')
                        && (ip_bytes[i] <= '9')) {
                    digits++;
                    if (digits > 3) {
                        throw new IOException(Messages.getString("security.18B", ip)); 
                    }
                    value = 10 * value + (ip_bytes[i] - 48);
                    i++;
                }
                if (digits == 0) {
                    throw new IOException(Messages.getString("security.18C", ip));
                }
                result[component] = (byte) value;
                component++;
                if (i >= ip_bytes.length) {
                    break;
                }
                if ((ip_bytes[i] != '.' && ip_bytes[i] != '/')) {
                    throw new IOException(Messages.getString("security.18C", ip)); 
                }
                if (ip_bytes[i] == '/') {
                    if (reading_second_bound) {
                        throw new IOException(Messages.getString("security.18C", ip)); 
                    }
                    if (component != 4) {
                        throw new IOException(Messages.getString("security.18D", ip)); 
                    }
                    reading_second_bound = true;
                }
                if (component > ((reading_second_bound) ? 7 : 3)) {
                    throw new IOException(Messages.getString("security.18D", ip)); 
                }
                i++;
            }
            if (component != num_components) {
                throw new IOException(Messages.getString("security.18D", ip)); 
            }
        } else {
            if (ip_bytes.length != 39 && ip_bytes.length != 79) {
                throw new IOException(Messages.getString("security.18E", ip)); 
            }
            int value = 0;
            boolean second_hex = false;
            boolean expect_delimiter = false;
            for (int i=0; i<ip_bytes.length; i++) {
                byte bytik = ip_bytes[i];
                if ((bytik >= '0') && (bytik <= '9')) {
                    value = (bytik - 48); 
                } else if ((bytik >= 'A') && (bytik <= 'F')) {
                    value = (bytik - 55); 
                } else if ((bytik >= 'a') && (bytik <= 'f')) {
                    value = (bytik - 87); 
                } else if (second_hex) {
                    throw new IOException(Messages.getString("security.18E", ip)); 
                } else if ((bytik == ':') || (bytik == '/')) {
                    if (component % 2 == 1) {
                        throw new IOException(Messages.getString("security.18E", ip)); 
                    }
                    if (bytik == '/') {
                        if (reading_second_bound) {
                            throw new IOException(
                                    Messages.getString("security.18E", ip)); 
                        }
                        if (component != 16) {
                            throw new IOException(Messages.getString("security.18F", ip)); 
                        }
                        reading_second_bound = true;
                    }
                    expect_delimiter = false;
                    continue;
                } else {
                    throw new IOException(Messages.getString("security.18E", ip)); 
                }
                if (expect_delimiter) { 
                    throw new IOException(Messages.getString("security.18E", ip)); 
                }
                if (!second_hex) {
                    result[component] = (byte) (value << 4);
                    second_hex = true;
                } else {
                    result[component] = (byte)
                        ((result[component] & 0xFF) | value);
                    expect_delimiter = (component % 2 == 1);
                    second_hex = false;
                    component++;
                }
            }
            if (second_hex || (component % 2 == 1)) {
                throw new IOException(Messages.getString("security.18E", ip)); 
            }
        }
        return result;
    }
    public static String ipBytesToStr(byte[] ip) {
        String result = ""; 
        if (ip.length < 9) { 
            for (int i=0; i<ip.length; i++) {
                result += Integer.toString(ip[i] & 0xff);
                if (i != ip.length-1) {
                    result += (i == 3) ? "/": "."; 
                }
            }
        } else {
            for (int i=0; i<ip.length; i++) {
                result += Integer.toHexString(0x00ff & ip[i]);
                if ((i % 2 != 0) && (i != ip.length-1)) {
                    result += (i == 15) ? "/": ":"; 
                }
            }
        }
        return result;
    }
    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
           new ASN1Implicit(0, OtherName.ASN1), 
           new ASN1Implicit(1, ASN1StringType.IA5STRING), 
           new ASN1Implicit(2, ASN1StringType.IA5STRING),
           new ASN1Implicit(3, ORAddress.ASN1),
           new ASN1Implicit(4, Name.ASN1),
           new ASN1Implicit(5, EDIPartyName.ASN1),
           new ASN1Implicit(6, ASN1StringType.IA5STRING),
           new ASN1Implicit(7, ASN1OctetString.getInstance()),
           new ASN1Implicit(8, ASN1Oid.getInstance()) }) {
        public Object getObjectToEncode(Object value) {
            return ((GeneralName) value).name;
        }
        public int getIndex(java.lang.Object object) {
            return  ((GeneralName) object).tag;
        }
        public Object getDecodedObject(BerInputStream in) throws IOException {
            GeneralName result;
            switch (in.choiceIndex) {
                case OTHER_NAME: 
                    result = new GeneralName((OtherName) in.content);
                    break;
                case RFC822_NAME: 
                case DNS_NAME: 
                    result = new GeneralName(in.choiceIndex, (String) in.content);
                    break;
                case X400_ADDR:
                    result = new GeneralName((ORAddress) in.content);
                    break;
                case DIR_NAME: 
                    result = new GeneralName((Name) in.content);
                    break;
                case EDIP_NAME: 
                    result = new GeneralName((EDIPartyName) in.content);
                    break;
                case UR_ID: 
                    String uri = (String) in.content;
                    if (uri.indexOf(":") == -1) { 
                        throw new IOException(
                            Messages.getString("security.190", uri)); 
                    }
                    result = new GeneralName(in.choiceIndex, uri);
                    break;
                case IP_ADDR: 
                    result = new GeneralName((byte[]) in.content);
                    break;
                case REG_ID: 
                    result = new GeneralName(in.choiceIndex, 
                            ObjectIdentifier.toString((int[]) in.content));
                    break;
                default:
                    throw new IOException(Messages.getString("security.191", in.choiceIndex)); 
            }
            result.encoding = in.getEncoded();
            return result;
        }
    };
}
