public abstract class AbstractContextLoader {
    private static final Log logger = LogFactory.getLog(AbstractContextLoader.class);
    protected void register(String key, ServletContext context, Object obj) throws ServletException {
        Map moduleRegistry = null;
        Object registryObj = context.getAttribute(WebKeys.MODULE_REGISTRY);
        if (registryObj == null) {
            moduleRegistry = new FastHashMap();
        } else {
            moduleRegistry = (Map) registryObj;
        }
        if (moduleRegistry.containsKey(key)) {
            Object modueObj = moduleRegistry.get(key);
            if (!modueObj.equals(obj)) {
                throw new ServletException("Found invalid module object");
            }
        } else {
            moduleRegistry.put(key, obj);
        }
        context.setAttribute(WebKeys.MODULE_REGISTRY, moduleRegistry);
    }
}
