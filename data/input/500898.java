public final class VMStack {
    native public static ClassLoader getCallingClassLoader();
    native public static ClassLoader getCallingClassLoader2();
    native public static Class<?> getStackClass2();
    native public static Class<?>[] getClasses(int maxDepth,
        boolean stopAtPrivileged);
    native public static StackTraceElement[] getThreadStackTrace(Thread t);
}
