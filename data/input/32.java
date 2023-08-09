public class WindowsLabelUI extends BasicLabelUI {
    private static final Object WINDOWS_LABEL_UI_KEY = new Object();
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        WindowsLabelUI windowsLabelUI =
                (WindowsLabelUI) appContext.get(WINDOWS_LABEL_UI_KEY);
        if (windowsLabelUI == null) {
            windowsLabelUI = new WindowsLabelUI();
            appContext.put(WINDOWS_LABEL_UI_KEY, windowsLabelUI);
        }
        return windowsLabelUI;
    }
    protected void paintEnabledText(JLabel l, Graphics g, String s,
                                    int textX, int textY) {
        int mnemonicIndex = l.getDisplayedMnemonicIndex();
        if (WindowsLookAndFeel.isMnemonicHidden() == true) {
            mnemonicIndex = -1;
        }
        g.setColor(l.getForeground());
        SwingUtilities2.drawStringUnderlineCharAt(l, g, s, mnemonicIndex,
                                                     textX, textY);
    }
    protected void paintDisabledText(JLabel l, Graphics g, String s,
                                     int textX, int textY) {
        int mnemonicIndex = l.getDisplayedMnemonicIndex();
        if (WindowsLookAndFeel.isMnemonicHidden() == true) {
            mnemonicIndex = -1;
        }
        if ( UIManager.getColor("Label.disabledForeground") instanceof Color &&
             UIManager.getColor("Label.disabledShadow") instanceof Color) {
            g.setColor( UIManager.getColor("Label.disabledShadow") );
            SwingUtilities2.drawStringUnderlineCharAt(l, g, s,
                                                         mnemonicIndex,
                                                         textX + 1, textY + 1);
            g.setColor( UIManager.getColor("Label.disabledForeground") );
            SwingUtilities2.drawStringUnderlineCharAt(l, g, s,
                                                         mnemonicIndex,
                                                         textX, textY);
        } else {
            Color background = l.getBackground();
            g.setColor(background.brighter());
            SwingUtilities2.drawStringUnderlineCharAt(l,g, s, mnemonicIndex,
                                                         textX + 1, textY + 1);
            g.setColor(background.darker());
            SwingUtilities2.drawStringUnderlineCharAt(l,g, s, mnemonicIndex,
                                                         textX, textY);
        }
    }
}
