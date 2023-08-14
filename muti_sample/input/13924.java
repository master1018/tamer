public class SwingBeanInfoBase extends SimpleBeanInfo
{
    public int getDefaultPropertyIndex() {
        return 0;
    }
    public Image getIcon(int kind) {
        return null;
    }
    public BeanInfo[] getAdditionalBeanInfo() {
        Class superClass = getBeanDescriptor().getBeanClass().getSuperclass();
        BeanInfo superBeanInfo = null;
        try {
            superBeanInfo = Introspector.getBeanInfo(superClass);
        } catch (IntrospectionException ie) {}
        if (superBeanInfo != null) {
            BeanInfo[] ret = new BeanInfo[1];
            ret[0] = superBeanInfo;
            return ret;
        }
        return null;
    }
}
