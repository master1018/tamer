class Win32LDTEntry implements Serializable {
  private short limitLow;
  private short baseLow;
  private byte  baseMid;
  private byte  flags1;
  private byte  flags2;
  private byte  baseHi;
  private Win32LDTEntry() {}
  public Win32LDTEntry(short limitLow,
                       short baseLow,
                       byte  baseMid,
                       byte  flags1,
                       byte  flags2,
                       byte  baseHi) {
    this.limitLow = limitLow;
    this.baseLow  = baseLow;
    this.baseMid  = baseMid;
    this.flags1   = flags1;
    this.flags2   = flags2;
    this.baseHi   = baseHi;
  }
  public long  getBase()     { return ( (baseLow & 0xFFFF)       |
                                       ((baseMid & 0xFF) << 16)  |
                                       ((baseHi  & 0xFF) << 24)) & 0xFFFFFFFF; }
  public short getLimitLow() { return limitLow; }
  public short getBaseLow()  { return baseLow; }
  public byte  getBaseMid()  { return baseMid; }
  public byte  getBaseHi()   { return baseHi; }
  public int   getType()     { return (flags1 & 0x1F); }
  public int   getPrivilegeLevel() { return ((flags1 & 0x60) >> 5); }
  public boolean isSegmentPhysical() { return ((flags1 & 0x70) != 0); }
  public int getLimitHi() { return (flags2 & 0x0F); }
  public boolean isDefaultBig() { return ((flags2 & 0x40) != 0); }
  public boolean isPageGranular() { return ((flags2 & 0x80) != 0); }
}
