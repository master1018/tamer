public final class RhinoTopLevel extends ImporterTopLevel {
    RhinoTopLevel(Context cx, RhinoScriptEngine engine) {
        super(cx);
        this.engine = engine;
        new LazilyLoadedCtor(this, "JSAdapter",
                "com.sun.script.javascript.JSAdapter",
                false);
        JavaAdapter.init(cx, this, false);
        String names[] = { "bindings", "scope", "sync"  };
        defineFunctionProperties(names, RhinoTopLevel.class,
                ScriptableObject.DONTENUM);
    }
    public static Object bindings(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (args.length == 1) {
            Object arg = args[0];
            if (arg instanceof Wrapper) {
                arg = ((Wrapper)arg).unwrap();
            }
            if (arg instanceof ExternalScriptable) {
                ScriptContext ctx = ((ExternalScriptable)arg).getContext();
                Bindings bind = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
                return Context.javaToJS(bind,
                           ScriptableObject.getTopLevelScope(thisObj));
            }
        }
        return cx.getUndefinedValue();
    }
    public static Object scope(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (args.length == 1) {
            Object arg = args[0];
            if (arg instanceof Wrapper) {
                arg = ((Wrapper)arg).unwrap();
            }
            if (arg instanceof Bindings) {
                ScriptContext ctx = new SimpleScriptContext();
                ctx.setBindings((Bindings)arg, ScriptContext.ENGINE_SCOPE);
                Scriptable res = new ExternalScriptable(ctx);
                res.setPrototype(ScriptableObject.getObjectPrototype(thisObj));
                res.setParentScope(ScriptableObject.getTopLevelScope(thisObj));
                return res;
            }
        }
        return cx.getUndefinedValue();
    }
    public static Object sync(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (args.length == 1 && args[0] instanceof Function) {
            return new Synchronizer((Function)args[0]);
        } else {
            throw Context.reportRuntimeError("wrong argument(s) for sync");
        }
    }
    RhinoScriptEngine getScriptEngine() {
        return engine;
    }
    private RhinoScriptEngine engine;
}
