public abstract class BindingsImpl extends BindingsBase {
    protected Bindings global = null;
    protected Bindings local = null;
    public void setGlobal(Bindings n) {
        global = n;
    }
    public void setLocal(Bindings n) {
        local = n;
    }
    public  Set<Map.Entry<String, Object>> entrySet() {
        return new BindingsEntrySet(this);
    }
    public Object get(Object key) {
        checkKey(key);
        Object ret  = null;
        if ((local != null) && (null != (ret = local.get(key)))) {
            return ret;
        }
        ret = getImpl((String)key);
        if (ret != null) {
            return ret;
        } else if (global != null) {
            return global.get(key);
        } else {
            return null;
        }
    }
    public Object remove(Object key) {
        checkKey(key);
        Object ret = get(key);
        if (ret != null) {
            removeImpl((String)key);
        }
        return ret;
    }
}
