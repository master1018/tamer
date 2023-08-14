final class PackageItem extends Item
{
    public PackageItem (final IItem parent, final String name, final String VMname) 
    {
        super (parent);
        m_name = name;
        m_VMname = VMname;
    }
    public String getName ()
    {
        return m_name;
    }
    public String getVMName ()
    {
        return m_VMname;
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
    private final String m_name, m_VMname;
    private static final Item.ItemMetadata METADATA; 
    static
    {
        METADATA = new Item.ItemMetadata (IItemMetadata.TYPE_ID_PACKAGE, "package",
            1 << IItemAttribute.ATTRIBUTE_NAME_ID |
            1 << IItemAttribute.ATTRIBUTE_CLASS_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_METHOD_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID);
    }
} 
