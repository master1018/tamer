final class AllItem extends Item
{
    public AllItem ()
    {
        super (null);
    }
    public String getName ()
    {
        return "all classes";
    }
    public void accept (final IItemVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public final IItemMetadata getMetadata ()
    {
        return METADATA;
    }
    public static IItemMetadata getTypeMetadata ()
    {
        return METADATA;
    }
    private static final Item.ItemMetadata METADATA; 
    static
    {        
        METADATA = new Item.ItemMetadata (IItemMetadata.TYPE_ID_ALL, "all",
            1 << IItemAttribute.ATTRIBUTE_NAME_ID |
            1 << IItemAttribute.ATTRIBUTE_CLASS_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_METHOD_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID);
    }
} 
