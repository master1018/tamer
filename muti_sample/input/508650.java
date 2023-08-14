public class ObjectDigestInfo
    extends ASN1Encodable
{
    DEREnumerated digestedObjectType;
    DERObjectIdentifier otherObjectTypeID;
    AlgorithmIdentifier digestAlgorithm;
    DERBitString objectDigest;
    public static ObjectDigestInfo getInstance(
            Object  obj)
    {
        if (obj == null || obj instanceof ObjectDigestInfo)
        {
            return (ObjectDigestInfo)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new ObjectDigestInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static ObjectDigestInfo getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public ObjectDigestInfo(ASN1Sequence seq)
    {
        if (seq.size() > 4 || seq.size() < 3)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        digestedObjectType = DEREnumerated.getInstance(seq.getObjectAt(0));
        int offset = 0;
        if (seq.size() == 4)
        {
            otherObjectTypeID = DERObjectIdentifier.getInstance(seq.getObjectAt(1));
            offset++;
        }
        digestAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1 + offset));
        objectDigest = DERBitString.getInstance(seq.getObjectAt(2 + offset));
    }
    public DEREnumerated getDigestedObjectType()
    {
        return digestedObjectType;
    }
    public DERObjectIdentifier getOtherObjectTypeID()
    {
        return otherObjectTypeID;
    }
    public AlgorithmIdentifier getDigestAlgorithm()
    {
        return digestAlgorithm;
    }
    public DERBitString getObjectDigest()
    {
        return objectDigest;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(digestedObjectType);
        if (otherObjectTypeID != null)
        {
            v.add(otherObjectTypeID);
        }
        v.add(digestAlgorithm);
        v.add(objectDigest);
        return new DERSequence(v);
    }
}
