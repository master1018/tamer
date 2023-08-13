public class RequiredModelMBeanSetAttributeTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        Descriptor somethingAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Something",
                "descriptorType=attribute",
                "getMethod=getSomething"
            });
        ModelMBeanAttributeInfo somethingAttributeInfo =
            new ModelMBeanAttributeInfo("Something",
                                        "java.lang.String",
                                        "Something attribute",
                                        true,
                                        true,
                                        false,
                                        somethingAttributeDescriptor);
        Descriptor somethingCachedAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=SomethingCached",
                "descriptorType=attribute",
                "getMethod=getSomethingCached",
                "currencyTimeLimit=5000"
            });
        ModelMBeanAttributeInfo somethingCachedAttributeInfo =
            new ModelMBeanAttributeInfo("SomethingCached",
                                        "java.lang.String",
                                        "Something cached attribute",
                                        true,
                                        true,
                                        false,
                                        somethingCachedAttributeDescriptor);
        ModelMBeanInfo mmbi = new ModelMBeanInfoSupport(
            Resource.class.getName(),
            "Resource MBean",
            new ModelMBeanAttributeInfo[] { somethingAttributeInfo, somethingCachedAttributeInfo },
            null,
            new ModelMBeanOperationInfo[] {},
            null);
        ModelMBean mmb = new RequiredModelMBean(mmbi);
        mmb.setManagedResource(resource, "ObjectReference");
        ObjectName mmbName = new ObjectName(":type=ResourceMBean");
        mbs.registerMBean(mmb, mmbName);
        System.out.println("\nTest that we receive ServiceNotFoundException");
        try {
            Attribute attr = new Attribute("Something", "Some string");
            mbs.setAttribute(mmbName, attr);
            System.out.println("TEST FAILED: Didn't caught exception");
            ok = false;
        } catch(MBeanException mbex) {
            Exception e = mbex.getTargetException();
            if(e == null || !(e instanceof ServiceNotFoundException)) {
                System.out.println("TEST FAILED: Caught wrong exception:" + e);
                ok = false;
            } else
                System.out.println("Received expected ServiceNotFoundException");
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught wrong exception: " + e);
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTest that we are not receiving ServiceNotFoundException");
        try {
            Attribute attr = new Attribute("SomethingCached", "Some string");
            mbs.setAttribute(mmbName, attr);
            System.out.println("No exception thrown");
       } catch (Exception e) {
            System.out.println("TEST FAILED: Caught an exception: " + e);
            e.printStackTrace(System.out);
            ok = false;
       }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            throw new Exception("TEST FAILED");
        }
    }
    public static class Resource {
        public String getSomething() {
            return "Something value";
        }
        public String getSomethingCached() {
            return "Something cached value";
        }
    }
    private static Resource resource = new Resource();
}
