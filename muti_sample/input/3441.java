public class JSpinner extends JComponent implements Accessible
{
    private static final String uiClassID = "SpinnerUI";
    private static final Action DISABLED_ACTION = new DisabledAction();
    private SpinnerModel model;
    private JComponent editor;
    private ChangeListener modelListener;
    private transient ChangeEvent changeEvent;
    private boolean editorExplicitlySet = false;
    public JSpinner(SpinnerModel model) {
        if (model == null) {
            throw new NullPointerException("model cannot be null");
        }
        this.model = model;
        this.editor = createEditor(model);
        setUIProperty("opaque",true);
        updateUI();
    }
    public JSpinner() {
        this(new SpinnerNumberModel());
    }
    public SpinnerUI getUI() {
        return (SpinnerUI)ui;
    }
    public void setUI(SpinnerUI ui) {
        super.setUI(ui);
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public void updateUI() {
        setUI((SpinnerUI)UIManager.getUI(this));
        invalidate();
    }
    protected JComponent createEditor(SpinnerModel model) {
        if (model instanceof SpinnerDateModel) {
            return new DateEditor(this);
        }
        else if (model instanceof SpinnerListModel) {
            return new ListEditor(this);
        }
        else if (model instanceof SpinnerNumberModel) {
            return new NumberEditor(this);
        }
        else {
            return new DefaultEditor(this);
        }
    }
    public void setModel(SpinnerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (!model.equals(this.model)) {
            SpinnerModel oldModel = this.model;
            this.model = model;
            if (modelListener != null) {
                oldModel.removeChangeListener(modelListener);
                this.model.addChangeListener(modelListener);
            }
            firePropertyChange("model", oldModel, model);
            if (!editorExplicitlySet) {
                setEditor(createEditor(model)); 
                editorExplicitlySet = false;
            }
            repaint();
            revalidate();
        }
    }
    public SpinnerModel getModel() {
        return model;
    }
    public Object getValue() {
        return getModel().getValue();
    }
    public void setValue(Object value) {
        getModel().setValue(value);
    }
    public Object getNextValue() {
        return getModel().getNextValue();
    }
    private class ModelListener implements ChangeListener, Serializable {
        public void stateChanged(ChangeEvent e) {
            fireStateChanged();
        }
    }
    public void addChangeListener(ChangeListener listener) {
        if (modelListener == null) {
            modelListener = new ModelListener();
            getModel().addChangeListener(modelListener);
        }
        listenerList.add(ChangeListener.class, listener);
    }
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    public ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }
    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    public Object getPreviousValue() {
        return getModel().getPreviousValue();
    }
    public void setEditor(JComponent editor) {
        if (editor == null) {
            throw new IllegalArgumentException("null editor");
        }
        if (!editor.equals(this.editor)) {
            JComponent oldEditor = this.editor;
            this.editor = editor;
            if (oldEditor instanceof DefaultEditor) {
                ((DefaultEditor)oldEditor).dismiss(this);
            }
            editorExplicitlySet = true;
            firePropertyChange("editor", oldEditor, editor);
            revalidate();
            repaint();
        }
    }
    public JComponent getEditor() {
        return editor;
    }
    public void commitEdit() throws ParseException {
        JComponent editor = getEditor();
        if (editor instanceof DefaultEditor) {
            ((DefaultEditor)editor).commitEdit();
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    public static class DefaultEditor extends JPanel
        implements ChangeListener, PropertyChangeListener, LayoutManager
    {
        public DefaultEditor(JSpinner spinner) {
            super(null);
            JFormattedTextField ftf = new JFormattedTextField();
            ftf.setName("Spinner.formattedTextField");
            ftf.setValue(spinner.getValue());
            ftf.addPropertyChangeListener(this);
            ftf.setEditable(false);
            ftf.setInheritsPopupMenu(true);
            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                ftf.setToolTipText(toolTipText);
            }
            add(ftf);
            setLayout(this);
            spinner.addChangeListener(this);
            ActionMap ftfMap = ftf.getActionMap();
            if (ftfMap != null) {
                ftfMap.put("increment", DISABLED_ACTION);
                ftfMap.put("decrement", DISABLED_ACTION);
            }
        }
        public void dismiss(JSpinner spinner) {
            spinner.removeChangeListener(this);
        }
        public JSpinner getSpinner() {
            for (Component c = this; c != null; c = c.getParent()) {
                if (c instanceof JSpinner) {
                    return (JSpinner)c;
                }
            }
            return null;
        }
        public JFormattedTextField getTextField() {
            return (JFormattedTextField)getComponent(0);
        }
        public void stateChanged(ChangeEvent e) {
            JSpinner spinner = (JSpinner)(e.getSource());
            getTextField().setValue(spinner.getValue());
        }
        public void propertyChange(PropertyChangeEvent e)
        {
            JSpinner spinner = getSpinner();
            if (spinner == null) {
                return;
            }
            Object source = e.getSource();
            String name = e.getPropertyName();
            if ((source instanceof JFormattedTextField) && "value".equals(name)) {
                Object lastValue = spinner.getValue();
                try {
                    spinner.setValue(getTextField().getValue());
                } catch (IllegalArgumentException iae) {
                    try {
                        ((JFormattedTextField)source).setValue(lastValue);
                    } catch (IllegalArgumentException iae2) {
                    }
                }
            }
        }
        public void addLayoutComponent(String name, Component child) {
        }
        public void removeLayoutComponent(Component child) {
        }
        private Dimension insetSize(Container parent) {
            Insets insets = parent.getInsets();
            int w = insets.left + insets.right;
            int h = insets.top + insets.bottom;
            return new Dimension(w, h);
        }
        public Dimension preferredLayoutSize(Container parent) {
            Dimension preferredSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getPreferredSize();
                preferredSize.width += childSize.width;
                preferredSize.height += childSize.height;
            }
            return preferredSize;
        }
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minimumSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getMinimumSize();
                minimumSize.width += childSize.width;
                minimumSize.height += childSize.height;
            }
            return minimumSize;
        }
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() > 0) {
                Insets insets = parent.getInsets();
                int w = parent.getWidth() - (insets.left + insets.right);
                int h = parent.getHeight() - (insets.top + insets.bottom);
                getComponent(0).setBounds(insets.left, insets.top, w, h);
            }
        }
        public void commitEdit()  throws ParseException {
            JFormattedTextField ftf = getTextField();
            ftf.commitEdit();
        }
        public int getBaseline(int width, int height) {
            super.getBaseline(width, height);
            Insets insets = getInsets();
            width = width - insets.left - insets.right;
            height = height - insets.top - insets.bottom;
            int baseline = getComponent(0).getBaseline(width, height);
            if (baseline >= 0) {
                return baseline + insets.top;
            }
            return -1;
        }
        public BaselineResizeBehavior getBaselineResizeBehavior() {
            return getComponent(0).getBaselineResizeBehavior();
        }
    }
    private static class DateEditorFormatter extends DateFormatter {
        private final SpinnerDateModel model;
        DateEditorFormatter(SpinnerDateModel model, DateFormat format) {
            super(format);
            this.model = model;
        }
        public void setMinimum(Comparable min) {
            model.setStart(min);
        }
        public Comparable getMinimum() {
            return  model.getStart();
        }
        public void setMaximum(Comparable max) {
            model.setEnd(max);
        }
        public Comparable getMaximum() {
            return model.getEnd();
        }
    }
    public static class DateEditor extends DefaultEditor
    {
        private static String getDefaultPattern(Locale loc) {
            ResourceBundle r = LocaleData.getDateFormatData(loc);
            String[] dateTimePatterns = r.getStringArray("DateTimePatterns");
            Object[] dateTimeArgs = {dateTimePatterns[DateFormat.SHORT],
                                     dateTimePatterns[DateFormat.SHORT + 4]};
            return MessageFormat.format(dateTimePatterns[8], dateTimeArgs);
        }
        public DateEditor(JSpinner spinner) {
            this(spinner, getDefaultPattern(spinner.getLocale()));
        }
        public DateEditor(JSpinner spinner, String dateFormatPattern) {
            this(spinner, new SimpleDateFormat(dateFormatPattern,
                                               spinner.getLocale()));
        }
        private DateEditor(JSpinner spinner, DateFormat format) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerDateModel)) {
                throw new IllegalArgumentException(
                                 "model not a SpinnerDateModel");
            }
            SpinnerDateModel model = (SpinnerDateModel)spinner.getModel();
            DateFormatter formatter = new DateEditorFormatter(model, format);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(
                                                  formatter);
            JFormattedTextField ftf = getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            try {
                String maxString = formatter.valueToString(model.getStart());
                String minString = formatter.valueToString(model.getEnd());
                ftf.setColumns(Math.max(maxString.length(),
                                        minString.length()));
            }
            catch (ParseException e) {
            }
        }
        public SimpleDateFormat getFormat() {
            return (SimpleDateFormat)((DateFormatter)(getTextField().getFormatter())).getFormat();
        }
        public SpinnerDateModel getModel() {
            return (SpinnerDateModel)(getSpinner().getModel());
        }
    }
    private static class NumberEditorFormatter extends NumberFormatter {
        private final SpinnerNumberModel model;
        NumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
            super(format);
            this.model = model;
            setValueClass(model.getValue().getClass());
        }
        public void setMinimum(Comparable min) {
            model.setMinimum(min);
        }
        public Comparable getMinimum() {
            return  model.getMinimum();
        }
        public void setMaximum(Comparable max) {
            model.setMaximum(max);
        }
        public Comparable getMaximum() {
            return model.getMaximum();
        }
    }
    public static class NumberEditor extends DefaultEditor
    {
        private static String getDefaultPattern(Locale locale) {
            ResourceBundle rb = LocaleData.getNumberFormatData(locale);
            String[] all = rb.getStringArray("NumberPatterns");
            return all[0];
        }
        public NumberEditor(JSpinner spinner) {
            this(spinner, getDefaultPattern(spinner.getLocale()));
        }
        public NumberEditor(JSpinner spinner, String decimalFormatPattern) {
            this(spinner, new DecimalFormat(decimalFormatPattern));
        }
        private NumberEditor(JSpinner spinner, DecimalFormat format) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerNumberModel)) {
                throw new IllegalArgumentException(
                          "model not a SpinnerNumberModel");
            }
            SpinnerNumberModel model = (SpinnerNumberModel)spinner.getModel();
            NumberFormatter formatter = new NumberEditorFormatter(model,
                                                                  format);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(
                                                  formatter);
            JFormattedTextField ftf = getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            ftf.setHorizontalAlignment(JTextField.RIGHT);
            try {
                String maxString = formatter.valueToString(model.getMinimum());
                String minString = formatter.valueToString(model.getMaximum());
                ftf.setColumns(Math.max(maxString.length(),
                                        minString.length()));
            }
            catch (ParseException e) {
            }
        }
        public DecimalFormat getFormat() {
            return (DecimalFormat)((NumberFormatter)(getTextField().getFormatter())).getFormat();
        }
        public SpinnerNumberModel getModel() {
            return (SpinnerNumberModel)(getSpinner().getModel());
        }
    }
    public static class ListEditor extends DefaultEditor
    {
        public ListEditor(JSpinner spinner) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerListModel)) {
                throw new IllegalArgumentException("model not a SpinnerListModel");
            }
            getTextField().setEditable(true);
            getTextField().setFormatterFactory(new
                              DefaultFormatterFactory(new ListFormatter()));
        }
        public SpinnerListModel getModel() {
            return (SpinnerListModel)(getSpinner().getModel());
        }
        private class ListFormatter extends
                          JFormattedTextField.AbstractFormatter {
            private DocumentFilter filter;
            public String valueToString(Object value) throws ParseException {
                if (value == null) {
                    return "";
                }
                return value.toString();
            }
            public Object stringToValue(String string) throws ParseException {
                return string;
            }
            protected DocumentFilter getDocumentFilter() {
                if (filter == null) {
                    filter = new Filter();
                }
                return filter;
            }
            private class Filter extends DocumentFilter {
                public void replace(FilterBypass fb, int offset, int length,
                                    String string, AttributeSet attrs) throws
                                           BadLocationException {
                    if (string != null && (offset + length) ==
                                          fb.getDocument().getLength()) {
                        Object next = getModel().findNextMatch(
                                         fb.getDocument().getText(0, offset) +
                                         string);
                        String value = (next != null) ? next.toString() : null;
                        if (value != null) {
                            fb.remove(0, offset + length);
                            fb.insertString(0, value, null);
                            getFormattedTextField().select(offset +
                                                           string.length(),
                                                           value.length());
                            return;
                        }
                    }
                    super.replace(fb, offset, length, string, attrs);
                }
                public void insertString(FilterBypass fb, int offset,
                                     String string, AttributeSet attr)
                       throws BadLocationException {
                    replace(fb, offset, 0, string, attr);
                }
            }
        }
    }
    private static class DisabledAction implements Action {
        public Object getValue(String key) {
            return null;
        }
        public void putValue(String key, Object value) {
        }
        public void setEnabled(boolean b) {
        }
        public boolean isEnabled() {
            return false;
        }
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        public void actionPerformed(ActionEvent ae) {
        }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJSpinner();
        }
        return accessibleContext;
    }
    protected class AccessibleJSpinner extends AccessibleJComponent
        implements AccessibleValue, AccessibleAction, AccessibleText,
                   AccessibleEditableText, ChangeListener {
        private Object oldModelValue = null;
        protected AccessibleJSpinner() {
            oldModelValue = model.getValue();
            JSpinner.this.addChangeListener(this);
        }
        public void stateChanged(ChangeEvent e) {
            if (e == null) {
                throw new NullPointerException();
            }
            Object newModelValue = model.getValue();
            firePropertyChange(ACCESSIBLE_VALUE_PROPERTY,
                               oldModelValue,
                               newModelValue);
            firePropertyChange(ACCESSIBLE_TEXT_PROPERTY,
                               null,
                               0); 
            oldModelValue = newModelValue;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPIN_BOX;
        }
        public int getAccessibleChildrenCount() {
            if (editor.getAccessibleContext() != null) {
                return 1;
            }
            return 0;
        }
        public Accessible getAccessibleChild(int i) {
            if (i != 0) {
                return null;
            }
            if (editor.getAccessibleContext() != null) {
                return (Accessible)editor;
            }
            return null;
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public AccessibleText getAccessibleText() {
            return this;
        }
        private AccessibleContext getEditorAccessibleContext() {
            if (editor instanceof DefaultEditor) {
                JTextField textField = ((DefaultEditor)editor).getTextField();
                if (textField != null) {
                    return textField.getAccessibleContext();
                }
            } else if (editor instanceof Accessible) {
                return editor.getAccessibleContext();
            }
            return null;
        }
        private AccessibleText getEditorAccessibleText() {
            AccessibleContext ac = getEditorAccessibleContext();
            if (ac != null) {
                return ac.getAccessibleText();
            }
            return null;
        }
        private AccessibleEditableText getEditorAccessibleEditableText() {
            AccessibleText at = getEditorAccessibleText();
            if (at instanceof AccessibleEditableText) {
                return (AccessibleEditableText)at;
            }
            return null;
        }
        public AccessibleValue getAccessibleValue() {
            return this;
        }
        public Number getCurrentAccessibleValue() {
            Object o = model.getValue();
            if (o instanceof Number) {
                return (Number)o;
            }
            return null;
        }
        public boolean setCurrentAccessibleValue(Number n) {
            try {
                model.setValue(n);
                return true;
            } catch (IllegalArgumentException iae) {
            }
            return false;
        }
        public Number getMinimumAccessibleValue() {
            if (model instanceof SpinnerNumberModel) {
                SpinnerNumberModel numberModel = (SpinnerNumberModel)model;
                Object o = numberModel.getMinimum();
                if (o instanceof Number) {
                    return (Number)o;
                }
            }
            return null;
        }
        public Number getMaximumAccessibleValue() {
            if (model instanceof SpinnerNumberModel) {
                SpinnerNumberModel numberModel = (SpinnerNumberModel)model;
                Object o = numberModel.getMaximum();
                if (o instanceof Number) {
                    return (Number)o;
                }
            }
            return null;
        }
        public int getAccessibleActionCount() {
            return 2;
        }
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return AccessibleAction.INCREMENT;
            } else if (i == 1) {
                return AccessibleAction.DECREMENT;
            }
            return null;
        }
        public boolean doAccessibleAction(int i) {
            if (i < 0 || i > 1) {
                return false;
            }
            Object o;
            if (i == 0) {
                o = getNextValue(); 
            } else {
                o = getPreviousValue(); 
            }
            try {
                model.setValue(o);
                return true;
            } catch (IllegalArgumentException iae) {
            }
            return false;
        }
        private boolean sameWindowAncestor(Component src, Component dest) {
            if (src == null || dest == null) {
                return false;
            }
            return SwingUtilities.getWindowAncestor(src) ==
                SwingUtilities.getWindowAncestor(dest);
        }
        public int getIndexAtPoint(Point p) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null && sameWindowAncestor(JSpinner.this, editor)) {
                Point editorPoint = SwingUtilities.convertPoint(JSpinner.this,
                                                                p,
                                                                editor);
                if (editorPoint != null) {
                    return at.getIndexAtPoint(editorPoint);
                }
            }
            return -1;
        }
        public Rectangle getCharacterBounds(int i) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null ) {
                Rectangle editorRect = at.getCharacterBounds(i);
                if (editorRect != null &&
                    sameWindowAncestor(JSpinner.this, editor)) {
                    return SwingUtilities.convertRectangle(editor,
                                                           editorRect,
                                                           JSpinner.this);
                }
            }
            return null;
        }
        public int getCharCount() {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getCharCount();
            }
            return -1;
        }
        public int getCaretPosition() {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getCaretPosition();
            }
            return -1;
        }
        public String getAtIndex(int part, int index) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getAtIndex(part, index);
            }
            return null;
        }
        public String getAfterIndex(int part, int index) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getAfterIndex(part, index);
            }
            return null;
        }
        public String getBeforeIndex(int part, int index) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getBeforeIndex(part, index);
            }
            return null;
        }
        public AttributeSet getCharacterAttribute(int i) {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getCharacterAttribute(i);
            }
            return null;
        }
        public int getSelectionStart() {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getSelectionStart();
            }
            return -1;
        }
        public int getSelectionEnd() {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getSelectionEnd();
            }
            return -1;
        }
        public String getSelectedText() {
            AccessibleText at = getEditorAccessibleText();
            if (at != null) {
                return at.getSelectedText();
            }
            return null;
        }
        public void setTextContents(String s) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.setTextContents(s);
            }
        }
        public void insertTextAtIndex(int index, String s) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.insertTextAtIndex(index, s);
            }
        }
        public String getTextRange(int startIndex, int endIndex) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                return at.getTextRange(startIndex, endIndex);
            }
            return null;
        }
        public void delete(int startIndex, int endIndex) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.delete(startIndex, endIndex);
            }
        }
        public void cut(int startIndex, int endIndex) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.cut(startIndex, endIndex);
            }
        }
        public void paste(int startIndex) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.paste(startIndex);
            }
        }
        public void replaceText(int startIndex, int endIndex, String s) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.replaceText(startIndex, endIndex, s);
            }
        }
        public void selectText(int startIndex, int endIndex) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.selectText(startIndex, endIndex);
            }
        }
        public void setAttributes(int startIndex, int endIndex, AttributeSet as) {
            AccessibleEditableText at = getEditorAccessibleEditableText();
            if (at != null) {
                at.setAttributes(startIndex, endIndex, as);
            }
        }
    }  
}
