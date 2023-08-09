public class SecondBeanBeanInfo extends SimpleBeanInfo {
    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(SecondBean.class);
    }
}
