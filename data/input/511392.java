@TestTargetClass(StackTraceElement.class) 
public class StackTraceElementOriginal extends TestCase {
    public void pureJavaMethod(Object test) throws Exception {
        throw new Exception("pure java method");
    }
    native public void pureNativeMethod(Object test);
}
