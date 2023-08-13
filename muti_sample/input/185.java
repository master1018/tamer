public class TestTagInheritence extends TestAbstractClass implements TestInterface{
  public String testAbstractClass_method1(int p1, int p2) throws java.io.IOException,
java.lang.NullPointerException {
      return null;
  }
  public String testAbstractClass_method2(int p1, int p2) throws java.io.IOException,
java.lang.NullPointerException {
      return null;
  }
  public String testInterface_method1(int p1, int p2) throws java.io.IOException,
java.lang.NullPointerException
  {
      return null;
  }
  public String testInterface_method2(int p1, int p2) throws java.io.IOException,
java.lang.NullPointerException {
      return null;
  }
    public void methodInTestInterfaceForAbstractClass() {}
    public void testSuperSuperMethod() {}
    public void testBadInheritDocTag () {}
    public <X,Y> String testSuperSuperMethod(int p1, int p2) {
        return null;
    }
    public <X,Y> String testSuperSuperMethod2(int p1, int p2)
    throws java.io.IOException, java.lang.NullPointerException {
        return null;
    }
}
