public class XResources_hy extends XResourceBundle
{
  public Object[][] getContents()
  {
    return new Object[][]
  {
    { "ui_language", "hy" }, { "help_language", "hy" }, { "language", "hy" },
    { "alphabet", new CharArrayWrapper(
      new char[]{ 0x0561, 0x0562, 0x0563, 0x0564, 0x0565, 0x0566, 0x0567,
                  0x0568, 0x0569, 0x056A, 0x056B, 0x056C, 0x056D, 0x056E,
                  0x056F, 0x0567, 0x0568, 0x0572, 0x0573, 0x0574, 0x0575,
                  0x0576, 0x0577, 0x0578, 0x0579, 0x057A, 0x057B, 0x057C,
                  0x057D, 0x057E, 0x057F, 0x0580, 0x0581, 0x0582, 0x0583,
                  0x0584 }) },
    { "tradAlphabet", new CharArrayWrapper(
      new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                  'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                  'Y', 'Z' }) },
    { "orientation", "LeftToRight" },
    { "numbering", "additive" },
    { "numberGroups", new IntArrayWrapper(new int[]{ 1000, 100, 10, 1 }) },
    { "digits", new CharArrayWrapper(
      new char[]{ 0x0561, 0x0562, 0x0563, 0x0564, 0x0565, 0x0566, 0x0567,
                  0x0568, 0x0569 }) },
    { "tens", new CharArrayWrapper(
      new char[]{ 0x056A, 0x056B, 0x056C, 0x056D, 0x056E, 0x056F, 0x0567,
                  0x0568, 0x0572 }) },
    { "hundreds", new CharArrayWrapper(
      new char[]{ 0x0573, 0x0574, 0x0575, 0x0576, 0x0577, 0x0578, 0x0579,
                  0x057A, 0x057B }) },
    { "thousands", new CharArrayWrapper(
      new char[]{ 0x057C, 0x057D, 0x057E, 0x057F, 0x0580, 0x0581, 0x0582,
                  0x0583, 0x0584 }) },
    { "tables", new StringArrayWrapper(new String[]{ "thousands", "hundreds", "tens", "digits" }) }
  };
  }
}
