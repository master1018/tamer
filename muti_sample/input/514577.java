final class IdentifiedDoubleValue extends SpecificDoubleValue
{
    private final ValueFactory valuefactory;
    private final int          id;
    public IdentifiedDoubleValue(ValueFactory valuefactory, int id)
    {
        this.valuefactory = valuefactory;
        this.id           = id;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.valuefactory.equals(((IdentifiedDoubleValue)object).valuefactory) &&
               this.id == ((IdentifiedDoubleValue)object).id;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               valuefactory.hashCode() ^
               id;
    }
    public String toString()
    {
        return "d"+id;
    }
}