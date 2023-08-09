public class CompressedData
    extends ASN1Encodable
{
    private DERInteger           version;
    private AlgorithmIdentifier  compressionAlgorithm;
    private ContentInfo          encapContentInfo;
    public CompressedData(
        AlgorithmIdentifier compressionAlgorithm,
        ContentInfo         encapContentInfo)
    {
        this.version = new DERInteger(0);
        this.compressionAlgorithm = compressionAlgorithm;
        this.encapContentInfo = encapContentInfo;
    }
    public CompressedData(
        ASN1Sequence seq)
    {
        this.version = (DERInteger)seq.getObjectAt(0);
        this.compressionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.encapContentInfo = ContentInfo.getInstance(seq.getObjectAt(2));
    }
    public static CompressedData getInstance(
        ASN1TaggedObject _ato,
        boolean _explicit)
    {
        return getInstance(ASN1Sequence.getInstance(_ato, _explicit));
    }
    public static CompressedData getInstance(
        Object _obj)
    {
        if (_obj == null || _obj instanceof CompressedData)
        {
            return (CompressedData)_obj;
        }
        if (_obj instanceof ASN1Sequence)
        {
            return new CompressedData((ASN1Sequence)_obj);
        }
        throw new IllegalArgumentException("Invalid CompressedData: " + _obj.getClass().getName());
    }
    public DERInteger getVersion()
    {
        return version;
    }
    public AlgorithmIdentifier getCompressionAlgorithmIdentifier()
    {
        return compressionAlgorithm;
    }
    public ContentInfo getEncapContentInfo()
    {
        return encapContentInfo;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(version);
        v.add(compressionAlgorithm);
        v.add(encapContentInfo);
        return new BERSequence(v);
    }
}
