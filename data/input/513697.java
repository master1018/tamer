public class RoleSyntax 
    extends ASN1Encodable
{
    private GeneralNames roleAuthority;
    private GeneralName roleName;
    public static RoleSyntax getInstance(
        Object obj)
    {
        if(obj == null || obj instanceof RoleSyntax)
        {
            return (RoleSyntax)obj;
        }
        else if(obj instanceof ASN1Sequence)
        {
            return new RoleSyntax((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Unknown object in RoleSyntax factory.");
    }
    public RoleSyntax(
        GeneralNames roleAuthority,
        GeneralName roleName)
    {
        if(roleName == null || 
                roleName.getTagNo() != GeneralName.uniformResourceIdentifier ||
                ((DERString)roleName.getName()).getString().equals(""))
        {
            throw new IllegalArgumentException("the role name MUST be non empty and MUST " +
                    "use the URI option of GeneralName");
        }
        this.roleAuthority = roleAuthority;
        this.roleName = roleName;
    }
    public RoleSyntax(
        GeneralName roleName)
    {
        this(null, roleName);
    }
    public RoleSyntax(
        String roleName)
    {
        this(new GeneralName(GeneralName.uniformResourceIdentifier,
                (roleName == null)? "": roleName));
    }
    public RoleSyntax(
        ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        for (int i = 0; i != seq.size(); i++)
        {
            ASN1TaggedObject taggedObject = ASN1TaggedObject.getInstance(seq.getObjectAt(i));
            switch (taggedObject.getTagNo())
            {
            case 0:
                roleAuthority = GeneralNames.getInstance(taggedObject, false);
                break;
            case 1:
                roleName = GeneralName.getInstance(taggedObject, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown tag in RoleSyntax");
            }
        }
    }
    public GeneralNames getRoleAuthority()
    {
        return this.roleAuthority;
    }
    public GeneralName getRoleName()
    {
        return this.roleName;
    }
    public String getRoleNameAsString()
    {
        DERString str = (DERString)this.roleName.getName();
        return str.getString();
    }
    public String[] getRoleAuthorityAsString() 
    {
        if(roleAuthority == null) 
        {
            return new String[0];
        }
        GeneralName[] names = roleAuthority.getNames();
        String[] namesString = new String[names.length];
        for(int i = 0; i < names.length; i++) 
        {
            DEREncodable value = names[i].getName();
            if(value instanceof DERString)
            {
                namesString[i] = ((DERString)value).getString();
            }
            else
            {
                namesString[i] = value.toString();
            }
        }
        return namesString;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if(this.roleAuthority != null)
        {
            v.add(new DERTaggedObject(false, 0, roleAuthority));
        }
        v.add(new DERTaggedObject(false, 1, roleName));
        return new DERSequence(v);
    }
    public String toString() 
    {
        StringBuffer buff = new StringBuffer("Name: " + this.getRoleNameAsString() +
                " - Auth: ");
        if(this.roleAuthority == null || roleAuthority.getNames().length == 0)
        {
            buff.append("N/A");
        }
        else 
        {
            String[] names = this.getRoleAuthorityAsString();
            buff.append('[').append(names[0]);
            for(int i = 1; i < names.length; i++) 
            {
                    buff.append(", ").append(names[i]);
            }
            buff.append(']');
        }
        return buff.toString();
    }
}
