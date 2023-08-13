public class PrimitiveClasses {
    public static void main(String[] args) throws Exception {
        Class[] primClasses = {
            boolean.class, byte.class, char.class, short.class,
            int.class, long.class, float.class, double.class
        };
        for (int i = 0; i < primClasses.length; i++) {
            Class pc = primClasses[i];
            if (new MarshalledObject(pc).get() != pc) {
                throw new Error();
            }
        }
    }
}
