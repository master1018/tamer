public class AttributeValue {
    public final boolean wasEncoded;
    public String escapedString;
    private String hexString;
    private int tag = -1;
    public byte[] encoded;
    public byte[] bytes; 
    public boolean hasQE; 
    public AttributeValue(String parsedString, boolean hasQorE) {
        wasEncoded = false;
        this.hasQE = hasQorE;
        this.rawString = parsedString;
        this.escapedString = makeEscaped(rawString);
    }
    public AttributeValue(String hexString, byte[] encoded) {
        wasEncoded = true;
        this.hexString = hexString;
        this.encoded = encoded;
        try {
            DerInputStream in = new DerInputStream(encoded);
            tag = in.tag;
            if (DirectoryString.ASN1.checkTag(tag)) {
                this.rawString = (String) DirectoryString.ASN1.decode(in);
                this.escapedString = makeEscaped(rawString);
            } else {
                this.rawString = hexString;
                this.escapedString = hexString;
            }
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(); 
            iae.initCause(e);
            throw iae;
        }
    }
    public String rawString;
    public AttributeValue(String rawString, byte[] encoded, int tag) {
        wasEncoded = true;
        this.encoded = encoded;
        this.tag = tag;
        if (rawString == null) {
            this.rawString = getHexString();
            this.escapedString = hexString;
        } else {
            this.rawString = rawString;
            this.escapedString = makeEscaped(rawString);
        }
    }
    public int getTag() {
        if (tag == -1) {
            if (Utils.isPrintableString(rawString)) {
                tag = ASN1StringType.PRINTABLESTRING.id;
            } else {
                tag = ASN1StringType.UTF8STRING.id;
            }
        }
        return tag;
    }
    public String getHexString() {
        if (hexString == null) {
            if (!wasEncoded) {
                if (Utils.isPrintableString(rawString)) {
                    encoded = ASN1StringType.PRINTABLESTRING.encode(rawString);
                } else {
                    encoded = ASN1StringType.UTF8STRING.encode(rawString);
                }
            }
            StringBuilder buf = new StringBuilder(encoded.length * 2 + 1);
            buf.append('#');
            for (int i = 0, c; i < encoded.length; i++) {
                c = (encoded[i] >> 4) & 0x0F;
                if (c < 10) {
                    buf.append((char) (c + 48));
                } else {
                    buf.append((char) (c + 87));
                }
                c = encoded[i] & 0x0F;
                if (c < 10) {
                    buf.append((char) (c + 48));
                } else {
                    buf.append((char) (c + 87));
                }
            }
            hexString = buf.toString();
        }
        return hexString;
    }
    public void appendQEString(StringBuffer buf) {
        buf.append('"');
        if (hasQE) {
            char c;
            for (int i = 0; i < rawString.length(); i++) {
                c = rawString.charAt(i);
                if (c == '"' || c == '\\') {
                    buf.append('\\');
                }
                buf.append(c);
            }
        } else {
            buf.append(rawString);
        }
        buf.append('"');
    }
    private String makeEscaped(String name) {
        int length = name.length();
        if (length == 0) {
            return name;
        }
        StringBuilder buf = new StringBuilder(length * 2);
        for (int index = 0; index < length; index++) {
            char ch = name.charAt(index);
            switch (ch) {
            case ' ':
                if (index == 0 || index == (length - 1)) {
                    buf.append('\\');
                }
                buf.append(' ');
                break;
            case '"':
            case '\\':
                hasQE = true;
            case ',':
            case '+':
            case '<':
            case '>':
            case ';':
            case '#': 
            case '=': 
                buf.append('\\');
            default:
                buf.append(ch);
            }
        }
        return buf.toString();
    }
    public String makeCanonical() {
        int length = rawString.length();
        if (length == 0) {
            return rawString;
        }
        StringBuilder buf = new StringBuilder(length * 2);
        int index = 0;
        if (rawString.charAt(0) == '#') {
            buf.append('\\');
            buf.append('#');
            index++;
        }
        int bufLength;
        for (; index < length; index++) {
            char ch = rawString.charAt(index);
            switch (ch) {
            case ' ':
                bufLength = buf.length();
                if (bufLength == 0 || buf.charAt(bufLength - 1) == ' ') {
                    break;
                }
                buf.append(' ');
                break;
            case '"':
            case '\\':
            case ',':
            case '+':
            case '<':
            case '>':
            case ';':
                buf.append('\\');
            default:
                buf.append(ch);
            }
        }
        for (bufLength = buf.length() - 1; bufLength > -1
                && buf.charAt(bufLength) == ' '; bufLength--) {
        }
        buf.setLength(bufLength + 1);
        return buf.toString();
    }
}