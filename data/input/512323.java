public class PublicKeyAndChallenge
    extends ASN1Encodable
{
    private ASN1Sequence         pkacSeq;
    private SubjectPublicKeyInfo spki;
    private DERIA5String         challenge;
    public static PublicKeyAndChallenge getInstance(Object obj)
    {
        if (obj instanceof PublicKeyAndChallenge)
        {
            return (PublicKeyAndChallenge)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new PublicKeyAndChallenge((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unkown object in factory");
    }
    public PublicKeyAndChallenge(ASN1Sequence seq)
    {
        pkacSeq = seq;
        spki = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(0));
        challenge = DERIA5String.getInstance(seq.getObjectAt(1));
    }
    public DERObject toASN1Object()
    {
        return pkacSeq;
    }
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return spki;
    }
    public DERIA5String getChallenge()
    {
        return challenge;
    }
}
