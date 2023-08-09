public class Test7042153 {
  static public class Bar { }
  static public class Foo { }
  static volatile boolean z;
  public static void main(String [] args) {
    Class cx = Bar.class;
    Class cy = Foo.class;
    z = (cx == cy);
  }
}
