public class QValuePanel extends JPanel {
    private static final long serialVersionUID = 5424845598235187017L;
    QTable table = null;
    SpreadsheetRowHeader rowHeader;
    JScrollPane scrollPane;
    public QValuePanel() {
        this.setLayout(new BorderLayout());
        table = new QTable(this);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setEnabled(false);
        rowHeader = new SpreadsheetRowHeader(table);
        scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setRowHeaderView(rowHeader);
        this.add(scrollPane, BorderLayout.CENTER);
    }
    public SpreadsheetRowHeader getRowHeader() {
        return rowHeader;
    }
    public QTable getQTable() {
        return table;
    }
}
