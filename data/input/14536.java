public class BasicLineNumberMapping {
  private List infoList;
  public BasicLineNumberMapping() {
  }
  public void addLineNumberInfo(BasicLineNumberInfo info) {
    if (infoList == null) {
      infoList = new ArrayList();
    }
    infoList.add(info);
  }
  public void sort() {
    if (infoList == null) return;
    Collections.sort(infoList, new Comparator() {
        public int compare(Object o1, Object o2) {
          BasicLineNumberInfo l1 = (BasicLineNumberInfo) o1;
          BasicLineNumberInfo l2 = (BasicLineNumberInfo) o2;
          Address a1 = l1.getStartPC();
          Address a2 = l2.getStartPC();
          if (AddressOps.lt(a1, a2)) { return -1; }
          if (AddressOps.gt(a1, a2)) { return 1; }
          return 0;
        }
      });
  }
  public void recomputeEndPCs() {
    if (infoList == null) return;
    for (int i = 0; i < infoList.size() - 1; i++) {
      BasicLineNumberInfo i1 = get(i);
      BasicLineNumberInfo i2 = get(i + 1);
      i1.setEndPC(i2.getStartPC());
    }
  }
  public BasicLineNumberInfo lineNumberForPC(Address pc) throws DebuggerException {
    if (infoList == null) return null;
    return searchLineNumbers(pc, 0, infoList.size() - 1);
  }
  public void iterate(LineNumberVisitor v) {
    if (infoList == null) return;
    for (int i = 0; i < infoList.size(); i++) {
      v.doLineNumber(get(i));
    }
  }
  private BasicLineNumberInfo get(int i) {
    return (BasicLineNumberInfo) infoList.get(i);
  }
  private BasicLineNumberInfo searchLineNumbers(Address addr, int lowIdx, int highIdx) {
    if (highIdx < lowIdx) return null;
    if (lowIdx == highIdx) {
      if (check(addr, lowIdx)) {
        return get(lowIdx);
      } else {
        return null;
      }
    } else if (lowIdx == highIdx - 1) {
      if (check(addr, lowIdx)) {
        return get(lowIdx);
      } else if (check(addr, highIdx)) {
        return get(highIdx);
      } else {
        return null;
      }
    }
    int midIdx = (lowIdx + highIdx) >> 1;
    BasicLineNumberInfo info = get(midIdx);
    if (AddressOps.lt(addr, info.getStartPC())) {
      return searchLineNumbers(addr, lowIdx, midIdx);
    } else if (AddressOps.equal(addr, info.getStartPC())) {
      return info;
    } else {
      return searchLineNumbers(addr, midIdx, highIdx);
    }
  }
  private boolean check(Address addr, int idx) {
    BasicLineNumberInfo info = get(idx);
    if (AddressOps.lte(info.getStartPC(), addr)) {
      return true;
    } else {
      return false;
    }
  }
}
