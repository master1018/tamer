public abstract class AbstractBuilder {
    protected Configuration configuration;
    protected static Set<String> containingPackagesSeen;
    protected static final boolean DEBUG = false;
    public AbstractBuilder(Configuration configuration) {
        this.configuration = configuration;
    }
    public abstract String getName();
    public abstract void build() throws IOException;
    protected void build(XMLNode node, Content contentTree) {
        String component = node.name;
        try {
            invokeMethod("build" + component,
                    new Class<?>[]{XMLNode.class, Content.class},
                    new Object[]{node, contentTree});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            configuration.root.printError("Unknown element: " + component);
            throw new DocletAbortException();
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            configuration.root.printError("Exception " +
                    e.getClass().getName() +
                    " thrown while processing element: " + component);
            throw new DocletAbortException();
        }
    }
    protected void buildChildren(XMLNode node, Content contentTree) {
        for (XMLNode child : node.children)
            build(child, contentTree);
    }
    protected void invokeMethod(String methodName, Class<?>[] paramClasses,
            Object[] params)
    throws Exception {
        if (DEBUG) {
            configuration.root.printError("DEBUG: " + this.getClass().getName() + "." + methodName);
        }
        Method method = this.getClass().getMethod(methodName, paramClasses);
        method.invoke(this, params);
    }
}
