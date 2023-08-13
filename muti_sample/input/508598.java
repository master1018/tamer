public abstract class EditableDialogCellEditor extends DialogCellEditor {
    private Text text; 
    private ModifyListener modifyListener;
    private boolean isSelection = false;
    private boolean isDeleteable = false;
    private boolean isSelectable = false;
    EditableDialogCellEditor(Composite parent) {
        super(parent);
    }
    @Override
    protected Button createButton(Composite parent) {
        Button result = new Button(parent, SWT.DOWN | SWT.FLAT);
        result.setText("..."); 
        return result;
    }
    @Override
    protected Control createContents(Composite cell) {
        text = new Text(cell, SWT.SINGLE);
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelection(e);
            }
        });
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
                if ((getControl() == null) || getControl().isDisposed()) {
                    return;
                }
                checkSelection(); 
                checkDeleteable();
                checkSelectable();
            }
        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                checkSelection();
                checkDeleteable();
                checkSelectable();
            }
        });
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                EditableDialogCellEditor.this.focusLost();
            }
        });
        text.setFont(cell.getFont());
        text.setBackground(cell.getBackground());
        text.setText("");
        text.addModifyListener(getModifyListener());
        return text;
    }
    private void checkDeleteable() {
        boolean oldIsDeleteable = isDeleteable;
        isDeleteable = isDeleteEnabled();
        if (oldIsDeleteable != isDeleteable) {
            fireEnablementChanged(DELETE);
        }
    }
    private void checkSelectable() {
        boolean oldIsSelectable = isSelectable;
        isSelectable = isSelectAllEnabled();
        if (oldIsSelectable != isSelectable) {
            fireEnablementChanged(SELECT_ALL);
        }
    }
    private void checkSelection() {
        boolean oldIsSelection = isSelection;
        isSelection = text.getSelectionCount() > 0;
        if (oldIsSelection != isSelection) {
            fireEnablementChanged(COPY);
            fireEnablementChanged(CUT);
        }
    }
    @Override
    protected void doSetFocus() {
        if (text != null) {
            text.selectAll();
            text.setFocus();
            checkSelection();
            checkDeleteable();
            checkSelectable();
        }
    }
    @Override
    protected void updateContents(Object value) {
        Assert.isTrue(text != null && (value == null || (value instanceof String)));
        if (value != null) {
            text.removeModifyListener(getModifyListener());
            text.setText((String) value);
            text.addModifyListener(getModifyListener());
        }
    }
    @Override
    protected Object doGetValue() {
        return text.getText();
    }
    protected void editOccured(ModifyEvent e) {
        String value = text.getText();
        if (value == null) {
            value = "";
        }
        Object typedValue = value;
        boolean oldValidState = isValueValid();
        boolean newValidState = isCorrect(typedValue);
        if (!newValidState) {
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[] { value }));
        }
        valueChanged(oldValidState, newValidState);
    }
    private ModifyListener getModifyListener() {
        if (modifyListener == null) {
            modifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    editOccured(e);
                }
            };
        }
        return modifyListener;
    }
    protected void handleDefaultSelection(SelectionEvent event) {
        fireApplyEditorValue();
        deactivate();
    }
    @Override
    public boolean isCopyEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0;
    }
    @Override
    public boolean isCutEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0;
    }
    @Override
    public boolean isDeleteEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0
                || text.getCaretPosition() < text.getCharCount();
    }
    @Override
    public boolean isPasteEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return true;
    }
    public boolean isSaveAllEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return true;
    }
    @Override
    public boolean isSelectAllEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getCharCount() > 0;
    }
    @Override
    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\r') { 
            if (text != null && !text.isDisposed()
                    && (text.getStyle() & SWT.MULTI) != 0) {
                if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                    super.keyReleaseOccured(keyEvent);
                }
            }
            return;
        }
        super.keyReleaseOccured(keyEvent);
    }
    @Override
    public void performCopy() {
        text.copy();
    }
    @Override
    public void performCut() {
        text.cut();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }
    @Override
    public void performDelete() {
        if (text.getSelectionCount() > 0) {
            text.insert(""); 
        } else {
            int pos = text.getCaretPosition();
            if (pos < text.getCharCount()) {
                text.setSelection(pos, pos + 1);
                text.insert(""); 
            }
        }
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }
    @Override
    public void performPaste() {
        text.paste();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }
    @Override
    public void performSelectAll() {
        text.selectAll();
        checkSelection();
        checkDeleteable();
    }
}
