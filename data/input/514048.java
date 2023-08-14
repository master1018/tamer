public class MemberSpecification
{
    public int    requiredSetAccessFlags;
    public int    requiredUnsetAccessFlags;
    public final String annotationType;
    public final String name;
    public final String descriptor;
    public MemberSpecification()
    {
        this(0,
             0,
             null,
             null,
             null);
    }
    public MemberSpecification(int    requiredSetAccessFlags,
                               int    requiredUnsetAccessFlags,
                               String annotationType,
                               String name,
                               String descriptor)
    {
        this.requiredSetAccessFlags   = requiredSetAccessFlags;
        this.requiredUnsetAccessFlags = requiredUnsetAccessFlags;
        this.annotationType           = annotationType;
        this.name                     = name;
        this.descriptor               = descriptor;
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        MemberSpecification other = (MemberSpecification)object;
        return
            (this.requiredSetAccessFlags   == other.requiredSetAccessFlags                                                                      ) &&
            (this.requiredUnsetAccessFlags == other.requiredUnsetAccessFlags                                                                    ) &&
            (this.annotationType == null ? other.annotationType == null : this.annotationType.equals(other.annotationType)) &&
            (this.name           == null ? other.name           == null : this.name.equals(other.name)                    ) &&
            (this.descriptor     == null ? other.descriptor     == null : this.descriptor.equals(other.descriptor)        );
    }
    public int hashCode()
    {
        return
            (requiredSetAccessFlags                                ) ^
            (requiredUnsetAccessFlags                              ) ^
            (annotationType == null ? 0 : annotationType.hashCode()) ^
            (name           == null ? 0 : name.hashCode()          ) ^
            (descriptor     == null ? 0 : descriptor.hashCode()    );
    }
}
