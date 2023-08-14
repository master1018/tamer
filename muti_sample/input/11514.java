public class SynthSpinnerUI extends BasicSpinnerUI
                            implements PropertyChangeListener, SynthUI {
    private SynthStyle style;
    private EditorFocusHandler editorFocusHandler = new EditorFocusHandler();
    public static ComponentUI createUI(JComponent c) {
        return new SynthSpinnerUI();
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        spinner.addPropertyChangeListener(this);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editor).getTextField();
            if (tf != null) {
                tf.addFocusListener(editorFocusHandler);
            }
        }
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        spinner.removePropertyChangeListener(this);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editor).getTextField();
            if (tf != null) {
                tf.removeFocusListener(editorFocusHandler);
            }
        }
    }
    @Override
    protected void installDefaults() {
        LayoutManager layout = spinner.getLayout();
        if (layout == null || layout instanceof UIResource) {
            spinner.setLayout(createLayout());
        }
        updateStyle(spinner);
    }
    private void updateStyle(JSpinner c) {
        SynthContext context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            if (oldStyle != null) {
                installKeyboardActions();
            }
        }
        context.dispose();
    }
    @Override
    protected void uninstallDefaults() {
        if (spinner.getLayout() instanceof UIResource) {
            spinner.setLayout(null);
        }
        SynthContext context = getContext(spinner, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
    }
    @Override
    protected LayoutManager createLayout() {
        return new SpinnerLayout();
    }
    @Override
    protected Component createPreviousButton() {
        JButton b = new SynthArrowButton(SwingConstants.SOUTH);
        b.setName("Spinner.previousButton");
        installPreviousButtonListeners(b);
        return b;
    }
    @Override
    protected Component createNextButton() {
        JButton b = new SynthArrowButton(SwingConstants.NORTH);
        b.setName("Spinner.nextButton");
        installNextButtonListeners(b);
        return b;
    }
    @Override
    protected JComponent createEditor() {
        JComponent editor = spinner.getEditor();
        editor.setName("Spinner.editor");
        updateEditorAlignment(editor);
        return editor;
    }
    @Override
    protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
        spinner.remove(oldEditor);
        spinner.add(newEditor, "Editor");
        if (oldEditor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)oldEditor).getTextField();
            if (tf != null) {
                tf.removeFocusListener(editorFocusHandler);
            }
        }
        if (newEditor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)newEditor).getTextField();
            if (tf != null) {
                tf.addFocusListener(editorFocusHandler);
            }
        }
    }
    private void updateEditorAlignment(JComponent editor) {
        if (editor instanceof JSpinner.DefaultEditor) {
            SynthContext context = getContext(spinner);
            Integer alignment = (Integer)context.getStyle().get(
                    context, "Spinner.editorAlignment");
            JTextField text = ((JSpinner.DefaultEditor)editor).getTextField();
            if (alignment != null) {
                text.setHorizontalAlignment(alignment);
            }
            text.putClientProperty("JComponent.sizeVariant",
                    spinner.getClientProperty("JComponent.sizeVariant"));
        }
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintSpinnerBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintSpinnerBorder(context, g, x, y, w, h);
    }
    private static class SpinnerLayout implements LayoutManager, UIResource
    {
        private Component nextButton = null;
        private Component previousButton = null;
        private Component editor = null;
        public void addLayoutComponent(String name, Component c) {
            if ("Next".equals(name)) {
                nextButton = c;
            }
            else if ("Previous".equals(name)) {
                previousButton = c;
            }
            else if ("Editor".equals(name)) {
                editor = c;
            }
        }
        public void removeLayoutComponent(Component c) {
            if (c == nextButton) {
                nextButton = null;
            }
            else if (c == previousButton) {
                previousButton = null;
            }
            else if (c == editor) {
                editor = null;
            }
        }
        private Dimension preferredSize(Component c) {
            return (c == null) ? new Dimension(0, 0) : c.getPreferredSize();
        }
        public Dimension preferredLayoutSize(Container parent) {
            Dimension nextD = preferredSize(nextButton);
            Dimension previousD = preferredSize(previousButton);
            Dimension editorD = preferredSize(editor);
            editorD.height = ((editorD.height + 1) / 2) * 2;
            Dimension size = new Dimension(editorD.width, editorD.height);
            size.width += Math.max(nextD.width, previousD.width);
            Insets insets = parent.getInsets();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }
        private void setBounds(Component c, int x, int y, int width, int height) {
            if (c != null) {
                c.setBounds(x, y, width, height);
            }
        }
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int availWidth = parent.getWidth() - (insets.left + insets.right);
            int availHeight = parent.getHeight() - (insets.top + insets.bottom);
            Dimension nextD = preferredSize(nextButton);
            Dimension previousD = preferredSize(previousButton);
            int nextHeight = availHeight / 2;
            int previousHeight = availHeight - nextHeight;
            int buttonsWidth = Math.max(nextD.width, previousD.width);
            int editorWidth = availWidth - buttonsWidth;
            int editorX, buttonsX;
            if (parent.getComponentOrientation().isLeftToRight()) {
                editorX = insets.left;
                buttonsX = editorX + editorWidth;
            }
            else {
                buttonsX = insets.left;
                editorX = buttonsX + buttonsWidth;
            }
            int previousY = insets.top + nextHeight;
            setBounds(editor, editorX, insets.top, editorWidth, availHeight);
            setBounds(nextButton, buttonsX, insets.top, buttonsWidth, nextHeight);
            setBounds(previousButton, buttonsX, previousY, buttonsWidth, previousHeight);
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        JSpinner spinner = (JSpinner)(e.getSource());
        SpinnerUI spinnerUI = spinner.getUI();
        if (spinnerUI instanceof SynthSpinnerUI) {
            SynthSpinnerUI ui = (SynthSpinnerUI)spinnerUI;
            if (SynthLookAndFeel.shouldUpdateStyle(e)) {
                ui.updateStyle(spinner);
            }
        }
    }
    private class EditorFocusHandler implements FocusListener{
        @Override public void focusGained(FocusEvent e) {
            spinner.repaint();
        }
        @Override public void focusLost(FocusEvent e) {
            spinner.repaint();
        }
    }
}
