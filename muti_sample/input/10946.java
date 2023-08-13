public class Test4948761 {
    private static final Class[] TYPES = {
            char.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
    };
    public static void main(String[] args) throws Exception {
        for (Class type : TYPES) {
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(type);
            if (pds.length > 0) {
                throw new Error("primitive type should not have properties");
            }
        }
    }
}
