public abstract class ConfigPanel extends JPanel {
    private Object config;
    public ConfigPanel(LayoutManager layout) {
        super(layout);
    }
    public ConfigPanel() {
        super();
    }
    public abstract Object getConfig();
}
