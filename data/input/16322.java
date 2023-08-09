public class ObjectHistogramElement {
  private Klass klass;
  private long  count; 
  private long  size;  
  public ObjectHistogramElement(Klass k) {
     klass = k;
     count = 0;
     size  = 0;
  }
  public void updateWith(Oop obj) {
    count = count + 1;
    size  = size  + obj.getObjectSize();
  }
  public int compare(ObjectHistogramElement other) {
    return (int) (other.size - size);
  }
  public Klass getKlass() {
    return klass;
  }
  public long getCount() {
    return count;
  }
  public long getSize() {
    return size;
  }
  private String getInternalName(Klass k) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    getKlass().printValueOn(new PrintStream(bos));
    return "* " + bos.toString();
  }
  public String getDescription() {
     Klass k = getKlass();
     if (k instanceof InstanceKlass) {
        return k.getName().asString().replace('/', '.');
     } else if (k instanceof ArrayKlass) {
       ArrayKlass ak = (ArrayKlass) k;
       if (k instanceof TypeArrayKlass) {
          TypeArrayKlass tak = (TypeArrayKlass) ak;
          return tak.getElementTypeName() + "[]";
       } else if (k instanceof ObjArrayKlass) {
          ObjArrayKlass oak = (ObjArrayKlass) ak;
          if (oak.equals(VM.getVM().getUniverse().systemObjArrayKlassObj())) {
            return "* System ObjArray";
          }
          Klass bottom = oak.getBottomKlass();
          int dim = (int) oak.getDimension();
          StringBuffer buf = new StringBuffer();
          if (bottom instanceof TypeArrayKlass) {
            buf.append(((TypeArrayKlass) bottom).getElementTypeName());
          } else if (bottom instanceof InstanceKlass) {
            buf.append(bottom.getName().asString().replace('/', '.'));
          } else {
            throw new RuntimeException("should not reach here");
          }
          for (int i=0; i < dim; i++) {
            buf.append("[]");
          }
          return buf.toString();
       }
    }
    return getInternalName(k);
  }
  public static void titleOn(PrintStream tty) {
    tty.println("Object Histogram:");
    tty.println();
    tty.println("num " + "\t" + "  #instances" + "\t" + "#bytes" + "\t" + "Class description");
    tty.println("--------------------------------------------------------------------------");
  }
  public void printOn(PrintStream tty) {
    tty.print(count + "\t" + size + "\t");
    tty.print(getDescription());
    tty.println();
  }
}
