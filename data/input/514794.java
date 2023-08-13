abstract class AbstractItemVisitor implements IItemVisitor
{
    public Object visit (final AllItem item, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final PackageItem item, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final SrcFileItem item, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final ClassItem item, final Object ctx)
    {
        return ctx;
    }
    public Object visit (final MethodItem item, final Object ctx)
    {
        return ctx;
    }
} 
