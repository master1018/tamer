public class GraphicsPrimitiveProxy extends GraphicsPrimitive {
    private Class owner;
    private String relativeClassName;
    public GraphicsPrimitiveProxy(Class owner, String relativeClassName,
                                  String methodSignature,
                                  int primID,
                                  SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        super(methodSignature, primID, srctype, comptype, dsttype);
        this.owner = owner;
        this.relativeClassName = relativeClassName;
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype) {
        throw new InternalError("makePrimitive called on a Proxy!");
    }
    GraphicsPrimitive instantiate() {
        String name = getPackageName(owner.getName()) + "."
                        + relativeClassName;
        try {
            Class clazz = Class.forName(name);
            GraphicsPrimitive p = (GraphicsPrimitive) clazz.newInstance();
            if (!satisfiesSameAs(p)) {
                throw new RuntimeException("Primitive " + p
                                           + " incompatible with proxy for "
                                           + name);
            }
            return p;
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex.toString());
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex.toString());
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex.toString());
        }
    }
    private static String getPackageName(String className) {
        int lastDotIdx = className.lastIndexOf('.');
        if (lastDotIdx < 0) {
            return className;
        }
        return className.substring(0, lastDotIdx);
    }
    public GraphicsPrimitive traceWrap() {
        return instantiate().traceWrap();
    }
}
