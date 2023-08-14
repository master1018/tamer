public class AttCertIssuer
    extends ASN1Encodable
    implements ASN1Choice
{
    ASN1Encodable   obj;
    DERObject       choiceObj;
    public static AttCertIssuer getInstance(
        Object  obj)
    {
        if (obj instanceof AttCertIssuer)
        {
            return (AttCertIssuer)obj;
        }
        else if (obj instanceof V2Form)
        {
            return new AttCertIssuer(V2Form.getInstance(obj));
        }
        else if (obj instanceof GeneralNames)
        {
            return new AttCertIssuer((GeneralNames)obj);
        }
        else if (obj instanceof ASN1TaggedObject)
        {
            return new AttCertIssuer(V2Form.getInstance((ASN1TaggedObject)obj, false));
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new AttCertIssuer(GeneralNames.getInstance(obj));
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass());
    }
    public static AttCertIssuer getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject()); 
    }
    public AttCertIssuer(
        GeneralNames  names)
    {
        obj = names;
        choiceObj = obj.getDERObject();
    }
    public AttCertIssuer(
        V2Form  v2Form)
    {
        obj = v2Form;
        choiceObj = new DERTaggedObject(false, 0, obj);
    }
    public ASN1Encodable getIssuer()
    {
        return obj;
    }
    public DERObject toASN1Object()
    {
        return choiceObj;
    }
}
