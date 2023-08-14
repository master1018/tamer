public class TableExample3 {
    public TableExample3() {
        JFrame frame = new JFrame("Table");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final String[] names = { "First Name", "Last Name", "Favorite Color",
            "Favorite Number", "Vegetarian" };
        final Object[][] data = {
            { "Mark", "Andrews", "Red", new Integer(2), Boolean.TRUE },
            { "Tom", "Ball", "Blue", new Integer(99), Boolean.FALSE },
            { "Alan", "Chung", "Green", new Integer(838), Boolean.FALSE },
            { "Jeff", "Dinkins", "Turquois", new Integer(8), Boolean.TRUE },
            { "Amy", "Fowler", "Yellow", new Integer(3), Boolean.FALSE },
            { "Brian", "Gerhold", "Green", new Integer(0), Boolean.FALSE },
            { "James", "Gosling", "Pink", new Integer(21), Boolean.FALSE },
            { "David", "Karlton", "Red", new Integer(1), Boolean.FALSE },
            { "Dave", "Kloba", "Yellow", new Integer(14), Boolean.FALSE },
            { "Peter", "Korn", "Purple", new Integer(12), Boolean.FALSE },
            { "Phil", "Milne", "Purple", new Integer(3), Boolean.FALSE },
            { "Dave", "Moore", "Green", new Integer(88), Boolean.FALSE },
            { "Hans", "Muller", "Maroon", new Integer(5), Boolean.FALSE },
            { "Rick", "Levenson", "Blue", new Integer(2), Boolean.FALSE },
            { "Tim", "Prinzing", "Blue", new Integer(22), Boolean.FALSE },
            { "Chester", "Rose", "Black", new Integer(0), Boolean.FALSE },
            { "Ray", "Ryan", "Gray", new Integer(77), Boolean.FALSE },
            { "Georges", "Saab", "Red", new Integer(4), Boolean.FALSE },
            { "Willie", "Walker", "Phthalo Blue", new Integer(4), Boolean.FALSE },
            { "Kathy", "Walrath", "Blue", new Integer(8), Boolean.FALSE },
            { "Arnaud", "Weber", "Green", new Integer(44), Boolean.FALSE }
        };
        @SuppressWarnings("serial")
        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() {
                return names.length;
            }
            public int getRowCount() {
                return data.length;
            }
            public Object getValueAt(int row, int col) {
                return data[row][col];
            }
            @Override
            public String getColumnName(int column) {
                return names[column];
            }
            @Override
            public Class getColumnClass(int col) {
                return getValueAt(0, col).getClass();
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return (col == 4);
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
            }
        };
        TableSorter sorter = new TableSorter(dataModel);
        JTable tableView = new JTable(sorter);
        sorter.addMouseListenerToHeaderInTable(tableView);
        JScrollPane scrollpane = new JScrollPane(tableView);
        scrollpane.setPreferredSize(new Dimension(700, 300));
        frame.getContentPane().add(scrollpane);
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TableExample3.class.getName()).log(Level.SEVERE,
                    "Failed to apply Nimbus look and feel", ex);
        }
        new TableExample3();
    }
}
