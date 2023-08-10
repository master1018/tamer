public class UserContext {
    public static final String USER_CONTEXT_KEY = "velosurf.util.UserContext:session-key";
    public UserContext() {
    }
    public synchronized void setError(String err) {
        error = err;
    }
    public String getError() {
        return error;
    }
    public synchronized void setLocalizer(Localizer loc) {
        localizer = loc;
    }
    public synchronized void setLocale(Locale loc) {
        locale = loc;
    }
    public String localize(String str, Object... params) {
        Logger.debug("@@@@ localize: str=<" + str + ">, params=" + Arrays.asList(params) + ", result=" + MessageFormat.format(str, params));
        if (localizer == null) {
            return MessageFormat.format(str, params);
        } else if (params.length == 0) {
            return localizer.get(str);
        } else {
            return localizer.get(str, params);
        }
    }
    public synchronized void clearValidationErrors() {
        validationErrors.clear();
    }
    public synchronized void addValidationError(String err) {
        validationErrors.add(err);
    }
    public synchronized List<String> getValidationErrors() {
        if (validationErrors.size() == 0) {
            return null;
        }
        List<String> ret = new ArrayList<String>(validationErrors);
        validationErrors.clear();
        return ret;
    }
    public Object get(String key) {
        if ("error".equalsIgnoreCase(key)) {
            return getError();
        } else if ("validationErrors".equalsIgnoreCase(key)) {
            return getValidationErrors();
        } else if ("locale".equalsIgnoreCase(key)) {
            return getLocale();
        }
        return null;
    }
    public Locale getLocale() {
        if (localizer != null) {
            return localizer.getLocale();
        } else if (locale != null) {
            return locale;
        } else {
            return Locale.getDefault();
        }
    }
    public synchronized void setLastInsertedID(Entity entity, long id) {
        lastInsertedIDs.put(entity, id);
    }
    public long getLastInsertedID(Entity entity) {
        Long id = lastInsertedIDs.get(entity);
        if (id != null) {
            return id.longValue();
        } else {
            Logger.error("getLastInsertID called for entity '" + entity + "' which doesn't have any");
            return -1;
        }
    }
    private String error = "";
    private List<String> validationErrors = new ArrayList<String>();
    private Localizer localizer = null;
    private Locale locale = null;
    private Map<Entity, Long> lastInsertedIDs = new HashMap<Entity, Long>();
}
