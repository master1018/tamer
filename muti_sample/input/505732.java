abstract class IntegerFactory
{
    public static Integer getInteger (final int value)
    {
        synchronized (s_values)
        {
            final Object _result = s_values.get (value);
            if (_result == null)
            {
                final Integer result = new Integer (value);
                s_values.put (value, result);
                return result; 
            }
            return (Integer) _result;
        }
    }
    private IntegerFactory () {} 
    private static final IntObjectMap s_values = new IntObjectMap (16661);
} 
