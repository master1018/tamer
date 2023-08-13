public class Test4461329 {
    public static void main(String[] args) {
        JColorChooser chooser = new JColorChooser();
        if (null == chooser.getPreviewPanel()) {
            throw new Error("Failed: getPreviewPanel() returned null");
        }
        JButton button = new JButton("Color"); 
        chooser.setPreviewPanel(button);
        if (button != chooser.getPreviewPanel()) {
            throw new Error("Failed in setPreviewPanel()");
        }
    }
}
