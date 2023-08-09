abstract class RTSettings
{
    public static synchronized boolean isStandaloneMode ()
    {
        return ! s_not_standalone;
    }
    public static synchronized void setStandaloneMode (final boolean standalone)
    {
        s_not_standalone = ! standalone;
    }
    private RTSettings () {} 
    private static boolean s_not_standalone;
} 
