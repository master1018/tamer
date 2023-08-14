public class XPlotter extends Plotter {
    JTable table;
    public XPlotter(JTable table,
                    Plotter.Unit unit) {
        super(unit,0,false);
        this.table = table;
    }
    @Override
    public void addValues(long time, long... values) {
        super.addValues(time, values);
        table.repaint();
    }
}
