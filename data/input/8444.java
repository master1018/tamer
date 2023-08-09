public final class BeanInfoFinder
        extends InstanceFinder<BeanInfo> {
    private static final String DEFAULT = "sun.beans.infos";
    public BeanInfoFinder() {
        super(BeanInfo.class, true, "BeanInfo", DEFAULT);
    }
    private static boolean isValid(Class<?> type, Method method) {
        return (method != null) && method.getDeclaringClass().isAssignableFrom(type);
    }
    @Override
    protected BeanInfo instantiate(Class<?> type, String prefix, String name) {
        BeanInfo info = !DEFAULT.equals(prefix) || "ComponentBeanInfo".equals(name)
                ? super.instantiate(type, prefix, name)
                : null;
        if (info != null) {
            BeanDescriptor bd = info.getBeanDescriptor();
            if (bd != null) {
                if (type.equals(bd.getBeanClass())) {
                    return info;
                }
            }
            else {
                PropertyDescriptor[] pds = info.getPropertyDescriptors();
                if (pds != null) {
                    for (PropertyDescriptor pd : pds) {
                        Method method = pd.getReadMethod();
                        if (method == null) {
                            method = pd.getWriteMethod();
                        }
                        if (isValid(type, method)) {
                            return info;
                        }
                    }
                }
                else {
                    MethodDescriptor[] mds = info.getMethodDescriptors();
                    if (mds != null) {
                        for (MethodDescriptor md : mds) {
                            if (isValid(type, md.getMethod())) {
                                return info;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
