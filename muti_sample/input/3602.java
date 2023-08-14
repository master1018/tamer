class A implements Serializable {}
class B implements Serializable {}
class Container implements Serializable {
    A a = new A();
}
class ReplacerObjectOutputStream extends ObjectOutputStream {
    static B b = new B();
  public ReplacerObjectOutputStream(OutputStream out) throws IOException {
    super(out);
    enableReplaceObject(true);
  }
  protected Object replaceObject(Object obj) throws IOException {
      if(obj instanceof A) {
          System.err.println("replaceObject(" + obj.toString() + ") with " +
                             b.toString());
          return b;
      } else return obj;
  }
}
public class BadSubstByReplace {
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        Container c = new Container();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ReplacerObjectOutputStream out =   new ReplacerObjectOutputStream(baos);
        out.writeObject(c);
        out.close();
        ObjectInputStream in =
            new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            c = (Container)in.readObject(); 
            throw new Error("Should have thrown ClassCastException");
        } catch ( ClassCastException e) {
            System.err.println("Caught expected exception " + e.toString());
            e.printStackTrace();
        } finally {
            in.close();
        }
    }
}
