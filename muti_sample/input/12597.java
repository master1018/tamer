public class SerializationTest {
    public static void main(String[] args) throws Exception {
        MBeanFeatureInfo mfi1 = new MBeanFeatureInfo("", "", null);
        test(mfi1);
        MBeanFeatureInfo mfi2 = new MBeanFeatureInfo("",
                                                     "",
                                                     ImmutableDescriptor.EMPTY_DESCRIPTOR);
        test(mfi2);
        MBeanFeatureInfo mfi3 = new MBeanFeatureInfo("", "",
                                     new ImmutableDescriptor(new String[] {"hi"},
                                                             new Object[] {"ha"}));
        test(mfi3);
        MBeanInfo mi1 = new MBeanInfo("",
                                      "",
                                      new MBeanAttributeInfo[]{},
                                      new MBeanConstructorInfo[]{},
                                      new MBeanOperationInfo[]{},
                                      new MBeanNotificationInfo[]{},
                                      null);
        test(mi1);
        MBeanInfo mi2 = new MBeanInfo("",
                                      "",
                                      new MBeanAttributeInfo[]{},
                                      new MBeanConstructorInfo[]{},
                                      new MBeanOperationInfo[]{},
                                      new MBeanNotificationInfo[]{},
                                      ImmutableDescriptor.EMPTY_DESCRIPTOR);
        test(mi2);
        MBeanInfo mi3 = new MBeanInfo("",
                                      "",
                                      new MBeanAttributeInfo[]{},
                                      new MBeanConstructorInfo[]{},
                                      new MBeanOperationInfo[]{},
                                      new MBeanNotificationInfo[]{},
                                      new ImmutableDescriptor(new String[] {"hi"},
                                                             new Object[] {"ha"}));
        test(mi3);
    }
    public static void test(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object newObj = ois.readObject();
        if (!obj.equals(newObj)) {
            throw new RuntimeException("Serialization/deserialization failed.");
        }
    }
}
