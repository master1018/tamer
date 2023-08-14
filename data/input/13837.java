abstract public class AbstractJavaHeapObjectVisitor
                implements JavaHeapObjectVisitor {
    abstract public void visit(JavaHeapObject other);
    public boolean exclude(JavaClass clazz, JavaField f) {
        return false;
    }
    public boolean mightExclude() {
        return false;
    }
}
