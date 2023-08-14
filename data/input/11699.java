public class NullStackTrace
{
    static final int TIMES = 1000;
    public static void main(String[] args)
    {
        for (int i=0; i<TIMES; i++) {
            Thread t = new Thread();
            t.start();
            StackTraceElement[] ste = t.getStackTrace();
            if (ste == null)
                throw new RuntimeException("Failed: Thread.getStackTrace should not return null");
        }
    }
}
