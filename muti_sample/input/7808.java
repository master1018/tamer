public class Bug4257318 {
  ResourceBundle res;
  boolean passed;
  public Bug4257318() {
    passed = false;
    res = ResourceBundle.getBundle("Bug4257318Res", new java.util.Locale("","",""));
  }
  boolean run() {
    String str = res.getString("Hello");
    passed = str.equals("Hello from the root bundle!");
    System.out.println("Root bundle string: " + str);
    return passed;
  }
  public static void main(String[] args) throws Exception {
    boolean passed;
    Bug4257318 test = new Bug4257318();
    passed = test.run();
    if (!passed) {
      throw new Exception("Bug4257318 Test: FAILED");
    }
  }
}
