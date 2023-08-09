public class ColorChooserComponentFactory {
    private ColorChooserComponentFactory() { } 
    public static AbstractColorChooserPanel[] getDefaultChooserPanels() {
        return new AbstractColorChooserPanel[] {
                new DefaultSwatchChooserPanel(),
                new ColorChooserPanel(new ColorModelHSV()),
                new ColorChooserPanel(new ColorModelHSL()),
                new ColorChooserPanel(new ColorModel()),
                new ColorChooserPanel(new ColorModelCMYK()),
        };
    }
    public static JComponent getPreviewPanel() {
        return new DefaultPreviewPanel();
    }
}
