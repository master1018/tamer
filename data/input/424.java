public abstract class Expression
{
  public abstract Object evaluate () throws EvaluationException;
  public void value (Object value)
  {
    _value = value;
  }
  public Object value ()
  {
    return _value;
  }
  public void rep (String rep)
  {
    _rep = rep;
  }
  public String rep ()
  {
    return _rep;
  }
  public void type (String type)
  {
    _type = type;
  }
  public String type ()
  {
    return _type;
  }
  protected static String defaultType (String targetType)
  {
    return (targetType == null) ? new String ("") : targetType;
  } 
  public Object coerceToTarget (Object obj)
  {
    if (obj instanceof BigInteger)
    {
      if (type ().indexOf ("unsigned") >= 0)
        return toUnsignedTarget ((BigInteger)obj);
      else
        return toSignedTarget ((BigInteger)obj);
    }
    return obj;
  } 
  protected BigInteger toUnsignedTarget (BigInteger b)
  {
    if (type ().equals ("unsigned short")) 
    {
      if (b != null && b.compareTo (zero) < 0) 
        return b.add (twoPow16);
    }
    else if (type ().equals ("unsigned long"))
    {
      if (b != null && b.compareTo (zero) < 0)
        return b.add (twoPow32);
    }
    else if (type ().equals ("unsigned long long"))
    {
      if (b != null && b.compareTo (zero) < 0)
        return b.add (twoPow64);
    }
    return b;
  } 
  protected BigInteger toSignedTarget (BigInteger b)
  {
    if (type ().equals ("short"))
    {
      if (b != null && b.compareTo (sMax) > 0)
        return b.subtract (twoPow16);
    }
    else if (type ().equals ("long"))
    {
      if (b != null && b.compareTo (lMax) > 0)
        return b.subtract (twoPow32);
    }
    else if (type ().equals ("long long"))
    {
      if (b != null && b.compareTo (llMax) > 0)
        return b.subtract (twoPow64);
    }
    return b;
  } 
  protected BigInteger toUnsigned (BigInteger b)
  {
    if (b != null && b.signum () == -1)
      if (type ().equals ("short"))
        return b.add (twoPow16);
      else if (type ().equals ("long"))
        return b.add (twoPow32);
      else if (type ().equals ("long long"))
        return b.add (twoPow64);
    return b;
  }
  public static final BigInteger negOne = BigInteger.valueOf (-1);
  public static final BigInteger zero   = BigInteger.valueOf (0);
  public static final BigInteger one    = BigInteger.valueOf (1);
  public static final BigInteger two    = BigInteger.valueOf (2);
  public static final BigInteger twoPow15 = two.pow (15);
  public static final BigInteger twoPow16 = two.pow (16);
  public static final BigInteger twoPow31 = two.pow (31);
  public static final BigInteger twoPow32 = two.pow (32);
  public static final BigInteger twoPow63 = two.pow (63);
  public static final BigInteger twoPow64 = two.pow (64);
  public static final BigInteger sMax = BigInteger.valueOf (Short.MAX_VALUE);
  public static final BigInteger sMin = BigInteger.valueOf (Short.MAX_VALUE);
  public static final BigInteger usMax = sMax.multiply (two).add (one);
  public static final BigInteger usMin = zero;
  public static final BigInteger lMax = BigInteger.valueOf (Integer.MAX_VALUE);
  public static final BigInteger lMin = BigInteger.valueOf (Integer.MAX_VALUE);
  public static final BigInteger ulMax = lMax.multiply (two).add (one);
  public static final BigInteger ulMin = zero;
  public static final BigInteger llMax = BigInteger.valueOf (Long.MAX_VALUE);
  public static final BigInteger llMin = BigInteger.valueOf (Long.MIN_VALUE);
  public static final BigInteger ullMax = llMax.multiply (two).add (one);
  public static final BigInteger ullMin = zero;
  private Object _value = null;
  private String _rep   = null;
  private String _type  = null;
} 
