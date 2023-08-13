public abstract class ASN1ValueCollection extends ASN1Constructured {
    public final ASN1Type type;
    public ASN1ValueCollection(int tagNumber, ASN1Type type) {
        super(tagNumber);
        this.type = type;
    }
    public Collection getValues(Object object) {
        return (Collection)object;
    }
}
