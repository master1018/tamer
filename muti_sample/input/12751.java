public class TCOffsets
{
  public int offset (String name)
  {
    Integer value = (Integer)tcs.get (name);
    return value == null ? -1 : value.intValue ();
  } 
  public void set (SymtabEntry entry)
  {
    if (entry == null)
      offset += 8;
    else
    {
      tcs.put (entry.fullName (), new Integer (offset));
      offset += 4;
      String repID = Util.stripLeadingUnderscoresFromID (entry.repositoryID ().ID ());
      if (entry instanceof InterfaceEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ());
      else if (entry instanceof StructEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 4;
      else if (entry instanceof UnionEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 12;
      else if (entry instanceof EnumEntry)
      {
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 4;
        Enumeration e = ((EnumEntry)entry).elements ().elements ();
        while (e.hasMoreElements ())
          offset += alignStrLen ((String)e.nextElement ());
      }
      else if (entry instanceof StringEntry)
        offset += 4;
      else if (entry instanceof TypedefEntry)
      {
        offset += alignStrLen (repID) + alignStrLen (entry.name ());
        if (((TypedefEntry)entry).arrayInfo ().size () != 0)
          offset += 8;
      }
    }
  } 
  public int alignStrLen (String string)
  {
    int len = string.length () + 1;
    int align = 4 - (len % 4);
    if (align == 4) align = 0;
    return len + align + 4;
  } 
  public void setMember (SymtabEntry entry)
  {
    offset += alignStrLen (entry.name ());
    if (((TypedefEntry)entry).arrayInfo ().size () != 0)
      offset += 4;
  } 
  public int currentOffset ()
  {
    return offset;
  } 
  public void bumpCurrentOffset (int value)
  {
    offset += value;
  } 
  private Hashtable tcs    = new Hashtable ();
  private int       offset = 0;
} 
