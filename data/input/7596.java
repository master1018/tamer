public class DescriptorSupportSerialTest {
    public static void main(String[] args) throws Exception {
        if (java.io.Serializable.class.isAssignableFrom(Thread.class))
            throw new Exception("TEST BAD: Thread is Serializable!");
        Method getName = Thread.class.getMethod("getName");
        Descriptor d = new DescriptorSupport();
        d.setField("name", "getName");
        d.setField("descriptorType", "operation");
        d.setField("TARGETObject", Thread.currentThread());
        d.setField("foo", "bar");
        ModelMBeanOperationInfo getNameInfo =
            new ModelMBeanOperationInfo("Get name", getName, d);
        ModelMBeanInfo mmbi =
            new ModelMBeanInfoSupport(Thread.class.getName(),
                                      "Thread!",
                                      null,  
                                      null,  
                                      new ModelMBeanOperationInfo[] {getNameInfo},
                                      null);  
        ModelMBean mmb = new RequiredModelMBean(mmbi);
        ObjectName on = new ObjectName("d:type=Thread");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        mbs.registerMBean(mmb, on);
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        try {
            test(mbsc, on);
            System.out.println("TEST PASSED");
        } finally {
            cc.close();
            cs.stop();
        }
    }
    private static void test(MBeanServerConnection mbsc, ObjectName on)
            throws Exception {
        ModelMBeanInfo mmbi2 = (ModelMBeanInfo) mbsc.getMBeanInfo(on);
        Descriptor d2 = mmbi2.getDescriptor("getName", "operation");
        if (!"bar".equals(d2.getFieldValue("foo")))
            throw new Exception("TEST FAILED: bad descriptor: " + d2);
        String name = (String) mbsc.invoke(on, "getName", null, null);
        String thisName = Thread.currentThread().getName();
        if (!thisName.equals(name)) {
            throw new Exception("TEST FAILED: wrong thread name: " +
                                name + " should be " + thisName);
        }
    }
}
