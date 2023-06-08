package com.catosist.run.business;

import com.catosist.run.dataaccess.domain.ColoresDao;
import com.catosist.run.dataaccess.domain.MarcasDao;
import com.catosist.run.dataaccess.domain.PersonaDao;
import com.catosist.run.dataaccess.domain.RutaDao;
import com.catosist.run.dataaccess.domain.VehiculoDao;
import com.catosist.run.dataaccess.domain.ViajeDao;
import com.catosist.run.dataaccess.model.ColoresVO;
import com.catosist.run.dataaccess.model.MarcasVO;
import com.catosist.run.dataaccess.model.PersonaVO;
import com.catosist.run.dataaccess.model.RutaVO;
import com.catosist.run.dataaccess.model.VehiculoVO;
import com.catosist.run.dataaccess.model.ViajeVO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Luizitoh
 */
public class ManViaje extends javax.swing.JFrame {

    /** Creates new form ManPers */
    public ManViaje() {
        refresh();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form");
        jPanel1.setName("jPanel1");
        jScrollPane1.setName("jScrollPane1");
        jTable1.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jTable1.setName("jTable1");
        jScrollPane1.setViewportView(jTable1);
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE).addContainerGap()));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ManViaje().setVisible(true);
            }
        });
    }

    private void refresh() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Ruta");
        model.addColumn("Placa Vehiculo");
        model.addColumn("Conductor");
        model.addColumn("Fecha");
        List<ViajeVO> viaj = viajeDao.getAll();
        for (Iterator<ViajeVO> it = viaj.iterator(); it.hasNext(); ) {
            ViajeVO viajeVO = it.next();
            model.addRow(toArray(viajeVO));
        }
        this.jTable1.setModel(model);
    }

    public JPanel getPanel() {
        return this.jPanel1;
    }

    public static Object[] toArray(ViajeVO o) {
        Object[] obj = new Object[4];
        obj[0] = o.getRuta().getDescripcion();
        obj[1] = o.getVehiculo().getPlaca();
        obj[2] = o.getPersona().getNombre();
        obj[3] = o.getFingreso();
        return obj;
    }

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTable jTable1;

    private ApplicationContext ctx;

    private ViajeDao viajeDao;

    private RutaDao rutaDao;

    private VehiculoDao vehiculoDao;

    private PersonaDao personaDao;

    private VehiculoVO vehiculoVO1;

    private ViajeVO viajeVO1;
}