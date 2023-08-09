public class Test4856008 {
    private static final JLabel LABEL = new JLabel();
    private static final JPopupMenu POPUP = new JPopupMenu();
    private static final JToolBar TOOLBAR = new JToolBar();
    private static final Border[] BORDERS = {
            new MotifBorders.BevelBorder(true, Color.BLACK, Color.WHITE),
            new MotifBorders.ButtonBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new MotifBorders.FocusBorder(Color.BLACK, Color.WHITE),
            new MotifBorders.FrameBorder(LABEL),
            new MotifBorders.MenuBarBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new MotifBorders.MotifPopupMenuBorder(new Font(null, Font.PLAIN, 10), Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new MotifBorders.ToggleButtonBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new WindowsBorders.ProgressBarBorder(Color.BLACK, Color.WHITE),
            new WindowsBorders.ToolBarBorder(Color.BLACK, Color.WHITE),
            new BevelBorder(BevelBorder.RAISED),
            new CompoundBorder(),
            new EmptyBorder(1, 2, 3, 4),
            new EtchedBorder(),
            new LineBorder(Color.BLACK, 2, true),
            new MatteBorder(1, 2, 3, 4, Color.BLACK),
            new SoftBevelBorder(BevelBorder.LOWERED),
            new TitledBorder("4856008"),
            new BorderUIResource(new EmptyBorder(1, 2, 3, 4)),
            new BasicBorders.ButtonBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new BasicBorders.FieldBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new BasicBorders.MarginBorder(),
            new BasicBorders.MenuBarBorder(Color.BLACK, Color.WHITE),
            new BasicBorders.RadioButtonBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new ToolBar().getRolloverMarginBorder(),
            new BasicBorders.SplitPaneBorder(Color.BLACK, Color.WHITE),
            BasicBorders.getSplitPaneDividerBorder(),
            new BasicBorders.ToggleButtonBorder(Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK),
            new MetalBorders.ButtonBorder(),
            new MetalBorders.Flush3DBorder(),
            new MetalBorders.InternalFrameBorder(),
            new MetalBorders.MenuBarBorder(),
            new MetalBorders.MenuItemBorder(),
            new MetalBorders.OptionDialogBorder(),
            new MetalBorders.PaletteBorder(),
            new MetalBorders.PopupMenuBorder(),
            new MetalBorders.ScrollPaneBorder(),
            new MetalBorders.TableHeaderBorder(),
            new MetalBorders.ToolBarBorder(),
            new MetalEditor().getEditorBorder(),
            new SynthFileChooser().getUIBorder(),
            new NimbusLookAndFeel().getDefaults().getBorder("TitledBorder.border"),
    };
    public static void main(String[] args) {
        for (Border border : BORDERS) {
            System.out.println(border.getClass());
            test(border, border.getBorderInsets(getComponent(border)));
            if (border instanceof AbstractBorder) {
                test((AbstractBorder) border);
            }
        }
    }
    private static void test(AbstractBorder border) {
        Insets insets = new Insets(0, 0, 0, 0);
        if (insets != border.getBorderInsets(getComponent(border), insets)) {
            throw new Error("both instances are differ for " + border.getClass());
        }
        test(border, insets);
    }
    private static void test(Border border, Insets insets) {
        Insets result = border.getBorderInsets(getComponent(border));
        if (insets == result) {
            throw new Error("both instances are the same for " + border.getClass());
        }
        if (!insets.equals(result)) {
            throw new Error("both insets are not equal for " + border.getClass());
        }
    }
    private static JComponent getComponent(Border border) {
        Class type = border.getClass();
        if (type.equals(MotifBorders.MotifPopupMenuBorder.class)) {
            return POPUP;
        }
        if (type.equals(WindowsBorders.ToolBarBorder.class)) {
            return TOOLBAR;
        }
        if (type.equals(MetalBorders.ToolBarBorder.class)) {
            return TOOLBAR;
        }
        return LABEL;
    }
    private static class ToolBar extends BasicToolBarUI {
        private Border getRolloverMarginBorder() {
            JToggleButton button = new JToggleButton();
            CompoundBorder border = (CompoundBorder) getNonRolloverBorder(button);
            return border.getInsideBorder();
        }
    }
    private static class MetalEditor extends MetalComboBoxEditor {
        private Border getEditorBorder() {
            return editor.getBorder();
        }
    }
    private static class SynthFileChooser extends SynthFileChooserUI {
        private static final JFileChooser CHOOSER = new JFileChooser();
        private String name;
        private SynthFileChooser() {
            super(CHOOSER);
        }
        private Border getUIBorder() {
            new SynthLookAndFeel().initialize();
            CHOOSER.setBorder(null);
            installDefaults(CHOOSER);
            return CHOOSER.getBorder();
        }
        @Override
        protected ActionMap createActionMap() {
            return new ActionMapUIResource();
        }
        @Override
        public String getFileName() {
            return this.name;
        }
        @Override
        public void setFileName(String name) {
            this.name = name;
        }
    }
}
