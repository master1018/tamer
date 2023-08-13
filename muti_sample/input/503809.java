public class ClassSpecification implements Cloneable
{
    public final String comments;
    public       int    requiredSetAccessFlags;
    public       int    requiredUnsetAccessFlags;
    public final String annotationType;
    public       String className;
    public final String extendsAnnotationType;
    public final String extendsClassName;
    public       List   fieldSpecifications;
    public       List   methodSpecifications;
    public ClassSpecification()
    {
        this(null,
             0,
             0,
             null,
             null,
             null,
             null);
    }
    public ClassSpecification(ClassSpecification classSpecification)
    {
        this(classSpecification.comments,
             classSpecification.requiredSetAccessFlags,
             classSpecification.requiredUnsetAccessFlags,
             classSpecification.annotationType,
             classSpecification.className,
             classSpecification.extendsAnnotationType,
             classSpecification.extendsClassName,
             classSpecification.fieldSpecifications,
             classSpecification.methodSpecifications);
    }
    public ClassSpecification(String comments,
                              int    requiredSetAccessFlags,
                              int    requiredUnsetAccessFlags,
                              String annotationType,
                              String className,
                              String extendsAnnotationType,
                              String extendsClassName)
    {
        this(comments,
             requiredSetAccessFlags,
             requiredUnsetAccessFlags,
             annotationType,
             className,
             extendsAnnotationType,
             extendsClassName,
             null,
             null);
    }
    public ClassSpecification(String comments,
                              int    requiredSetAccessFlags,
                              int    requiredUnsetAccessFlags,
                              String annotationType,
                              String className,
                              String extendsAnnotationType,
                              String extendsClassName,
                              List   fieldSpecifications,
                              List   methodSpecifications)
    {
        this.comments                 = comments;
        this.requiredSetAccessFlags   = requiredSetAccessFlags;
        this.requiredUnsetAccessFlags = requiredUnsetAccessFlags;
        this.annotationType           = annotationType;
        this.className                = className;
        this.extendsAnnotationType    = extendsAnnotationType;
        this.extendsClassName         = extendsClassName;
        this.fieldSpecifications      = fieldSpecifications;
        this.methodSpecifications     = methodSpecifications;
    }
    public void addField(MemberSpecification fieldSpecification)
    {
        if (fieldSpecifications == null)
        {
            fieldSpecifications = new ArrayList();
        }
        fieldSpecifications.add(fieldSpecification);
    }
    public void addMethod(MemberSpecification methodSpecification)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }
        methodSpecifications.add(methodSpecification);
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        ClassSpecification other = (ClassSpecification)object;
        return
            (this.requiredSetAccessFlags   == other.requiredSetAccessFlags                                                                               ) &&
            (this.requiredUnsetAccessFlags == other.requiredUnsetAccessFlags                                                                             ) &&
            (this.annotationType           == null ? other.annotationType        == null : this.annotationType.equals(other.annotationType)              ) &&
            (this.className                == null ? other.className             == null : this.className.equals(other.className)                        ) &&
            (this.extendsAnnotationType    == null ? other.extendsAnnotationType == null : this.extendsAnnotationType.equals(other.extendsAnnotationType)) &&
            (this.extendsClassName         == null ? other.extendsClassName      == null : this.extendsClassName.equals(other.extendsClassName)          ) &&
            (this.fieldSpecifications      == null ? other.fieldSpecifications   == null : this.fieldSpecifications.equals(other.fieldSpecifications)    ) &&
            (this.methodSpecifications     == null ? other.methodSpecifications  == null : this.methodSpecifications.equals(other.methodSpecifications)  );
    }
    public int hashCode()
    {
        return
            (requiredSetAccessFlags                                              ) ^
            (requiredUnsetAccessFlags                                            ) ^
            (annotationType        == null ? 0 : annotationType.hashCode()       ) ^
            (className             == null ? 0 : className.hashCode()            ) ^
            (extendsAnnotationType == null ? 0 : extendsAnnotationType.hashCode()) ^
            (extendsClassName      == null ? 0 : extendsClassName.hashCode()     ) ^
            (fieldSpecifications   == null ? 0 : fieldSpecifications.hashCode()  ) ^
            (methodSpecifications  == null ? 0 : methodSpecifications.hashCode() );
    }
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }
}
