public class OpenTypeDescriptorTest {
    public static void main(String[] args) throws Exception {
        Descriptor constraints =
                new ImmutableDescriptor("defaultValue=25",
                                        "minValue=3");
        Date now = new Date();
        Date then = new Date(System.currentTimeMillis() - 86400000);
        final DescriptorRead[] testObjects = {
            new OpenMBeanAttributeInfoSupport("name", "descr",
                                              SimpleType.STRING,
                                              true, false, false),
            new OpenMBeanAttributeInfoSupport("name", "descr",
                                              SimpleType.INTEGER,
                                              false, true, false, constraints),
            new OpenMBeanAttributeInfoSupport("name", "descr",
                                              SimpleType.BOOLEAN,
                                              true, false, false, true),
            new OpenMBeanAttributeInfoSupport("name", "descr",
                                              SimpleType.FLOAT,
                                              true, true, false,
                                              1.0f, 0.0f, 2.0f),
            new OpenMBeanAttributeInfoSupport("name", "descr",
                                              SimpleType.DATE,
                                              true, false, false,
                                              now, new Date[] {now, then}),
            new OpenMBeanParameterInfoSupport("name", "descr",
                                              SimpleType.STRING),
            new OpenMBeanParameterInfoSupport("name", "descr",
                                              SimpleType.INTEGER, constraints),
            new OpenMBeanParameterInfoSupport("name", "descr",
                                              SimpleType.BOOLEAN, true),
            new OpenMBeanParameterInfoSupport("name", "descr",
                                              SimpleType.FLOAT,
                                              1.0f, 0.0f, 2.0f),
            new OpenMBeanParameterInfoSupport("name", "descr",
                                              SimpleType.DATE,
                                              now, new Date[] {now, then}),
            new OpenMBeanOperationInfoSupport("name", "descr", null,
                                              ArrayType.getPrimitiveArrayType(
                                                      int[][].class),
                                              ACTION),
            new OpenMBeanOperationInfoSupport("name", "descr", null,
                                              ArrayType.getArrayType(
                                                      SimpleType.INTEGER),
                                              ACTION, constraints),
        };
        for (DescriptorRead x : testObjects) {
            OpenType descriptorType = (OpenType)
                    x.getDescriptor().getFieldValue("openType");
            OpenType openType;
            if (x instanceof OpenMBeanParameterInfo)
                openType = ((OpenMBeanParameterInfo) x).getOpenType();
            else if (x instanceof OpenMBeanOperationInfo)
                openType = ((OpenMBeanOperationInfo) x).getReturnOpenType();
            else
                throw new Exception("Can't get OpenType for " + x.getClass());
            if (openType.equals(descriptorType))
                System.out.println("OK: " + x);
            else {
                failure("OpenType is " + openType + ", descriptor says " +
                        descriptorType + " for " + x);
            }
        }
        System.out.println("Testing serial compatibility with Java "+
                MBeanFeatureInfoSerialStore.SERIALIZER_VM_VERSION+
                " "+
                MBeanFeatureInfoSerialStore.SERIALIZER_VM_VENDOR);
        for (String key : MBeanFeatureInfoSerialStore.keySet() ) {
            MBeanFeatureInfo f = MBeanFeatureInfoSerialStore.get(key);
            DescriptorRead x = (DescriptorRead)f;
            OpenType descriptorType = (OpenType)
                    x.getDescriptor().getFieldValue("openType");
            OpenType openType;
            if (x instanceof OpenMBeanParameterInfo)
                openType = ((OpenMBeanParameterInfo) x).getOpenType();
            else if (x instanceof OpenMBeanOperationInfo)
                openType = ((OpenMBeanOperationInfo) x).getReturnOpenType();
            else
                throw new Exception("Can't get OpenType for " + key +": "+
                        x.getClass());
            if (openType.equals(descriptorType))
                System.out.println("OK "+key+": " + x);
            else {
                failure("OpenType for "+key+" is " + openType +
                        ", descriptor says " +
                        descriptorType + " for " + x);
            }
        }
        Descriptor d =
                new ImmutableDescriptor(new String[] {"openType"},
                                        new Object[] {SimpleType.STRING});
        for (Type t : Type.values()) {
            try {
                switch (t) {
                    case ATTR:
                        new OpenMBeanAttributeInfoSupport("name", "descr",
                                                          SimpleType.INTEGER,
                                                          true, true, false, d);
                        break;
                    case PARAM:
                        new OpenMBeanParameterInfoSupport("name", "descr",
                                                          SimpleType.INTEGER, d);
                        break;
                    case OPER:
                        new OpenMBeanOperationInfoSupport("name", "descr", null,
                                                          SimpleType.INTEGER,
                                                          ACTION, d);
                        break;
                }
                failure("did not get expected exception for " + t);
            } catch (IllegalArgumentException e) {
                System.out.println("OK: got expected exception for " + t);
            } catch (Exception e) {
                failure("wrong exception for " + t + ": " + e);
            }
        }
        if (failed != null)
            throw new Exception(failed);
        System.out.println("Test passed");
    }
    private static enum Type {ATTR, PARAM, OPER}
    private static void failure(String what) {
        System.out.println("FAILED: what");
        failed = what;
    }
    private static String failed;
}
