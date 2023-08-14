public abstract class MXBeanMappingFactory {
    protected MXBeanMappingFactory() {}
    public static final MXBeanMappingFactory DEFAULT =
            new DefaultMXBeanMappingFactory();
    public abstract MXBeanMapping mappingForType(Type t, MXBeanMappingFactory f)
    throws OpenDataException;
}
