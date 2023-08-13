public class TimeComboBox extends JComboBox implements ItemListener, PropertyChangeListener {
    private ArrayList<Plotter> plotters = new ArrayList<Plotter>();
    public TimeComboBox(Plotter... plotterArray) {
        super(Plotter.rangeNames);
        addItemListener(this);
        if (plotterArray != null && plotterArray.length > 0) {
            plotters.addAll(Arrays.asList(plotterArray));
            selectValue(plotterArray[0].getViewRange());
            for (Plotter plotter : plotters) {
                plotter.addPropertyChangeListener(this);
            }
        }
    }
    public void addPlotter(Plotter plotter) {
        plotters.add(plotter);
        if (plotters.size() == 1) {
            selectValue(plotter.getViewRange());
        }
        plotter.addPropertyChangeListener(this);
    }
    public void itemStateChanged(ItemEvent ev) {
        for (Plotter plotter : plotters) {
            plotter.setViewRange(Plotter.rangeValues[getSelectedIndex()]);
        }
    }
    private void selectValue(int value) {
        for (int i = 0; i < Plotter.rangeValues.length; i++) {
            if (Plotter.rangeValues[i] == value) {
                setSelectedItem(Plotter.rangeNames[i]);
            }
        }
        if (plotters.size() > 1) {
            for (Plotter plotter : plotters) {
                plotter.setViewRange(value);
            }
        }
    }
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName() == "viewRange") {
            selectValue((Integer)ev.getNewValue());
        }
    }
}
