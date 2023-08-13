public class FindObjectByType implements HeapVisitor {
  private Klass type;
  private List results = new ArrayList();
  public FindObjectByType(Klass type) {
    this.type = type;
  }
  public List getResults() {
    return results;
  }
  public void prologue(long size) {}
  public void epilogue()          {}
  public boolean doObj(Oop obj) {
    if (obj.getKlass().equals(type)) {
      results.add(obj);
    }
        return false;
  }
}
