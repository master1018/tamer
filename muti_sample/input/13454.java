public class RequiredModelMBeanGetAttributeTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        Method nullGetter =
            Resource.class.getMethod("getNull", (Class[]) null);
        Method integerGetter =
            Resource.class.getMethod("getInteger", (Class[]) null);
        Method hashtableGetter =
            Resource.class.getMethod("getHashtable", (Class[]) null);
        Method mapGetter =
            Resource.class.getMethod("getMap", (Class[]) null);
        Descriptor nullOperationDescriptor =
            new DescriptorSupport(new String[] {
                "name=getNull",
                "descriptorType=operation",
                "role=getter"
            });
        ModelMBeanOperationInfo nullOperationInfo =
            new ModelMBeanOperationInfo("Null attribute",
                                        nullGetter,
                                        nullOperationDescriptor);
        Descriptor integerOperationDescriptor =
            new DescriptorSupport(new String[] {
                "name=getInteger",
                "descriptorType=operation",
                "role=getter"
            });
        ModelMBeanOperationInfo integerOperationInfo =
            new ModelMBeanOperationInfo("Integer attribute",
                                        integerGetter,
                                        integerOperationDescriptor);
        Descriptor hashtableOperationDescriptor =
            new DescriptorSupport(new String[] {
                "name=getHashtable",
                "descriptorType=operation",
                "role=getter"
            });
        ModelMBeanOperationInfo hashtableOperationInfo =
            new ModelMBeanOperationInfo("Hashtable attribute",
                                        hashtableGetter,
                                        hashtableOperationDescriptor);
        Descriptor mapOperationDescriptor =
            new DescriptorSupport(new String[] {
                "name=getMap",
                "descriptorType=operation",
                "role=getter"
            });
        ModelMBeanOperationInfo mapOperationInfo =
            new ModelMBeanOperationInfo("Map attribute",
                                        mapGetter,
                                        mapOperationDescriptor);
        Descriptor nullAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Null",
                "descriptorType=attribute",
                "getMethod=getNull"
            });
        ModelMBeanAttributeInfo nullAttributeInfo =
            new ModelMBeanAttributeInfo("Null",
                                        "java.lang.Object",
                                        "Null attribute",
                                        true,
                                        false,
                                        false,
                                        nullAttributeDescriptor);
        Descriptor integerAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Integer",
                "descriptorType=attribute",
                "getMethod=getInteger"
            });
        ModelMBeanAttributeInfo integerAttributeInfo =
            new ModelMBeanAttributeInfo("Integer",
                                        "int",
                                        "Integer attribute",
                                        true,
                                        false,
                                        false,
                                        integerAttributeDescriptor);
        Descriptor hashtableAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Hashtable",
                "descriptorType=attribute",
                "getMethod=getHashtable"
            });
        ModelMBeanAttributeInfo hashtableAttributeInfo =
            new ModelMBeanAttributeInfo("Hashtable",
                                        "java.util.Hashtable",
                                        "Hashtable attribute",
                                        true,
                                        false,
                                        false,
                                        hashtableAttributeDescriptor);
        Descriptor mapAttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Map",
                "descriptorType=attribute",
                "getMethod=getMap"
            });
        ModelMBeanAttributeInfo mapAttributeInfo =
            new ModelMBeanAttributeInfo("Map",
                                        "java.util.Map",
                                        "Map attribute",
                                        true,
                                        false,
                                        false,
                                        mapAttributeDescriptor);
        Descriptor null2AttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Null2",
                "descriptorType=attribute"
            });
        null2AttributeDescriptor.setField("default", null);
        ModelMBeanAttributeInfo null2AttributeInfo =
            new ModelMBeanAttributeInfo("Null2",
                                        "java.lang.Object",
                                        "Null2 attribute",
                                        true,
                                        false,
                                        false,
                                        null2AttributeDescriptor);
        Descriptor integer2AttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Integer2",
                "descriptorType=attribute"
            });
        integer2AttributeDescriptor.setField("default", 10);
        ModelMBeanAttributeInfo integer2AttributeInfo =
            new ModelMBeanAttributeInfo("Integer2",
                                        "int",
                                        "Integer2 attribute",
                                        true,
                                        false,
                                        false,
                                        integer2AttributeDescriptor);
        Descriptor hashtable2AttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Hashtable2",
                "descriptorType=attribute"
            });
        hashtable2AttributeDescriptor.setField("default", new Hashtable());
        ModelMBeanAttributeInfo hashtable2AttributeInfo =
            new ModelMBeanAttributeInfo("Hashtable2",
                                        "java.util.Hashtable",
                                        "Hashtable2 attribute",
                                        true,
                                        false,
                                        false,
                                        hashtable2AttributeDescriptor);
        Descriptor map2AttributeDescriptor =
            new DescriptorSupport(new String[] {
                "name=Map2",
                "descriptorType=attribute"
            });
        map2AttributeDescriptor.setField("default", new Hashtable());
        ModelMBeanAttributeInfo map2AttributeInfo =
            new ModelMBeanAttributeInfo("Map2",
                                        "java.util.Map",
                                        "Map2 attribute",
                                        true,
                                        false,
                                        false,
                                        map2AttributeDescriptor);
        ModelMBeanInfo mmbi = new ModelMBeanInfoSupport(
            Resource.class.getName(),
            "Resource MBean",
            new ModelMBeanAttributeInfo[] { nullAttributeInfo,
                                            integerAttributeInfo,
                                            hashtableAttributeInfo,
                                            mapAttributeInfo,
                                            null2AttributeInfo,
                                            integer2AttributeInfo,
                                            hashtable2AttributeInfo,
                                            map2AttributeInfo },
            null,
            new ModelMBeanOperationInfo[] { nullOperationInfo,
                                            integerOperationInfo,
                                            hashtableOperationInfo,
                                            mapOperationInfo },
            null);
        ModelMBean mmb = new RequiredModelMBean(mmbi);
        mmb.setManagedResource(resource, "ObjectReference");
        ObjectName mmbName = new ObjectName(":type=ResourceMBean");
        mbs.registerMBean(mmb, mmbName);
        System.out.println("\nTesting that we can call getNull()... ");
        try {
            Object o = mbs.getAttribute(mmbName, "Null");
            System.out.println("getNull() = " + o);
            System.out.println("Attribute's declared type = java.lang.Object");
            System.out.println("Returned value's type = null");
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getInteger()... ");
        try {
            Integer i = (Integer) mbs.getAttribute(mmbName, "Integer");
            System.out.println("getInteger() = " + i);
            System.out.println("Attribute's declared type = int");
            System.out.println("Returned value's type = " +
                               i.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getHashtable()... ");
        try {
            Hashtable h = (Hashtable) mbs.getAttribute(mmbName, "Hashtable");
            System.out.println("getHashtable() = " + h);
            System.out.println("Attribute's declared type = " +
                               "java.util.Hashtable");
            System.out.println("Returned value's type = " +
                               h.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getMap()... ");
        try {
            Map m = (Map) mbs.getAttribute(mmbName, "Map");
            System.out.println("getMap() = " + m);
            System.out.println("Attribute's declared type = " +
                               "java.util.Map");
            System.out.println("Returned value's type = " +
                               m.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getNull2()... ");
        try {
            Object o = mbs.getAttribute(mmbName, "Null2");
            System.out.println("getNull2() = " + o);
            System.out.println("Attribute's declared type = java.lang.Object");
            System.out.println("Returned value's type = null");
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getInteger2()... ");
        try {
            Integer i = (Integer) mbs.getAttribute(mmbName, "Integer2");
            System.out.println("getInteger2() = " + i);
            System.out.println("Attribute's declared type = int");
            System.out.println("Returned value's type = " +
                               i.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getHashtable2()... ");
        try {
            Hashtable h = (Hashtable) mbs.getAttribute(mmbName, "Hashtable2");
            System.out.println("getHashtable2() = " + h);
            System.out.println("Attribute's declared type = " +
                               "java.util.Hashtable");
            System.out.println("Returned value's type = " +
                               h.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("\nTesting that we can call getMap2()... ");
        try {
            Map m = (Map) mbs.getAttribute(mmbName, "Map2");
            System.out.println("getMap2() = " + m);
            System.out.println("Attribute's declared type = " +
                               "java.util.Map");
            System.out.println("Returned value's type = " +
                               m.getClass().getName());
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        if (ok)
            System.out.println("\nTest passed.\n");
        else {
            System.out.println("\nTest failed.\n");
            System.exit(1);
        }
    }
    public static class Resource {
        public Object getNull() {
            return null;
        }
        public int getInteger() {
            return 10;
        }
        public Hashtable getHashtable() {
            return new Hashtable();
        }
        public Map getMap() {
            return new Hashtable();
        }
    }
    private static Resource resource = new Resource();
}
