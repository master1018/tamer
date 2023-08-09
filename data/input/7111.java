public class MultiLookAndFeel extends LookAndFeel {
    public String getName() {
        return "Multiplexing Look and Feel";
    }
    public String getID() {
        return "Multiplex";
    }
    public String getDescription() {
        return "Allows multiple UI instances per component instance";
    }
    public boolean isNativeLookAndFeel() {
        return false;
    }
    public boolean isSupportedLookAndFeel() {
        return true;
    }
    public UIDefaults getDefaults() {
        String packageName = "javax.swing.plaf.multi.Multi";
        Object[] uiDefaults = {
                   "ButtonUI", packageName + "ButtonUI",
         "CheckBoxMenuItemUI", packageName + "MenuItemUI",
                 "CheckBoxUI", packageName + "ButtonUI",
             "ColorChooserUI", packageName + "ColorChooserUI",
                 "ComboBoxUI", packageName + "ComboBoxUI",
              "DesktopIconUI", packageName + "DesktopIconUI",
              "DesktopPaneUI", packageName + "DesktopPaneUI",
               "EditorPaneUI", packageName + "TextUI",
              "FileChooserUI", packageName + "FileChooserUI",
       "FormattedTextFieldUI", packageName + "TextUI",
            "InternalFrameUI", packageName + "InternalFrameUI",
                    "LabelUI", packageName + "LabelUI",
                     "ListUI", packageName + "ListUI",
                  "MenuBarUI", packageName + "MenuBarUI",
                 "MenuItemUI", packageName + "MenuItemUI",
                     "MenuUI", packageName + "MenuItemUI",
               "OptionPaneUI", packageName + "OptionPaneUI",
                    "PanelUI", packageName + "PanelUI",
            "PasswordFieldUI", packageName + "TextUI",
       "PopupMenuSeparatorUI", packageName + "SeparatorUI",
                "PopupMenuUI", packageName + "PopupMenuUI",
              "ProgressBarUI", packageName + "ProgressBarUI",
      "RadioButtonMenuItemUI", packageName + "MenuItemUI",
              "RadioButtonUI", packageName + "ButtonUI",
                 "RootPaneUI", packageName + "RootPaneUI",
                "ScrollBarUI", packageName + "ScrollBarUI",
               "ScrollPaneUI", packageName + "ScrollPaneUI",
                "SeparatorUI", packageName + "SeparatorUI",
                   "SliderUI", packageName + "SliderUI",
                  "SpinnerUI", packageName + "SpinnerUI",
                "SplitPaneUI", packageName + "SplitPaneUI",
               "TabbedPaneUI", packageName + "TabbedPaneUI",
              "TableHeaderUI", packageName + "TableHeaderUI",
                    "TableUI", packageName + "TableUI",
                 "TextAreaUI", packageName + "TextUI",
                "TextFieldUI", packageName + "TextUI",
                 "TextPaneUI", packageName + "TextUI",
             "ToggleButtonUI", packageName + "ButtonUI",
         "ToolBarSeparatorUI", packageName + "SeparatorUI",
                  "ToolBarUI", packageName + "ToolBarUI",
                  "ToolTipUI", packageName + "ToolTipUI",
                     "TreeUI", packageName + "TreeUI",
                 "ViewportUI", packageName + "ViewportUI",
        };
        UIDefaults table = new MultiUIDefaults(uiDefaults.length / 2, 0.75f);
        table.putDefaults(uiDefaults);
        return table;
    }
    public static ComponentUI createUIs(ComponentUI mui,
                                        Vector      uis,
                                        JComponent  target) {
        ComponentUI ui;
        ui = UIManager.getDefaults().getUI(target);
        if (ui != null) {
            uis.addElement(ui);
            LookAndFeel[] auxiliaryLookAndFeels;
            auxiliaryLookAndFeels = UIManager.getAuxiliaryLookAndFeels();
            if (auxiliaryLookAndFeels != null) {
                for (int i = 0; i < auxiliaryLookAndFeels.length; i++) {
                    ui = auxiliaryLookAndFeels[i].getDefaults().getUI(target);
                    if (ui != null) {
                        uis.addElement(ui);
                    }
                }
            }
        } else {
            return null;
        }
        if (uis.size() == 1) {
            return (ComponentUI) uis.elementAt(0);
        } else {
            return mui;
        }
    }
    protected static ComponentUI[] uisToArray(Vector uis) {
        if (uis == null) {
            return new ComponentUI[0];
        } else {
            int count = uis.size();
            if (count > 0) {
                ComponentUI[] u = new ComponentUI[count];
                for (int i = 0; i < count; i++) {
                    u[i] = (ComponentUI)uis.elementAt(i);
                }
                return u;
            } else {
                return null;
            }
        }
    }
}
class MultiUIDefaults extends UIDefaults {
    MultiUIDefaults(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    protected void getUIError(String msg) {
        System.err.println("Multiplexing LAF:  " + msg);
    }
}
