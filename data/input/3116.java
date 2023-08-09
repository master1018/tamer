public class VMSupportsCS8 {
    public static void main(String[] args) throws Exception {
        if (System.getProperty("sun.cpu.isalist").matches
            (".*\\b(sparcv9|pentium_pro|ia64|amd64).*")
            ||
            System.getProperty("os.arch").matches
            (".*\\b(ia64|amd64).*")) {
            System.out.println("This system is known to have hardware CS8");
            Class klass = Class.forName("java.util.concurrent.atomic.AtomicLong");
            Field field = klass.getDeclaredField("VM_SUPPORTS_LONG_CAS");
            field.setAccessible(true);
            boolean VMSupportsCS8 = field.getBoolean(null);
            if (! VMSupportsCS8)
                throw new Exception("Unexpected value for VMSupportsCS8");
        }
    }
}
