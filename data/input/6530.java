public class Minst {
    private static int engaged = 0;
    public static void method_entry(int cnum, int mnum) {
        Class<Minst> x = Minst.class;
        synchronized ( x ) {
            if ( engaged > 0 ) {
                engaged = 0;
                String className = "Unknown";
                String methodName = "Unknown";
                Exception exp = new Exception();
                StackTraceElement[] stack = exp.getStackTrace();
                if (stack.length > 1) {
                    StackTraceElement location = stack[1];
                    className = location.getClassName();
                    methodName = location.getMethodName();
                }
                System.out.println("Reached method entry: " +
                     className + "." + methodName + "()");
                engaged++;
            }
        }
    }
}
