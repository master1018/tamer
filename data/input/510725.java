public class OtherSigningCertificate
    extends ASN1Encodable
{
    ASN1Sequence certs;
    ASN1Sequence policies;
    public static OtherSigningCertificate getInstance(Object o)
    {
        if (o == null || o instanceof OtherSigningCertificate)
        {
            return (OtherSigningCertificate) o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new OtherSigningCertificate((ASN1Sequence) o);
        }
        throw new IllegalArgumentException(
                "unknown object in 'OtherSigningCertificate' factory : "
                        + o.getClass().getName() + ".");
    }
    public OtherSigningCertificate(ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        this.certs = ASN1Sequence.getInstance(seq.getObjectAt(0));
        if (seq.size() > 1)
        {
            this.policies = ASN1Sequence.getInstance(seq.getObjectAt(1));
        }
    }
    public OtherSigningCertificate(
        OtherCertID otherCertID)
    {
        certs = new DERSequence(otherCertID);
    }
    public OtherCertID[] getCerts()
    {
        OtherCertID[] cs = new OtherCertID[certs.size()];
        for (int i = 0; i != certs.size(); i++)
        {
            cs[i] = OtherCertID.getInstance(certs.getObjectAt(i));
        }
        return cs;
    }
    public PolicyInformation[] getPolicies()
    {
        if (policies == null)
        {
            return null;
        }
        PolicyInformation[] ps = new PolicyInformation[policies.size()];
        for (int i = 0; i != policies.size(); i++)
        {
            ps[i] = PolicyInformation.getInstance(policies.getObjectAt(i));
        }
        return ps;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(certs);
        if (policies != null)
        {
            v.add(policies);
        }
        return new DERSequence(v);
    }
}
