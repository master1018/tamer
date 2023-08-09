public abstract class AbstractScriptEngine  implements ScriptEngine {
    protected ScriptContext context;
    public AbstractScriptEngine() {
        context = new SimpleScriptContext();
    }
    public AbstractScriptEngine(Bindings n) {
        this();
        if (n == null) {
            throw new NullPointerException("n is null");
        }
        context.setBindings(n, ScriptContext.ENGINE_SCOPE);
    }
    public void setContext(ScriptContext ctxt) {
        if (ctxt == null) {
            throw new NullPointerException("null context");
        }
        context = ctxt;
    }
    public ScriptContext getContext() {
        return context;
    }
    public Bindings getBindings(int scope) {
        if (scope == ScriptContext.GLOBAL_SCOPE) {
            return context.getBindings(ScriptContext.GLOBAL_SCOPE);
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            return context.getBindings(ScriptContext.ENGINE_SCOPE);
        } else {
            throw new IllegalArgumentException("Invalid scope value.");
        }
    }
    public void setBindings(Bindings bindings, int scope) {
        if (scope == ScriptContext.GLOBAL_SCOPE) {
            context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);;
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);;
        } else {
            throw new IllegalArgumentException("Invalid scope value.");
        }
    }
    public void put(String key, Object value) {
        Bindings nn = getBindings(ScriptContext.ENGINE_SCOPE);
        if (nn != null) {
            nn.put(key, value);
        }
    }
    public Object get(String key) {
        Bindings nn = getBindings(ScriptContext.ENGINE_SCOPE);
        if (nn != null) {
            return nn.get(key);
        }
        return null;
    }
    public Object eval(Reader reader, Bindings bindings ) throws ScriptException {
        ScriptContext ctxt = getScriptContext(bindings);
        return eval(reader, ctxt);
    }
    public Object eval(String script, Bindings bindings) throws ScriptException {
        ScriptContext ctxt = getScriptContext(bindings);
        return eval(script , ctxt);
    }
    public Object eval(Reader reader) throws ScriptException {
        return eval(reader, context);
    }
    public Object eval(String script) throws ScriptException {
        return eval(script, context);
    }
    protected ScriptContext getScriptContext(Bindings nn) {
        SimpleScriptContext ctxt = new SimpleScriptContext();
        Bindings gs = getBindings(ScriptContext.GLOBAL_SCOPE);
        if (gs != null) {
            ctxt.setBindings(gs, ScriptContext.GLOBAL_SCOPE);
        }
        if (nn != null) {
            ctxt.setBindings(nn,
                    ScriptContext.ENGINE_SCOPE);
        } else {
            throw new NullPointerException("Engine scope Bindings may not be null.");
        }
        ctxt.setReader(context.getReader());
        ctxt.setWriter(context.getWriter());
        ctxt.setErrorWriter(context.getErrorWriter());
        return ctxt;
    }
}
