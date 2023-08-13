public class MetalTextFieldUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new MetalTextFieldUI();
    }
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }
 }
