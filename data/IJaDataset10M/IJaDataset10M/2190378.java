package ocupacion;

import grupo.DatosGrupo;
import grupo.GrupoUtil;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Date;
import javax.swing.SwingConstants;
import reserva.DatosReserva;
import reserva.ReservaUtil;
import utilidad.Util;
import utilidad.clasesBase.BaseVO;
import utilidad.componentes.MiTableCellRendererFecha;
import utilidad.componentes.MiTableModel;
import utilidad.vo.ColumnaTablaVO;

public class NotificacionOcupacionDetallada extends javax.swing.JFrame {

    public static ResourceBundle bundle = ResourceBundle.getBundle("ocupacion/Bundle");

    public NotificacionOcupacionDetallada(ArrayList<OcupacionVO> listaOcupacionesErroneas, String entidad, int idEntidad, String datosEntidad) {
        initComponents();
        this.listaOcupacionesErroneas = listaOcupacionesErroneas;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.datosEntidad = datosEntidad;
        inicializarForm();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jLTitulo = new javax.swing.JLabel();
        jBAceptar = new javax.swing.JButton();
        jBEliminar = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTOcupacionesPrevias = new utilidad.componentes.MiTable();
        jBVerOcupacionPrevia = new javax.swing.JButton();
        jLDatosEntidad = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        jLTitulo.setFont(new java.awt.Font("Arial", 0, 18));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ocupacion/Bundle");
        jLTitulo.setText(bundle.getString("NotificacionOcupacionDetallada.jLTitulo.text"));
        jLTitulo.setToolTipText(bundle.getString("NotificacionOcupacionDetallada.jLTitulo.toolTipText"));
        jLTitulo.setName("jLTitulo");
        jBAceptar.setText(bundle.getString("NotificacionOcupacionDetallada.jBAceptar.text"));
        jBAceptar.setName("jBAceptar");
        jBAceptar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAceptarActionPerformed(evt);
            }
        });
        jBEliminar.setText(bundle.getString("NotificacionOcupacionDetallada.jBEliminar.text"));
        jBEliminar.setName("jBEliminar");
        jBEliminar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBEliminarActionPerformed(evt);
            }
        });
        jScrollPane5.setName("jScrollPane5");
        jTOcupacionesPrevias.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jTOcupacionesPrevias.setName("jTOcupacionesPrevias");
        jTOcupacionesPrevias.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTOcupacionesPreviasMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(jTOcupacionesPrevias);
        jBVerOcupacionPrevia.setText(bundle.getString("NotificacionOcupacionDetallada.jBVerOcupacionPrevia.text_1"));
        jBVerOcupacionPrevia.setToolTipText(bundle.getString("NotificacionOcupacionDetallada.jBVerOcupacionPrevia.toolTipText"));
        jBVerOcupacionPrevia.setName("jBVerOcupacionPrevia");
        jBVerOcupacionPrevia.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBVerOcupacionPreviaActionPerformed(evt);
            }
        });
        jLDatosEntidad.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLDatosEntidad.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLDatosEntidad.setText(bundle.getString("NotificacionOcupacionDetallada.jLDatosEntidad.text_1"));
        jLDatosEntidad.setToolTipText(bundle.getString("NotificacionOcupacionDetallada.jLDatosEntidad.toolTipText"));
        jLDatosEntidad.setName("jLDatosEntidad");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLDatosEntidad, javax.swing.GroupLayout.PREFERRED_SIZE, 628, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 637, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(jBEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jBAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jBVerOcupacionPrevia, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jLTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 637, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLDatosEntidad, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jBVerOcupacionPrevia).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jBAceptar).addComponent(jBEliminar)).addGap(19, 19, 19)));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 665) / 2, (screenSize.height - 363) / 2, 665, 363);
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        Util.eliminarReferenciaLista(this);
    }

    private void jBAceptarActionPerformed(java.awt.event.ActionEvent evt) {
        aceptar();
    }

    private void jBEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        eliminar();
    }

    private void jTOcupacionesPreviasMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            verOcupacionPrevia();
        }
    }

    private void jBVerOcupacionPreviaActionPerformed(java.awt.event.ActionEvent evt) {
        verOcupacionPrevia();
    }

    private void inicializarForm() {
        Util.cambiarIcono(this);
        this.setTitle(bundle.getString("Notificación_de_Ocupación_Detallada"));
        jLDatosEntidad.setText(datosEntidad);
        cargarModeloOcupacionesPrevias();
    }

    public void aceptar() {
        this.dispose();
    }

    public void eliminar() {
        if (fecha != null) {
            OcupacionUtil.eliminarOcupacionEntidad(entidad, idEntidad, fecha);
        } else {
            OcupacionUtil.eliminarOcupacionEntidad(entidad, idEntidad);
        }
        generarOcupacion = false;
        this.dispose();
    }

    protected void cargarModeloOcupacionesPrevias() {
        crearModeloOcupacionesPrevias();
        cargarListaColumnasOcupacionesPrevias();
        cargarListaObjetosOcupacionesPrevias();
        calcularTotalesOcupacionesPrevias();
        asignarModeloOcupacionesPrevias();
    }

    protected void asignarModeloOcupacionesPrevias() {
        jTOcupacionesPrevias.setModel(modeloOcupacionesPrevias);
        jTOcupacionesPrevias.inicializarColumnas(modeloOcupacionesPrevias.getListaColumnas());
    }

    public void crearModeloOcupacionesPrevias() {
        this.modeloOcupacionesPrevias = new MiTableModel("ocupacion.OcupacionVO");
    }

    public void calcularTotalesOcupacionesPrevias() {
    }

    public void cargarListaObjetosOcupacionesPrevias() {
        OcupacionParametros param = new OcupacionParametros();
        modeloOcupacionesPrevias.setListaObjetos(listaOcupacionesErroneas);
    }

    public void cargarListaColumnasOcupacionesPrevias() {
        ColumnaTablaVO col = null;
        ArrayList<ColumnaTablaVO> listaColumnas = new ArrayList<ColumnaTablaVO>();
        int i = 0;
        listaColumnas.add(new ColumnaTablaVO(bundle.getString("Fecha"), "getFecha", i++));
        col = new ColumnaTablaVO("Inicio", "getHoraInicio", i++);
        col.setTablaCellRenderer(new MiTableCellRendererFecha("H:mm"));
        col.setAnchoMax(60);
        listaColumnas.add(col);
        col = new ColumnaTablaVO("Fin", "getHoraFin", i++);
        col.setTablaCellRenderer(new MiTableCellRendererFecha("H:mm"));
        col.setAnchoMax(60);
        listaColumnas.add(col);
        col = new ColumnaTablaVO(bundle.getString("Instalacion"), "getNombreInstalacion", i++);
        col.setAlineacion(SwingConstants.CENTER);
        col.setAnchoMin(120);
        listaColumnas.add(col);
        col = new ColumnaTablaVO(bundle.getString("Unidad"), "getNombreUnidad", i++);
        col.setAlineacion(SwingConstants.CENTER);
        col.setAnchoMin(120);
        listaColumnas.add(col);
        modeloOcupacionesPrevias.setListaColumnas(listaColumnas);
    }

    protected BaseVO getObjSeleccionadoTablaOcupacionesPrevias() {
        BaseVO obj = null;
        int filaModeloSel = jTOcupacionesPrevias.getFilaSeleccionadaModelo();
        if (filaModeloSel != -1) {
            obj = (BaseVO) this.modeloOcupacionesPrevias.getObjDeLista(filaModeloSel);
        }
        return obj;
    }

    public void verOcupacionPrevia() {
        OcupacionVO ocupacionErroneaSel = (OcupacionVO) this.getObjSeleccionadoTablaOcupacionesPrevias();
        OcupacionParametros param = new OcupacionParametros();
        param.idUnidad = ocupacionErroneaSel.getIdUnidad();
        param.fechaDesde = ocupacionErroneaSel.getFecha();
        param.fechaHasta = ocupacionErroneaSel.getFecha();
        ArrayList<OcupacionVO> listaOcupaciones = OcupacionUtil.listado(param);
        OcupacionVO ocupacionCoincidente = OcupacionUtil.getOcupacionCoincidenteEnHoras(ocupacionErroneaSel, listaOcupaciones);
        if (ocupacionCoincidente != null) {
            if (ocupacionCoincidente.getEntidad().equals(OcupacionUtil.ENTIDAD_GRUPO)) {
                DatosGrupo frm = new DatosGrupo(GrupoUtil.buscarGrupo(ocupacionCoincidente.getIdEntidad(), false));
                Util.mostrarVentana(frm);
            } else if (ocupacionCoincidente.getEntidad().equals(OcupacionUtil.ENTIDAD_RESERVA)) {
                DatosReserva frm = new DatosReserva(ReservaUtil.buscarReserva(ocupacionCoincidente.getIdEntidad()));
                Util.mostrarVentana(frm);
            } else {
                Util.mostrarMensaje("El_Día_Seleccionado_Es_Festivo");
            }
        }
    }

    private ArrayList listaOcupacionesErroneas = new ArrayList();

    private String entidad = null;

    private Integer idEntidad = null;

    private Date fecha = null;

    private String datosEntidad = null;

    public Boolean generarOcupacion = true;

    public MiTableModel modeloOcupacionesPrevias = null;

    private javax.swing.JButton jBAceptar;

    private javax.swing.JButton jBEliminar;

    private javax.swing.JButton jBVerOcupacionPrevia;

    private javax.swing.JLabel jLDatosEntidad;

    private javax.swing.JLabel jLTitulo;

    private javax.swing.JScrollPane jScrollPane5;

    private utilidad.componentes.MiTable jTOcupacionesPrevias;
}