package com.velocityme.client.gui.loginhistory;

import com.velocityme.utility.LicenceKey;
import java.util.Collection;

/**
 *
 * @author  Robert Crida Work
 */
public class LoginHistoryDisplay extends javax.swing.JPanel {

    private LoginHistoryTableModel m_modelLoginHistory;

    /** Creates new form ContactDetailTypeValueDisplay */
    public LoginHistoryDisplay() {
        initComponents();
    }

    public void setLoginValues(Collection p_loginValues) {
        m_modelLoginHistory.addValues(p_loginValues);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        m_modelLoginHistory = new LoginHistoryTableModel();
        jTableLicences = new javax.swing.JTable(m_modelLoginHistory);
        setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(jTableLicences);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTable jTableLicences;
}
