public class InterfaceState
{
  public static final int Private   = 0,
                          Protected = 1,
                          Public    = 2;
  public InterfaceState (int m, TypedefEntry e)
  {
    modifier = m;
    entry    = e;
    if (modifier < Private || modifier > Public)
      modifier = Public;
  } 
  public int          modifier = Public;
  public TypedefEntry entry    = null;
} 
