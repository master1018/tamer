public class ProcessInfo {
  public ProcessInfo(String name, int pid) {
    this.name = name;
    this.pid = pid;
  }
  public String getName() {
    return name;
  }
  public int getPid() {
    return pid;
  }
  private String name;
  private int pid;
}
