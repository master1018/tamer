public class LivenessPathList {
  public LivenessPathList() {
    list = new ArrayList();
  }
  public int size() {
    return list.size();
  }
  public LivenessPath get(int i) {
    return (LivenessPath) list.get(i);
  }
  void add(LivenessPath path) {
    list.add(path);
  }
  void remove(LivenessPath path) {
    list.remove(path);
  }
  private ArrayList list;
}
