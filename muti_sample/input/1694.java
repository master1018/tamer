public class LocationValue extends ScopeValue {
  private Location location;
  public LocationValue(Location location) {
    this.location = location;
  }
  public boolean isLocation() {
    return true;
  }
  public Location getLocation() {
    return location;
  }
  LocationValue(DebugInfoReadStream stream) {
    location = new Location(stream);
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    getLocation().printOn(tty);
  }
};
