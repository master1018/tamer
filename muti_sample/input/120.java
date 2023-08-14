class IsindexView extends ComponentView implements ActionListener {
    JTextField textField;
    public IsindexView(Element elem) {
        super(elem);
    }
    public Component createComponent() {
        AttributeSet attr = getElement().getAttributes();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(null);
        String prompt = (String)attr.getAttribute(HTML.Attribute.PROMPT);
        if (prompt == null) {
            prompt = UIManager.getString("IsindexView.prompt");
        }
        JLabel label = new JLabel(prompt);
        textField = new JTextField();
        textField.addActionListener(this);
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.setAlignmentY(1.0f);
        panel.setOpaque(false);
        return panel;
    }
    public void actionPerformed(ActionEvent evt) {
        String data = textField.getText();
        if (data != null) {
            data = URLEncoder.encode(data);
        }
        AttributeSet attr = getElement().getAttributes();
        HTMLDocument hdoc = (HTMLDocument)getElement().getDocument();
        String action = (String) attr.getAttribute(HTML.Attribute.ACTION);
        if (action == null) {
            action = hdoc.getBase().toString();
        }
        try {
            URL url = new URL(action+"?"+data);
            JEditorPane pane = (JEditorPane)getContainer();
            pane.setPage(url);
        } catch (MalformedURLException e1) {
        } catch (IOException e2) {
        }
    }
}
