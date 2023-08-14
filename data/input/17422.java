public class EnumTypeImpl extends ClassTypeImpl implements EnumType {
    EnumTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
    }
    public EnumDeclaration getDeclaration() {
        return (EnumDeclaration) super.getDeclaration();
    }
    public void accept(TypeVisitor v) {
        v.visitEnumType(this);
    }
}
