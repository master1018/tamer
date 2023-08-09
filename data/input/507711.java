public class PasswordRecipientInfo
    extends ASN1Encodable
{
    private DERInteger          version;
    private AlgorithmIdentifier keyDerivationAlgorithm;
    private AlgorithmIdentifier keyEncryptionAlgorithm;
    private ASN1OctetString     encryptedKey;
    public PasswordRecipientInfo(
        AlgorithmIdentifier     keyEncryptionAlgorithm,
        ASN1OctetString         encryptedKey)
    {
        this.version = new DERInteger(0);
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        this.encryptedKey = encryptedKey;
    }
    public PasswordRecipientInfo(
        AlgorithmIdentifier     keyDerivationAlgorithm,
        AlgorithmIdentifier     keyEncryptionAlgorithm,
        ASN1OctetString         encryptedKey)
    {
        this.version = new DERInteger(0);
        this.keyDerivationAlgorithm = keyDerivationAlgorithm;
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        this.encryptedKey = encryptedKey;
    }
    public PasswordRecipientInfo(
        ASN1Sequence seq)
    {
        version = (DERInteger)seq.getObjectAt(0);
        if (seq.getObjectAt(1) instanceof ASN1TaggedObject)
        {
            keyDerivationAlgorithm = AlgorithmIdentifier.getInstance((ASN1TaggedObject)seq.getObjectAt(1), false);
            keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(2));
            encryptedKey = (ASN1OctetString)seq.getObjectAt(3);
        }
        else
        {
            keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
            encryptedKey = (ASN1OctetString)seq.getObjectAt(2);
        }
    }
    public static PasswordRecipientInfo getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static PasswordRecipientInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof PasswordRecipientInfo)
        {
            return (PasswordRecipientInfo)obj;
        }
        if(obj instanceof ASN1Sequence)
        {
            return new PasswordRecipientInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid PasswordRecipientInfo: " + obj.getClass().getName());
    }
    public DERInteger getVersion()
    {
        return version;
    }
    public AlgorithmIdentifier getKeyDerivationAlgorithm()
    {
        return keyDerivationAlgorithm;
    }
    public AlgorithmIdentifier getKeyEncryptionAlgorithm()
    {
        return keyEncryptionAlgorithm;
    }
    public ASN1OctetString getEncryptedKey()
    {
        return encryptedKey;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(version);
        if (keyDerivationAlgorithm != null)
        {
            v.add(new DERTaggedObject(false, 0, keyDerivationAlgorithm));
        }
        v.add(keyEncryptionAlgorithm);
        v.add(encryptedKey);
        return new DERSequence(v);
    }
}
