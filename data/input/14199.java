public class MonitorValue {
  private ScopeValue owner;
  private Location   basicLock;
  private boolean    eliminated;
  public MonitorValue(DebugInfoReadStream stream) {
    basicLock = new Location(stream);
    owner     = ScopeValue.readFrom(stream);
    eliminated= stream.readBoolean();
  }
  public ScopeValue owner()     { return owner; }
  public Location   basicLock() { return basicLock; }
  public boolean   eliminated() { return eliminated; }
  public void printOn(PrintStream tty) {
    tty.print("monitor{");
    owner().printOn(tty);
    tty.print(",");
    basicLock().printOn(tty);
    tty.print("}");
    if (eliminated) {
      tty.print(" (eliminated)");
    }
  }
}
