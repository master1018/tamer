package asfabdesk.apresentacao.resources;

/**
 *
 * @author Marco Aurélio
 */
public class TelaSecretaria extends javax.swing.JInternalFrame {

    /** Creates new form TelaSecretaria */
    public TelaSecretaria() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        txtMatricula1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        txtMatricula = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        jPanel3.setName("jPanel3");
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        setClosable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(asfabdesk.apresentacao.AsfabDeskApp.class).getContext().getResourceMap(TelaSecretaria.class);
        setTitle(resourceMap.getString("Form.title"));
        setName("Form");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel4.setBackground(resourceMap.getColor("jPanel4.background"));
        jPanel4.setName("jPanel4");
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.setBackground(resourceMap.getColor("jPanel2.background"));
        jPanel2.setName("jPanel2");
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        txtMatricula1.setFont(resourceMap.getFont("txtMatricula1.font"));
        txtMatricula1.setName("txtMatricula1");
        jPanel2.add(txtMatricula1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 90, 60, -1));
        jButton1.setForeground(resourceMap.getColor("jButton1.foreground"));
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon"));
        jButton1.setText(resourceMap.getString("jButton1.text"));
        jButton1.setMaximumSize(null);
        jButton1.setMinimumSize(null);
        jButton1.setName("jButton1");
        jButton1.setPreferredSize(null);
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 80, 20));
        txtMatricula.setFont(resourceMap.getFont("txtMatricula.font"));
        txtMatricula.setMaximumSize(null);
        txtMatricula.setName("txtMatricula");
        jPanel2.add(txtMatricula, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 180, -1));
        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon"));
        jLabel1.setText(resourceMap.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 0, -1, -1));
        jLabel2.setFont(resourceMap.getFont("jLabel2.font"));
        jLabel2.setText(resourceMap.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, -1, -1));
        jLabel3.setFont(resourceMap.getFont("jLabel3.font"));
        jLabel3.setText(resourceMap.getString("jLabel3.text"));
        jLabel3.setName("jLabel3");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 70, -1, -1));
        jLabel4.setFont(resourceMap.getFont("jLabel4.font"));
        jLabel4.setText(resourceMap.getString("jLabel4.text"));
        jLabel4.setName("jLabel4");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, -1, -1));
        try {
            jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##) ####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        jFormattedTextField1.setName("jFormattedTextField1");
        jPanel2.add(jFormattedTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 90, -1));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon"));
        jLabel7.setText(resourceMap.getString("jLabel7.text"));
        jLabel7.setName("jLabel7");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 100, 100));
        jPanel4.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 320, 170));
        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 340, 190));
        pack();
    }

    private javax.swing.JButton jButton1;

    private javax.swing.JFormattedTextField jFormattedTextField1;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JTextField txtMatricula;

    private javax.swing.JTextField txtMatricula1;
}