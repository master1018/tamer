abstract class ElementFactory
{
    public static IAttributeCollection newAttributeCollection (final int capacity)
    {
        return new AttributeCollection (capacity);
    }
    public static IConstantCollection newConstantCollection (final int capacity)
    {
        return new ConstantCollection (capacity);
    }
    public static IFieldCollection newFieldCollection (final int capacity)
    {
        return new FieldCollection (capacity);
    }
    public static IInterfaceCollection newInterfaceCollection (final int capacity)
    {
        return new InterfaceCollection (capacity);
    }
    public static IMethodCollection newMethodCollection (final int capacity)
    {
        return new MethodCollection (capacity);
    }
} 
