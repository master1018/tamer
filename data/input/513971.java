final class ConvertedCharacterValue extends SpecificIntegerValue
{
    private final IntegerValue value;
    public ConvertedCharacterValue(IntegerValue value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedCharacterValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(char)("+value+")";
    }
}