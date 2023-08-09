final class ViewPanel extends javax.swing.JPanel {
    private final ViewOptionsPanelController controller;
    ViewPanel(ViewOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        portSpinner = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        nodeTextArea = new javax.swing.JTextArea();
        nodeWidthSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Node Text");
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Node Width");
        nodeTextArea.setColumns(20);
        nodeTextArea.setRows(5);
        jScrollPane1.setViewportView(nodeTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Network Port");
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel2))
                .add(39, 39, 39)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(portSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nodeWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 365, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(nodeWidthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .add(18, 18, 18)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(portSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3)))
                    .add(jLabel1))
                .add(73, 73, 73))
        );
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 232, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(206, Short.MAX_VALUE))
        );
    }
    void load() {
        nodeTextArea.setText(Settings.get().get(Settings.NODE_TEXT, Settings.NODE_TEXT_DEFAULT));
        nodeWidthSpinner.setValue(Integer.parseInt(Settings.get().get(Settings.NODE_WIDTH, Settings.NODE_WIDTH_DEFAULT)));
        portSpinner.setValue(Integer.parseInt(Settings.get().get(Settings.PORT, Settings.PORT_DEFAULT)));
    }
    void store() {
        Settings.get().put(Settings.NODE_TEXT, nodeTextArea.getText());
        Settings.get().put(Settings.NODE_WIDTH, nodeWidthSpinner.getValue().toString());
        Settings.get().put(Settings.PORT, portSpinner.getValue().toString());
    }
    boolean valid() {
        return true;
    }
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea nodeTextArea;
    private javax.swing.JSpinner nodeWidthSpinner;
    private javax.swing.JSpinner portSpinner;
}
