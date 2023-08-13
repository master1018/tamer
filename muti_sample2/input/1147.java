public class test {
            _yticklabels = new Vector();
        }
        _yticks.addElement(new Double(position));
        _yticklabels.addElement(label);
    }
    public synchronized void clear(boolean axes) {
        _xBottom = Double.MAX_VALUE;
        _xTop = -Double.MAX_VALUE;
        _yBottom = Double.MAX_VALUE;
        _yTop = -Double.MAX_VALUE;
        if (axes) {
            _yMax = 0;
            _yMin = 0;
            _xMax = 0;
            _xMin = 0;
            _xRangeGiven = false;
            _yRangeGiven = false;
            _xlog = false;
            _ylog = false;
            _grid = true;
            _usecolor = true;
            _filespec = null;
            _xlabel = null;
            _ylabel = null;
            _title = null;
            _legendStrings = new Vector();
            _legendDatasets = new Vector();
            _xticks = null;
            _xticklabels = null;
            _yticks = null;
            _yticklabels = null;
        }
    }
    public synchronized void fillPlot() {
        _setXRange(_xBottom, _xTop);
        _setYRange(_yBottom, _yTop);
        if (this.useSync) PlotRegistry.instance().changeRange(_xBottom, _xTop);
        repaint();
    }
    public static Color getColorByName(String name) {
        try {
            Color col = new Color(Integer.parseInt(name, 16));
            return col;
        } catch (NumberFormatException e) {
        }
}
