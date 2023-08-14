public class TableExample2 {
    public TableExample2(String URL, String driver, String user,
            String passwd, String query) {
        JFrame frame = new JFrame("Table");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JDBCAdapter dt = new JDBCAdapter(URL, driver, user, passwd);
        dt.executeQuery(query);
        JTable tableView = new JTable(dt);
        JScrollPane scrollpane = new JScrollPane(tableView);
        scrollpane.setPreferredSize(new Dimension(700, 300));
        frame.getContentPane().add(scrollpane);
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Needs database parameters eg. ...");
            System.err.println(
                    "java TableExample2 \"jdbc:derby:
                    + "org.apache.derby.jdbc.ClientDriver app app "
                    + "\"select * from app.customer\"");
            return;
        }
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TableExample2.class.getName()).log(Level.SEVERE,
                    "Failed to apply Nimbus look and feel", ex);
        }
        new TableExample2(args[0], args[1], args[2], args[3], args[4]);
    }
}
