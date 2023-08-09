public class ColorCustomizationTest
{
    final static int WIDTH = 200;
    final static int HEIGHT = 100;
    static NimbusLookAndFeel nimbus;
    final JLabel label;
    final Graphics g;
    ColorCustomizationTest() {
        label = new JLabel();
        label.setSize(200, 100);
        g = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB).getGraphics();
    }
    public static void main(String[] args) throws Exception {
        nimbus = new NimbusLookAndFeel();
        try {
            UIManager.setLookAndFeel(nimbus);
        } catch (UnsupportedLookAndFeelException e) {
            throw new Error("Unable to set Nimbus LAF");
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                new ColorCustomizationTest().test();
            }
        });
    }
    void check(Color c) {
        SwingUtilities.updateComponentTreeUI(label);
        label.paint(g);
        if (label.getBackground().getRGB() != c.getRGB()) {
            System.err.println("Color mismatch!");
            System.err.println("   found: " + label.getBackground());
            System.err.println("   expected: " + c);
            throw new RuntimeException("Test failed");
        }
    }
    void test() {
        testOverrides();
        testInheritance();
        testNames();
        testBaseColor();
    }
    void testOverrides() {
        Color defaultColor = label.getBackground();
        UIDefaults defs = new UIDefaults();
        defs.put("Label.background", new ColorUIResource(Color.RED));
        label.putClientProperty("Nimbus.Overrides", defs);
        check(Color.RED);
        defs = new UIDefaults();
        defs.put("Label.background", new ColorUIResource(Color.GREEN));
        label.putClientProperty("Nimbus.Overrides", defs);
        check(Color.GREEN);
        label.putClientProperty("Nimbus.Overrides", null);
        check(defaultColor);
    }
    void testInheritance() {
        Color defaultColor = label.getBackground();
        UIManager.put("Label[Enabled].background", new ColorUIResource(Color.RED));
        UIDefaults defs = new UIDefaults();
        defs.put("Label.background", new ColorUIResource(Color.GREEN));
        label.putClientProperty("Nimbus.Overrides", defs);
        check(Color.RED);
        label.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        check(Color.GREEN);
        label.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        check(Color.RED);
        UIManager.put("Label[Enabled].background", null);
        label.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        label.putClientProperty("Nimbus.Overrides", null);
        check(defaultColor);
    }
    void testNames() {
        Color defaultColor = label.getBackground();
        UIManager.put("\"BlueLabel\"[Enabled].background",
                new ColorUIResource(Color.BLUE));
        UIManager.put("\"RedLabel\"[Enabled].background",
                new ColorUIResource(Color.RED));
        nimbus.register(Region.LABEL, "\"BlueLabel\"");
        nimbus.register(Region.LABEL, "\"RedLabel\"");
        label.setName("BlueLabel");
        check(Color.BLUE);
        label.setName("RedLabel");
        check(Color.RED);
        label.setName(null);
        check(defaultColor);
    }
    void testBaseColor() {
        UIManager.put("control", Color.GREEN);
        check(Color.GREEN);
    }
}
