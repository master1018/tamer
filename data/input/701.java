public abstract class LayerFactory {
    public static final Integer SHAPE_DIAMOND = new Integer(0);
    public static final Integer SHAPE_SQUARE = new Integer(1);
    protected static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayerFactory.class);
    private static LayerFactory instance = null;
    protected LayerFactory() {
    }
    public static LayerFactory getInstance() throws ResourceException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving instance");
        }
        if (instance == null) {
            instance = new LayerFactoryImpl();
        }
        return instance;
    }
    public abstract BoardLayer createLayer(FieldElementTypes t, FieldElementAppearances a, Direction dir) throws ResourceException;
    public abstract ActionLayer createActionLayer(String cmd, ShapeEnumeration e) throws ResourceException;
    public abstract ButtonLayer createButtonLayer(ButtonEnumeration type) throws ResourceException;
    public abstract ButtonLayer createCardLayer(int cardId) throws ResourceException;
    public abstract StaticLayer createStaticLayer(StaticEnumeration e) throws ResourceException;
    public abstract StaticLayer createStaticLayer(String text) throws ResourceException;
    public abstract Robot createRobot(RobotEnumeration robot, Direction d) throws ResourceException;
    public abstract StaticLayer createStaticLayer(StaticEnumeration e, String text) throws ResourceException;
}
