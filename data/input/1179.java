public class MyTheme extends DefaultMetalTheme {
    private final ColorUIResource primary1 = new ColorUIResource(255, 255, 0);
    private final ColorUIResource primary2 = new ColorUIResource(0, 255, 255);
    private final ColorUIResource primary3 = new ColorUIResource(255, 0, 255);
    public MyTheme() {
    }
    @Override
    public final String getName() {
        return "MyTheme";
    }
    @Override
    protected final ColorUIResource getPrimary1() {
        return this.primary1;
    }
    @Override
    protected final ColorUIResource getPrimary2() {
        return this.primary2;
    }
    @Override
    protected final ColorUIResource getPrimary3() {
        return this.primary3;
    }
}
