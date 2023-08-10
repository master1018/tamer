public class UDOEditor extends JComponent {
    JTextField outTypes = new JTextField();
    JTextField inTypes = new JTextField();
    BlueEditorPane codeBody = new BlueEditorPane();
    JTextArea comments = new JTextArea();
    private boolean isUpdating = false;
    UserDefinedOpcode udo = null;
    UndoManager undo = new NoStyleChangeUndoManager();
    public UDOEditor() {
        DocumentListener dl = new SimpleDocumentListener() {
            public void documentChanged(DocumentEvent e) {
                updateValue(e.getDocument());
            }
        };
        outTypes.getDocument().addDocumentListener(dl);
        inTypes.getDocument().addDocumentListener(dl);
        codeBody.getDocument().addDocumentListener(dl);
        comments.getDocument().addDocumentListener(dl);
        JButton testOpcodeButton = new JButton("Test Opcode");
        testOpcodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testOpcode();
            }
        });
        LabelledItemPanel itemPanel = new LabelledItemPanel();
        itemPanel.addItem("Out Types:", outTypes);
        itemPanel.addItem("In Types:", inTypes);
        itemPanel.addItem("", testOpcodeButton);
        final JTabbedPane tabs = new JTabbedPane();
        tabs.add("Code", codeBody);
        tabs.add("Comments", new JScrollPane(comments));
        comments.setWrapStyleWord(true);
        comments.setLineWrap(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        this.setLayout(new BorderLayout());
        this.add(itemPanel, BorderLayout.NORTH);
        this.add(tabs, BorderLayout.CENTER);
        editUserDefinedOpcode(null);
        UndoableEditListener ul = new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                UndoableEdit event = e.getEdit();
                if (event.getPresentationName().equals("style change")) {
                    undo.addEdit(event);
                } else {
                    TabSelectionWrapper wrapper = new TabSelectionWrapper(event, tabs);
                    undo.addEdit(wrapper);
                }
            }
        };
        outTypes.getDocument().addUndoableEditListener(ul);
        inTypes.getDocument().addUndoableEditListener(ul);
        codeBody.getDocument().addUndoableEditListener(ul);
        comments.getDocument().addUndoableEditListener(ul);
        AbstractAction undoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undo.canUndo()) {
                    undo.undo();
                }
            }
        };
        AbstractAction redoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undo.canRedo()) {
                    undo.redo();
                }
            }
        };
        outTypes.getDocument().addDocumentListener(dl);
        inTypes.getDocument().addDocumentListener(dl);
        codeBody.getDocument().addDocumentListener(dl);
        comments.getDocument().addDocumentListener(dl);
        setUndoActions(outTypes, undoAction, redoAction);
        setUndoActions(inTypes, undoAction, redoAction);
        setUndoActions(codeBody, undoAction, redoAction);
        setUndoActions(comments, undoAction, redoAction);
        undo.setLimit(1000);
    }
    private void setUndoActions(JComponent field, Action undoAction, Action redoAction) {
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, BlueSystem.getMenuShortcutKey());
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, BlueSystem.getMenuShortcutKey() | KeyEvent.SHIFT_DOWN_MASK);
        field.getInputMap().put(undoKeyStroke, "undo");
        field.getInputMap().put(redoKeyStroke, "redo");
        field.getActionMap().put("undo", undoAction);
        field.getActionMap().put("redo", redoAction);
    }
    protected void testOpcode() {
        if (udo != null) {
            InfoDialog.showInformationDialog(SwingUtilities.getRoot(this), udo.generateCode(), "User-Defined Opcode");
        }
    }
    protected void updateValue(Document document) {
        if (udo == null || isUpdating) {
            return;
        }
        if (document == outTypes.getDocument()) {
            udo.outTypes = outTypes.getText();
        } else if (document == inTypes.getDocument()) {
            udo.inTypes = inTypes.getText();
        } else if (document == codeBody.getDocument()) {
            udo.codeBody = codeBody.getText();
        } else if (document == comments.getDocument()) {
            udo.comments = comments.getText();
        }
    }
    public void editUserDefinedOpcode(UserDefinedOpcode udo) {
        isUpdating = true;
        this.udo = udo;
        setFields(udo);
        isUpdating = false;
        undo.discardAllEdits();
    }
    private void setFields(UserDefinedOpcode udo) {
        if (udo == null) {
            outTypes.setText("");
            inTypes.setText("");
            codeBody.setText("");
            comments.setText("");
            outTypes.setEnabled(false);
            inTypes.setEnabled(false);
            codeBody.setEnabled(false);
            comments.setEnabled(false);
        } else {
            outTypes.setText(udo.outTypes);
            inTypes.setText(udo.inTypes);
            codeBody.setText(udo.codeBody);
            comments.setText(udo.comments);
            outTypes.setEnabled(true);
            inTypes.setEnabled(true);
            codeBody.setEnabled(true);
            comments.setEnabled(true);
        }
    }
    public static void main(String[] args) {
        GUI.setBlueLookAndFeel();
        UDOEditor editor = new UDOEditor();
        editor.editUserDefinedOpcode(new UserDefinedOpcode());
        GUI.showComponentAsStandalone(editor, "UDO Editor", true);
    }
}
