public class ValueRepositoryId
{
  private MessageDigest sha;       
  private int           index;     
  private Hashtable     types;     
  private String        hashcode;  
  public ValueRepositoryId ()
  {
    try
    {
      sha = MessageDigest.getInstance ("SHA-1");
    }
    catch (Exception exception)
    {}
    index    = 0;
    types    = new Hashtable ();
    hashcode = null;
  } 
  public void addValue (int value)
  {
    sha.update ((byte)((value >> 24) & 0x0F));
    sha.update ((byte)((value >> 16) & 0x0F));
    sha.update ((byte)((value >>  8) & 0x0F));
    sha.update ((byte)(value & 0x0F));
    index++;
  } 
  public void addType (SymtabEntry entry)
  {
    types.put (entry, new Integer (index));
  }
  public boolean isNewType (SymtabEntry entry)
  {
    Object index = types.get (entry);
    if (index == null)
    {
      addType (entry);
      return true;
    }
    addValue (0xFFFFFFFF);
    addValue (((Integer)index).intValue ());
    return false;
  } 
  public String getHashcode ()
  {
    if (hashcode == null)
    {
      byte [] digest = sha.digest ();
      hashcode = hexOf (digest[0]) + hexOf (digest[1]) +
                 hexOf (digest[2]) + hexOf (digest[3]) +
                 hexOf (digest[4]) + hexOf (digest[5]) +
                 hexOf (digest[6]) + hexOf (digest[7]);
    }
    return hashcode;
  } 
  private static String hexOf (byte value)
  {
    int d1 = (value >> 4) & 0x0F;
    int d2 = value & 0x0F;
    return "0123456789ABCDEF".substring (d1, d1 + 1) +
           "0123456789ABCDEF".substring (d2, d2 + 1);
  } 
} 
