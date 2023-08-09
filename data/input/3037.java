public class Interval {
  private Object lowEndpoint;
  private Object highEndpoint;
  public Interval(Object lowEndpoint, Object highEndpoint) {
    this.lowEndpoint = lowEndpoint;
    this.highEndpoint = highEndpoint;
  }
  public Object getLowEndpoint() {
    return lowEndpoint;
  }
  public Object getHighEndpoint() {
    return highEndpoint;
  }
  public boolean overlaps(Interval arg, Comparator endpointComparator) {
    return overlaps(arg.getLowEndpoint(), arg.getHighEndpoint(), endpointComparator);
  }
  public boolean overlaps(Object otherLowEndpoint,
                          Object otherHighEndpoint,
                          Comparator endpointComparator) {
    if (endpointComparator.compare(highEndpoint, otherLowEndpoint) <= 0) {
      return false;
    }
    if (endpointComparator.compare(lowEndpoint, otherHighEndpoint) >= 0) {
      return false;
    }
    return true;
  }
  public String toString() {
    return "[ " + getLowEndpoint().toString() + ", " + getHighEndpoint().toString() + ")";
  }
}
