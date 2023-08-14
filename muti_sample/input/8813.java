public class StripACC_SUPER {
    public static void main(String[] args) throws Exception {
        int access = StripACC_SUPER.class.getModifiers();
        if (java.lang.reflect.Modifier.isSynchronized(access))
            throw new Exception("ACC_SUPER bit is not being stripped");
    }
}
