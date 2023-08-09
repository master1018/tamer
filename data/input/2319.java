public class bug6875716 {
    public static void main(String[] args) throws Exception {
        JLayer<Component> layer = new JLayer<Component>(new Component(){});
        layer.setGlassPane(null);
        try {
            layer.remove((Component)null);
        } catch (NullPointerException e) {
            return;
        }
        throw new RuntimeException("Test failed");
    }
}
