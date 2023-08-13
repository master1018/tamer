public class WombatBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(Wombat.class);
        bd.setValue("test", Boolean.TRUE);
        return bd;
    }
}
