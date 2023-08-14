public class VoidDescriptor implements ReturnType {
    private static VoidDescriptor singleton = new VoidDescriptor();
    private VoidDescriptor(){}
    public static VoidDescriptor make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){v.visitVoidDescriptor(this);}
}
