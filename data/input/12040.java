public class BasicSpinnerUI extends SpinnerUI
{
    protected JSpinner spinner;
    private Handler handler;
    private static final ArrowButtonHandler nextButtonHandler = new ArrowButtonHandler("increment", true);
    private static final ArrowButtonHandler previousButtonHandler = new ArrowButtonHandler("decrement", false);
    private PropertyChangeListener propertyChangeListener;
    private static final Dimension zeroSize = new Dimension(0, 0);
    public static ComponentUI createUI(JComponent c) {
        return new BasicSpinnerUI();
    }
    private void maybeAdd(Component c, String s) {
        if (c != null) {
            spinner.add(c, s);
        }
    }
    public void installUI(JComponent c) {
        this.spinner = (JSpinner)c;
        installDefaults();
        installListeners();
        maybeAdd(createNextButton(), "Next");
        maybeAdd(createPreviousButton(), "Previous");
        maybeAdd(createEditor(), "Editor");
        updateEnabledState();
        installKeyboardActions();
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults();
        uninstallListeners();
        this.spinner = null;
        c.removeAll();
    }
    protected void installListeners() {
        propertyChangeListener = createPropertyChangeListener();
        spinner.addPropertyChangeListener(propertyChangeListener);
        if (DefaultLookup.getBoolean(spinner, this,
            "Spinner.disableOnBoundaryValues", false)) {
            spinner.addChangeListener(getHandler());
        }
        JComponent editor = spinner.getEditor();
        if (editor != null && editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editor).getTextField();
            if (tf != null) {
                tf.addFocusListener(nextButtonHandler);
                tf.addFocusListener(previousButtonHandler);
            }
        }
    }
    protected void uninstallListeners() {
        spinner.removePropertyChangeListener(propertyChangeListener);
        spinner.removeChangeListener(handler);
        JComponent editor = spinner.getEditor();
        removeEditorBorderListener(editor);
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editor).getTextField();
            if (tf != null) {
                tf.removeFocusListener(nextButtonHandler);
                tf.removeFocusListener(previousButtonHandler);
            }
        }
        propertyChangeListener = null;
        handler = null;
    }
    protected void installDefaults() {
        spinner.setLayout(createLayout());
        LookAndFeel.installBorder(spinner, "Spinner.border");
        LookAndFeel.installColorsAndFont(spinner, "Spinner.background", "Spinner.foreground", "Spinner.font");
        LookAndFeel.installProperty(spinner, "opaque", Boolean.TRUE);
    }
    protected void uninstallDefaults() {
        spinner.setLayout(null);
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected void installNextButtonListeners(Component c) {
        installButtonListeners(c, nextButtonHandler);
    }
    protected void installPreviousButtonListeners(Component c) {
        installButtonListeners(c, previousButtonHandler);
    }
    private void installButtonListeners(Component c,
                                        ArrowButtonHandler handler) {
        if (c instanceof JButton) {
            ((JButton)c).addActionListener(handler);
        }
        c.addMouseListener(handler);
    }
    protected LayoutManager createLayout() {
        return getHandler();
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    protected Component createPreviousButton() {
        Component c = createArrowButton(SwingConstants.SOUTH);
        c.setName("Spinner.previousButton");
        installPreviousButtonListeners(c);
        return c;
    }
    protected Component createNextButton() {
        Component c = createArrowButton(SwingConstants.NORTH);
        c.setName("Spinner.nextButton");
        installNextButtonListeners(c);
        return c;
    }
    private Component createArrowButton(int direction) {
        JButton b = new BasicArrowButton(direction);
        Border buttonBorder = UIManager.getBorder("Spinner.arrowButtonBorder");
        if (buttonBorder instanceof UIResource) {
            b.setBorder(new CompoundBorder(buttonBorder, null));
        } else {
            b.setBorder(buttonBorder);
        }
        b.setInheritsPopupMenu(true);
        return b;
    }
    protected JComponent createEditor() {
        JComponent editor = spinner.getEditor();
        maybeRemoveEditorBorder(editor);
        installEditorBorderListener(editor);
        editor.setInheritsPopupMenu(true);
        updateEditorAlignment(editor);
        return editor;
    }
    protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
        spinner.remove(oldEditor);
        maybeRemoveEditorBorder(newEditor);
        installEditorBorderListener(newEditor);
        newEditor.setInheritsPopupMenu(true);
        spinner.add(newEditor, "Editor");
    }
    private void updateEditorAlignment(JComponent editor) {
        if (editor instanceof JSpinner.DefaultEditor) {
            int alignment = UIManager.getInt("Spinner.editorAlignment");
            JTextField text = ((JSpinner.DefaultEditor)editor).getTextField();
            text.setHorizontalAlignment(alignment);
        }
    }
    private void maybeRemoveEditorBorder(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
                editor.getBorder() == null &&
                editor.getComponentCount() > 0) {
                editor = (JComponent)editor.getComponent(0);
            }
            if (editor != null && editor.getBorder() instanceof UIResource) {
                editor.setBorder(null);
            }
        }
    }
    private void installEditorBorderListener(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
                editor.getBorder() == null &&
                editor.getComponentCount() > 0) {
                editor = (JComponent)editor.getComponent(0);
            }
            if (editor != null &&
                (editor.getBorder() == null ||
                 editor.getBorder() instanceof UIResource)) {
                editor.addPropertyChangeListener(getHandler());
            }
        }
    }
    private void removeEditorBorderListener(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
                editor.getComponentCount() > 0) {
                editor = (JComponent)editor.getComponent(0);
            }
            if (editor != null) {
                editor.removePropertyChangeListener(getHandler());
            }
        }
    }
    private void updateEnabledState() {
        updateEnabledState(spinner, spinner.isEnabled());
    }
    private void updateEnabledState(Container c, boolean enabled) {
        for (int counter = c.getComponentCount() - 1; counter >= 0;counter--) {
            Component child = c.getComponent(counter);
            if (DefaultLookup.getBoolean(spinner, this,
                "Spinner.disableOnBoundaryValues", false)) {
                SpinnerModel model = spinner.getModel();
                if (child.getName() == "Spinner.nextButton" &&
                    model.getNextValue() == null) {
                    child.setEnabled(false);
                }
                else if (child.getName() == "Spinner.previousButton" &&
                         model.getPreviousValue() == null) {
                    child.setEnabled(false);
                }
                else {
                    child.setEnabled(enabled);
                }
            }
            else {
                child.setEnabled(enabled);
            }
            if (child instanceof Container) {
                updateEnabledState((Container)child, enabled);
            }
        }
    }
    protected void installKeyboardActions() {
        InputMap iMap = getInputMap(JComponent.
                                   WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(spinner, JComponent.
                                         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                         iMap);
        LazyActionMap.installLazyActionMap(spinner, BasicSpinnerUI.class,
                "Spinner.actionMap");
    }
    private InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(spinner, this,
                    "Spinner.ancestorInputMap");
        }
        return null;
    }
    static void loadActionMap(LazyActionMap map) {
        map.put("increment", nextButtonHandler);
        map.put("decrement", previousButtonHandler);
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        JComponent editor = spinner.getEditor();
        Insets insets = spinner.getInsets();
        width = width - insets.left - insets.right;
        height = height - insets.top - insets.bottom;
        if (width >= 0 && height >= 0) {
            int baseline = editor.getBaseline(width, height);
            if (baseline >= 0) {
                return insets.top + baseline;
            }
        }
        return -1;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        return spinner.getEditor().getBaselineResizeBehavior();
    }
    private static class ArrowButtonHandler extends AbstractAction
                                            implements FocusListener, MouseListener, UIResource {
        final javax.swing.Timer autoRepeatTimer;
        final boolean isNext;
        JSpinner spinner = null;
        JButton arrowButton = null;
        ArrowButtonHandler(String name, boolean isNext) {
            super(name);
            this.isNext = isNext;
            autoRepeatTimer = new javax.swing.Timer(60, this);
            autoRepeatTimer.setInitialDelay(300);
        }
        private JSpinner eventToSpinner(AWTEvent e) {
            Object src = e.getSource();
            while ((src instanceof Component) && !(src instanceof JSpinner)) {
                src = ((Component)src).getParent();
            }
            return (src instanceof JSpinner) ? (JSpinner)src : null;
        }
        public void actionPerformed(ActionEvent e) {
            JSpinner spinner = this.spinner;
            if (!(e.getSource() instanceof javax.swing.Timer)) {
                spinner = eventToSpinner(e);
                if (e.getSource() instanceof JButton) {
                    arrowButton = (JButton)e.getSource();
                }
            } else {
                if (arrowButton!=null && !arrowButton.getModel().isPressed()
                    && autoRepeatTimer.isRunning()) {
                    autoRepeatTimer.stop();
                    spinner = null;
                    arrowButton = null;
                }
            }
            if (spinner != null) {
                try {
                    int calendarField = getCalendarField(spinner);
                    spinner.commitEdit();
                    if (calendarField != -1) {
                        ((SpinnerDateModel)spinner.getModel()).
                                 setCalendarField(calendarField);
                    }
                    Object value = (isNext) ? spinner.getNextValue() :
                               spinner.getPreviousValue();
                    if (value != null) {
                        spinner.setValue(value);
                        select(spinner);
                    }
                } catch (IllegalArgumentException iae) {
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                } catch (ParseException pe) {
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                }
            }
        }
        private void select(JSpinner spinner) {
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DateEditor) {
                JSpinner.DateEditor dateEditor = (JSpinner.DateEditor)editor;
                JFormattedTextField ftf = dateEditor.getTextField();
                Format format = dateEditor.getFormat();
                Object value;
                if (format != null && (value = spinner.getValue()) != null) {
                    SpinnerDateModel model = dateEditor.getModel();
                    DateFormat.Field field = DateFormat.Field.ofCalendarField(
                        model.getCalendarField());
                    if (field != null) {
                        try {
                            AttributedCharacterIterator iterator = format.
                                formatToCharacterIterator(value);
                            if (!select(ftf, iterator, field) &&
                                       field == DateFormat.Field.HOUR0) {
                                select(ftf, iterator, DateFormat.Field.HOUR1);
                            }
                        }
                        catch (IllegalArgumentException iae) {}
                    }
                }
            }
        }
        private boolean select(JFormattedTextField ftf,
                               AttributedCharacterIterator iterator,
                               DateFormat.Field field) {
            int max = ftf.getDocument().getLength();
            iterator.first();
            do {
                Map attrs = iterator.getAttributes();
                if (attrs != null && attrs.containsKey(field)){
                    int start = iterator.getRunStart(field);
                    int end = iterator.getRunLimit(field);
                    if (start != -1 && end != -1 && start <= max &&
                                       end <= max) {
                        ftf.select(start, end);
                    }
                    return true;
                }
            } while (iterator.next() != CharacterIterator.DONE);
            return false;
        }
        private int getCalendarField(JSpinner spinner) {
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DateEditor) {
                JSpinner.DateEditor dateEditor = (JSpinner.DateEditor)editor;
                JFormattedTextField ftf = dateEditor.getTextField();
                int start = ftf.getSelectionStart();
                JFormattedTextField.AbstractFormatter formatter =
                                    ftf.getFormatter();
                if (formatter instanceof InternationalFormatter) {
                    Format.Field[] fields = ((InternationalFormatter)
                                             formatter).getFields(start);
                    for (int counter = 0; counter < fields.length; counter++) {
                        if (fields[counter] instanceof DateFormat.Field) {
                            int calendarField;
                            if (fields[counter] == DateFormat.Field.HOUR1) {
                                calendarField = Calendar.HOUR;
                            }
                            else {
                                calendarField = ((DateFormat.Field)
                                        fields[counter]).getCalendarField();
                            }
                            if (calendarField != -1) {
                                return calendarField;
                            }
                        }
                    }
                }
            }
            return -1;
        }
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
                spinner = eventToSpinner(e);
                autoRepeatTimer.start();
                focusSpinnerIfNecessary();
            }
        }
        public void mouseReleased(MouseEvent e) {
            autoRepeatTimer.stop();
            arrowButton = null;
            spinner = null;
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
            if (spinner != null && !autoRepeatTimer.isRunning() && spinner == eventToSpinner(e)) {
                autoRepeatTimer.start();
            }
        }
        public void mouseExited(MouseEvent e) {
            if (autoRepeatTimer.isRunning()) {
                autoRepeatTimer.stop();
            }
        }
        private void focusSpinnerIfNecessary() {
            Component fo = KeyboardFocusManager.
                              getCurrentKeyboardFocusManager().getFocusOwner();
            if (spinner.isRequestFocusEnabled() && (
                        fo == null ||
                        !SwingUtilities.isDescendingFrom(fo, spinner))) {
                Container root = spinner;
                if (!root.isFocusCycleRoot()) {
                    root = root.getFocusCycleRootAncestor();
                }
                if (root != null) {
                    FocusTraversalPolicy ftp = root.getFocusTraversalPolicy();
                    Component child = ftp.getComponentAfter(root, spinner);
                    if (child != null && SwingUtilities.isDescendingFrom(
                                                        child, spinner)) {
                        child.requestFocus();
                    }
                }
            }
        }
        public void focusGained(FocusEvent e) {
        }
        public void focusLost(FocusEvent e) {
            if (spinner == eventToSpinner(e)) {
                if (autoRepeatTimer.isRunning()) {
                    autoRepeatTimer.stop();
                }
                spinner = null;
                if (arrowButton != null) {
                    ButtonModel model = arrowButton.getModel();
                    model.setPressed(false);
                    model.setArmed(false);
                    arrowButton = null;
                }
            }
        }
    }
    private static class Handler implements LayoutManager,
            PropertyChangeListener, ChangeListener {
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
            return (c == null) ? zeroSize : c.getPreferredSize();
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
            int width  = parent.getWidth();
            int height = parent.getHeight();
            Insets insets = parent.getInsets();
            if (nextButton == null && previousButton == null) {
                setBounds(editor, insets.left,  insets.top, width - insets.left - insets.right,
                        height - insets.top - insets.bottom);
                return;
            }
            Dimension nextD = preferredSize(nextButton);
            Dimension previousD = preferredSize(previousButton);
            int buttonsWidth = Math.max(nextD.width, previousD.width);
            int editorHeight = height - (insets.top + insets.bottom);
            Insets buttonInsets = UIManager.getInsets("Spinner.arrowButtonInsets");
            if (buttonInsets == null) {
                buttonInsets = insets;
            }
            int editorX, editorWidth, buttonsX;
            if (parent.getComponentOrientation().isLeftToRight()) {
                editorX = insets.left;
                editorWidth = width - insets.left - buttonsWidth - buttonInsets.right;
                buttonsX = width - buttonsWidth - buttonInsets.right;
            } else {
                buttonsX = buttonInsets.left;
                editorX = buttonsX + buttonsWidth;
                editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
            }
            int nextY = buttonInsets.top;
            int nextHeight = (height / 2) + (height % 2) - nextY;
            int previousY = buttonInsets.top + nextHeight;
            int previousHeight = height - previousY - buttonInsets.bottom;
            setBounds(editor,         editorX,  insets.top, editorWidth, editorHeight);
            setBounds(nextButton,     buttonsX, nextY,      buttonsWidth, nextHeight);
            setBounds(previousButton, buttonsX, previousY,  buttonsWidth, previousHeight);
        }
        public void propertyChange(PropertyChangeEvent e)
        {
            String propertyName = e.getPropertyName();
            if (e.getSource() instanceof JSpinner) {
                JSpinner spinner = (JSpinner)(e.getSource());
                SpinnerUI spinnerUI = spinner.getUI();
                if (spinnerUI instanceof BasicSpinnerUI) {
                    BasicSpinnerUI ui = (BasicSpinnerUI)spinnerUI;
                    if ("editor".equals(propertyName)) {
                        JComponent oldEditor = (JComponent)e.getOldValue();
                        JComponent newEditor = (JComponent)e.getNewValue();
                        ui.replaceEditor(oldEditor, newEditor);
                        ui.updateEnabledState();
                        if (oldEditor instanceof JSpinner.DefaultEditor) {
                            JTextField tf =
                                ((JSpinner.DefaultEditor)oldEditor).getTextField();
                            if (tf != null) {
                                tf.removeFocusListener(nextButtonHandler);
                                tf.removeFocusListener(previousButtonHandler);
                            }
                        }
                        if (newEditor instanceof JSpinner.DefaultEditor) {
                            JTextField tf =
                                ((JSpinner.DefaultEditor)newEditor).getTextField();
                            if (tf != null) {
                                if (tf.getFont() instanceof UIResource) {
                                    tf.setFont(spinner.getFont());
                                }
                                tf.addFocusListener(nextButtonHandler);
                                tf.addFocusListener(previousButtonHandler);
                            }
                        }
                    }
                    else if ("enabled".equals(propertyName) ||
                             "model".equals(propertyName)) {
                        ui.updateEnabledState();
                    }
                else if ("font".equals(propertyName)) {
                    JComponent editor = spinner.getEditor();
                    if (editor!=null && editor instanceof JSpinner.DefaultEditor) {
                        JTextField tf =
                            ((JSpinner.DefaultEditor)editor).getTextField();
                        if (tf != null) {
                            if (tf.getFont() instanceof UIResource) {
                                tf.setFont(spinner.getFont());
                            }
                        }
                    }
                }
                else if (JComponent.TOOL_TIP_TEXT_KEY.equals(propertyName)) {
                    updateToolTipTextForChildren(spinner);
                }
                }
            } else if (e.getSource() instanceof JComponent) {
                JComponent c = (JComponent)e.getSource();
                if ((c.getParent() instanceof JPanel) &&
                    (c.getParent().getParent() instanceof JSpinner) &&
                    "border".equals(propertyName)) {
                    JSpinner spinner = (JSpinner)c.getParent().getParent();
                    SpinnerUI spinnerUI = spinner.getUI();
                    if (spinnerUI instanceof BasicSpinnerUI) {
                        BasicSpinnerUI ui = (BasicSpinnerUI)spinnerUI;
                        ui.maybeRemoveEditorBorder(c);
                    }
                }
            }
        }
        private void updateToolTipTextForChildren(JComponent spinner) {
            String toolTipText = spinner.getToolTipText();
            Component[] children = spinner.getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof JSpinner.DefaultEditor) {
                    JTextField tf = ((JSpinner.DefaultEditor)children[i]).getTextField();
                    if (tf != null) {
                        tf.setToolTipText(toolTipText);
                    }
                } else if (children[i] instanceof JComponent) {
                    ((JComponent)children[i]).setToolTipText( spinner.getToolTipText() );
                }
            }
        }
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof JSpinner) {
                JSpinner spinner = (JSpinner)e.getSource();
                SpinnerUI spinnerUI = spinner.getUI();
                if (DefaultLookup.getBoolean(spinner, spinnerUI,
                    "Spinner.disableOnBoundaryValues", false) &&
                    spinnerUI instanceof BasicSpinnerUI) {
                    BasicSpinnerUI ui = (BasicSpinnerUI)spinnerUI;
                    ui.updateEnabledState();
                }
            }
        }
    }
}
