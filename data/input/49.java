public class Sign extends Entity {
    private static final Logger logger = Log4J.getLogger(Sign.class);
    private static final List<String> NON_OBSTACLE_CLASSES = Arrays.asList("book_blue", "book_red", "transparent");
    public static void generateRPClass() {
        try {
            RPClass sign = new RPClass("sign");
            sign.isA("entity");
            sign.add("text", RPClass.LONG_STRING);
            sign.add("class", RPClass.STRING);
        } catch (RPClass.SyntaxException e) {
            logger.error("cannot generate RPClass", e);
        }
    }
    public Sign() throws AttributeNotFoundException {
        super();
        put("type", "sign");
    }
    public void setText(String text) {
        put("text", text);
    }
    public void setClass(String clazz) {
        put("class", clazz);
    }
    @Override
    public boolean isObstacle(Entity entity) {
        return !(has("class") && NON_OBSTACLE_CLASSES.contains(get("class")));
    }
}
