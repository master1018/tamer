public class Test6888156 {
    private JTable table;
    private Icon ICON = new Icon() {
        @Override public int getIconWidth() {
            return 24;
        }
        @Override public int getIconHeight() {
            return 24;
        }
        @Override public void paintIcon(Component c, Graphics g, int w, int h) {
        }
    };
    public Test6888156() {
        TableModel model = new AbstractTableModel() {
            @Override public int getRowCount() {
                return 3;
            }
            @Override public int getColumnCount() {
                return 2;
            }
            @Override public Object getValueAt(int rowIndex, int columnIndex) {
                return (columnIndex == 1 ? ICON : 4);
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1 ? Icon.class : int.class);
            }
        };
        table = new JTable(model);
    }
    public void test(final String laf) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                try {
                    System.out.println(laf);
                    UIManager.setLookAndFeel(laf);
                } catch (Exception e) {
                    System.err.println(laf + " is unsupported; continuing");
                    return;
                }
                SwingUtilities.updateComponentTreeUI(table);
                table.setSize(100, 100);
                table.paint(
                        new BufferedImage(100, 100, BufferedImage.OPAQUE).
                            getGraphics());
            }
        });
    }
    public static void main(String[] args) throws Exception {
        Test6888156 t = new Test6888156();
        t.test("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        t.test("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            t.test(laf.getClassName());
        }
    }
}
