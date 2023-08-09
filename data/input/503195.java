public abstract class ASN1StringType extends ASN1Type {
    public static final ASN1StringType BMPSTRING = new ASN1StringType(
            TAG_BMPSTRING) {
    };
    public static final ASN1StringType IA5STRING = new ASN1StringType(
            TAG_IA5STRING) {
    };
    public static final ASN1StringType GENERALSTRING = new ASN1StringType(
            TAG_GENERALSTRING) {
    };
    public static final ASN1StringType PRINTABLESTRING = new ASN1StringType(
            TAG_PRINTABLESTRING) {
    };
    public static final ASN1StringType TELETEXSTRING = new ASN1StringType(
            TAG_TELETEXSTRING) {
    };
    public static final ASN1StringType UNIVERSALSTRING = new ASN1StringType(
            TAG_UNIVERSALSTRING) {
    };
    public static final ASN1StringType UTF8STRING = new ASN1StringType(
            TAG_UTF8STRING) {
        public Object getDecodedObject(BerInputStream in) throws IOException {
            return new String(in.buffer, in.contentOffset, in.length, "UTF-8"); 
        }
        public void setEncodingContent(BerOutputStream out) {
            try {
                byte[] bytes = ((String) out.content).getBytes("UTF-8"); 
                out.content = bytes;
                out.length = bytes.length;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    };
    public ASN1StringType(int tagNumber) {
        super(tagNumber);
    }
    public final boolean checkTag(int identifier) {
        return this.id == identifier || this.constrId == identifier;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readString(this);
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        return new String(in.buffer, in.contentOffset, in.length, "ISO-8859-1");
    }
    public void encodeASN(BerOutputStream out) {
        out.encodeTag(id);
        encodeContent(out);
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeString();
    }
    public void setEncodingContent(BerOutputStream out) {
        try {
            byte[] bytes = ((String) out.content).getBytes("UTF-8"); 
            out.content = bytes;
            out.length = bytes.length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
