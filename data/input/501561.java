abstract class AttributeElementFactory
{
    public static IExceptionHandlerTable newExceptionHandlerTable (final int capacity)
    {
        return new ExceptionHandlerTable (capacity);
    }
    private AttributeElementFactory () {} 
} 
