public class LegacyMatcherProvider implements Serializable {
    private static final long serialVersionUID = -4143082656571251917L;
    private ArgumentsMatcher defaultMatcher;
    private boolean defaultMatcherSet;
    private transient Map<Method, ArgumentsMatcher> matchers = new HashMap<Method, ArgumentsMatcher>();
    public ArgumentsMatcher getMatcher(Method method) {
        if (!matchers.containsKey(method)) {
            if (!defaultMatcherSet) {
                setDefaultMatcher(MockControl.EQUALS_MATCHER);
            }
            matchers.put(method, defaultMatcher);
        }
        return matchers.get(method);
    }
    public void setDefaultMatcher(ArgumentsMatcher matcher) {
        if (defaultMatcherSet) {
            throw new RuntimeExceptionWrapper(
                    new IllegalStateException(
                            "default matcher can only be set once directly after creation of the MockControl"));
        }
        defaultMatcher = matcher;
        defaultMatcherSet = true;
    }
    public void setMatcher(Method method, ArgumentsMatcher matcher) {
        if (matchers.containsKey(method) && matchers.get(method) != matcher) {
            throw new RuntimeExceptionWrapper(new IllegalStateException(
                    "for method "
                            + method.getName()
                            + "("
                            + (method.getParameterTypes().length == 0 ? ""
                                    : "...")
                            + "), a matcher has already been set"));
        }
        matchers.put(method, matcher);
    }
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        Map<MethodSerializationWrapper, ArgumentsMatcher> map = (Map<MethodSerializationWrapper, ArgumentsMatcher>) stream
                .readObject();
        matchers = new HashMap<Method, ArgumentsMatcher>(map.size());
        for (Map.Entry<MethodSerializationWrapper, ArgumentsMatcher> entry : map
                .entrySet()) {
            try {
                Method method = entry.getKey().getMethod();
                matchers.put(method, entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new IOException(e.toString());
            }
        }
    }
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
        Map<MethodSerializationWrapper, ArgumentsMatcher> map = new HashMap<MethodSerializationWrapper, ArgumentsMatcher>(
                matchers.size());
        for (Map.Entry<Method, ArgumentsMatcher> matcher : matchers.entrySet()) {
            map.put(new MethodSerializationWrapper(matcher.getKey()), matcher
                    .getValue());
        }
        stream.writeObject(map);
    }
}
