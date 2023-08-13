public class KeyAgreeRecipientInfo
    extends ASN1Encodable
{
    private DERInteger                  version;
    private OriginatorIdentifierOrKey   originator;
    private ASN1OctetString             ukm;
    private AlgorithmIdentifier         keyEncryptionAlgorithm;
    private ASN1Sequence                recipientEncryptedKeys;
    public KeyAgreeRecipientInfo(
        OriginatorIdentifierOrKey   originator,
        ASN1OctetString             ukm,
        AlgorithmIdentifier         keyEncryptionAlgorithm,
        ASN1Sequence                recipientEncryptedKeys)
    {
        this.version = new DERInteger(3);
        this.originator = originator;
        this.ukm = ukm;
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        this.recipientEncryptedKeys = recipientEncryptedKeys;
    }
    public KeyAgreeRecipientInfo(
        ASN1Sequence seq)
    {
        int index = 0;
        version = (DERInteger)seq.getObjectAt(index++);
        originator = OriginatorIdentifierOrKey.getInstance(
                            (ASN1TaggedObject)seq.getObjectAt(index++), true);
        if (seq.getObjectAt(index) instanceof ASN1TaggedObject)
        {
            ukm = ASN1OctetString.getInstance(
                            (ASN1TaggedObject)seq.getObjectAt(index++), true);
        }
        keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(
                                                seq.getObjectAt(index++));
        recipientEncryptedKeys = (ASN1Sequence)seq.getObjectAt(index++);
    }
    public static KeyAgreeRecipientInfo getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static KeyAgreeRecipientInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof KeyAgreeRecipientInfo)
        {
            return (KeyAgreeRecipientInfo)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new KeyAgreeRecipientInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException(
        "Illegal object in KeyAgreeRecipientInfo: " + obj.getClass().getName());
    } 
    public DERInteger getVersion()
    {
        return version;
    }
    public OriginatorIdentifierOrKey getOriginator()
    {
        return originator;
    }
    public ASN1OctetString getUserKeyingMaterial()
    {
        return ukm;
    }
    public AlgorithmIdentifier getKeyEncryptionAlgorithm()
    {
        return keyEncryptionAlgorithm;
    }
    public ASN1Sequence getRecipientEncryptedKeys()
    {
        return recipientEncryptedKeys;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(version);
        v.add(new DERTaggedObject(true, 0, originator));
        if (ukm != null)
        {
            v.add(new DERTaggedObject(true, 1, ukm));
        }
        v.add(keyEncryptionAlgorithm);
        v.add(recipientEncryptedKeys);
        return new DERSequence(v);
    }
}
