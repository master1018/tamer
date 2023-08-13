public class IsModifiableClassAgent
{
    public static boolean fail = false;
    public static boolean completed = false;
    public static void
    premain(    String agentArgs,
                Instrumentation instrumentation)
        {
            System.out.println("IsModifiableClassAgent started");
            Class[] allClasses = instrumentation.getAllLoadedClasses();
            int modCount = 0;
            int unmodCount = 0;
            for (int i = 0; i < allClasses.length; i++)
                {
                    Class klass = allClasses[i];
                    boolean isMod = instrumentation.isModifiableClass(klass);
                    if (isMod && klass.isArray()) {
                        System.err.println("Error: array class returned as modifiable: " + klass);
                        fail = true;
                    }
                    if (isMod && klass.isPrimitive()) {
                        System.err.println("Error: primitive class returned as modifiable: " + klass);
                        fail = true;
                    }
                    try {
                        instrumentation.retransformClasses(klass);
                        if (!isMod) {
                            System.err.println("Error: unmodifiable class retransformable: " + klass);
                            fail = true;
                        }
                    } catch (UnmodifiableClassException e) {
                        if (isMod) {
                            System.err.println("Error: modifiable class not retransformable: " + klass);
                            System.err.println("  exception: " + e);
                            fail = true;
                        }
                    } catch (Throwable e) {
                        System.err.println("Error: bad return from retransform: " + klass);
                        System.err.println("  ERROR: " + e);
                        fail = true;
                    }
                    if (isMod) {
                        ++modCount;
                    } else {
                        ++unmodCount;
                    }
                }
            System.out.println("modifiable: " + modCount + ". unmodifiable: " + unmodCount);
            completed = true;
        }
}
