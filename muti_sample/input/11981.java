public class InstrumentationHandoff
{
    private static Instrumentation      sInstrumentation;
    private
    InstrumentationHandoff()
        {
        }
    public static void
    premain(String options, Instrumentation inst)
    {
        System.out.println("InstrumentationHandoff JPLIS agent initialized");
        sInstrumentation = inst;
    }
    public static Instrumentation
    getInstrumentation()
    {
        return sInstrumentation;
    }
    public static Instrumentation
    getInstrumentationOrThrow()
    {
        Instrumentation result = getInstrumentation();
        if ( result == null )
            {
            throw new NullPointerException("instrumentation instance not initialized");
            }
        return result;
    }
}
