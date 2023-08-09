public class X509Extensions
    extends ASN1Encodable
{
    public static final DERObjectIdentifier SubjectDirectoryAttributes = new DERObjectIdentifier("2.5.29.9");
    public static final DERObjectIdentifier SubjectKeyIdentifier = new DERObjectIdentifier("2.5.29.14");
    public static final DERObjectIdentifier KeyUsage = new DERObjectIdentifier("2.5.29.15");
    public static final DERObjectIdentifier PrivateKeyUsagePeriod = new DERObjectIdentifier("2.5.29.16");
    public static final DERObjectIdentifier SubjectAlternativeName = new DERObjectIdentifier("2.5.29.17");
    public static final DERObjectIdentifier IssuerAlternativeName = new DERObjectIdentifier("2.5.29.18");
    public static final DERObjectIdentifier BasicConstraints = new DERObjectIdentifier("2.5.29.19");
    public static final DERObjectIdentifier CRLNumber = new DERObjectIdentifier("2.5.29.20");
    public static final DERObjectIdentifier ReasonCode = new DERObjectIdentifier("2.5.29.21");
    public static final DERObjectIdentifier InstructionCode = new DERObjectIdentifier("2.5.29.23");
    public static final DERObjectIdentifier InvalidityDate = new DERObjectIdentifier("2.5.29.24");
    public static final DERObjectIdentifier DeltaCRLIndicator = new DERObjectIdentifier("2.5.29.27");
    public static final DERObjectIdentifier IssuingDistributionPoint = new DERObjectIdentifier("2.5.29.28");
    public static final DERObjectIdentifier CertificateIssuer = new DERObjectIdentifier("2.5.29.29");
    public static final DERObjectIdentifier NameConstraints = new DERObjectIdentifier("2.5.29.30");
    public static final DERObjectIdentifier CRLDistributionPoints = new DERObjectIdentifier("2.5.29.31");
    public static final DERObjectIdentifier CertificatePolicies = new DERObjectIdentifier("2.5.29.32");
    public static final DERObjectIdentifier PolicyMappings = new DERObjectIdentifier("2.5.29.33");
    public static final DERObjectIdentifier AuthorityKeyIdentifier = new DERObjectIdentifier("2.5.29.35");
    public static final DERObjectIdentifier PolicyConstraints = new DERObjectIdentifier("2.5.29.36");
    public static final DERObjectIdentifier ExtendedKeyUsage = new DERObjectIdentifier("2.5.29.37");
    public static final DERObjectIdentifier FreshestCRL = new DERObjectIdentifier("2.5.29.46");
    public static final DERObjectIdentifier InhibitAnyPolicy = new DERObjectIdentifier("2.5.29.54");
    public static final DERObjectIdentifier AuthorityInfoAccess = new DERObjectIdentifier("1.3.6.1.5.5.7.1.1");
    public static final DERObjectIdentifier SubjectInfoAccess = new DERObjectIdentifier("1.3.6.1.5.5.7.1.11");
    public static final DERObjectIdentifier BiometricInfo = new DERObjectIdentifier("1.3.6.1.5.5.7.1.2");
    public static final DERObjectIdentifier QCStatements = new DERObjectIdentifier("1.3.6.1.5.5.7.1.3");
    private OrderedTable table = new OrderedTable();
    public static X509Extensions getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static X509Extensions getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof X509Extensions)
        {
            return (X509Extensions)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new X509Extensions((ASN1Sequence)obj);
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public X509Extensions(
        ASN1Sequence  seq)
    {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1Sequence            s = ASN1Sequence.getInstance(e.nextElement());
            int sSize = s.size();
            DERObjectIdentifier key = (DERObjectIdentifier) s.getObjectAt(0);
            Object value;
            if (sSize == 3)
            {
                value = new X509Extension(DERBoolean.getInstance(s.getObjectAt(1)), ASN1OctetString.getInstance(s.getObjectAt(2)));
            }
            else if (sSize == 2)
            {
                value = new X509Extension(false, ASN1OctetString.getInstance(s.getObjectAt(1)));
            }
            else
            {
                throw new IllegalArgumentException("Bad sequence size: " + sSize);
            }
            table.add(key, value);
        }
    }
    public X509Extensions(
        Hashtable  extensions)
    {
        this(null, extensions);
    }
    public X509Extensions(
        Vector      ordering,
        Hashtable   extensions)
    {
        Enumeration e;
        if (ordering == null)
        {
            e = extensions.keys();
        }
        else
        {
            e = ordering.elements();
        }
        while (e.hasMoreElements())
        {
            DERObjectIdentifier     oid = (DERObjectIdentifier)e.nextElement();
            X509Extension           ext = (X509Extension)extensions.get(oid);
            table.add(oid, ext);
        }
    }
    public X509Extensions(
        Vector      objectIDs,
        Vector      values)
    {
        Enumeration e = objectIDs.elements();
        int count = 0;
        while (e.hasMoreElements())
        {
            DERObjectIdentifier     oid = (DERObjectIdentifier)e.nextElement();
            X509Extension           ext = (X509Extension)values.elementAt(count);
            table.add(oid, ext);
            count++;
        }
    }
    public Enumeration oids()
    {
        return table.getKeys();
    }
    public X509Extension getExtension(
        DERObjectIdentifier oid)
    {
        return (X509Extension)table.get(oid);
    }
    public DERObject toASN1Object()
    {
        int                     size = table.size();
        ASN1EncodableVector     vec = new ASN1EncodableVector();
        for (int i = 0; i < size; i++) {
            DERObjectIdentifier     oid = table.getKey(i);
            X509Extension           ext = (X509Extension)table.getValue(i);
            ASN1EncodableVector     v = new ASN1EncodableVector();
            v.add(oid);
            if (ext.isCritical())
            {
                v.add(DERBoolean.TRUE);
            }
            v.add(ext.getValue());
            vec.add(new DERSequence(v));
        }
        return new DERSequence(vec);
    }
    public int hashCode()
    {
        int             size = table.size();
        int             hashCode = 0;
        for (int i = 0; i < size; i++) {
            hashCode ^= table.getKey(i).hashCode();
            hashCode ^= table.getValue(i).hashCode();
        }
        return hashCode;
    }
    public boolean equals(
        Object o)
    {
        if (!(o instanceof X509Extensions))
        {
            return false;
        }
        X509Extensions  other = (X509Extensions)o;
        Enumeration     e1 = table.getKeys();
        Enumeration     e2 = other.table.getKeys();
        while (e1.hasMoreElements() && e2.hasMoreElements())
        {
            Object  o1 = e1.nextElement();
            Object  o2 = e2.nextElement();
            if (!o1.equals(o2))
            {
                return false;
            }
        }
        if (e1.hasMoreElements() || e2.hasMoreElements())
        {
            return false;
        }
        return true;
    }
}
