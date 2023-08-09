import proguard.classfile.Clazz;
final class IdentifiedReferenceValue extends ReferenceValue
{
    private final ValueFactory valuefactory;
    private final int          id;
    public IdentifiedReferenceValue(String       type,
                                    Clazz        referencedClass,
                                    boolean      mayBeNull,
                                    ValueFactory valuefactory,
                                    int          id)
    {
        super(type, referencedClass, mayBeNull);
        this.valuefactory = valuefactory;
        this.id           = id;
    }
    public int equal(ReferenceValue other)
    {
        return this.equals(other) ? ALWAYS : MAYBE;
    }
    public boolean isSpecific()
    {
        return true;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.valuefactory.equals(((IdentifiedReferenceValue)object).valuefactory) &&
               this.id == ((IdentifiedReferenceValue)object).id;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               valuefactory.hashCode() ^
               id;
    }
    public String toString()
    {
        return super.toString()+'#'+id;
    }
}