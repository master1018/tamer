public class OriginatorPublicKey
    extends ASN1Encodable
{
    private AlgorithmIdentifier algorithm;
    private DERBitString        publicKey;
    public OriginatorPublicKey(
        AlgorithmIdentifier algorithm,
        byte[]              publicKey)
    {
        this.algorithm = algorithm;
        this.publicKey = new DERBitString(publicKey);
    }
    public OriginatorPublicKey(
        ASN1Sequence seq)
    {
        algorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0));
        publicKey = (DERBitString)seq.getObjectAt(1);
    }
    public static OriginatorPublicKey getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static OriginatorPublicKey getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof OriginatorPublicKey)
        {
            return (OriginatorPublicKey)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new OriginatorPublicKey((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid OriginatorPublicKey: " + obj.getClass().getName());
    } 
    public AlgorithmIdentifier getAlgorithm()
    {
        return algorithm;
    }
    public DERBitString getPublicKey()
    {
        return publicKey;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(algorithm);
        v.add(publicKey);
        return new DERSequence(v);
    }
}
