class A { }
class MyObjectOutputStream extends ObjectOutputStream {
  public MyObjectOutputStream(OutputStream out) throws IOException {
    super(out);
    enableReplaceObject(true);
  }
  protected Object replaceObject(Object obj) throws IOException {
    if(obj instanceof A) return null;
    else return obj;
  }
}
public class ReplaceWithNull {
    public static void main(String args[]) throws IOException {
        A a = new A();
        MyObjectOutputStream out =
            new MyObjectOutputStream(new ByteArrayOutputStream());
        out.writeObject(a);     
        out.close();
    }
}
