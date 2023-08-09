public class UnserializableTargetObjectTest {
    public static class Resource { 
        int count;
        int operationCount;
        public void operation() {
            operationCount++;
        }
        public int getCount() {
            return count;
        }
        public void setCount(int count) {
            this.count = count;
        }
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        Resource resource3 = new Resource();
        Method operationMethod = Resource.class.getMethod("operation");
        Method getCountMethod = Resource.class.getMethod("getCount");
        Method setCountMethod = Resource.class.getMethod("setCount", int.class);
        Descriptor operationDescriptor =
            new DescriptorSupport(new String[] {
                                    "descriptorType", "name", "targetObject"
                                  }, new Object[] {
                                    "operation", "operation", resource1
                                  });
        Descriptor getCountDescriptor =
            new DescriptorSupport(new String[] {
                                    "descriptorType", "name", "targetObject"
                                  }, new Object[] {
                                    "operation", "getCount", resource2
                                  });
        Descriptor setCountDescriptor =
            new DescriptorSupport(new String[] {
                                    "descriptorType", "name", "targetObject"
                                  }, new Object[] {
                                    "operation", "setCount", resource2
                                  });
        Descriptor countDescriptor =
            new DescriptorSupport(new String[] {
                                    "descriptorType", "name", "getMethod", "setMethod"
                                  }, new Object[] {
                                    "attribute", "Count", "getCount", "setCount"
                                  });
        ModelMBeanOperationInfo operationInfo =
            new ModelMBeanOperationInfo("operation description",
                                        operationMethod, operationDescriptor);
        ModelMBeanOperationInfo getCountInfo =
            new ModelMBeanOperationInfo("getCount description",
                                        getCountMethod, getCountDescriptor);
        ModelMBeanOperationInfo setCountInfo =
            new ModelMBeanOperationInfo("setCount description",
                                        setCountMethod, setCountDescriptor);
        ModelMBeanAttributeInfo countInfo =
            new ModelMBeanAttributeInfo("Count", "Count description",
                                        getCountMethod, setCountMethod,
                                        countDescriptor);
        ModelMBeanInfo mmbi =
            new ModelMBeanInfoSupport(Resource.class.getName(),
                                      "ModelMBean to test targetObject",
                                      new ModelMBeanAttributeInfo[] {countInfo},
                                      null,  
                                      new ModelMBeanOperationInfo[] {
                                          operationInfo, getCountInfo, setCountInfo
                                      },
                                      null); 
        ModelMBean mmb = new RequiredModelMBean(mmbi);
        mmb.setManagedResource(resource3, "ObjectReference");
        mbs.registerMBean(mmb, name);
        mbs.invoke(name, "operation", null, null);
        mbs.setAttribute(name, new Attribute("Count", 53));
        if (resource1.operationCount != 1)
            throw new Exception("operationCount: " + resource1.operationCount);
        if (resource2.count != 53)
            throw new Exception("count: " + resource2.count);
        int got = (Integer) mbs.getAttribute(name, "Count");
        if (got != 53)
            throw new Exception("got count: " + got);
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        ModelMBeanInfo rmmbi = (ModelMBeanInfo) mbsc.getMBeanInfo(name);
        cc.close();
        cs.stop();
        System.out.println("TEST PASSED");
    }
}
