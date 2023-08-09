public class OceanTheme extends DefaultMetalTheme {
    private static final ColorUIResource PRIMARY1 =
                              new ColorUIResource(0x6382BF);
    private static final ColorUIResource PRIMARY2 =
                              new ColorUIResource(0xA3B8CC);
    private static final ColorUIResource PRIMARY3 =
                              new ColorUIResource(0xB8CFE5);
    private static final ColorUIResource SECONDARY1 =
                              new ColorUIResource(0x7A8A99);
    private static final ColorUIResource SECONDARY2 =
                              new ColorUIResource(0xB8CFE5);
    private static final ColorUIResource SECONDARY3 =
                              new ColorUIResource(0xEEEEEE);
    private static final ColorUIResource CONTROL_TEXT_COLOR =
                              new PrintColorUIResource(0x333333, Color.BLACK);
    private static final ColorUIResource INACTIVE_CONTROL_TEXT_COLOR =
                              new ColorUIResource(0x999999);
    private static final ColorUIResource MENU_DISABLED_FOREGROUND =
                              new ColorUIResource(0x999999);
    private static final ColorUIResource OCEAN_BLACK =
                              new PrintColorUIResource(0x333333, Color.BLACK);
    private static final ColorUIResource OCEAN_DROP =
                              new ColorUIResource(0xD2E9FF);
    private static class COIcon extends IconUIResource {
        private Icon rtl;
        public COIcon(Icon ltr, Icon rtl) {
            super(ltr);
            this.rtl = rtl;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (MetalUtils.isLeftToRight(c)) {
                super.paintIcon(c, g, x, y);
            } else {
                rtl.paintIcon(c, g, x, y);
            }
        }
    }
    private static class IFIcon extends IconUIResource {
        private Icon pressed;
        public IFIcon(Icon normal, Icon pressed) {
            super(normal);
            this.pressed = pressed;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            ButtonModel model = ((AbstractButton)c).getModel();
            if (model.isPressed() && model.isArmed()) {
                pressed.paintIcon(c, g, x, y);
            } else {
                super.paintIcon(c, g, x, y);
            }
        }
    }
    public OceanTheme() {
    }
    public void addCustomEntriesToTable(UIDefaults table) {
        Object focusBorder = new SwingLazyValue(
                      "javax.swing.plaf.BorderUIResource$LineBorderUIResource",
                      new Object[] {getPrimary1()});
        java.util.List buttonGradient = Arrays.asList(
                 new Object[] {new Float(.3f), new Float(0f),
                 new ColorUIResource(0xDDE8F3), getWhite(), getSecondary2() });
        Color cccccc = new ColorUIResource(0xCCCCCC);
        Color dadada = new ColorUIResource(0xDADADA);
        Color c8ddf2 = new ColorUIResource(0xC8DDF2);
        Object directoryIcon = getIconResource("icons/ocean/directory.gif");
        Object fileIcon = getIconResource("icons/ocean/file.gif");
        java.util.List sliderGradient = Arrays.asList(new Object[] {
            new Float(.3f), new Float(.2f),
            c8ddf2, getWhite(), new ColorUIResource(SECONDARY2) });
        Object[] defaults = new Object[] {
            "Button.gradient", buttonGradient,
            "Button.rollover", Boolean.TRUE,
            "Button.toolBarBorderBackground", INACTIVE_CONTROL_TEXT_COLOR,
            "Button.disabledToolBarBorderBackground", cccccc,
            "Button.rolloverIconType", "ocean",
            "CheckBox.rollover", Boolean.TRUE,
            "CheckBox.gradient", buttonGradient,
            "CheckBoxMenuItem.gradient", buttonGradient,
            "FileChooser.homeFolderIcon",
                 getIconResource("icons/ocean/homeFolder.gif"),
            "FileChooser.newFolderIcon",
                 getIconResource("icons/ocean/newFolder.gif"),
            "FileChooser.upFolderIcon",
                 getIconResource("icons/ocean/upFolder.gif"),
            "FileView.computerIcon",
                 getIconResource("icons/ocean/computer.gif"),
            "FileView.directoryIcon", directoryIcon,
            "FileView.hardDriveIcon",
                 getIconResource("icons/ocean/hardDrive.gif"),
            "FileView.fileIcon", fileIcon,
            "FileView.floppyDriveIcon",
                 getIconResource("icons/ocean/floppy.gif"),
            "Label.disabledForeground", getInactiveControlTextColor(),
            "Menu.opaque", Boolean.FALSE,
            "MenuBar.gradient", Arrays.asList(new Object[] {
                     new Float(1f), new Float(0f),
                     getWhite(), dadada,
                     new ColorUIResource(dadada) }),
            "MenuBar.borderColor", cccccc,
            "InternalFrame.activeTitleGradient", buttonGradient,
            "InternalFrame.closeIcon",
                     new UIDefaults.LazyValue() {
                         public Object createValue(UIDefaults table) {
                             return new IFIcon(getHastenedIcon("icons/ocean/close.gif", table),
                                               getHastenedIcon("icons/ocean/close-pressed.gif", table));
                         }
                     },
            "InternalFrame.iconifyIcon",
                     new UIDefaults.LazyValue() {
                         public Object createValue(UIDefaults table) {
                             return new IFIcon(getHastenedIcon("icons/ocean/iconify.gif", table),
                                               getHastenedIcon("icons/ocean/iconify-pressed.gif", table));
                         }
                     },
            "InternalFrame.minimizeIcon",
                     new UIDefaults.LazyValue() {
                         public Object createValue(UIDefaults table) {
                             return new IFIcon(getHastenedIcon("icons/ocean/minimize.gif", table),
                                               getHastenedIcon("icons/ocean/minimize-pressed.gif", table));
                         }
                     },
            "InternalFrame.icon",
                     getIconResource("icons/ocean/menu.gif"),
            "InternalFrame.maximizeIcon",
                     new UIDefaults.LazyValue() {
                         public Object createValue(UIDefaults table) {
                             return new IFIcon(getHastenedIcon("icons/ocean/maximize.gif", table),
                                               getHastenedIcon("icons/ocean/maximize-pressed.gif", table));
                         }
                     },
            "InternalFrame.paletteCloseIcon",
                     new UIDefaults.LazyValue() {
                         public Object createValue(UIDefaults table) {
                             return new IFIcon(getHastenedIcon("icons/ocean/paletteClose.gif", table),
                                               getHastenedIcon("icons/ocean/paletteClose-pressed.gif", table));
                         }
                     },
            "List.focusCellHighlightBorder", focusBorder,
            "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI",
            "OptionPane.errorIcon",
                   getIconResource("icons/ocean/error.png"),
            "OptionPane.informationIcon",
                   getIconResource("icons/ocean/info.png"),
            "OptionPane.questionIcon",
                   getIconResource("icons/ocean/question.png"),
            "OptionPane.warningIcon",
                   getIconResource("icons/ocean/warning.png"),
            "RadioButton.gradient", buttonGradient,
            "RadioButton.rollover", Boolean.TRUE,
            "RadioButtonMenuItem.gradient", buttonGradient,
            "ScrollBar.gradient", buttonGradient,
            "Slider.altTrackColor", new ColorUIResource(0xD2E2EF),
            "Slider.gradient", sliderGradient,
            "Slider.focusGradient", sliderGradient,
            "SplitPane.oneTouchButtonsOpaque", Boolean.FALSE,
            "SplitPane.dividerFocusColor", c8ddf2,
            "TabbedPane.borderHightlightColor", getPrimary1(),
            "TabbedPane.contentAreaColor", c8ddf2,
            "TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 3),
            "TabbedPane.selected", c8ddf2,
            "TabbedPane.tabAreaBackground", dadada,
            "TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 6),
            "TabbedPane.unselectedBackground", SECONDARY3,
            "Table.focusCellHighlightBorder", focusBorder,
            "Table.gridColor", SECONDARY1,
            "TableHeader.focusCellBackground", c8ddf2,
            "ToggleButton.gradient", buttonGradient,
            "ToolBar.borderColor", cccccc,
            "ToolBar.isRollover", Boolean.TRUE,
            "Tree.closedIcon", directoryIcon,
            "Tree.collapsedIcon",
                  new UIDefaults.LazyValue() {
                      public Object createValue(UIDefaults table) {
                          return new COIcon(getHastenedIcon("icons/ocean/collapsed.gif", table),
                                            getHastenedIcon("icons/ocean/collapsed-rtl.gif", table));
                      }
                  },
            "Tree.expandedIcon",
                  getIconResource("icons/ocean/expanded.gif"),
            "Tree.leafIcon", fileIcon,
            "Tree.openIcon", directoryIcon,
            "Tree.selectionBorderColor", getPrimary1(),
            "Tree.dropLineColor", getPrimary1(),
            "Table.dropLineColor", getPrimary1(),
            "Table.dropLineShortColor", OCEAN_BLACK,
            "Table.dropCellBackground", OCEAN_DROP,
            "Tree.dropCellBackground", OCEAN_DROP,
            "List.dropCellBackground", OCEAN_DROP,
            "List.dropLineColor", getPrimary1()
        };
        table.putDefaults(defaults);
    }
    boolean isSystemTheme() {
        return true;
    }
    public String getName() {
        return "Ocean";
    }
    protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    }
    protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    }
    protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    }
    protected ColorUIResource getSecondary1() {
        return SECONDARY1;
    }
    protected ColorUIResource getSecondary2() {
        return SECONDARY2;
    }
    protected ColorUIResource getSecondary3() {
        return SECONDARY3;
    }
    protected ColorUIResource getBlack() {
        return OCEAN_BLACK;
    }
    public ColorUIResource getDesktopColor() {
        return MetalTheme.white;
    }
    public ColorUIResource getInactiveControlTextColor() {
        return INACTIVE_CONTROL_TEXT_COLOR;
    }
    public ColorUIResource getControlTextColor() {
        return CONTROL_TEXT_COLOR;
    }
    public ColorUIResource getMenuDisabledForeground() {
        return MENU_DISABLED_FOREGROUND;
    }
    private Object getIconResource(String iconID) {
        return SwingUtilities2.makeIcon(getClass(), OceanTheme.class, iconID);
    }
    private Icon getHastenedIcon(String iconID, UIDefaults table) {
        Object res = getIconResource(iconID);
        return (Icon)((UIDefaults.LazyValue)res).createValue(table);
    }
}
