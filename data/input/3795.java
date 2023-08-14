public class ConstIterator implements Iterator {
  private Iterator iter;
  public ConstIterator(Iterator iter) {
    this.iter = iter;
  }
  public boolean hasNext() {
    return iter.hasNext();
  }
  public Object next() {
    return iter.next();
  }
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
