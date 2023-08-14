public abstract class JSJavaObject extends DefaultScriptObject {
   public JSJavaObject(Oop oop, JSJavaFactory factory) {
       this.oop = oop;
       this.factory = factory;
   }
   public final Oop getOop() {
       return oop;
   }
   public boolean equals(Object o) {
      if (o == null || !(o instanceof JSJavaObject)) {
         return false;
      }
      JSJavaObject other = (JSJavaObject) o;
      return oop.equals(other.oop);
   }
   public int hashCode() {
      return oop.hashCode();
   }
   public String toString() {
      return "Object " + oop.getHandle().toString();
   }
   private final Oop oop;
   protected final JSJavaFactory factory;
}
