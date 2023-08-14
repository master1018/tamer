public class Test6824600 {
    public static void main(String[] args) throws Exception {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        HackedDesktopProperty desktopProperty = new HackedDesktopProperty("Button.background", null);
        desktopProperty.getValueFromDesktop();
        int length = toolkit.getPropertyChangeListeners().length;
        desktopProperty.getValueFromDesktop();
        desktopProperty.getValueFromDesktop();
        desktopProperty.invalidate();
        desktopProperty.getValueFromDesktop();
        desktopProperty.getValueFromDesktop();
        if (length != toolkit.getPropertyChangeListeners().length) {
            throw new RuntimeException("New listeners were added into Toolkit");
        }
    }
    public static class HackedDesktopProperty extends DesktopProperty {
        public HackedDesktopProperty(String key, Object fallback) {
            super(key, fallback);
        }
        public Object getValueFromDesktop() {
            return super.getValueFromDesktop();
        }
    }
}
