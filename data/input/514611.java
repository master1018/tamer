public class SubjectDirectoryAttributes 
    extends ASN1Encodable
{
    private Vector attributes = new Vector();
    public static SubjectDirectoryAttributes getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof SubjectDirectoryAttributes)
        {
            return (SubjectDirectoryAttributes)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new SubjectDirectoryAttributes((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public SubjectDirectoryAttributes(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1Sequence s = ASN1Sequence.getInstance(e.nextElement());
            attributes.addElement(new Attribute(s));
        }
    }
    public SubjectDirectoryAttributes(Vector attributes)
    {
        Enumeration e = attributes.elements();
        while (e.hasMoreElements())
        {
            this.attributes.addElement(e.nextElement());
        }
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        Enumeration e = attributes.elements();
        while (e.hasMoreElements())
        {
            vec.add((Attribute)e.nextElement());
        }
        return new DERSequence(vec);
    }
    public Vector getAttributes()
    {
        return attributes;
    }
}
