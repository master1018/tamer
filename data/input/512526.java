public abstract class ASN1Primitive extends ASN1Type {
    public ASN1Primitive(int tagNumber) {
        super(tagNumber);
    }
    public final boolean checkTag(int identifier) {
        return this.id == identifier;
    }
    public void encodeASN(BerOutputStream out) {
        out.encodeTag(id);
        encodeContent(out);
    }
}
