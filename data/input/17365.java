final class JavaAdapter extends ScriptableObject implements Function {
    private JavaAdapter(Invocable engine) {
        this.engine = engine;
    }
    static void init(Context cx, Scriptable scope, boolean sealed)
    throws RhinoException {
        RhinoTopLevel topLevel = (RhinoTopLevel) scope;
        Invocable engine = topLevel.getScriptEngine();
        JavaAdapter obj = new JavaAdapter(engine);
        obj.setParentScope(scope);
        obj.setPrototype(getFunctionPrototype(scope));
        ScriptableObject.putProperty(topLevel, "JavaAdapter", obj);
    }
    public String getClassName() {
        return "JavaAdapter";
    }
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
            Object[] args) throws RhinoException {
        return construct(cx, scope, args);
    }
    public Scriptable construct(Context cx, Scriptable scope, Object[] args)
    throws RhinoException {
        if (args.length == 2) {
            Class<?> clazz = null;
            Object obj1 = args[0];
            if (obj1 instanceof Wrapper) {
                Object o = ((Wrapper)obj1).unwrap();
                if (o instanceof Class && ((Class)o).isInterface()) {
                    clazz = (Class) o;
                }
            } else if (obj1 instanceof Class) {
                if (((Class)obj1).isInterface()) {
                    clazz = (Class) obj1;
                }
            }
            if (clazz == null) {
                throw Context.reportRuntimeError("JavaAdapter: first arg should be interface Class");
            }
            Scriptable topLevel = ScriptableObject.getTopLevelScope(scope);
            return cx.toObject(engine.getInterface(args[1],  clazz), topLevel);
        } else {
            throw Context.reportRuntimeError("JavaAdapter requires two arguments");
        }
    }
    private Invocable engine;
}
