public class WindowsLookAndFeel extends BasicLookAndFeel
{
    static final Object HI_RES_DISABLED_ICON_CLIENT_KEY =
        new StringUIClientPropertyKey(
            "WindowsLookAndFeel.generateHiResDisabledIcon");
    private boolean updatePending = false;
    private boolean useSystemFontSettings = true;
    private boolean useSystemFontSizeSettings;
    private DesktopProperty themeActive, dllName, colorName, sizeName;
    private DesktopProperty aaSettings;
    private transient LayoutStyle style;
    private int baseUnitX;
    private int baseUnitY;
    public String getName() {
        return "Windows";
    }
    public String getDescription() {
        return "The Microsoft Windows Look and Feel";
    }
    public String getID() {
        return "Windows";
    }
    public boolean isNativeLookAndFeel() {
        return OSInfo.getOSType() == OSInfo.OSType.WINDOWS;
    }
    public boolean isSupportedLookAndFeel() {
        return isNativeLookAndFeel();
    }
    public void initialize() {
        super.initialize();
        if (OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_95) <= 0) {
            isClassicWindows = true;
        } else {
            isClassicWindows = false;
            XPStyle.invalidateStyle();
        }
        String systemFonts = java.security.AccessController.doPrivileged(
               new GetPropertyAction("swing.useSystemFontSettings"));
        useSystemFontSettings = (systemFonts == null ||
                                 Boolean.valueOf(systemFonts).booleanValue());
        if (useSystemFontSettings) {
            Object value = UIManager.get("Application.useSystemFontSettings");
            useSystemFontSettings = (value == null ||
                                     Boolean.TRUE.equals(value));
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            addKeyEventPostProcessor(WindowsRootPaneUI.altProcessor);
    }
    protected void initClassDefaults(UIDefaults table)
    {
        super.initClassDefaults(table);
        final String windowsPackageName = "com.sun.java.swing.plaf.windows.";
        Object[] uiDefaults = {
              "ButtonUI", windowsPackageName + "WindowsButtonUI",
            "CheckBoxUI", windowsPackageName + "WindowsCheckBoxUI",
    "CheckBoxMenuItemUI", windowsPackageName + "WindowsCheckBoxMenuItemUI",
               "LabelUI", windowsPackageName + "WindowsLabelUI",
         "RadioButtonUI", windowsPackageName + "WindowsRadioButtonUI",
 "RadioButtonMenuItemUI", windowsPackageName + "WindowsRadioButtonMenuItemUI",
        "ToggleButtonUI", windowsPackageName + "WindowsToggleButtonUI",
         "ProgressBarUI", windowsPackageName + "WindowsProgressBarUI",
              "SliderUI", windowsPackageName + "WindowsSliderUI",
           "SeparatorUI", windowsPackageName + "WindowsSeparatorUI",
           "SplitPaneUI", windowsPackageName + "WindowsSplitPaneUI",
             "SpinnerUI", windowsPackageName + "WindowsSpinnerUI",
          "TabbedPaneUI", windowsPackageName + "WindowsTabbedPaneUI",
            "TextAreaUI", windowsPackageName + "WindowsTextAreaUI",
           "TextFieldUI", windowsPackageName + "WindowsTextFieldUI",
       "PasswordFieldUI", windowsPackageName + "WindowsPasswordFieldUI",
            "TextPaneUI", windowsPackageName + "WindowsTextPaneUI",
          "EditorPaneUI", windowsPackageName + "WindowsEditorPaneUI",
                "TreeUI", windowsPackageName + "WindowsTreeUI",
             "ToolBarUI", windowsPackageName + "WindowsToolBarUI",
    "ToolBarSeparatorUI", windowsPackageName + "WindowsToolBarSeparatorUI",
            "ComboBoxUI", windowsPackageName + "WindowsComboBoxUI",
         "TableHeaderUI", windowsPackageName + "WindowsTableHeaderUI",
       "InternalFrameUI", windowsPackageName + "WindowsInternalFrameUI",
         "DesktopPaneUI", windowsPackageName + "WindowsDesktopPaneUI",
         "DesktopIconUI", windowsPackageName + "WindowsDesktopIconUI",
         "FileChooserUI", windowsPackageName + "WindowsFileChooserUI",
                "MenuUI", windowsPackageName + "WindowsMenuUI",
            "MenuItemUI", windowsPackageName + "WindowsMenuItemUI",
             "MenuBarUI", windowsPackageName + "WindowsMenuBarUI",
           "PopupMenuUI", windowsPackageName + "WindowsPopupMenuUI",
  "PopupMenuSeparatorUI", windowsPackageName + "WindowsPopupMenuSeparatorUI",
           "ScrollBarUI", windowsPackageName + "WindowsScrollBarUI",
            "RootPaneUI", windowsPackageName + "WindowsRootPaneUI"
        };
        table.putDefaults(uiDefaults);
    }
    protected void initSystemColorDefaults(UIDefaults table)
    {
        String[] defaultSystemColors = {
                "desktop", "#005C5C", 
          "activeCaption", "#000080", 
      "activeCaptionText", "#FFFFFF", 
    "activeCaptionBorder", "#C0C0C0", 
        "inactiveCaption", "#808080", 
    "inactiveCaptionText", "#C0C0C0", 
  "inactiveCaptionBorder", "#C0C0C0", 
                 "window", "#FFFFFF", 
           "windowBorder", "#000000", 
             "windowText", "#000000", 
                   "menu", "#C0C0C0", 
       "menuPressedItemB", "#000080", 
       "menuPressedItemF", "#FFFFFF", 
               "menuText", "#000000", 
                   "text", "#C0C0C0", 
               "textText", "#000000", 
          "textHighlight", "#000080", 
      "textHighlightText", "#FFFFFF", 
       "textInactiveText", "#808080", 
                "control", "#C0C0C0", 
            "controlText", "#000000", 
       "controlHighlight", "#C0C0C0",
     "controlLtHighlight", "#FFFFFF", 
          "controlShadow", "#808080", 
        "controlDkShadow", "#000000", 
              "scrollbar", "#E0E0E0", 
                   "info", "#FFFFE1", 
               "infoText", "#000000"  
        };
        loadSystemColors(table, defaultSystemColors, isNativeLookAndFeel());
    }
    private void initResourceBundle(UIDefaults table) {
        table.addResourceBundle( "com.sun.java.swing.plaf.windows.resources.windows" );
    }
    protected void initComponentDefaults(UIDefaults table)
    {
        super.initComponentDefaults( table );
        initResourceBundle(table);
        Integer twelve = Integer.valueOf(12);
        Integer fontPlain = Integer.valueOf(Font.PLAIN);
        Integer fontBold = Integer.valueOf(Font.BOLD);
        Object dialogPlain12 = new SwingLazyValue(
                               "javax.swing.plaf.FontUIResource",
                               null,
                               new Object[] {Font.DIALOG, fontPlain, twelve});
        Object sansSerifPlain12 =  new SwingLazyValue(
                          "javax.swing.plaf.FontUIResource",
                          null,
                          new Object[] {Font.SANS_SERIF, fontPlain, twelve});
        Object monospacedPlain12 = new SwingLazyValue(
                          "javax.swing.plaf.FontUIResource",
                          null,
                          new Object[] {Font.MONOSPACED, fontPlain, twelve});
        Object dialogBold12 = new SwingLazyValue(
                          "javax.swing.plaf.FontUIResource",
                          null,
                          new Object[] {Font.DIALOG, fontBold, twelve});
        ColorUIResource red = new ColorUIResource(Color.red);
        ColorUIResource black = new ColorUIResource(Color.black);
        ColorUIResource white = new ColorUIResource(Color.white);
        ColorUIResource gray = new ColorUIResource(Color.gray);
        ColorUIResource darkGray = new ColorUIResource(Color.darkGray);
        ColorUIResource scrollBarTrackHighlight = darkGray;
        isClassicWindows = OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_95) <= 0;
        Object treeExpandedIcon = WindowsTreeUI.ExpandedIcon.createExpandedIcon();
        Object treeCollapsedIcon = WindowsTreeUI.CollapsedIcon.createCollapsedIcon();
        Object fieldInputMap = new UIDefaults.LazyInputMap(new Object[] {
                      "control C", DefaultEditorKit.copyAction,
                      "control V", DefaultEditorKit.pasteAction,
                      "control X", DefaultEditorKit.cutAction,
                           "COPY", DefaultEditorKit.copyAction,
                          "PASTE", DefaultEditorKit.pasteAction,
                            "CUT", DefaultEditorKit.cutAction,
                 "control INSERT", DefaultEditorKit.copyAction,
                   "shift INSERT", DefaultEditorKit.pasteAction,
                   "shift DELETE", DefaultEditorKit.cutAction,
                      "control A", DefaultEditorKit.selectAllAction,
             "control BACK_SLASH", "unselect",
                     "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                   "control LEFT", DefaultEditorKit.previousWordAction,
                  "control RIGHT", DefaultEditorKit.nextWordAction,
             "control shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
            "control shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                           "HOME", DefaultEditorKit.beginLineAction,
                            "END", DefaultEditorKit.endLineAction,
                     "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                      "shift END", DefaultEditorKit.selectionEndLineAction,
                     "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
               "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                         "ctrl H", DefaultEditorKit.deletePrevCharAction,
                         "DELETE", DefaultEditorKit.deleteNextCharAction,
                    "ctrl DELETE", DefaultEditorKit.deleteNextWordAction,
                "ctrl BACK_SPACE", DefaultEditorKit.deletePrevWordAction,
                          "RIGHT", DefaultEditorKit.forwardAction,
                           "LEFT", DefaultEditorKit.backwardAction,
                       "KP_RIGHT", DefaultEditorKit.forwardAction,
                        "KP_LEFT", DefaultEditorKit.backwardAction,
                          "ENTER", JTextField.notifyAction,
                "control shift O", "toggle-componentOrientation"
        });
        Object passwordInputMap = new UIDefaults.LazyInputMap(new Object[] {
                      "control C", DefaultEditorKit.copyAction,
                      "control V", DefaultEditorKit.pasteAction,
                      "control X", DefaultEditorKit.cutAction,
                           "COPY", DefaultEditorKit.copyAction,
                          "PASTE", DefaultEditorKit.pasteAction,
                            "CUT", DefaultEditorKit.cutAction,
                 "control INSERT", DefaultEditorKit.copyAction,
                   "shift INSERT", DefaultEditorKit.pasteAction,
                   "shift DELETE", DefaultEditorKit.cutAction,
                      "control A", DefaultEditorKit.selectAllAction,
             "control BACK_SLASH", "unselect",
                     "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                   "control LEFT", DefaultEditorKit.beginLineAction,
                  "control RIGHT", DefaultEditorKit.endLineAction,
             "control shift LEFT", DefaultEditorKit.selectionBeginLineAction,
            "control shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                           "HOME", DefaultEditorKit.beginLineAction,
                            "END", DefaultEditorKit.endLineAction,
                     "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                      "shift END", DefaultEditorKit.selectionEndLineAction,
                     "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
               "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                         "ctrl H", DefaultEditorKit.deletePrevCharAction,
                         "DELETE", DefaultEditorKit.deleteNextCharAction,
                          "RIGHT", DefaultEditorKit.forwardAction,
                           "LEFT", DefaultEditorKit.backwardAction,
                       "KP_RIGHT", DefaultEditorKit.forwardAction,
                        "KP_LEFT", DefaultEditorKit.backwardAction,
                          "ENTER", JTextField.notifyAction,
                "control shift O", "toggle-componentOrientation"
        });
        Object multilineInputMap = new UIDefaults.LazyInputMap(new Object[] {
                      "control C", DefaultEditorKit.copyAction,
                      "control V", DefaultEditorKit.pasteAction,
                      "control X", DefaultEditorKit.cutAction,
                           "COPY", DefaultEditorKit.copyAction,
                          "PASTE", DefaultEditorKit.pasteAction,
                            "CUT", DefaultEditorKit.cutAction,
                 "control INSERT", DefaultEditorKit.copyAction,
                   "shift INSERT", DefaultEditorKit.pasteAction,
                   "shift DELETE", DefaultEditorKit.cutAction,
                     "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                   "control LEFT", DefaultEditorKit.previousWordAction,
                  "control RIGHT", DefaultEditorKit.nextWordAction,
             "control shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
            "control shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                      "control A", DefaultEditorKit.selectAllAction,
             "control BACK_SLASH", "unselect",
                           "HOME", DefaultEditorKit.beginLineAction,
                            "END", DefaultEditorKit.endLineAction,
                     "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                      "shift END", DefaultEditorKit.selectionEndLineAction,
                   "control HOME", DefaultEditorKit.beginAction,
                    "control END", DefaultEditorKit.endAction,
             "control shift HOME", DefaultEditorKit.selectionBeginAction,
              "control shift END", DefaultEditorKit.selectionEndAction,
                             "UP", DefaultEditorKit.upAction,
                           "DOWN", DefaultEditorKit.downAction,
                     "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
               "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                         "ctrl H", DefaultEditorKit.deletePrevCharAction,
                         "DELETE", DefaultEditorKit.deleteNextCharAction,
                    "ctrl DELETE", DefaultEditorKit.deleteNextWordAction,
                "ctrl BACK_SPACE", DefaultEditorKit.deletePrevWordAction,
                          "RIGHT", DefaultEditorKit.forwardAction,
                           "LEFT", DefaultEditorKit.backwardAction,
                       "KP_RIGHT", DefaultEditorKit.forwardAction,
                        "KP_LEFT", DefaultEditorKit.backwardAction,
                        "PAGE_UP", DefaultEditorKit.pageUpAction,
                      "PAGE_DOWN", DefaultEditorKit.pageDownAction,
                  "shift PAGE_UP", "selection-page-up",
                "shift PAGE_DOWN", "selection-page-down",
             "ctrl shift PAGE_UP", "selection-page-left",
           "ctrl shift PAGE_DOWN", "selection-page-right",
                       "shift UP", DefaultEditorKit.selectionUpAction,
                     "shift DOWN", DefaultEditorKit.selectionDownAction,
                          "ENTER", DefaultEditorKit.insertBreakAction,
                            "TAB", DefaultEditorKit.insertTabAction,
                      "control T", "next-link-action",
                "control shift T", "previous-link-action",
                  "control SPACE", "activate-link-action",
                "control shift O", "toggle-componentOrientation"
        });
        Object menuItemAcceleratorDelimiter = "+";
        Object ControlBackgroundColor = new DesktopProperty(
                                                       "win.3d.backgroundColor",
                                                        table.get("control"));
        Object ControlLightColor      = new DesktopProperty(
                                                       "win.3d.lightColor",
                                                        table.get("controlHighlight"));
        Object ControlHighlightColor  = new DesktopProperty(
                                                       "win.3d.highlightColor",
                                                        table.get("controlLtHighlight"));
        Object ControlShadowColor     = new DesktopProperty(
                                                       "win.3d.shadowColor",
                                                        table.get("controlShadow"));
        Object ControlDarkShadowColor = new DesktopProperty(
                                                       "win.3d.darkShadowColor",
                                                        table.get("controlDkShadow"));
        Object ControlTextColor       = new DesktopProperty(
                                                       "win.button.textColor",
                                                        table.get("controlText"));
        Object MenuBackgroundColor    = new DesktopProperty(
                                                       "win.menu.backgroundColor",
                                                        table.get("menu"));
        Object MenuBarBackgroundColor = new DesktopProperty(
                                                       "win.menubar.backgroundColor",
                                                        table.get("menu"));
        Object MenuTextColor          = new DesktopProperty(
                                                       "win.menu.textColor",
                                                        table.get("menuText"));
        Object SelectionBackgroundColor = new DesktopProperty(
                                                       "win.item.highlightColor",
                                                        table.get("textHighlight"));
        Object SelectionTextColor     = new DesktopProperty(
                                                       "win.item.highlightTextColor",
                                                        table.get("textHighlightText"));
        Object WindowBackgroundColor  = new DesktopProperty(
                                                       "win.frame.backgroundColor",
                                                        table.get("window"));
        Object WindowTextColor        = new DesktopProperty(
                                                       "win.frame.textColor",
                                                        table.get("windowText"));
        Object WindowBorderWidth      = new DesktopProperty(
                                                       "win.frame.sizingBorderWidth",
                                                       Integer.valueOf(1));
        Object TitlePaneHeight        = new DesktopProperty(
                                                       "win.frame.captionHeight",
                                                       Integer.valueOf(18));
        Object TitleButtonWidth       = new DesktopProperty(
                                                       "win.frame.captionButtonWidth",
                                                       Integer.valueOf(16));
        Object TitleButtonHeight      = new DesktopProperty(
                                                       "win.frame.captionButtonHeight",
                                                       Integer.valueOf(16));
        Object InactiveTextColor      = new DesktopProperty(
                                                       "win.text.grayedTextColor",
                                                        table.get("textInactiveText"));
        Object ScrollbarBackgroundColor = new DesktopProperty(
                                                       "win.scrollbar.backgroundColor",
                                                        table.get("scrollbar"));
        Object TextBackground         = new XPColorValue(Part.EP_EDIT, null, Prop.FILLCOLOR,
                                                         WindowBackgroundColor);
        Object ReadOnlyTextBackground = ControlBackgroundColor;
        Object DisabledTextBackground = ControlBackgroundColor;
        Object MenuFont = dialogPlain12;
        Object FixedControlFont = monospacedPlain12;
        Object ControlFont = dialogPlain12;
        Object MessageFont = dialogPlain12;
        Object WindowFont = dialogBold12;
        Object ToolTipFont = sansSerifPlain12;
        Object IconFont = ControlFont;
        Object scrollBarWidth = new DesktopProperty("win.scrollbar.width", Integer.valueOf(16));
        Object menuBarHeight = new DesktopProperty("win.menu.height", null);
        Object hotTrackingOn = new DesktopProperty("win.item.hotTrackingOn", true);
        Object showMnemonics = new DesktopProperty("win.menu.keyboardCuesOn", Boolean.TRUE);
        if (useSystemFontSettings) {
            MenuFont = getDesktopFontValue("win.menu.font", MenuFont);
            FixedControlFont = getDesktopFontValue("win.ansiFixed.font", FixedControlFont);
            ControlFont = getDesktopFontValue("win.defaultGUI.font", ControlFont);
            MessageFont = getDesktopFontValue("win.messagebox.font", MessageFont);
            WindowFont = getDesktopFontValue("win.frame.captionFont", WindowFont);
            IconFont    = getDesktopFontValue("win.icon.font", IconFont);
            ToolTipFont = getDesktopFontValue("win.tooltip.font", ToolTipFont);
            Object aaTextInfo = SwingUtilities2.AATextInfo.getAATextInfo(true);
            table.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
            this.aaSettings =
                new FontDesktopProperty(SunToolkit.DESKTOPFONTHINTS);
        }
        if (useSystemFontSizeSettings) {
            MenuFont = new WindowsFontSizeProperty("win.menu.font.height", Font.DIALOG, Font.PLAIN, 12);
            FixedControlFont = new WindowsFontSizeProperty("win.ansiFixed.font.height", Font.MONOSPACED,
                       Font.PLAIN, 12);
            ControlFont = new WindowsFontSizeProperty("win.defaultGUI.font.height", Font.DIALOG, Font.PLAIN, 12);
            MessageFont = new WindowsFontSizeProperty("win.messagebox.font.height", Font.DIALOG, Font.PLAIN, 12);
            WindowFont = new WindowsFontSizeProperty("win.frame.captionFont.height", Font.DIALOG, Font.BOLD, 12);
            ToolTipFont = new WindowsFontSizeProperty("win.tooltip.font.height", Font.SANS_SERIF, Font.PLAIN, 12);
            IconFont    = new WindowsFontSizeProperty("win.icon.font.height", Font.DIALOG, Font.PLAIN, 12);
        }
        if (!(this instanceof WindowsClassicLookAndFeel) &&
            (OSInfo.getOSType() == OSInfo.OSType.WINDOWS &&
             OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_XP) >= 0) &&
            AccessController.doPrivileged(new GetPropertyAction("swing.noxp")) == null) {
            this.themeActive = new TriggerDesktopProperty("win.xpstyle.themeActive");
            this.dllName     = new TriggerDesktopProperty("win.xpstyle.dllName");
            this.colorName   = new TriggerDesktopProperty("win.xpstyle.colorName");
            this.sizeName    = new TriggerDesktopProperty("win.xpstyle.sizeName");
        }
        Object[] defaults = {
            "AuditoryCues.playList", null, 
            "Application.useSystemFontSettings", Boolean.valueOf(useSystemFontSettings),
            "TextField.focusInputMap", fieldInputMap,
            "PasswordField.focusInputMap", passwordInputMap,
            "TextArea.focusInputMap", multilineInputMap,
            "TextPane.focusInputMap", multilineInputMap,
            "EditorPane.focusInputMap", multilineInputMap,
            "Button.font", ControlFont,
            "Button.background", ControlBackgroundColor,
            "Button.foreground", ControlTextColor,
            "Button.shadow", ControlShadowColor,
            "Button.darkShadow", ControlDarkShadowColor,
            "Button.light", ControlLightColor,
            "Button.highlight", ControlHighlightColor,
            "Button.disabledForeground", InactiveTextColor,
            "Button.disabledShadow", ControlHighlightColor,
            "Button.focus", black,
            "Button.dashedRectGapX", new XPValue(Integer.valueOf(3), Integer.valueOf(5)),
            "Button.dashedRectGapY", new XPValue(Integer.valueOf(3), Integer.valueOf(4)),
            "Button.dashedRectGapWidth", new XPValue(Integer.valueOf(6), Integer.valueOf(10)),
            "Button.dashedRectGapHeight", new XPValue(Integer.valueOf(6), Integer.valueOf(8)),
            "Button.textShiftOffset", new XPValue(Integer.valueOf(0),
                                                  Integer.valueOf(1)),
            "Button.showMnemonics", showMnemonics,
            "Button.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                            "SPACE", "pressed",
                   "released SPACE", "released"
                 }),
            "CheckBox.font", ControlFont,
            "CheckBox.interiorBackground", WindowBackgroundColor,
            "CheckBox.background", ControlBackgroundColor,
            "CheckBox.foreground", WindowTextColor,
            "CheckBox.shadow", ControlShadowColor,
            "CheckBox.darkShadow", ControlDarkShadowColor,
            "CheckBox.light", ControlLightColor,
            "CheckBox.highlight", ControlHighlightColor,
            "CheckBox.focus", black,
            "CheckBox.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                            "SPACE", "pressed",
                   "released SPACE", "released"
                 }),
            "CheckBox.totalInsets", new Insets(4, 4, 4, 4),
            "CheckBoxMenuItem.font", MenuFont,
            "CheckBoxMenuItem.background", MenuBackgroundColor,
            "CheckBoxMenuItem.foreground", MenuTextColor,
            "CheckBoxMenuItem.selectionForeground", SelectionTextColor,
            "CheckBoxMenuItem.selectionBackground", SelectionBackgroundColor,
            "CheckBoxMenuItem.acceleratorForeground", MenuTextColor,
            "CheckBoxMenuItem.acceleratorSelectionForeground", SelectionTextColor,
            "CheckBoxMenuItem.commandSound", "win.sound.menuCommand",
            "ComboBox.font", ControlFont,
            "ComboBox.background", WindowBackgroundColor,
            "ComboBox.foreground", WindowTextColor,
            "ComboBox.buttonBackground", ControlBackgroundColor,
            "ComboBox.buttonShadow", ControlShadowColor,
            "ComboBox.buttonDarkShadow", ControlDarkShadowColor,
            "ComboBox.buttonHighlight", ControlHighlightColor,
            "ComboBox.selectionBackground", SelectionBackgroundColor,
            "ComboBox.selectionForeground", SelectionTextColor,
            "ComboBox.editorBorder", new XPValue(new EmptyBorder(1,2,1,1),
                                                 new EmptyBorder(1,4,1,4)),
            "ComboBox.disabledBackground",
                        new XPColorValue(Part.CP_COMBOBOX, State.DISABLED,
                        Prop.FILLCOLOR, DisabledTextBackground),
            "ComboBox.disabledForeground",
                        new XPColorValue(Part.CP_COMBOBOX, State.DISABLED,
                        Prop.TEXTCOLOR, InactiveTextColor),
            "ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
                   "ESCAPE", "hidePopup",
                  "PAGE_UP", "pageUpPassThrough",
                "PAGE_DOWN", "pageDownPassThrough",
                     "HOME", "homePassThrough",
                      "END", "endPassThrough",
                     "DOWN", "selectNext2",
                  "KP_DOWN", "selectNext2",
                       "UP", "selectPrevious2",
                    "KP_UP", "selectPrevious2",
                    "ENTER", "enterPressed",
                       "F4", "togglePopup",
                 "alt DOWN", "togglePopup",
              "alt KP_DOWN", "togglePopup",
                   "alt UP", "togglePopup",
                "alt KP_UP", "togglePopup"
              }),
            "Desktop.background", new DesktopProperty(
                                                 "win.desktop.backgroundColor",
                                                  table.get("desktop")),
            "Desktop.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                   "ctrl F5", "restore",
                   "ctrl F4", "close",
                   "ctrl F7", "move",
                   "ctrl F8", "resize",
                   "RIGHT", "right",
                   "KP_RIGHT", "right",
                   "LEFT", "left",
                   "KP_LEFT", "left",
                   "UP", "up",
                   "KP_UP", "up",
                   "DOWN", "down",
                   "KP_DOWN", "down",
                   "ESCAPE", "escape",
                   "ctrl F9", "minimize",
                   "ctrl F10", "maximize",
                   "ctrl F6", "selectNextFrame",
                   "ctrl TAB", "selectNextFrame",
                   "ctrl alt F6", "selectNextFrame",
                   "shift ctrl alt F6", "selectPreviousFrame",
                   "ctrl F12", "navigateNext",
                   "shift ctrl F12", "navigatePrevious"
               }),
            "DesktopIcon.width", Integer.valueOf(160),
            "EditorPane.font", ControlFont,
            "EditorPane.background", WindowBackgroundColor,
            "EditorPane.foreground", WindowTextColor,
            "EditorPane.selectionBackground", SelectionBackgroundColor,
            "EditorPane.selectionForeground", SelectionTextColor,
            "EditorPane.caretForeground", WindowTextColor,
            "EditorPane.inactiveForeground", InactiveTextColor,
            "EditorPane.inactiveBackground", WindowBackgroundColor,
            "EditorPane.disabledBackground", DisabledTextBackground,
            "FileChooser.homeFolderIcon",  new LazyWindowsIcon(null,
                                                               "icons/HomeFolder.gif"),
            "FileChooser.listFont", IconFont,
            "FileChooser.listViewBackground", new XPColorValue(Part.LVP_LISTVIEW, null, Prop.FILLCOLOR,
                                                               WindowBackgroundColor),
            "FileChooser.listViewBorder", new XPBorderValue(Part.LVP_LISTVIEW,
                                                  new SwingLazyValue(
                                                        "javax.swing.plaf.BorderUIResource",
                                                        "getLoweredBevelBorderUIResource")),
            "FileChooser.listViewIcon",    new LazyWindowsIcon("fileChooserIcon ListView",
                                                               "icons/ListView.gif"),
            "FileChooser.listViewWindowsStyle", Boolean.TRUE,
            "FileChooser.detailsViewIcon", new LazyWindowsIcon("fileChooserIcon DetailsView",
                                                               "icons/DetailsView.gif"),
            "FileChooser.viewMenuIcon", new LazyWindowsIcon("fileChooserIcon ViewMenu",
                                                            "icons/ListView.gif"),
            "FileChooser.upFolderIcon",    new LazyWindowsIcon("fileChooserIcon UpFolder",
                                                               "icons/UpFolder.gif"),
            "FileChooser.newFolderIcon",   new LazyWindowsIcon("fileChooserIcon NewFolder",
                                                               "icons/NewFolder.gif"),
            "FileChooser.useSystemExtensionHiding", Boolean.TRUE,
            "FileChooser.lookInLabelMnemonic", Integer.valueOf(KeyEvent.VK_I),
            "FileChooser.fileNameLabelMnemonic", Integer.valueOf(KeyEvent.VK_N),
            "FileChooser.filesOfTypeLabelMnemonic", Integer.valueOf(KeyEvent.VK_T),
            "FileChooser.usesSingleFilePane", Boolean.TRUE,
            "FileChooser.noPlacesBar", new DesktopProperty("win.comdlg.noPlacesBar",
                                                           Boolean.FALSE),
            "FileChooser.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                     "ESCAPE", "cancelSelection",
                     "F2", "editFileName",
                     "F5", "refresh",
                     "BACK_SPACE", "Go Up"
                 }),
            "FileView.directoryIcon", SwingUtilities2.makeIcon(getClass(),
                                                               WindowsLookAndFeel.class,
                                                               "icons/Directory.gif"),
            "FileView.fileIcon", SwingUtilities2.makeIcon(getClass(),
                                                          WindowsLookAndFeel.class,
                                                          "icons/File.gif"),
            "FileView.computerIcon", SwingUtilities2.makeIcon(getClass(),
                                                              WindowsLookAndFeel.class,
                                                              "icons/Computer.gif"),
            "FileView.hardDriveIcon", SwingUtilities2.makeIcon(getClass(),
                                                               WindowsLookAndFeel.class,
                                                               "icons/HardDrive.gif"),
            "FileView.floppyDriveIcon", SwingUtilities2.makeIcon(getClass(),
                                                                 WindowsLookAndFeel.class,
                                                                 "icons/FloppyDrive.gif"),
            "FormattedTextField.font", ControlFont,
            "InternalFrame.titleFont", WindowFont,
            "InternalFrame.titlePaneHeight",   TitlePaneHeight,
            "InternalFrame.titleButtonWidth",  TitleButtonWidth,
            "InternalFrame.titleButtonHeight", TitleButtonHeight,
            "InternalFrame.titleButtonToolTipsOn", hotTrackingOn,
            "InternalFrame.borderColor", ControlBackgroundColor,
            "InternalFrame.borderShadow", ControlShadowColor,
            "InternalFrame.borderDarkShadow", ControlDarkShadowColor,
            "InternalFrame.borderHighlight", ControlHighlightColor,
            "InternalFrame.borderLight", ControlLightColor,
            "InternalFrame.borderWidth", WindowBorderWidth,
            "InternalFrame.minimizeIconBackground", ControlBackgroundColor,
            "InternalFrame.resizeIconHighlight", ControlLightColor,
            "InternalFrame.resizeIconShadow", ControlShadowColor,
            "InternalFrame.activeBorderColor", new DesktopProperty(
                                                       "win.frame.activeBorderColor",
                                                       table.get("windowBorder")),
            "InternalFrame.inactiveBorderColor", new DesktopProperty(
                                                       "win.frame.inactiveBorderColor",
                                                       table.get("windowBorder")),
            "InternalFrame.activeTitleBackground", new DesktopProperty(
                                                        "win.frame.activeCaptionColor",
                                                         table.get("activeCaption")),
            "InternalFrame.activeTitleGradient", new DesktopProperty(
                                                        "win.frame.activeCaptionGradientColor",
                                                         table.get("activeCaption")),
            "InternalFrame.activeTitleForeground", new DesktopProperty(
                                                        "win.frame.captionTextColor",
                                                         table.get("activeCaptionText")),
            "InternalFrame.inactiveTitleBackground", new DesktopProperty(
                                                        "win.frame.inactiveCaptionColor",
                                                         table.get("inactiveCaption")),
            "InternalFrame.inactiveTitleGradient", new DesktopProperty(
                                                        "win.frame.inactiveCaptionGradientColor",
                                                         table.get("inactiveCaption")),
            "InternalFrame.inactiveTitleForeground", new DesktopProperty(
                                                        "win.frame.inactiveCaptionTextColor",
                                                         table.get("inactiveCaptionText")),
            "InternalFrame.maximizeIcon",
                WindowsIconFactory.createFrameMaximizeIcon(),
            "InternalFrame.minimizeIcon",
                WindowsIconFactory.createFrameMinimizeIcon(),
            "InternalFrame.iconifyIcon",
                WindowsIconFactory.createFrameIconifyIcon(),
            "InternalFrame.closeIcon",
                WindowsIconFactory.createFrameCloseIcon(),
            "InternalFrame.icon",
                new SwingLazyValue(
        "com.sun.java.swing.plaf.windows.WindowsInternalFrameTitlePane$ScalableIconUIResource",
                    new Object[][] { {
                        SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/JavaCup16.png"),
                        SwingUtilities2.makeIcon(getClass(), WindowsLookAndFeel.class, "icons/JavaCup32.png")
                    } }),
            "InternalFrame.closeSound", "win.sound.close",
            "InternalFrame.maximizeSound", "win.sound.maximize",
            "InternalFrame.minimizeSound", "win.sound.minimize",
            "InternalFrame.restoreDownSound", "win.sound.restoreDown",
            "InternalFrame.restoreUpSound", "win.sound.restoreUp",
            "InternalFrame.windowBindings", new Object[] {
                "shift ESCAPE", "showSystemMenu",
                  "ctrl SPACE", "showSystemMenu",
                      "ESCAPE", "hideSystemMenu"},
            "Label.font", ControlFont,
            "Label.background", ControlBackgroundColor,
            "Label.foreground", WindowTextColor,
            "Label.disabledForeground", InactiveTextColor,
            "Label.disabledShadow", ControlHighlightColor,
            "List.font", ControlFont,
            "List.background", WindowBackgroundColor,
            "List.foreground", WindowTextColor,
            "List.selectionBackground", SelectionBackgroundColor,
            "List.selectionForeground", SelectionTextColor,
            "List.lockToPositionOnScroll", Boolean.TRUE,
            "List.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                           "ctrl C", "copy",
                           "ctrl V", "paste",
                           "ctrl X", "cut",
                             "COPY", "copy",
                            "PASTE", "paste",
                              "CUT", "cut",
                   "control INSERT", "copy",
                     "shift INSERT", "paste",
                     "shift DELETE", "cut",
                               "UP", "selectPreviousRow",
                            "KP_UP", "selectPreviousRow",
                         "shift UP", "selectPreviousRowExtendSelection",
                      "shift KP_UP", "selectPreviousRowExtendSelection",
                    "ctrl shift UP", "selectPreviousRowExtendSelection",
                 "ctrl shift KP_UP", "selectPreviousRowExtendSelection",
                          "ctrl UP", "selectPreviousRowChangeLead",
                       "ctrl KP_UP", "selectPreviousRowChangeLead",
                             "DOWN", "selectNextRow",
                          "KP_DOWN", "selectNextRow",
                       "shift DOWN", "selectNextRowExtendSelection",
                    "shift KP_DOWN", "selectNextRowExtendSelection",
                  "ctrl shift DOWN", "selectNextRowExtendSelection",
               "ctrl shift KP_DOWN", "selectNextRowExtendSelection",
                        "ctrl DOWN", "selectNextRowChangeLead",
                     "ctrl KP_DOWN", "selectNextRowChangeLead",
                             "LEFT", "selectPreviousColumn",
                          "KP_LEFT", "selectPreviousColumn",
                       "shift LEFT", "selectPreviousColumnExtendSelection",
                    "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                  "ctrl shift LEFT", "selectPreviousColumnExtendSelection",
               "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
                        "ctrl LEFT", "selectPreviousColumnChangeLead",
                     "ctrl KP_LEFT", "selectPreviousColumnChangeLead",
                            "RIGHT", "selectNextColumn",
                         "KP_RIGHT", "selectNextColumn",
                      "shift RIGHT", "selectNextColumnExtendSelection",
                   "shift KP_RIGHT", "selectNextColumnExtendSelection",
                 "ctrl shift RIGHT", "selectNextColumnExtendSelection",
              "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
                       "ctrl RIGHT", "selectNextColumnChangeLead",
                    "ctrl KP_RIGHT", "selectNextColumnChangeLead",
                             "HOME", "selectFirstRow",
                       "shift HOME", "selectFirstRowExtendSelection",
                  "ctrl shift HOME", "selectFirstRowExtendSelection",
                        "ctrl HOME", "selectFirstRowChangeLead",
                              "END", "selectLastRow",
                        "shift END", "selectLastRowExtendSelection",
                   "ctrl shift END", "selectLastRowExtendSelection",
                         "ctrl END", "selectLastRowChangeLead",
                          "PAGE_UP", "scrollUp",
                    "shift PAGE_UP", "scrollUpExtendSelection",
               "ctrl shift PAGE_UP", "scrollUpExtendSelection",
                     "ctrl PAGE_UP", "scrollUpChangeLead",
                        "PAGE_DOWN", "scrollDown",
                  "shift PAGE_DOWN", "scrollDownExtendSelection",
             "ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
                   "ctrl PAGE_DOWN", "scrollDownChangeLead",
                           "ctrl A", "selectAll",
                       "ctrl SLASH", "selectAll",
                  "ctrl BACK_SLASH", "clearSelection",
                            "SPACE", "addToSelection",
                       "ctrl SPACE", "toggleAndAnchor",
                      "shift SPACE", "extendTo",
                 "ctrl shift SPACE", "moveSelectionTo"
                 }),
            "PopupMenu.font", MenuFont,
            "PopupMenu.background", MenuBackgroundColor,
            "PopupMenu.foreground", MenuTextColor,
            "PopupMenu.popupSound", "win.sound.menuPopup",
            "PopupMenu.consumeEventOnClose", Boolean.TRUE,
            "Menu.font", MenuFont,
            "Menu.foreground", MenuTextColor,
            "Menu.background", MenuBackgroundColor,
            "Menu.useMenuBarBackgroundForTopLevel", Boolean.TRUE,
            "Menu.selectionForeground", SelectionTextColor,
            "Menu.selectionBackground", SelectionBackgroundColor,
            "Menu.acceleratorForeground", MenuTextColor,
            "Menu.acceleratorSelectionForeground", SelectionTextColor,
            "Menu.menuPopupOffsetX", Integer.valueOf(0),
            "Menu.menuPopupOffsetY", Integer.valueOf(0),
            "Menu.submenuPopupOffsetX", Integer.valueOf(-4),
            "Menu.submenuPopupOffsetY", Integer.valueOf(-3),
            "Menu.crossMenuMnemonic", Boolean.FALSE,
            "Menu.preserveTopLevelSelection", Boolean.TRUE,
            "MenuBar.font", MenuFont,
            "MenuBar.background", new XPValue(MenuBarBackgroundColor,
                                              MenuBackgroundColor),
            "MenuBar.foreground", MenuTextColor,
            "MenuBar.shadow", ControlShadowColor,
            "MenuBar.highlight", ControlHighlightColor,
            "MenuBar.height", menuBarHeight,
            "MenuBar.rolloverEnabled", hotTrackingOn,
            "MenuBar.windowBindings", new Object[] {
                "F10", "takeFocus" },
            "MenuItem.font", MenuFont,
            "MenuItem.acceleratorFont", MenuFont,
            "MenuItem.foreground", MenuTextColor,
            "MenuItem.background", MenuBackgroundColor,
            "MenuItem.selectionForeground", SelectionTextColor,
            "MenuItem.selectionBackground", SelectionBackgroundColor,
            "MenuItem.disabledForeground", InactiveTextColor,
            "MenuItem.acceleratorForeground", MenuTextColor,
            "MenuItem.acceleratorSelectionForeground", SelectionTextColor,
            "MenuItem.acceleratorDelimiter", menuItemAcceleratorDelimiter,
            "MenuItem.commandSound", "win.sound.menuCommand",
            "MenuItem.disabledAreNavigable", Boolean.TRUE,
            "RadioButton.font", ControlFont,
            "RadioButton.interiorBackground", WindowBackgroundColor,
            "RadioButton.background", ControlBackgroundColor,
            "RadioButton.foreground", WindowTextColor,
            "RadioButton.shadow", ControlShadowColor,
            "RadioButton.darkShadow", ControlDarkShadowColor,
            "RadioButton.light", ControlLightColor,
            "RadioButton.highlight", ControlHighlightColor,
            "RadioButton.focus", black,
            "RadioButton.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                          "SPACE", "pressed",
                 "released SPACE", "released"
              }),
            "RadioButton.totalInsets", new Insets(4, 4, 4, 4),
            "RadioButtonMenuItem.font", MenuFont,
            "RadioButtonMenuItem.foreground", MenuTextColor,
            "RadioButtonMenuItem.background", MenuBackgroundColor,
            "RadioButtonMenuItem.selectionForeground", SelectionTextColor,
            "RadioButtonMenuItem.selectionBackground", SelectionBackgroundColor,
            "RadioButtonMenuItem.disabledForeground", InactiveTextColor,
            "RadioButtonMenuItem.acceleratorForeground", MenuTextColor,
            "RadioButtonMenuItem.acceleratorSelectionForeground", SelectionTextColor,
            "RadioButtonMenuItem.commandSound", "win.sound.menuCommand",
            "OptionPane.font", MessageFont,
            "OptionPane.messageFont", MessageFont,
            "OptionPane.buttonFont", MessageFont,
            "OptionPane.background", ControlBackgroundColor,
            "OptionPane.foreground", WindowTextColor,
            "OptionPane.buttonMinimumWidth", new XPDLUValue(50, 50, SwingConstants.EAST),
            "OptionPane.messageForeground", ControlTextColor,
            "OptionPane.errorIcon",       new LazyWindowsIcon("optionPaneIcon Error",
                                                              "icons/Error.gif"),
            "OptionPane.informationIcon", new LazyWindowsIcon("optionPaneIcon Information",
                                                              "icons/Inform.gif"),
            "OptionPane.questionIcon",    new LazyWindowsIcon("optionPaneIcon Question",
                                                              "icons/Question.gif"),
            "OptionPane.warningIcon",     new LazyWindowsIcon("optionPaneIcon Warning",
                                                              "icons/Warn.gif"),
            "OptionPane.windowBindings", new Object[] {
                "ESCAPE", "close" },
            "OptionPane.errorSound", "win.sound.hand", 
            "OptionPane.informationSound", "win.sound.asterisk", 
            "OptionPane.questionSound", "win.sound.question", 
            "OptionPane.warningSound", "win.sound.exclamation", 
            "FormattedTextField.focusInputMap",
              new UIDefaults.LazyInputMap(new Object[] {
                           "ctrl C", DefaultEditorKit.copyAction,
                           "ctrl V", DefaultEditorKit.pasteAction,
                           "ctrl X", DefaultEditorKit.cutAction,
                             "COPY", DefaultEditorKit.copyAction,
                            "PASTE", DefaultEditorKit.pasteAction,
                              "CUT", DefaultEditorKit.cutAction,
                   "control INSERT", DefaultEditorKit.copyAction,
                     "shift INSERT", DefaultEditorKit.pasteAction,
                     "shift DELETE", DefaultEditorKit.cutAction,
                       "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                      "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                   "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                        "ctrl LEFT", DefaultEditorKit.previousWordAction,
                     "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
                       "ctrl RIGHT", DefaultEditorKit.nextWordAction,
                    "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
                  "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
               "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                 "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
              "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                           "ctrl A", DefaultEditorKit.selectAllAction,
                             "HOME", DefaultEditorKit.beginLineAction,
                              "END", DefaultEditorKit.endLineAction,
                       "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                        "shift END", DefaultEditorKit.selectionEndLineAction,
                       "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                 "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                           "ctrl H", DefaultEditorKit.deletePrevCharAction,
                           "DELETE", DefaultEditorKit.deleteNextCharAction,
                      "ctrl DELETE", DefaultEditorKit.deleteNextWordAction,
                  "ctrl BACK_SPACE", DefaultEditorKit.deletePrevWordAction,
                            "RIGHT", DefaultEditorKit.forwardAction,
                             "LEFT", DefaultEditorKit.backwardAction,
                         "KP_RIGHT", DefaultEditorKit.forwardAction,
                          "KP_LEFT", DefaultEditorKit.backwardAction,
                            "ENTER", JTextField.notifyAction,
                  "ctrl BACK_SLASH", "unselect",
                   "control shift O", "toggle-componentOrientation",
                           "ESCAPE", "reset-field-edit",
                               "UP", "increment",
                            "KP_UP", "increment",
                             "DOWN", "decrement",
                          "KP_DOWN", "decrement",
              }),
            "FormattedTextField.inactiveBackground", ReadOnlyTextBackground,
            "FormattedTextField.disabledBackground", DisabledTextBackground,
            "Panel.font", ControlFont,
            "Panel.background", ControlBackgroundColor,
            "Panel.foreground", WindowTextColor,
            "PasswordField.font", ControlFont,
            "PasswordField.background", TextBackground,
            "PasswordField.foreground", WindowTextColor,
            "PasswordField.inactiveForeground", InactiveTextColor,      
            "PasswordField.inactiveBackground", ReadOnlyTextBackground, 
            "PasswordField.disabledBackground", DisabledTextBackground, 
            "PasswordField.selectionBackground", SelectionBackgroundColor,
            "PasswordField.selectionForeground", SelectionTextColor,
            "PasswordField.caretForeground",WindowTextColor,
            "PasswordField.echoChar", new XPValue(new Character((char)0x25CF),
                                                  new Character('*')),
            "ProgressBar.font", ControlFont,
            "ProgressBar.foreground",  SelectionBackgroundColor,
            "ProgressBar.background", ControlBackgroundColor,
            "ProgressBar.shadow", ControlShadowColor,
            "ProgressBar.highlight", ControlHighlightColor,
            "ProgressBar.selectionForeground", ControlBackgroundColor,
            "ProgressBar.selectionBackground", SelectionBackgroundColor,
            "ProgressBar.cellLength", Integer.valueOf(7),
            "ProgressBar.cellSpacing", Integer.valueOf(2),
            "ProgressBar.indeterminateInsets", new Insets(3, 3, 3, 3),
            "RootPane.defaultButtonWindowKeyBindings", new Object[] {
                             "ENTER", "press",
                    "released ENTER", "release",
                        "ctrl ENTER", "press",
               "ctrl released ENTER", "release"
              },
            "ScrollBar.background", ScrollbarBackgroundColor,
            "ScrollBar.foreground", ControlBackgroundColor,
            "ScrollBar.track", white,
            "ScrollBar.trackForeground", ScrollbarBackgroundColor,
            "ScrollBar.trackHighlight", black,
            "ScrollBar.trackHighlightForeground", scrollBarTrackHighlight,
            "ScrollBar.thumb", ControlBackgroundColor,
            "ScrollBar.thumbHighlight", ControlHighlightColor,
            "ScrollBar.thumbDarkShadow", ControlDarkShadowColor,
            "ScrollBar.thumbShadow", ControlShadowColor,
            "ScrollBar.width", scrollBarWidth,
            "ScrollBar.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                       "RIGHT", "positiveUnitIncrement",
                    "KP_RIGHT", "positiveUnitIncrement",
                        "DOWN", "positiveUnitIncrement",
                     "KP_DOWN", "positiveUnitIncrement",
                   "PAGE_DOWN", "positiveBlockIncrement",
              "ctrl PAGE_DOWN", "positiveBlockIncrement",
                        "LEFT", "negativeUnitIncrement",
                     "KP_LEFT", "negativeUnitIncrement",
                          "UP", "negativeUnitIncrement",
                       "KP_UP", "negativeUnitIncrement",
                     "PAGE_UP", "negativeBlockIncrement",
                "ctrl PAGE_UP", "negativeBlockIncrement",
                        "HOME", "minScroll",
                         "END", "maxScroll"
                 }),
            "ScrollPane.font", ControlFont,
            "ScrollPane.background", ControlBackgroundColor,
            "ScrollPane.foreground", ControlTextColor,
            "ScrollPane.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                           "RIGHT", "unitScrollRight",
                        "KP_RIGHT", "unitScrollRight",
                            "DOWN", "unitScrollDown",
                         "KP_DOWN", "unitScrollDown",
                            "LEFT", "unitScrollLeft",
                         "KP_LEFT", "unitScrollLeft",
                              "UP", "unitScrollUp",
                           "KP_UP", "unitScrollUp",
                         "PAGE_UP", "scrollUp",
                       "PAGE_DOWN", "scrollDown",
                    "ctrl PAGE_UP", "scrollLeft",
                  "ctrl PAGE_DOWN", "scrollRight",
                       "ctrl HOME", "scrollHome",
                        "ctrl END", "scrollEnd"
                 }),
            "Separator.background", ControlHighlightColor,
            "Separator.foreground", ControlShadowColor,
            "Slider.font", ControlFont,
            "Slider.foreground", ControlBackgroundColor,
            "Slider.background", ControlBackgroundColor,
            "Slider.highlight", ControlHighlightColor,
            "Slider.shadow", ControlShadowColor,
            "Slider.focus", ControlDarkShadowColor,
            "Slider.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                       "RIGHT", "positiveUnitIncrement",
                    "KP_RIGHT", "positiveUnitIncrement",
                        "DOWN", "negativeUnitIncrement",
                     "KP_DOWN", "negativeUnitIncrement",
                   "PAGE_DOWN", "negativeBlockIncrement",
                        "LEFT", "negativeUnitIncrement",
                     "KP_LEFT", "negativeUnitIncrement",
                          "UP", "positiveUnitIncrement",
                       "KP_UP", "positiveUnitIncrement",
                     "PAGE_UP", "positiveBlockIncrement",
                        "HOME", "minScroll",
                         "END", "maxScroll"
                 }),
            "Spinner.font", ControlFont,
            "Spinner.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                               "UP", "increment",
                            "KP_UP", "increment",
                             "DOWN", "decrement",
                          "KP_DOWN", "decrement",
               }),
            "SplitPane.background", ControlBackgroundColor,
            "SplitPane.highlight", ControlHighlightColor,
            "SplitPane.shadow", ControlShadowColor,
            "SplitPane.darkShadow", ControlDarkShadowColor,
            "SplitPane.dividerSize", Integer.valueOf(5),
            "SplitPane.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                        "UP", "negativeIncrement",
                      "DOWN", "positiveIncrement",
                      "LEFT", "negativeIncrement",
                     "RIGHT", "positiveIncrement",
                     "KP_UP", "negativeIncrement",
                   "KP_DOWN", "positiveIncrement",
                   "KP_LEFT", "negativeIncrement",
                  "KP_RIGHT", "positiveIncrement",
                      "HOME", "selectMin",
                       "END", "selectMax",
                        "F8", "startResize",
                        "F6", "toggleFocus",
                  "ctrl TAB", "focusOutForward",
            "ctrl shift TAB", "focusOutBackward"
               }),
            "TabbedPane.tabsOverlapBorder", new XPValue(Boolean.TRUE, Boolean.FALSE),
            "TabbedPane.tabInsets",         new XPValue(new InsetsUIResource(1, 4, 1, 4),
                                                        new InsetsUIResource(0, 4, 1, 4)),
            "TabbedPane.tabAreaInsets",     new XPValue(new InsetsUIResource(3, 2, 2, 2),
                                                        new InsetsUIResource(3, 2, 0, 2)),
            "TabbedPane.font", ControlFont,
            "TabbedPane.background", ControlBackgroundColor,
            "TabbedPane.foreground", ControlTextColor,
            "TabbedPane.highlight", ControlHighlightColor,
            "TabbedPane.light", ControlLightColor,
            "TabbedPane.shadow", ControlShadowColor,
            "TabbedPane.darkShadow", ControlDarkShadowColor,
            "TabbedPane.focus", ControlTextColor,
            "TabbedPane.focusInputMap",
              new UIDefaults.LazyInputMap(new Object[] {
                         "RIGHT", "navigateRight",
                      "KP_RIGHT", "navigateRight",
                          "LEFT", "navigateLeft",
                       "KP_LEFT", "navigateLeft",
                            "UP", "navigateUp",
                         "KP_UP", "navigateUp",
                          "DOWN", "navigateDown",
                       "KP_DOWN", "navigateDown",
                     "ctrl DOWN", "requestFocusForVisibleComponent",
                  "ctrl KP_DOWN", "requestFocusForVisibleComponent",
                }),
            "TabbedPane.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                         "ctrl TAB", "navigateNext",
                   "ctrl shift TAB", "navigatePrevious",
                   "ctrl PAGE_DOWN", "navigatePageDown",
                     "ctrl PAGE_UP", "navigatePageUp",
                          "ctrl UP", "requestFocus",
                       "ctrl KP_UP", "requestFocus",
                 }),
            "Table.font", ControlFont,
            "Table.foreground", ControlTextColor,  
            "Table.background", WindowBackgroundColor,  
            "Table.highlight", ControlHighlightColor,
            "Table.light", ControlLightColor,
            "Table.shadow", ControlShadowColor,
            "Table.darkShadow", ControlDarkShadowColor,
            "Table.selectionForeground", SelectionTextColor,
            "Table.selectionBackground", SelectionBackgroundColor,
            "Table.gridColor", gray,  
            "Table.focusCellBackground", WindowBackgroundColor,
            "Table.focusCellForeground", ControlTextColor,
            "Table.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                               "ctrl C", "copy",
                               "ctrl V", "paste",
                               "ctrl X", "cut",
                                 "COPY", "copy",
                                "PASTE", "paste",
                                  "CUT", "cut",
                       "control INSERT", "copy",
                         "shift INSERT", "paste",
                         "shift DELETE", "cut",
                                "RIGHT", "selectNextColumn",
                             "KP_RIGHT", "selectNextColumn",
                          "shift RIGHT", "selectNextColumnExtendSelection",
                       "shift KP_RIGHT", "selectNextColumnExtendSelection",
                     "ctrl shift RIGHT", "selectNextColumnExtendSelection",
                  "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
                           "ctrl RIGHT", "selectNextColumnChangeLead",
                        "ctrl KP_RIGHT", "selectNextColumnChangeLead",
                                 "LEFT", "selectPreviousColumn",
                              "KP_LEFT", "selectPreviousColumn",
                           "shift LEFT", "selectPreviousColumnExtendSelection",
                        "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                      "ctrl shift LEFT", "selectPreviousColumnExtendSelection",
                   "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
                            "ctrl LEFT", "selectPreviousColumnChangeLead",
                         "ctrl KP_LEFT", "selectPreviousColumnChangeLead",
                                 "DOWN", "selectNextRow",
                              "KP_DOWN", "selectNextRow",
                           "shift DOWN", "selectNextRowExtendSelection",
                        "shift KP_DOWN", "selectNextRowExtendSelection",
                      "ctrl shift DOWN", "selectNextRowExtendSelection",
                   "ctrl shift KP_DOWN", "selectNextRowExtendSelection",
                            "ctrl DOWN", "selectNextRowChangeLead",
                         "ctrl KP_DOWN", "selectNextRowChangeLead",
                                   "UP", "selectPreviousRow",
                                "KP_UP", "selectPreviousRow",
                             "shift UP", "selectPreviousRowExtendSelection",
                          "shift KP_UP", "selectPreviousRowExtendSelection",
                        "ctrl shift UP", "selectPreviousRowExtendSelection",
                     "ctrl shift KP_UP", "selectPreviousRowExtendSelection",
                              "ctrl UP", "selectPreviousRowChangeLead",
                           "ctrl KP_UP", "selectPreviousRowChangeLead",
                                 "HOME", "selectFirstColumn",
                           "shift HOME", "selectFirstColumnExtendSelection",
                      "ctrl shift HOME", "selectFirstRowExtendSelection",
                            "ctrl HOME", "selectFirstRow",
                                  "END", "selectLastColumn",
                            "shift END", "selectLastColumnExtendSelection",
                       "ctrl shift END", "selectLastRowExtendSelection",
                             "ctrl END", "selectLastRow",
                              "PAGE_UP", "scrollUpChangeSelection",
                        "shift PAGE_UP", "scrollUpExtendSelection",
                   "ctrl shift PAGE_UP", "scrollLeftExtendSelection",
                         "ctrl PAGE_UP", "scrollLeftChangeSelection",
                            "PAGE_DOWN", "scrollDownChangeSelection",
                      "shift PAGE_DOWN", "scrollDownExtendSelection",
                 "ctrl shift PAGE_DOWN", "scrollRightExtendSelection",
                       "ctrl PAGE_DOWN", "scrollRightChangeSelection",
                                  "TAB", "selectNextColumnCell",
                            "shift TAB", "selectPreviousColumnCell",
                                "ENTER", "selectNextRowCell",
                          "shift ENTER", "selectPreviousRowCell",
                               "ctrl A", "selectAll",
                           "ctrl SLASH", "selectAll",
                      "ctrl BACK_SLASH", "clearSelection",
                               "ESCAPE", "cancel",
                                   "F2", "startEditing",
                                "SPACE", "addToSelection",
                           "ctrl SPACE", "toggleAndAnchor",
                          "shift SPACE", "extendTo",
                     "ctrl shift SPACE", "moveSelectionTo",
                                   "F8", "focusHeader"
                 }),
            "Table.sortIconHighlight", ControlShadowColor,
            "Table.sortIconLight", white,
            "TableHeader.font", ControlFont,
            "TableHeader.foreground", ControlTextColor, 
            "TableHeader.background", ControlBackgroundColor, 
            "TableHeader.focusCellBackground",
                new XPValue(XPValue.NULL_VALUE,     
                            WindowBackgroundColor), 
            "TextArea.font", FixedControlFont,
            "TextArea.background", WindowBackgroundColor,
            "TextArea.foreground", WindowTextColor,
            "TextArea.inactiveForeground", InactiveTextColor,
            "TextArea.inactiveBackground", WindowBackgroundColor,
            "TextArea.disabledBackground", DisabledTextBackground,
            "TextArea.selectionBackground", SelectionBackgroundColor,
            "TextArea.selectionForeground", SelectionTextColor,
            "TextArea.caretForeground", WindowTextColor,
            "TextField.font", ControlFont,
            "TextField.background", TextBackground,
            "TextField.foreground", WindowTextColor,
            "TextField.shadow", ControlShadowColor,
            "TextField.darkShadow", ControlDarkShadowColor,
            "TextField.light", ControlLightColor,
            "TextField.highlight", ControlHighlightColor,
            "TextField.inactiveForeground", InactiveTextColor,      
            "TextField.inactiveBackground", ReadOnlyTextBackground, 
            "TextField.disabledBackground", DisabledTextBackground, 
            "TextField.selectionBackground", SelectionBackgroundColor,
            "TextField.selectionForeground", SelectionTextColor,
            "TextField.caretForeground", WindowTextColor,
            "TextPane.font", ControlFont,
            "TextPane.background", WindowBackgroundColor,
            "TextPane.foreground", WindowTextColor,
            "TextPane.selectionBackground", SelectionBackgroundColor,
            "TextPane.selectionForeground", SelectionTextColor,
            "TextPane.inactiveBackground", WindowBackgroundColor,
            "TextPane.disabledBackground", DisabledTextBackground,
            "TextPane.caretForeground", WindowTextColor,
            "TitledBorder.font", ControlFont,
            "TitledBorder.titleColor",
                        new XPColorValue(Part.BP_GROUPBOX, null, Prop.TEXTCOLOR,
                                         WindowTextColor),
            "ToggleButton.font", ControlFont,
            "ToggleButton.background", ControlBackgroundColor,
            "ToggleButton.foreground", ControlTextColor,
            "ToggleButton.shadow", ControlShadowColor,
            "ToggleButton.darkShadow", ControlDarkShadowColor,
            "ToggleButton.light", ControlLightColor,
            "ToggleButton.highlight", ControlHighlightColor,
            "ToggleButton.focus", ControlTextColor,
            "ToggleButton.textShiftOffset", Integer.valueOf(1),
            "ToggleButton.focusInputMap",
              new UIDefaults.LazyInputMap(new Object[] {
                            "SPACE", "pressed",
                   "released SPACE", "released"
                }),
            "ToolBar.font", MenuFont,
            "ToolBar.background", ControlBackgroundColor,
            "ToolBar.foreground", ControlTextColor,
            "ToolBar.shadow", ControlShadowColor,
            "ToolBar.darkShadow", ControlDarkShadowColor,
            "ToolBar.light", ControlLightColor,
            "ToolBar.highlight", ControlHighlightColor,
            "ToolBar.dockingBackground", ControlBackgroundColor,
            "ToolBar.dockingForeground", red,
            "ToolBar.floatingBackground", ControlBackgroundColor,
            "ToolBar.floatingForeground", darkGray,
            "ToolBar.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                        "UP", "navigateUp",
                     "KP_UP", "navigateUp",
                      "DOWN", "navigateDown",
                   "KP_DOWN", "navigateDown",
                      "LEFT", "navigateLeft",
                   "KP_LEFT", "navigateLeft",
                     "RIGHT", "navigateRight",
                  "KP_RIGHT", "navigateRight"
                 }),
            "ToolBar.separatorSize", null,
            "ToolTip.font", ToolTipFont,
            "ToolTip.background", new DesktopProperty("win.tooltip.backgroundColor", table.get("info")),
            "ToolTip.foreground", new DesktopProperty("win.tooltip.textColor", table.get("infoText")),
            "ToolTipManager.enableToolTipMode", "activeApplication",
            "Tree.selectionBorderColor", black,
            "Tree.drawDashedFocusIndicator", Boolean.TRUE,
            "Tree.lineTypeDashed", Boolean.TRUE,
            "Tree.font", ControlFont,
            "Tree.background", WindowBackgroundColor,
            "Tree.foreground", WindowTextColor,
            "Tree.hash", gray,
            "Tree.leftChildIndent", Integer.valueOf(8),
            "Tree.rightChildIndent", Integer.valueOf(11),
            "Tree.textForeground", WindowTextColor,
            "Tree.textBackground", WindowBackgroundColor,
            "Tree.selectionForeground", SelectionTextColor,
            "Tree.selectionBackground", SelectionBackgroundColor,
            "Tree.expandedIcon", treeExpandedIcon,
            "Tree.collapsedIcon", treeCollapsedIcon,
            "Tree.openIcon",   new ActiveWindowsIcon("win.icon.shellIconBPP",
                                   "shell32Icon 5", "icons/TreeOpen.gif"),
            "Tree.closedIcon", new ActiveWindowsIcon("win.icon.shellIconBPP",
                                   "shell32Icon 4", "icons/TreeClosed.gif"),
            "Tree.focusInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                                    "ADD", "expand",
                               "SUBTRACT", "collapse",
                                 "ctrl C", "copy",
                                 "ctrl V", "paste",
                                 "ctrl X", "cut",
                                   "COPY", "copy",
                                  "PASTE", "paste",
                                    "CUT", "cut",
                         "control INSERT", "copy",
                           "shift INSERT", "paste",
                           "shift DELETE", "cut",
                                     "UP", "selectPrevious",
                                  "KP_UP", "selectPrevious",
                               "shift UP", "selectPreviousExtendSelection",
                            "shift KP_UP", "selectPreviousExtendSelection",
                          "ctrl shift UP", "selectPreviousExtendSelection",
                       "ctrl shift KP_UP", "selectPreviousExtendSelection",
                                "ctrl UP", "selectPreviousChangeLead",
                             "ctrl KP_UP", "selectPreviousChangeLead",
                                   "DOWN", "selectNext",
                                "KP_DOWN", "selectNext",
                             "shift DOWN", "selectNextExtendSelection",
                          "shift KP_DOWN", "selectNextExtendSelection",
                        "ctrl shift DOWN", "selectNextExtendSelection",
                     "ctrl shift KP_DOWN", "selectNextExtendSelection",
                              "ctrl DOWN", "selectNextChangeLead",
                           "ctrl KP_DOWN", "selectNextChangeLead",
                                  "RIGHT", "selectChild",
                               "KP_RIGHT", "selectChild",
                                   "LEFT", "selectParent",
                                "KP_LEFT", "selectParent",
                                "PAGE_UP", "scrollUpChangeSelection",
                          "shift PAGE_UP", "scrollUpExtendSelection",
                     "ctrl shift PAGE_UP", "scrollUpExtendSelection",
                           "ctrl PAGE_UP", "scrollUpChangeLead",
                              "PAGE_DOWN", "scrollDownChangeSelection",
                        "shift PAGE_DOWN", "scrollDownExtendSelection",
                   "ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
                         "ctrl PAGE_DOWN", "scrollDownChangeLead",
                                   "HOME", "selectFirst",
                             "shift HOME", "selectFirstExtendSelection",
                        "ctrl shift HOME", "selectFirstExtendSelection",
                              "ctrl HOME", "selectFirstChangeLead",
                                    "END", "selectLast",
                              "shift END", "selectLastExtendSelection",
                         "ctrl shift END", "selectLastExtendSelection",
                               "ctrl END", "selectLastChangeLead",
                                     "F2", "startEditing",
                                 "ctrl A", "selectAll",
                             "ctrl SLASH", "selectAll",
                        "ctrl BACK_SLASH", "clearSelection",
                              "ctrl LEFT", "scrollLeft",
                           "ctrl KP_LEFT", "scrollLeft",
                             "ctrl RIGHT", "scrollRight",
                          "ctrl KP_RIGHT", "scrollRight",
                                  "SPACE", "addToSelection",
                             "ctrl SPACE", "toggleAndAnchor",
                            "shift SPACE", "extendTo",
                       "ctrl shift SPACE", "moveSelectionTo"
                 }),
            "Tree.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                     "ESCAPE", "cancel"
                 }),
            "Viewport.font", ControlFont,
            "Viewport.background", ControlBackgroundColor,
            "Viewport.foreground", WindowTextColor,
        };
        table.putDefaults(defaults);
        table.putDefaults(getLazyValueDefaults());
        initVistaComponentDefaults(table);
    }
    static boolean isOnVista() {
        return OSInfo.getOSType() == OSInfo.OSType.WINDOWS
                && OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_VISTA) >= 0;
    }
    private void initVistaComponentDefaults(UIDefaults table) {
        if (! isOnVista()) {
            return;
        }
        String[] menuClasses = { "MenuItem", "Menu",
                "CheckBoxMenuItem", "RadioButtonMenuItem",
        };
        Object menuDefaults[] = new Object[menuClasses.length * 2];
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".opaque";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(Boolean.FALSE, oldValue);
        }
        table.putDefaults(menuDefaults);
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".acceleratorSelectionForeground";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(
                    table.getColor(
                        menuClasses[i] + ".acceleratorForeground"),
                        oldValue);
        }
        table.putDefaults(menuDefaults);
        VistaMenuItemCheckIconFactory menuItemCheckIconFactory =
            WindowsIconFactory.getMenuItemCheckIconFactory();
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".checkIconFactory";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(menuItemCheckIconFactory, oldValue);
        }
        table.putDefaults(menuDefaults);
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".checkIcon";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(menuItemCheckIconFactory.getIcon(menuClasses[i]),
                    oldValue);
        }
        table.putDefaults(menuDefaults);
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".evenHeight";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] = new XPValue(Boolean.TRUE, oldValue);
        }
        table.putDefaults(menuDefaults);
        InsetsUIResource insets = new InsetsUIResource(0, 0, 0, 0);
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".margin";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] = new XPValue(insets, oldValue);
        }
        table.putDefaults(menuDefaults);
        Integer checkIconOffsetInteger =
            Integer.valueOf(0);
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".checkIconOffset";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(checkIconOffsetInteger, oldValue);
        }
        table.putDefaults(menuDefaults);
        Integer afterCheckIconGap = WindowsPopupMenuUI.getSpanBeforeGutter()
                + WindowsPopupMenuUI.getGutterWidth()
                + WindowsPopupMenuUI.getSpanAfterGutter();
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".afterCheckIconGap";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] =
                new XPValue(afterCheckIconGap, oldValue);
        }
        table.putDefaults(menuDefaults);
        Object minimumTextOffset = new UIDefaults.ActiveValue() {
            public Object createValue(UIDefaults table) {
                return VistaMenuItemCheckIconFactory.getIconWidth()
                + WindowsPopupMenuUI.getSpanBeforeGutter()
                + WindowsPopupMenuUI.getGutterWidth()
                + WindowsPopupMenuUI.getSpanAfterGutter();
            }
        };
        for (int i = 0, j = 0; i < menuClasses.length; i++) {
            String key = menuClasses[i] + ".minimumTextOffset";
            Object oldValue = table.get(key);
            menuDefaults[j++] = key;
            menuDefaults[j++] = new XPValue(minimumTextOffset, oldValue);
        }
        table.putDefaults(menuDefaults);
        String POPUP_MENU_BORDER = "PopupMenu.border";
        Object popupMenuBorder = new XPBorderValue(Part.MENU,
                new SwingLazyValue(
                  "javax.swing.plaf.basic.BasicBorders",
                  "getInternalFrameBorder"),
                  BorderFactory.createEmptyBorder(2, 2, 2, 2));
        table.put(POPUP_MENU_BORDER, popupMenuBorder);
        table.put("Table.ascendingSortIcon", new XPValue(
            new SkinIcon(Part.HP_HEADERSORTARROW, State.SORTEDDOWN),
            new SwingLazyValue(
                "sun.swing.plaf.windows.ClassicSortArrowIcon",
                null, new Object[] { Boolean.TRUE })));
        table.put("Table.descendingSortIcon", new XPValue(
            new SkinIcon(Part.HP_HEADERSORTARROW, State.SORTEDUP),
            new SwingLazyValue(
                "sun.swing.plaf.windows.ClassicSortArrowIcon",
                null, new Object[] { Boolean.FALSE })));
    }
    private Object getDesktopFontValue(String fontName, Object backup) {
        if (useSystemFontSettings) {
            return new WindowsFontProperty(fontName, backup);
        }
        return null;
    }
    private Object[] getLazyValueDefaults() {
        Object buttonBorder =
            new XPBorderValue(Part.BP_PUSHBUTTON,
                              new SwingLazyValue(
                               "javax.swing.plaf.basic.BasicBorders",
                               "getButtonBorder"));
        Object textFieldBorder =
            new XPBorderValue(Part.EP_EDIT,
                              new SwingLazyValue(
                               "javax.swing.plaf.basic.BasicBorders",
                               "getTextFieldBorder"));
        Object textFieldMargin =
            new XPValue(new InsetsUIResource(2, 2, 2, 2),
                        new InsetsUIResource(1, 1, 1, 1));
        Object spinnerBorder =
            new XPBorderValue(Part.EP_EDIT, textFieldBorder,
                              new EmptyBorder(2, 2, 2, 2));
        Object spinnerArrowInsets =
            new XPValue(new InsetsUIResource(1, 1, 1, 1),
                        null);
        Object comboBoxBorder = new XPBorderValue(Part.CP_COMBOBOX, textFieldBorder);
        Object focusCellHighlightBorder = new SwingLazyValue(
                          "com.sun.java.swing.plaf.windows.WindowsBorders",
                          "getFocusCellHighlightBorder");
        Object etchedBorder = new SwingLazyValue(
                          "javax.swing.plaf.BorderUIResource",
                          "getEtchedBorderUIResource");
        Object internalFrameBorder = new SwingLazyValue(
                "com.sun.java.swing.plaf.windows.WindowsBorders",
                "getInternalFrameBorder");
        Object loweredBevelBorder = new SwingLazyValue(
                          "javax.swing.plaf.BorderUIResource",
                          "getLoweredBevelBorderUIResource");
        Object marginBorder = new SwingLazyValue(
                            "javax.swing.plaf.basic.BasicBorders$MarginBorder");
        Object menuBarBorder = new SwingLazyValue(
                "javax.swing.plaf.basic.BasicBorders",
                "getMenuBarBorder");
        Object popupMenuBorder = new XPBorderValue(Part.MENU,
                        new SwingLazyValue(
                          "javax.swing.plaf.basic.BasicBorders",
                          "getInternalFrameBorder"));
        Object progressBarBorder = new SwingLazyValue(
                              "com.sun.java.swing.plaf.windows.WindowsBorders",
                              "getProgressBarBorder");
        Object radioButtonBorder = new SwingLazyValue(
                               "javax.swing.plaf.basic.BasicBorders",
                               "getRadioButtonBorder");
        Object scrollPaneBorder =
            new XPBorderValue(Part.LBP_LISTBOX, textFieldBorder);
        Object tableScrollPaneBorder =
            new XPBorderValue(Part.LBP_LISTBOX, loweredBevelBorder);
        Object tableHeaderBorder = new SwingLazyValue(
                          "com.sun.java.swing.plaf.windows.WindowsBorders",
                          "getTableHeaderBorder");
        Object toolBarBorder = new SwingLazyValue(
                              "com.sun.java.swing.plaf.windows.WindowsBorders",
                              "getToolBarBorder");
        Object toolTipBorder = new SwingLazyValue(
                              "javax.swing.plaf.BorderUIResource",
                              "getBlackLineBorderUIResource");
        Object checkBoxIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getCheckBoxIcon");
        Object radioButtonIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getRadioButtonIcon");
        Object radioButtonMenuItemIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getRadioButtonMenuItemIcon");
        Object menuItemCheckIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getMenuItemCheckIcon");
        Object menuItemArrowIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getMenuItemArrowIcon");
        Object menuArrowIcon = new SwingLazyValue(
                     "com.sun.java.swing.plaf.windows.WindowsIconFactory",
                     "getMenuArrowIcon");
        Object[] lazyDefaults = {
            "Button.border", buttonBorder,
            "CheckBox.border", radioButtonBorder,
            "ComboBox.border", comboBoxBorder,
            "DesktopIcon.border", internalFrameBorder,
            "FormattedTextField.border", textFieldBorder,
            "FormattedTextField.margin", textFieldMargin,
            "InternalFrame.border", internalFrameBorder,
            "List.focusCellHighlightBorder", focusCellHighlightBorder,
            "Table.focusCellHighlightBorder", focusCellHighlightBorder,
            "Menu.border", marginBorder,
            "MenuBar.border", menuBarBorder,
            "MenuItem.border", marginBorder,
            "PasswordField.border", textFieldBorder,
            "PasswordField.margin", textFieldMargin,
            "PopupMenu.border", popupMenuBorder,
            "ProgressBar.border", progressBarBorder,
            "RadioButton.border", radioButtonBorder,
            "ScrollPane.border", scrollPaneBorder,
            "Spinner.border", spinnerBorder,
            "Spinner.arrowButtonInsets", spinnerArrowInsets,
            "Spinner.arrowButtonSize", new Dimension(17, 9),
            "Table.scrollPaneBorder", tableScrollPaneBorder,
            "TableHeader.cellBorder", tableHeaderBorder,
            "TextArea.margin", textFieldMargin,
            "TextField.border", textFieldBorder,
            "TextField.margin", textFieldMargin,
            "TitledBorder.border",
                        new XPBorderValue(Part.BP_GROUPBOX, etchedBorder),
            "ToggleButton.border", radioButtonBorder,
            "ToolBar.border", toolBarBorder,
            "ToolTip.border", toolTipBorder,
            "CheckBox.icon", checkBoxIcon,
            "Menu.arrowIcon", menuArrowIcon,
            "MenuItem.checkIcon", menuItemCheckIcon,
            "MenuItem.arrowIcon", menuItemArrowIcon,
            "RadioButton.icon", radioButtonIcon,
            "RadioButtonMenuItem.checkIcon", radioButtonMenuItemIcon,
            "InternalFrame.layoutTitlePaneAtOrigin",
                        new XPValue(Boolean.TRUE, Boolean.FALSE),
            "Table.ascendingSortIcon", new XPValue(
                  new SwingLazyValue(
                     "sun.swing.icon.SortArrowIcon",
                     null, new Object[] { Boolean.TRUE,
                                          "Table.sortIconColor" }),
                  new SwingLazyValue(
                      "sun.swing.plaf.windows.ClassicSortArrowIcon",
                      null, new Object[] { Boolean.TRUE })),
            "Table.descendingSortIcon", new XPValue(
                  new SwingLazyValue(
                     "sun.swing.icon.SortArrowIcon",
                     null, new Object[] { Boolean.FALSE,
                                          "Table.sortIconColor" }),
                  new SwingLazyValue(
                     "sun.swing.plaf.windows.ClassicSortArrowIcon",
                     null, new Object[] { Boolean.FALSE })),
        };
        return lazyDefaults;
    }
    public void uninitialize() {
        super.uninitialize();
        if (WindowsPopupMenuUI.mnemonicListener != null) {
            MenuSelectionManager.defaultManager().
                removeChangeListener(WindowsPopupMenuUI.mnemonicListener);
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removeKeyEventPostProcessor(WindowsRootPaneUI.altProcessor);
        DesktopProperty.flushUnreferencedProperties();
    }
    private static boolean isMnemonicHidden = true;
    private static boolean isClassicWindows = false;
    public static void setMnemonicHidden(boolean hide) {
        if (UIManager.getBoolean("Button.showMnemonics") == true) {
            isMnemonicHidden = false;
        } else {
            isMnemonicHidden = hide;
        }
    }
    public static boolean isMnemonicHidden() {
        if (UIManager.getBoolean("Button.showMnemonics") == true) {
            isMnemonicHidden = false;
        }
        return isMnemonicHidden;
    }
    public static boolean isClassicWindows() {
        return isClassicWindows;
    }
     public void provideErrorFeedback(Component component) {
         super.provideErrorFeedback(component);
     }
    public LayoutStyle getLayoutStyle() {
        LayoutStyle style = this.style;
        if (style == null) {
            style = new WindowsLayoutStyle();
            this.style = style;
        }
        return style;
    }
    protected Action createAudioAction(Object key) {
        if (key != null) {
            String audioKey = (String)key;
            String audioValue = (String)UIManager.get(key);
            return new AudioAction(audioKey, audioValue);
        } else {
            return null;
        }
    }
    static void repaintRootPane(Component c) {
        JRootPane root = null;
        for (; c != null; c = c.getParent()) {
            if (c instanceof JRootPane) {
                root = (JRootPane)c;
            }
        }
        if (root != null) {
            root.repaint();
        } else {
            c.repaint();
        }
    }
    private static class AudioAction extends AbstractAction {
        private Runnable audioRunnable;
        private String audioResource;
        public AudioAction(String name, String resource) {
            super(name);
            audioResource = resource;
        }
        public void actionPerformed(ActionEvent e) {
            if (audioRunnable == null) {
                audioRunnable = (Runnable)Toolkit.getDefaultToolkit().getDesktopProperty(audioResource);
            }
            if (audioRunnable != null) {
                new Thread(audioRunnable).start();
            }
        }
    }
    private static class LazyWindowsIcon implements UIDefaults.LazyValue {
        private String nativeImage;
        private String resource;
        LazyWindowsIcon(String nativeImage, String resource) {
            this.nativeImage = nativeImage;
            this.resource = resource;
        }
        public Object createValue(UIDefaults table) {
            if (nativeImage != null) {
                Image image = (Image)ShellFolder.get(nativeImage);
                if (image != null) {
                    return new ImageIcon(image);
                }
            }
            return SwingUtilities2.makeIcon(getClass(),
                                            WindowsLookAndFeel.class,
                                            resource);
        }
    }
    private class ActiveWindowsIcon implements UIDefaults.ActiveValue {
        private Icon icon;
        private String nativeImageName;
        private String fallbackName;
        private DesktopProperty desktopProperty;
        ActiveWindowsIcon(String desktopPropertyName,
                            String nativeImageName, String fallbackName) {
            this.nativeImageName = nativeImageName;
            this.fallbackName = fallbackName;
            if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS &&
                    OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_XP) < 0) {
                this.desktopProperty = new TriggerDesktopProperty(desktopPropertyName) {
                    @Override protected void updateUI() {
                        icon = null;
                        super.updateUI();
                    }
                };
            }
        }
        @Override
        public Object createValue(UIDefaults table) {
            if (icon == null) {
                Image image = (Image)ShellFolder.get(nativeImageName);
                if (image != null) {
                    icon = new ImageIconUIResource(image);
                }
            }
            if (icon == null && fallbackName != null) {
                UIDefaults.LazyValue fallback = (UIDefaults.LazyValue)
                        SwingUtilities2.makeIcon(WindowsLookAndFeel.class,
                            BasicLookAndFeel.class, fallbackName);
                icon = (Icon) fallback.createValue(table);
            }
            return icon;
        }
    }
    private static class SkinIcon implements Icon, UIResource {
        private final Part part;
        private final State state;
        SkinIcon(Part part, State state) {
            this.part = part;
            this.state = state;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            XPStyle xp = XPStyle.getXP();
            assert xp != null;
            if (xp != null) {
                Skin skin = xp.getSkin(null, part);
                skin.paintSkin(g, x, y, state);
            }
        }
        public int getIconWidth() {
            int width = 0;
            XPStyle xp = XPStyle.getXP();
            assert xp != null;
            if (xp != null) {
                Skin skin = xp.getSkin(null, part);
                width = skin.getWidth();
            }
            return width;
        }
        public int getIconHeight() {
            int height = 0;
            XPStyle xp = XPStyle.getXP();
            if (xp != null) {
                Skin skin = xp.getSkin(null, part);
                height = skin.getHeight();
            }
            return height;
        }
    }
    private static class WindowsFontProperty extends DesktopProperty {
        WindowsFontProperty(String key, Object backup) {
            super(key, backup);
        }
        public void invalidate(LookAndFeel laf) {
            if ("win.defaultGUI.font.height".equals(getKey())) {
                ((WindowsLookAndFeel)laf).style = null;
            }
            super.invalidate(laf);
        }
        protected Object configureValue(Object value) {
            if (value instanceof Font) {
                Font font = (Font)value;
                if ("MS Sans Serif".equals(font.getName())) {
                    int size = font.getSize();
                    int dpi;
                    try {
                        dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                    } catch (HeadlessException ex) {
                        dpi = 96;
                    }
                    if (Math.round(size * 72F / dpi) < 8) {
                        size = Math.round(8 * dpi / 72F);
                    }
                    Font msFont = new FontUIResource("Microsoft Sans Serif",
                                          font.getStyle(), size);
                    if (msFont.getName() != null &&
                        msFont.getName().equals(msFont.getFamily())) {
                        font = msFont;
                    } else if (size != font.getSize()) {
                        font = new FontUIResource("MS Sans Serif",
                                                  font.getStyle(), size);
                    }
                }
                if (FontUtilities.fontSupportsDefaultEncoding(font)) {
                    if (!(font instanceof UIResource)) {
                        font = new FontUIResource(font);
                    }
                }
                else {
                    font = FontUtilities.getCompositeFontUIResource(font);
                }
                return font;
            }
            return super.configureValue(value);
        }
    }
    private static class WindowsFontSizeProperty extends DesktopProperty {
        private String fontName;
        private int fontSize;
        private int fontStyle;
        WindowsFontSizeProperty(String key, String fontName,
                                int fontStyle, int fontSize) {
            super(key, null);
            this.fontName = fontName;
            this.fontSize = fontSize;
            this.fontStyle = fontStyle;
        }
        protected Object configureValue(Object value) {
            if (value == null) {
                value = new FontUIResource(fontName, fontStyle, fontSize);
            }
            else if (value instanceof Integer) {
                value = new FontUIResource(fontName, fontStyle,
                                           ((Integer)value).intValue());
            }
            return value;
        }
    }
    private static class XPValue implements UIDefaults.ActiveValue {
        protected Object classicValue, xpValue;
        private final static Object NULL_VALUE = new Object();
        XPValue(Object xpValue, Object classicValue) {
            this.xpValue = xpValue;
            this.classicValue = classicValue;
        }
        public Object createValue(UIDefaults table) {
            Object value = null;
            if (XPStyle.getXP() != null) {
                value = getXPValue(table);
            }
            if (value == null) {
                value = getClassicValue(table);
            } else if (value == NULL_VALUE) {
                value = null;
            }
            return value;
        }
        protected Object getXPValue(UIDefaults table) {
            return recursiveCreateValue(xpValue, table);
        }
        protected Object getClassicValue(UIDefaults table) {
            return recursiveCreateValue(classicValue, table);
        }
        private Object recursiveCreateValue(Object value, UIDefaults table) {
            if (value instanceof UIDefaults.LazyValue) {
                value = ((UIDefaults.LazyValue)value).createValue(table);
            }
            if (value instanceof UIDefaults.ActiveValue) {
                return ((UIDefaults.ActiveValue)value).createValue(table);
            } else {
                return value;
            }
        }
    }
    private static class XPBorderValue extends XPValue {
        private final Border extraMargin;
        XPBorderValue(Part xpValue, Object classicValue) {
            this(xpValue, classicValue, null);
        }
        XPBorderValue(Part xpValue, Object classicValue, Border extraMargin) {
            super(xpValue, classicValue);
            this.extraMargin = extraMargin;
        }
        public Object getXPValue(UIDefaults table) {
            Border xpBorder = XPStyle.getXP().getBorder(null, (Part)xpValue);
            if (extraMargin != null) {
                return new BorderUIResource.
                        CompoundBorderUIResource(xpBorder, extraMargin);
            } else {
                return xpBorder;
            }
        }
    }
    private static class XPColorValue extends XPValue {
        XPColorValue(Part part, State state, Prop prop, Object classicValue) {
            super(new XPColorValueKey(part, state, prop), classicValue);
        }
        public Object getXPValue(UIDefaults table) {
            XPColorValueKey key = (XPColorValueKey)xpValue;
            return XPStyle.getXP().getColor(key.skin, key.prop, null);
        }
        private static class XPColorValueKey {
            Skin skin;
            Prop prop;
            XPColorValueKey(Part part, State state, Prop prop) {
                this.skin = new Skin(part, state);
                this.prop = prop;
            }
        }
    }
    private class XPDLUValue extends XPValue {
        private int direction;
        XPDLUValue(int xpdlu, int classicdlu, int direction) {
            super(Integer.valueOf(xpdlu), Integer.valueOf(classicdlu));
            this.direction = direction;
        }
        public Object getXPValue(UIDefaults table) {
            int px = dluToPixels(((Integer)xpValue).intValue(), direction);
            return Integer.valueOf(px);
        }
        public Object getClassicValue(UIDefaults table) {
            int px = dluToPixels(((Integer)classicValue).intValue(), direction);
            return Integer.valueOf(px);
        }
    }
    private class TriggerDesktopProperty extends DesktopProperty {
        TriggerDesktopProperty(String key) {
            super(key, null);
            getValueFromDesktop();
        }
        protected void updateUI() {
            super.updateUI();
            getValueFromDesktop();
        }
    }
    private class FontDesktopProperty extends TriggerDesktopProperty {
        FontDesktopProperty(String key) {
            super(key);
        }
        protected void updateUI() {
            Object aaTextInfo = SwingUtilities2.AATextInfo.getAATextInfo(true);
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            defaults.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
            super.updateUI();
        }
    }
    private class WindowsLayoutStyle extends DefaultLayoutStyle {
        @Override
        public int getPreferredGap(JComponent component1,
                JComponent component2, ComponentPlacement type, int position,
                Container parent) {
            super.getPreferredGap(component1, component2, type, position,
                                  parent);
            switch(type) {
            case INDENT:
                if (position == SwingConstants.EAST ||
                        position == SwingConstants.WEST) {
                    int indent = getIndent(component1, position);
                    if (indent > 0) {
                        return indent;
                    }
                    return 10;
                }
            case RELATED:
                if (isLabelAndNonlabel(component1, component2, position)) {
                    return getButtonGap(component1, component2, position,
                                        dluToPixels(3, position));
                }
                return getButtonGap(component1, component2, position,
                                    dluToPixels(4, position));
            case UNRELATED:
                return getButtonGap(component1, component2, position,
                                    dluToPixels(7, position));
            }
            return 0;
        }
        @Override
        public int getContainerGap(JComponent component, int position,
                                   Container parent) {
            super.getContainerGap(component, position, parent);
            return getButtonGap(component, position, dluToPixels(7, position));
        }
    }
    private int dluToPixels(int dlu, int direction) {
        if (baseUnitX == 0) {
            calculateBaseUnits();
        }
        if (direction == SwingConstants.EAST ||
            direction == SwingConstants.WEST) {
            return dlu * baseUnitX / 4;
        }
        assert (direction == SwingConstants.NORTH ||
                direction == SwingConstants.SOUTH);
        return dlu * baseUnitY / 8;
    }
    private void calculateBaseUnits() {
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
                UIManager.getFont("Button.font"));
        baseUnitX = metrics.stringWidth(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        baseUnitX = (baseUnitX / 26 + 1) / 2;
        baseUnitY = metrics.getAscent() + metrics.getDescent() - 1;
    }
    public Icon getDisabledIcon(JComponent component, Icon icon) {
        if (icon != null
                && component != null
                && Boolean.TRUE.equals(component.getClientProperty(HI_RES_DISABLED_ICON_CLIENT_KEY))
                && icon.getIconWidth() > 0
                && icon.getIconHeight() > 0) {
            BufferedImage img = new BufferedImage(icon.getIconWidth(),
                    icon.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(component, img.getGraphics(), 0, 0);
            ImageFilter filter = new RGBGrayFilter();
            ImageProducer producer = new FilteredImageSource(img.getSource(), filter);
            Image resultImage = component.createImage(producer);
            return new ImageIconUIResource(resultImage);
        }
        return super.getDisabledIcon(component, icon);
    }
    private static class RGBGrayFilter extends RGBImageFilter {
        public RGBGrayFilter() {
            canFilterIndexColorModel = true;
        }
        public int filterRGB(int x, int y, int rgb) {
            float avg = (((rgb >> 16) & 0xff) / 255f +
                          ((rgb >>  8) & 0xff) / 255f +
                           (rgb        & 0xff) / 255f) / 3;
            float alpha = (((rgb>>24)&0xff)/255f);
            avg = Math.min(1.0f, (1f-avg)/(100.0f/35.0f) + avg);
            int rgbval = (int)(alpha * 255f) << 24 |
                         (int)(avg   * 255f) << 16 |
                         (int)(avg   * 255f) <<  8 |
                         (int)(avg   * 255f);
            return rgbval;
        }
    }
}
