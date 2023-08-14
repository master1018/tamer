public abstract class ASN1TaggedObject
    extends DERObject
{
    int             tagNo;
    boolean         empty = false;
    boolean         explicit = true;
    DEREncodable    obj = null;
    static public ASN1TaggedObject getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        if (explicit)
        {
            return (ASN1TaggedObject)obj.getObject();
        }
        throw new IllegalArgumentException("implicitly tagged tagged object");
    }
    static public ASN1TaggedObject getInstance(
        Object obj) 
    {
        if (obj == null || obj instanceof ASN1TaggedObject) 
        {
                return (ASN1TaggedObject)obj;
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }
    public ASN1TaggedObject(
        int             tagNo,
        DEREncodable    obj)
    {
        this.explicit = true;
        this.tagNo = tagNo;
        this.obj = obj;
    }
    public ASN1TaggedObject(
        boolean         explicit,
        int             tagNo,
        DEREncodable    obj)
    {
        if (obj instanceof ASN1Choice)
        {
            this.explicit = true;
        }
        else
        {
            this.explicit = explicit;
        }
        this.tagNo = tagNo;
        this.obj = obj;
    }
    public boolean equals(
        Object o)
    {
        if (!(o instanceof ASN1TaggedObject))
        {
            return false;
        }
        ASN1TaggedObject other = (ASN1TaggedObject)o;
        if (tagNo != other.tagNo || empty != other.empty || explicit != other.explicit)
        {
            return false;
        }
        if(obj == null)
        {
            if(other.obj != null)
            {
                return false;
            }
        }
        else
        {
            if(!(obj.equals(other.obj)))
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        int code = tagNo;
        if (obj != null)
        {
            code ^= obj.hashCode();
        }
        return code;
    }
    public int getTagNo()
    {
        return tagNo;
    }
    public boolean isExplicit()
    {
        return explicit;
    }
    public boolean isEmpty()
    {
        return empty;
    }
    public DERObject getObject()
    {
        if (obj != null)
        {
            return obj.getDERObject();
        }
        return null;
    }
    abstract void encode(DEROutputStream  out)
        throws IOException;
    public String toString()
    {
        return "[" + tagNo + "]" + obj;
    }
}
