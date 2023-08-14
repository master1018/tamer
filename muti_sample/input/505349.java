public final class LoggingPermission extends BasicPermission implements Guard,
        Serializable {
    private static final long serialVersionUID = 63564341580231582L;
    public LoggingPermission(String name, String actions) {
        super(name, actions);
        if (!"control".equals(name)) { 
            throw new IllegalArgumentException(Messages.getString("logging.6")); 
        }
        if (null != actions && !"".equals(actions)) { 
            throw new IllegalArgumentException(Messages.getString("logging.7")); 
        }
    }
}
