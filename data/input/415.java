public class GraphSelectionEvent extends EventObject {
    protected Object[] cells;
    protected boolean[] areNew;
    public GraphSelectionEvent(Object source, Object[] cells, boolean[] areNew) {
        super(source);
        this.cells = cells;
        this.areNew = areNew;
    }
    public Object[] getCells() {
        int numCells;
        Object[] retCells;
        numCells = cells.length;
        retCells = new Object[numCells];
        System.arraycopy(cells, 0, retCells, 0, numCells);
        return retCells;
    }
    public Object getCell() {
        return cells[0];
    }
    public boolean isAddedCell() {
        return areNew[0];
    }
    public boolean isAddedCell(Object cell) {
        for (int counter = cells.length - 1; counter >= 0; counter--) if (cells[counter].equals(cell)) return areNew[counter];
        throw new IllegalArgumentException("cell is not a cell identified by the GraphSelectionEvent");
    }
    public boolean isAddedCell(int index) {
        if (cells == null || index < 0 || index >= cells.length) {
            throw new IllegalArgumentException("index is beyond range of added cells identified by GraphSelectionEvent");
        }
        return areNew[index];
    }
    public Object cloneWithSource(Object newSource) {
        return new GraphSelectionEvent(newSource, cells, areNew);
    }
}
