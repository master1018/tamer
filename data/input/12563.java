public class Test4984912 {
    public static void main(String[] args) {
        PropertyDescriptor[] array = BeanUtils.getPropertyDescriptors(SimpleBean.class);
        for (PropertyDescriptor pd : array) {
            BeanUtils.reportPropertyDescriptor(pd);
        }
        if (array.length != 3)
            throw new Error("unexpected count of properties: " + array.length);
    }
    public static class SimpleBean {
        private int property;
        public int getProperty() {
            return this.property;
        }
        public void setProperty(int property) {
            this.property = property;
        }
    }
    public static class SimpleBeanBeanInfo extends SimpleBeanInfo {
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                PropertyDescriptor pdProperty = new PropertyDescriptor("property", SimpleBean.class, "getProperty", "setProperty");
                PropertyDescriptor pdNullable = new PropertyDescriptor("nullable", SimpleBean.class, null, null);
                PropertyDescriptor pdIndexed = new IndexedPropertyDescriptor("indexed", SimpleBean.class, null, null, null, null);
                pdNullable.setValue("name", "value");
                return new PropertyDescriptor[] {pdProperty, pdNullable, pdIndexed};
            }
            catch (IntrospectionException exception) {
                throw new Error(exception);
            }
        }
    }
}
