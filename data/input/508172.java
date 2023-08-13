abstract class Exceptions
{
    public static boolean unexpectedFailure (final Throwable t, final Class [] expected)
    {
        if (t == null) return false;
        if (expected == null) return true;
        final Class reClass = t.getClass ();
        for (int e = 0; e < expected.length; ++ e)
        {
            if (expected [e] == null) continue;
            if (expected [e].isAssignableFrom (reClass))
                return false;
        }
        return true;
    }
    private Exceptions () {} 
} 
