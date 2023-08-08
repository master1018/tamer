public class DefaultTypeTargetHandler implements TypeTargetHandler {
    private static Log log = LogFactory.getLog(DefaultTypeTargetHandler.class);
    private Map<String, TargetHandler> typeHandlers;
    public DefaultTypeTargetHandler() {
        this(null);
    }
    public DefaultTypeTargetHandler(Map<String, TargetHandler> typeHandlers) {
        this.typeHandlers = typeHandlers;
        addDefaultTargetHandler();
    }
    public void handleTarget(WebAction webAction, WebObjectSource webObjectSource) throws ServletException, IOException {
        Target target = webAction.getTarget();
        if (target == null) {
            if (log.isDebugEnabled()) log.debug("the action " + webAction + " has no Target defined, handling is not needed.");
            return;
        }
        TargetHandler th = getTargetHandler(target.getType());
        if (th == null) throw new NullPointerException("no TargetHandler found for handling Target of type '" + target.getType() + "'");
        th.handleTarget(webAction, webObjectSource);
    }
    public Map<String, TargetHandler> getTypeHandlers() {
        return typeHandlers;
    }
    public void setTypeHandlers(Map<String, TargetHandler> typeHandlers) {
        this.typeHandlers = typeHandlers;
    }
    public TargetHandler getTargetHandler(String type) {
        Map<String, TargetHandler> h = getTypeHandlers();
        return h == null ? null : h.get(consistentTargetType(type));
    }
    public void addTargetHandler(String type, TargetHandler targetHandler) {
        if (this.typeHandlers == null) this.typeHandlers = new HashMap<String, TargetHandler>();
        this.typeHandlers.put(consistentTargetType(type), targetHandler);
        if (log.isDebugEnabled()) log.debug("add " + targetHandler + " for handling '" + consistentTargetType(type) + "' type target");
    }
    protected void addDefaultTargetHandler() {
        addTargetHandler(Target.FORWARD, new ForwardTargetHandler());
        addTargetHandler(Target.REDIRECT, new RedirectTargetHandler());
    }
    protected String consistentTargetType(String targetType) {
        return targetType == null ? null : targetType.toLowerCase();
    }
}
