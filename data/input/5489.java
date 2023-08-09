public class ReversePtrs  {
  private HashMap rp;
  public ReversePtrs() {
    rp = new HashMap();
  }
  public void put(LivenessPathElement from, Oop to) {
    if (to == null) return;
    ArrayList al = (ArrayList) rp.get((Object) to);
    if (al == null) al = new ArrayList();
    al.add(0, (Object) from);
    rp.put((Object) to, (Object) al);
  }
  public ArrayList get(Oop obj) {
    return (ArrayList) rp.get((Object)obj);
  }
}
