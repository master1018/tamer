public class Options {
    private static final long serialVersionUID = 0;
    public static final Context.Key<Options> optionsKey =
        new Context.Key<Options>();
    private LinkedHashMap<String,String> values;
    public static Options instance(Context context) {
        Options instance = context.get(optionsKey);
        if (instance == null)
            instance = new Options(context);
        return instance;
    }
    protected Options(Context context) {
        values = new LinkedHashMap<String,String>();
        context.put(optionsKey, this);
    }
    public String get(String name) {
        return values.get(name);
    }
    public String get(OptionName name) {
        return values.get(name.optionName);
    }
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = get(name);
        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }
    public boolean isSet(String name) {
        return (values.get(name) != null);
    }
    public boolean isSet(OptionName name) {
        return (values.get(name.optionName) != null);
    }
    public boolean isSet(OptionName name, String value) {
        return (values.get(name.optionName + value) != null);
    }
    public boolean isUnset(String name) {
        return (values.get(name) == null);
    }
    public boolean isUnset(OptionName name) {
        return (values.get(name.optionName) == null);
    }
    public boolean isUnset(OptionName name, String value) {
        return (values.get(name.optionName + value) == null);
    }
    public void put(String name, String value) {
        values.put(name, value);
    }
    public void put(OptionName name, String value) {
        values.put(name.optionName, value);
    }
    public void putAll(Options options) {
        values.putAll(options.values);
    }
    public void remove(String name) {
        values.remove(name);
    }
    public Set<String> keySet() {
        return values.keySet();
    }
    public int size() {
        return values.size();
    }
    public boolean lint(String s) {
        return
            isSet(XLINT_CUSTOM, s) ||
            (isSet(XLINT) || isSet(XLINT_CUSTOM, "all")) &&
                isUnset(XLINT_CUSTOM, "-" + s);
    }
}
