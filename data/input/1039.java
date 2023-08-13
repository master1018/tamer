public class BasicComboBoxEditor implements ComboBoxEditor,FocusListener {
    protected JTextField editor;
    private Object oldValue;
    public BasicComboBoxEditor() {
        editor = createEditorComponent();
    }
    public Component getEditorComponent() {
        return editor;
    }
    protected JTextField createEditorComponent() {
        JTextField editor = new BorderlessTextField("",9);
        editor.setBorder(null);
        return editor;
    }
    public void setItem(Object anObject) {
        String text;
        if ( anObject != null )  {
            text = anObject.toString();
            oldValue = anObject;
        } else {
            text = "";
        }
        if (! text.equals(editor.getText())) {
            editor.setText(text);
        }
    }
    public Object getItem() {
        Object newValue = editor.getText();
        if (oldValue != null && !(oldValue instanceof String))  {
            if (newValue.equals(oldValue.toString()))  {
                return oldValue;
            } else {
                Class<?> cls = oldValue.getClass();
                try {
                    Method method = cls.getMethod("valueOf", new Class[]{String.class});
                    newValue = method.invoke(oldValue, new Object[] { editor.getText()});
                } catch (Exception ex) {
                }
            }
        }
        return newValue;
    }
    public void selectAll() {
        editor.selectAll();
        editor.requestFocus();
    }
    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {}
    public void addActionListener(ActionListener l) {
        editor.addActionListener(l);
    }
    public void removeActionListener(ActionListener l) {
        editor.removeActionListener(l);
    }
    static class BorderlessTextField extends JTextField {
        public BorderlessTextField(String value,int n) {
            super(value,n);
        }
        public void setText(String s) {
            if (getText().equals(s)) {
                return;
            }
            super.setText(s);
        }
        public void setBorder(Border b) {
            if (!(b instanceof UIResource)) {
                super.setBorder(b);
            }
        }
    }
    public static class UIResource extends BasicComboBoxEditor
    implements javax.swing.plaf.UIResource {
    }
}
