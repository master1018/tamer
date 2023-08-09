public class EditFilterDialog extends javax.swing.JDialog {
    private CustomFilter customFilter;
    private boolean accepted;
    public EditFilterDialog(CustomFilter customFilter) {
        super(WindowManager.getDefault().getMainWindow(), true);
        this.customFilter = customFilter;
        initComponents();
        sourceTextArea.setText(customFilter.getCode());
        nameTextField.setText(customFilter.getName());
    }
    public boolean wasAccepted() {
        return accepted;
    }
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceTextArea = new javax.swing.JTextArea();
        nameTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(EditFilterDialog.class, "title")); 
        setResizable(false);
        sourceTextArea.setColumns(20);
        sourceTextArea.setRows(5);
        jScrollPane1.setViewportView(sourceTextArea);
        nameTextField.setText("null");
        nameLabel.setText(org.openide.util.NbBundle.getMessage(EditFilterDialog.class, "jLabel1.text")); 
        sourceLabel.setText(org.openide.util.NbBundle.getMessage(EditFilterDialog.class, "jLabel2.text")); 
        okButton.setText(org.openide.util.NbBundle.getMessage(EditFilterDialog.class, "jButton1.text")); 
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonClicked(evt);
                okButtonClicked(evt);
            }
        });
        cancelButton.setText(org.openide.util.NbBundle.getMessage(EditFilterDialog.class, "jButton2.text")); 
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonClicked(evt);
            }
        });
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(sourceLabel)
                            .add(nameLabel))
                        .add(25, 25, 25)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sourceLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 337, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );
        pack();
    }
private void okButtonClicked(java.awt.event.ActionEvent evt) {
        this.customFilter.setName(this.nameTextField.getText());
        this.customFilter.setCode(this.sourceTextArea.getText());
        accepted = true;
        setVisible(false);
}
private void cancelButtonClicked(java.awt.event.ActionEvent evt) {
        setVisible(false);
}
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JTextArea sourceTextArea;
}
