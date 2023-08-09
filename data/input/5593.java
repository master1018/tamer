public class ObjectHistogram implements HeapVisitor {
  public ObjectHistogram() { map = new HashMap(); }
  private HashMap map;
  public void prologue(long size) {}
  public boolean doObj(Oop obj) {
    Klass klass = obj.getKlass();
    if (!map.containsKey(klass)) map.put(klass, new ObjectHistogramElement(klass));
    ((ObjectHistogramElement) map.get(klass)).updateWith(obj);
        return false;
  }
  public void epilogue() {}
  public List getElements() {
    List list = new ArrayList();
    list.addAll(map.values());
    Collections.sort(list, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((ObjectHistogramElement) o1).compare((ObjectHistogramElement) o2);
      }
    });
    return list;
  }
  public void print() { printOn(System.out); }
  public void printOn(PrintStream tty) {
    List list = getElements();
    ObjectHistogramElement.titleOn(tty);
    Iterator iterator = list.listIterator();
    int num=0;
    int totalCount=0;
    int totalSize=0;
    while (iterator.hasNext()) {
      ObjectHistogramElement el = (ObjectHistogramElement) iterator.next();
      num++;
      totalCount+=el.getCount();
      totalSize+=el.getSize();
      tty.print(num + ":" + "\t\t");
      el.printOn(tty);
    }
    tty.println("Total : " + "\t" + totalCount + "\t" + totalSize);
  }
}
