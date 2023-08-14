public class Test6186134 {
  int num = 0;
  public Test6186134(int n) {
    num = n;
  }
  public boolean more() {
    return num-- > 0;
  }
  public ArrayList test1() {
    ArrayList res = new ArrayList();
    int maxResults = Integer.MAX_VALUE;
    int n = 0;
    boolean more = more();
    while ((n++ < maxResults) && more) {
      res.add(new Object());
      more = more();
    }
    return res;
  }
  public static void main(String[] pars) {
    int n = Integer.parseInt(pars[0]);
    for (int i=0; i<n; i++) {
      Test6186134 t = new Test6186134(10);
      int size = t.test1().size();
      if (size != 10) {
        System.out.println("wrong size: " + size +", should be 10");
        System.exit(97);
      }
    }
    System.out.println("Passed");
  }
}
