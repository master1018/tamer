public class OnUnregisterTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        DescriptorSupport desc;
        ModelMBeanAttributeInfo mmbai;
        ModelMBeanInfo mmbi;
        ModelMBean mmb;
        desc = new DescriptorSupport("name=foo",
                                     "descriptorType=attribute",
                                     "persistPolicy=OnUnregister");
        mmbai = new ModelMBeanAttributeInfo("foo", "int", "a foo",
                                            true, true, false, desc);
        mmbi = new ModelMBeanInfoSupport("a.b.c", "description",
                                         new ModelMBeanAttributeInfo[] {mmbai},
                                         null, null, null);
        mmb = new RequiredModelMBean(mmbi);
        mbs.registerMBean(mmb, on);
        mbs.unregisterMBean(on);
        desc = new DescriptorSupport("name=foo", "descriptorType=attribute");
        mmbai = new ModelMBeanAttributeInfo("foo", "int", "a foo",
                                            true, true, false, desc);
        desc = new DescriptorSupport("name=bar",
                                     "descriptorType=mbean",
                                     "persistPolicy=onUnregister");
        mmbi = new ModelMBeanInfoSupport("a.b.c", "description",
                                         new ModelMBeanAttributeInfo[] {mmbai},
                                         null, null, null, desc);
        mmb = new RequiredModelMBean(mmbi);
        mbs.registerMBean(mmb, on);
        mbs.unregisterMBean(on);
    }
}
