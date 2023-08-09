public class SimpleClassTypeSignature implements FieldTypeSignature {
    private boolean dollar;
    private String name;
    private TypeArgument[] typeArgs;
    private SimpleClassTypeSignature(String n, boolean dollar, TypeArgument[] tas) {
        name = n;
        this.dollar = dollar;
        typeArgs = tas;
    }
    public static SimpleClassTypeSignature make(String n,
                                                boolean dollar,
                                                TypeArgument[] tas){
        return new SimpleClassTypeSignature(n, dollar, tas);
    }
    public boolean getDollar(){return dollar;}
    public String getName(){return name;}
    public TypeArgument[] getTypeArguments(){return typeArgs;}
    public void accept(TypeTreeVisitor<?> v){
        v.visitSimpleClassTypeSignature(this);
    }
}
