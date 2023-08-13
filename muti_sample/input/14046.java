public class AVA implements DerEncoder {
    private static final Debug debug = Debug.getInstance("x509", "\t[AVA]");
    private static final boolean PRESERVE_OLD_DC_ENCODING =
        AccessController.doPrivileged(new GetBooleanAction
            ("com.sun.security.preserveOldDCEncoding"));
    final static int DEFAULT = 1;
    final static int RFC1779 = 2;
    final static int RFC2253 = 3;
    final ObjectIdentifier oid;
    final DerValue value;
    private static final String specialChars = ",+=\n<>#;";
    private static final String specialChars2253 = ",+\"\\<>;";
    private static final String specialCharsAll = ",=\n+<>#;\\\" ";
    private static final String hexDigits = "0123456789ABCDEF";
    public AVA(ObjectIdentifier type, DerValue val) {
        if ((type == null) || (val == null)) {
            throw new NullPointerException();
        }
        oid = type;
        value = val;
    }
    AVA(Reader in) throws IOException {
        this(in, DEFAULT);
    }
    AVA(Reader in, Map<String, String> keywordMap) throws IOException {
        this(in, DEFAULT, keywordMap);
    }
    AVA(Reader in, int format) throws IOException {
        this(in, format, Collections.<String, String>emptyMap());
    }
    AVA(Reader in, int format, Map<String, String> keywordMap)
        throws IOException {
        StringBuilder   temp = new StringBuilder();
        int             c;
        while (true) {
            c = readChar(in, "Incorrect AVA format");
            if (c == '=') {
                break;
            }
            temp.append((char)c);
        }
        oid = AVAKeyword.getOID(temp.toString(), format, keywordMap);
        temp.setLength(0);
        if (format == RFC2253) {
            c = in.read();
            if (c == ' ') {
                throw new IOException("Incorrect AVA RFC2253 format - " +
                                        "leading space must be escaped");
            }
        } else {
            do {
                c = in.read();
            } while ((c == ' ') || (c == '\n'));
        }
        if (c == -1) {
            value = new DerValue("");
            return;
        }
        if (c == '#') {
            value = parseHexString(in, format);
        } else if ((c == '"') && (format != RFC2253)) {
            value = parseQuotedString(in, temp);
        } else {
            value = parseString(in, c, format, temp);
        }
    }
    public ObjectIdentifier getObjectIdentifier() {
        return oid;
    }
    public DerValue getDerValue() {
        return value;
    }
    public String getValueString() {
        try {
            String s = value.getAsString();
            if (s == null) {
                throw new RuntimeException("AVA string is null");
            }
            return s;
        } catch (IOException e) {
            throw new RuntimeException("AVA error: " + e, e);
        }
    }
    private static DerValue parseHexString
        (Reader in, int format) throws IOException {
        int c;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = 0;
        int cNdx = 0;
        while (true) {
            c = in.read();
            if (isTerminator(c, format)) {
                break;
            }
            int cVal = hexDigits.indexOf(Character.toUpperCase((char)c));
            if (cVal == -1) {
                throw new IOException("AVA parse, invalid hex " +
                                              "digit: "+ (char)c);
            }
            if ((cNdx % 2) == 1) {
                b = (byte)((b * 16) + (byte)(cVal));
                baos.write(b);
            } else {
                b = (byte)(cVal);
            }
            cNdx++;
        }
        if (cNdx == 0) {
            throw new IOException("AVA parse, zero hex digits");
        }
        if (cNdx % 2 == 1) {
            throw new IOException("AVA parse, odd number of hex digits");
        }
        return new DerValue(baos.toByteArray());
    }
    private DerValue parseQuotedString
        (Reader in, StringBuilder temp) throws IOException {
        int c = readChar(in, "Quoted string did not end in quote");
        List<Byte> embeddedHex = new ArrayList<Byte>();
        boolean isPrintableString = true;
        while (c != '"') {
            if (c == '\\') {
                c = readChar(in, "Quoted string did not end in quote");
                Byte hexByte = null;
                if ((hexByte = getEmbeddedHexPair(c, in)) != null) {
                    isPrintableString = false;
                    embeddedHex.add(hexByte);
                    c = in.read();
                    continue;
                }
                if (c != '\\' && c != '"' &&
                    specialChars.indexOf((char)c) < 0) {
                    throw new IOException
                        ("Invalid escaped character in AVA: " +
                        (char)c);
                }
            }
            if (embeddedHex.size() > 0) {
                String hexString = getEmbeddedHexString(embeddedHex);
                temp.append(hexString);
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char)c);
            temp.append((char)c);
            c = readChar(in, "Quoted string did not end in quote");
        }
        if (embeddedHex.size() > 0) {
            String hexString = getEmbeddedHexString(embeddedHex);
            temp.append(hexString);
            embeddedHex.clear();
        }
        do {
            c = in.read();
        } while ((c == '\n') || (c == ' '));
        if (c != -1) {
            throw new IOException("AVA had characters other than "
                    + "whitespace after terminating quote");
        }
        if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) ||
            (this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) &&
                PRESERVE_OLD_DC_ENCODING == false)) {
            return new DerValue(DerValue.tag_IA5String,
                                        temp.toString().trim());
        } else if (isPrintableString) {
            return new DerValue(temp.toString().trim());
        } else {
            return new DerValue(DerValue.tag_UTF8String,
                                        temp.toString().trim());
        }
    }
    private DerValue parseString
        (Reader in, int c, int format, StringBuilder temp) throws IOException {
        List<Byte> embeddedHex = new ArrayList<Byte>();
        boolean isPrintableString = true;
        boolean escape = false;
        boolean leadingChar = true;
        int spaceCount = 0;
        do {
            escape = false;
            if (c == '\\') {
                escape = true;
                c = readChar(in, "Invalid trailing backslash");
                Byte hexByte = null;
                if ((hexByte = getEmbeddedHexPair(c, in)) != null) {
                    isPrintableString = false;
                    embeddedHex.add(hexByte);
                    c = in.read();
                    leadingChar = false;
                    continue;
                }
                if ((format == DEFAULT &&
                        specialCharsAll.indexOf((char)c) == -1) ||
                    (format == RFC1779  &&
                        specialChars.indexOf((char)c) == -1 &&
                        c != '\\' && c != '\"')) {
                    throw new IOException
                        ("Invalid escaped character in AVA: '" +
                        (char)c + "'");
                } else if (format == RFC2253) {
                    if (c == ' ') {
                        if (!leadingChar && !trailingSpace(in)) {
                                throw new IOException
                                        ("Invalid escaped space character " +
                                        "in AVA.  Only a leading or trailing " +
                                        "space character can be escaped.");
                        }
                    } else if (c == '#') {
                        if (!leadingChar) {
                            throw new IOException
                                ("Invalid escaped '#' character in AVA.  " +
                                "Only a leading '#' can be escaped.");
                        }
                    } else if (specialChars2253.indexOf((char)c) == -1) {
                        throw new IOException
                                ("Invalid escaped character in AVA: '" +
                                (char)c + "'");
                    }
                }
            } else {
                if (format == RFC2253) {
                    if (specialChars2253.indexOf((char)c) != -1) {
                        throw new IOException
                                ("Character '" + (char)c +
                                "' in AVA appears without escape");
                    }
                }
            }
            if (embeddedHex.size() > 0) {
                for (int i = 0; i < spaceCount; i++) {
                    temp.append(" ");
                }
                spaceCount = 0;
                String hexString = getEmbeddedHexString(embeddedHex);
                temp.append(hexString);
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char)c);
            if (c == ' ' && escape == false) {
                spaceCount++;
            } else {
                for (int i = 0; i < spaceCount; i++) {
                    temp.append(" ");
                }
                spaceCount = 0;
                temp.append((char)c);
            }
            c = in.read();
            leadingChar = false;
        } while (isTerminator(c, format) == false);
        if (format == RFC2253 && spaceCount > 0) {
            throw new IOException("Incorrect AVA RFC2253 format - " +
                                        "trailing space must be escaped");
        }
        if (embeddedHex.size() > 0) {
            String hexString = getEmbeddedHexString(embeddedHex);
            temp.append(hexString);
            embeddedHex.clear();
        }
        if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) ||
            (this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) &&
                PRESERVE_OLD_DC_ENCODING == false)) {
            return new DerValue(DerValue.tag_IA5String, temp.toString());
        } else if (isPrintableString) {
            return new DerValue(temp.toString());
        } else {
            return new DerValue(DerValue.tag_UTF8String, temp.toString());
        }
    }
    private static Byte getEmbeddedHexPair(int c1, Reader in)
        throws IOException {
        if (hexDigits.indexOf(Character.toUpperCase((char)c1)) >= 0) {
            int c2 = readChar(in, "unexpected EOF - " +
                        "escaped hex value must include two valid digits");
            if (hexDigits.indexOf(Character.toUpperCase((char)c2)) >= 0) {
                int hi = Character.digit((char)c1, 16);
                int lo = Character.digit((char)c2, 16);
                return new Byte((byte)((hi<<4) + lo));
            } else {
                throw new IOException
                        ("escaped hex value must include two valid digits");
            }
        }
        return null;
    }
    private static String getEmbeddedHexString(List<Byte> hexList)
                                                throws IOException {
        int n = hexList.size();
        byte[] hexBytes = new byte[n];
        for (int i = 0; i < n; i++) {
                hexBytes[i] = hexList.get(i).byteValue();
        }
        return new String(hexBytes, "UTF8");
    }
    private static boolean isTerminator(int ch, int format) {
        switch (ch) {
        case -1:
        case '+':
        case ',':
            return true;
        case ';':
        case '>':
            return format != RFC2253;
        default:
            return false;
        }
    }
    private static int readChar(Reader in, String errMsg) throws IOException {
        int c = in.read();
        if (c == -1) {
            throw new IOException(errMsg);
        }
        return c;
    }
    private static boolean trailingSpace(Reader in) throws IOException {
        boolean trailing = false;
        if (!in.markSupported()) {
            return true;
        } else {
            in.mark(9999);
            while (true) {
                int nextChar = in.read();
                if (nextChar == -1) {
                    trailing = true;
                    break;
                } else if (nextChar == ' ') {
                    continue;
                } else if (nextChar == '\\') {
                    int followingChar = in.read();
                    if (followingChar != ' ') {
                        trailing = false;
                        break;
                    }
                } else {
                    trailing = false;
                    break;
                }
            }
            in.reset();
            return trailing;
        }
    }
    AVA(DerValue derval) throws IOException {
        if (derval.tag != DerValue.tag_Sequence) {
            throw new IOException("AVA not a sequence");
        }
        oid = X500Name.intern(derval.data.getOID());
        value = derval.data.getDerValue();
        if (derval.data.available() != 0) {
            throw new IOException("AVA, extra bytes = "
                + derval.data.available());
        }
    }
    AVA(DerInputStream in) throws IOException {
        this(in.getDerValue());
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AVA == false) {
            return false;
        }
        AVA other = (AVA)obj;
        return this.toRFC2253CanonicalString().equals
                                (other.toRFC2253CanonicalString());
    }
    public int hashCode() {
        return toRFC2253CanonicalString().hashCode();
    }
    public void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }
    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream         tmp = new DerOutputStream();
        DerOutputStream         tmp2 = new DerOutputStream();
        tmp.putOID(oid);
        value.encode(tmp);
        tmp2.write(DerValue.tag_Sequence, tmp);
        out.write(tmp2.toByteArray());
    }
    private String toKeyword(int format, Map<String, String> oidMap) {
        return AVAKeyword.getKeyword(oid, format, oidMap);
    }
    public String toString() {
        return toKeywordValueString
            (toKeyword(DEFAULT, Collections.<String, String>emptyMap()));
    }
    public String toRFC1779String() {
        return toRFC1779String(Collections.<String, String>emptyMap());
    }
    public String toRFC1779String(Map<String, String> oidMap) {
        return toKeywordValueString(toKeyword(RFC1779, oidMap));
    }
    public String toRFC2253String() {
        return toRFC2253String(Collections.<String, String>emptyMap());
    }
    public String toRFC2253String(Map<String, String> oidMap) {
        StringBuilder typeAndValue = new StringBuilder(100);
        typeAndValue.append(toKeyword(RFC2253, oidMap));
        typeAndValue.append('=');
        if ((typeAndValue.charAt(0) >= '0' && typeAndValue.charAt(0) <= '9') ||
            !isDerString(value, false))
        {
            byte[] data = null;
            try {
                data = value.toByteArray();
            } catch (IOException ie) {
                throw new IllegalArgumentException("DER Value conversion");
            }
            typeAndValue.append('#');
            for (int j = 0; j < data.length; j++) {
                byte b = data[j];
                typeAndValue.append(Character.forDigit(0xF & (b >>> 4), 16));
                typeAndValue.append(Character.forDigit(0xF & b, 16));
            }
        } else {
            String valStr = null;
            try {
                valStr = new String(value.getDataBytes(), "UTF8");
            } catch (IOException ie) {
                throw new IllegalArgumentException("DER Value conversion");
            }
            final String escapees = ",=+<>#;\"\\";
            StringBuilder sbuffer = new StringBuilder();
            for (int i = 0; i < valStr.length(); i++) {
                char c = valStr.charAt(i);
                if (DerValue.isPrintableStringChar(c) ||
                    escapees.indexOf(c) >= 0) {
                    if (escapees.indexOf(c) >= 0) {
                        sbuffer.append('\\');
                    }
                    sbuffer.append(c);
                } else if (c == '\u0000') {
                    sbuffer.append("\\00");
                } else if (debug != null && Debug.isOn("ava")) {
                    byte[] valueBytes = null;
                    try {
                        valueBytes = Character.toString(c).getBytes("UTF8");
                    } catch (IOException ie) {
                        throw new IllegalArgumentException
                                        ("DER Value conversion");
                    }
                    for (int j = 0; j < valueBytes.length; j++) {
                        sbuffer.append('\\');
                        char hexChar = Character.forDigit
                                (0xF & (valueBytes[j] >>> 4), 16);
                        sbuffer.append(Character.toUpperCase(hexChar));
                        hexChar = Character.forDigit
                                (0xF & (valueBytes[j]), 16);
                        sbuffer.append(Character.toUpperCase(hexChar));
                    }
                } else {
                    sbuffer.append(c);
                }
            }
            char[] chars = sbuffer.toString().toCharArray();
            sbuffer = new StringBuilder();
            int lead;   
            for (lead = 0; lead < chars.length; lead++) {
                if (chars[lead] != ' ' && chars[lead] != '\r') {
                    break;
                }
            }
            int trail;  
            for (trail = chars.length - 1; trail >= 0; trail--) {
                if (chars[trail] != ' ' && chars[trail] != '\r') {
                    break;
                }
            }
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (i < lead || i > trail) {
                    sbuffer.append('\\');
                }
                sbuffer.append(c);
            }
            typeAndValue.append(sbuffer.toString());
        }
        return typeAndValue.toString();
    }
    public String toRFC2253CanonicalString() {
        StringBuilder typeAndValue = new StringBuilder(40);
        typeAndValue.append
            (toKeyword(RFC2253, Collections.<String, String>emptyMap()));
        typeAndValue.append('=');
        if ((typeAndValue.charAt(0) >= '0' && typeAndValue.charAt(0) <= '9') ||
            !isDerString(value, true))
        {
            byte[] data = null;
            try {
                data = value.toByteArray();
            } catch (IOException ie) {
                throw new IllegalArgumentException("DER Value conversion");
            }
            typeAndValue.append('#');
            for (int j = 0; j < data.length; j++) {
                byte b = data[j];
                typeAndValue.append(Character.forDigit(0xF & (b >>> 4), 16));
                typeAndValue.append(Character.forDigit(0xF & b, 16));
            }
        } else {
            String valStr = null;
            try {
                valStr = new String(value.getDataBytes(), "UTF8");
            } catch (IOException ie) {
                throw new IllegalArgumentException("DER Value conversion");
            }
            final String escapees = ",+<>;\"\\";
            StringBuilder sbuffer = new StringBuilder();
            boolean previousWhite = false;
            for (int i = 0; i < valStr.length(); i++) {
                char c = valStr.charAt(i);
                if (DerValue.isPrintableStringChar(c) ||
                    escapees.indexOf(c) >= 0 ||
                    (i == 0 && c == '#')) {
                    if ((i == 0 && c == '#') || escapees.indexOf(c) >= 0) {
                        sbuffer.append('\\');
                    }
                    if (!Character.isWhitespace(c)) {
                        previousWhite = false;
                        sbuffer.append(c);
                    } else {
                        if (previousWhite == false) {
                            previousWhite = true;
                            sbuffer.append(c);
                        } else {
                            continue;
                        }
                    }
                } else if (debug != null && Debug.isOn("ava")) {
                    previousWhite = false;
                    byte valueBytes[] = null;
                    try {
                        valueBytes = Character.toString(c).getBytes("UTF8");
                    } catch (IOException ie) {
                        throw new IllegalArgumentException
                                        ("DER Value conversion");
                    }
                    for (int j = 0; j < valueBytes.length; j++) {
                        sbuffer.append('\\');
                        sbuffer.append(Character.forDigit
                                        (0xF & (valueBytes[j] >>> 4), 16));
                        sbuffer.append(Character.forDigit
                                        (0xF & (valueBytes[j]), 16));
                    }
                } else {
                    previousWhite = false;
                    sbuffer.append(c);
                }
            }
            typeAndValue.append(sbuffer.toString().trim());
        }
        String canon = typeAndValue.toString();
        canon = canon.toUpperCase(Locale.US).toLowerCase(Locale.US);
        return Normalizer.normalize(canon, Normalizer.Form.NFKD);
    }
    private static boolean isDerString(DerValue value, boolean canonical) {
        if (canonical) {
            switch (value.tag) {
                case DerValue.tag_PrintableString:
                case DerValue.tag_UTF8String:
                    return true;
                default:
                    return false;
            }
        } else {
            switch (value.tag) {
                case DerValue.tag_PrintableString:
                case DerValue.tag_T61String:
                case DerValue.tag_IA5String:
                case DerValue.tag_GeneralString:
                case DerValue.tag_BMPString:
                case DerValue.tag_UTF8String:
                    return true;
                default:
                    return false;
            }
        }
    }
    boolean hasRFC2253Keyword() {
        return AVAKeyword.hasKeyword(oid, RFC2253);
    }
    private String toKeywordValueString(String keyword) {
        StringBuilder   retval = new StringBuilder(40);
        retval.append(keyword);
        retval.append("=");
        try {
            String valStr = value.getAsString();
            if (valStr == null) {
                byte    data [] = value.toByteArray();
                retval.append('#');
                for (int i = 0; i < data.length; i++) {
                    retval.append(hexDigits.charAt((data [i] >> 4) & 0x0f));
                    retval.append(hexDigits.charAt(data [i] & 0x0f));
                }
            } else {
                boolean quoteNeeded = false;
                StringBuilder sbuffer = new StringBuilder();
                boolean previousWhite = false;
                final String escapees = ",+=\n<>#;\\\"";
                for (int i = 0; i < valStr.length(); i++) {
                    char c = valStr.charAt(i);
                    if (DerValue.isPrintableStringChar(c) ||
                        escapees.indexOf(c) >= 0) {
                        if (!quoteNeeded &&
                            ((i == 0 && (c == ' ' || c == '\n')) ||
                                escapees.indexOf(c) >= 0)) {
                            quoteNeeded = true;
                        }
                        if (!(c == ' ' || c == '\n')) {
                            if (c == '"' || c == '\\') {
                                sbuffer.append('\\');
                            }
                            previousWhite = false;
                        } else {
                            if (!quoteNeeded && previousWhite) {
                                quoteNeeded = true;
                            }
                            previousWhite = true;
                        }
                        sbuffer.append(c);
                    } else if (debug != null && Debug.isOn("ava")) {
                        previousWhite = false;
                        byte[] valueBytes =
                                Character.toString(c).getBytes("UTF8");
                        for (int j = 0; j < valueBytes.length; j++) {
                            sbuffer.append('\\');
                            char hexChar = Character.forDigit
                                        (0xF & (valueBytes[j] >>> 4), 16);
                            sbuffer.append(Character.toUpperCase(hexChar));
                            hexChar = Character.forDigit
                                        (0xF & (valueBytes[j]), 16);
                            sbuffer.append(Character.toUpperCase(hexChar));
                        }
                    } else {
                        previousWhite = false;
                        sbuffer.append(c);
                    }
                }
                if (sbuffer.length() > 0) {
                    char trailChar = sbuffer.charAt(sbuffer.length() - 1);
                    if (trailChar == ' ' || trailChar == '\n') {
                        quoteNeeded = true;
                    }
                }
                if (quoteNeeded) {
                    retval.append("\"" + sbuffer.toString() + "\"");
                } else {
                    retval.append(sbuffer.toString());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("DER Value conversion");
        }
        return retval.toString();
    }
}
class AVAKeyword {
    private static final Map<ObjectIdentifier,AVAKeyword> oidMap;
    private static final Map<String,AVAKeyword> keywordMap;
    private String keyword;
    private ObjectIdentifier oid;
    private boolean rfc1779Compliant, rfc2253Compliant;
    private AVAKeyword(String keyword, ObjectIdentifier oid,
               boolean rfc1779Compliant, boolean rfc2253Compliant) {
        this.keyword = keyword;
        this.oid = oid;
        this.rfc1779Compliant = rfc1779Compliant;
        this.rfc2253Compliant = rfc2253Compliant;
        oidMap.put(oid, this);
        keywordMap.put(keyword, this);
    }
    private boolean isCompliant(int standard) {
        switch (standard) {
        case AVA.RFC1779:
            return rfc1779Compliant;
        case AVA.RFC2253:
            return rfc2253Compliant;
        case AVA.DEFAULT:
            return true;
        default:
            throw new IllegalArgumentException("Invalid standard " + standard);
        }
    }
    static ObjectIdentifier getOID(String keyword, int standard)
            throws IOException {
        return getOID
            (keyword, standard, Collections.<String, String>emptyMap());
    }
    static ObjectIdentifier getOID
        (String keyword, int standard, Map<String, String> extraKeywordMap)
            throws IOException {
        keyword = keyword.toUpperCase(Locale.ENGLISH);
        if (standard == AVA.RFC2253) {
            if (keyword.startsWith(" ") || keyword.endsWith(" ")) {
                throw new IOException("Invalid leading or trailing space " +
                        "in keyword \"" + keyword + "\"");
            }
        } else {
            keyword = keyword.trim();
        }
        String oidString = extraKeywordMap.get(keyword);
        if (oidString == null) {
            AVAKeyword ak = keywordMap.get(keyword);
            if ((ak != null) && ak.isCompliant(standard)) {
                return ak.oid;
            }
        } else {
            return new ObjectIdentifier(oidString);
        }
        if (standard == AVA.RFC1779) {
            if (keyword.startsWith("OID.") == false) {
                throw new IOException("Invalid RFC1779 keyword: " + keyword);
            }
            keyword = keyword.substring(4);
        } else if (standard == AVA.DEFAULT) {
            if (keyword.startsWith("OID.")) {
                keyword = keyword.substring(4);
            }
        }
        boolean number = false;
        if (keyword.length() != 0) {
            char ch = keyword.charAt(0);
            if ((ch >= '0') && (ch <= '9')) {
                number = true;
            }
        }
        if (number == false) {
            throw new IOException("Invalid keyword \"" + keyword + "\"");
        }
        return new ObjectIdentifier(keyword);
    }
    static String getKeyword(ObjectIdentifier oid, int standard) {
        return getKeyword
            (oid, standard, Collections.<String, String>emptyMap());
    }
    static String getKeyword
        (ObjectIdentifier oid, int standard, Map<String, String> extraOidMap) {
        String oidString = oid.toString();
        String keywordString = extraOidMap.get(oidString);
        if (keywordString == null) {
            AVAKeyword ak = oidMap.get(oid);
            if ((ak != null) && ak.isCompliant(standard)) {
                return ak.keyword;
            }
        } else {
            if (keywordString.length() == 0) {
                throw new IllegalArgumentException("keyword cannot be empty");
            }
            keywordString = keywordString.trim();
            char c = keywordString.charAt(0);
            if (c < 65 || c > 122 || (c > 90 && c < 97)) {
                throw new IllegalArgumentException
                    ("keyword does not start with letter");
            }
            for (int i=1; i<keywordString.length(); i++) {
                c = keywordString.charAt(i);
                if ((c < 65 || c > 122 || (c > 90 && c < 97)) &&
                    (c < 48 || c > 57) && c != '_') {
                    throw new IllegalArgumentException
                    ("keyword character is not a letter, digit, or underscore");
                }
            }
            return keywordString;
        }
        if (standard == AVA.RFC2253) {
            return oidString;
        } else {
            return "OID." + oidString;
        }
    }
    static boolean hasKeyword(ObjectIdentifier oid, int standard) {
        AVAKeyword ak = oidMap.get(oid);
        if (ak == null) {
            return false;
        }
        return ak.isCompliant(standard);
    }
    static {
        oidMap = new HashMap<ObjectIdentifier,AVAKeyword>();
        keywordMap = new HashMap<String,AVAKeyword>();
        new AVAKeyword("CN",           X500Name.commonName_oid,   true,  true);
        new AVAKeyword("C",            X500Name.countryName_oid,  true,  true);
        new AVAKeyword("L",            X500Name.localityName_oid, true,  true);
        new AVAKeyword("S",            X500Name.stateName_oid,    false, false);
        new AVAKeyword("ST",           X500Name.stateName_oid,    true,  true);
        new AVAKeyword("O",            X500Name.orgName_oid,      true,  true);
        new AVAKeyword("OU",           X500Name.orgUnitName_oid,  true,  true);
        new AVAKeyword("T",            X500Name.title_oid,        false, false);
        new AVAKeyword("IP",           X500Name.ipAddress_oid,    false, false);
        new AVAKeyword("STREET",       X500Name.streetAddress_oid,true,  true);
        new AVAKeyword("DC",           X500Name.DOMAIN_COMPONENT_OID,
                                                                  false, true);
        new AVAKeyword("DNQUALIFIER",  X500Name.DNQUALIFIER_OID,  false, false);
        new AVAKeyword("DNQ",          X500Name.DNQUALIFIER_OID,  false, false);
        new AVAKeyword("SURNAME",      X500Name.SURNAME_OID,      false, false);
        new AVAKeyword("GIVENNAME",    X500Name.GIVENNAME_OID,    false, false);
        new AVAKeyword("INITIALS",     X500Name.INITIALS_OID,     false, false);
        new AVAKeyword("GENERATION",   X500Name.GENERATIONQUALIFIER_OID,
                                                                  false, false);
        new AVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
        new AVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID,
                                                                  false, false);
        new AVAKeyword("UID",          X500Name.userid_oid,       false, true);
        new AVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
    }
}
