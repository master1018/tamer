public class SimpleBeanInfo implements BeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        return null;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }
    public int getDefaultPropertyIndex() {
        return -1;
    }
    public EventSetDescriptor[] getEventSetDescriptors() {
        return null;
    }
    public int getDefaultEventIndex() {
        return -1;
    }
    public MethodDescriptor[] getMethodDescriptors() {
        return null;
    }
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }
    public java.awt.Image getIcon(int iconKind) {
        return null;
    }
    public java.awt.Image loadImage(final String resourceName) {
        try {
            final Class c = getClass();
            java.awt.image.ImageProducer ip = (java.awt.image.ImageProducer)
                java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        java.net.URL url;
                        if ((url = c.getResource(resourceName)) == null) {
                            return null;
                        } else {
                            try {
                                return url.getContent();
                            } catch (java.io.IOException ioe) {
                                return null;
                            }
                        }
                    }
            });
            if (ip == null)
                return null;
            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            return tk.createImage(ip);
        } catch (Exception ex) {
            return null;
        }
    }
}
