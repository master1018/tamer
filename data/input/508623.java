public class ProfilesTableModel extends DefaultTableModel {
    private static final String[] NAMES = { "measure", "layout", "draw" };
    private final double[] profiles;
    private final NumberFormat formatter;
    public ProfilesTableModel(double[] profiles) {
        this.profiles = profiles;
        formatter = NumberFormat.getNumberInstance();
    }
    @Override
    public int getRowCount() {
        return profiles == null ? 0 : profiles.length;
    }
    @Override
    public Object getValueAt(int row, int column) {
        if (profiles == null) return "";
        if (column == 0) {
            return NAMES[row];
        }
        return formatter.format(profiles[row]) + "";
    }
    @Override
    public int getColumnCount() {
        return 2;
    }
    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Operation" : "Duration (ms)";
    }
    @Override
    public boolean isCellEditable(int arg0, int arg1) {
        return false;
    }
    @Override
    public void setValueAt(Object arg0, int arg1, int arg2) {
    }
}
