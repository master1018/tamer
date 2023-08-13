public class DefaultTreeSelectionModel implements Cloneable, Serializable, TreeSelectionModel
{
    public static final String          SELECTION_MODE_PROPERTY = "selectionMode";
    protected SwingPropertyChangeSupport     changeSupport;
    protected TreePath[]                selection;
    protected EventListenerList   listenerList = new EventListenerList();
    transient protected RowMapper               rowMapper;
    protected DefaultListSelectionModel     listSelectionModel;
    protected int                           selectionMode;
    protected TreePath                      leadPath;
    protected int                           leadIndex;
    protected int                           leadRow;
    private Hashtable<TreePath, Boolean>    uniquePaths;
    private Hashtable<TreePath, Boolean>    lastPaths;
    private TreePath[]                      tempPaths;
    public DefaultTreeSelectionModel() {
        listSelectionModel = new DefaultListSelectionModel();
        selectionMode = DISCONTIGUOUS_TREE_SELECTION;
        leadIndex = leadRow = -1;
        uniquePaths = new Hashtable<TreePath, Boolean>();
        lastPaths = new Hashtable<TreePath, Boolean>();
        tempPaths = new TreePath[1];
    }
    public void setRowMapper(RowMapper newMapper) {
        rowMapper = newMapper;
        resetRowSelection();
    }
    public RowMapper getRowMapper() {
        return rowMapper;
    }
    public void setSelectionMode(int mode) {
        int            oldMode = selectionMode;
        selectionMode = mode;
        if(selectionMode != TreeSelectionModel.SINGLE_TREE_SELECTION &&
           selectionMode != TreeSelectionModel.CONTIGUOUS_TREE_SELECTION &&
           selectionMode != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
            selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
        if(oldMode != selectionMode && changeSupport != null)
            changeSupport.firePropertyChange(SELECTION_MODE_PROPERTY,
                                             Integer.valueOf(oldMode),
                                             Integer.valueOf(selectionMode));
    }
    public int getSelectionMode() {
        return selectionMode;
    }
    public void setSelectionPath(TreePath path) {
        if(path == null)
            setSelectionPaths(null);
        else {
            TreePath[]          newPaths = new TreePath[1];
            newPaths[0] = path;
            setSelectionPaths(newPaths);
        }
    }
    public void setSelectionPaths(TreePath[] pPaths) {
        int            newCount, newCounter, oldCount, oldCounter;
        TreePath[]     paths = pPaths;
        if(paths == null)
            newCount = 0;
        else
            newCount = paths.length;
        if(selection == null)
            oldCount = 0;
        else
            oldCount = selection.length;
        if((newCount + oldCount) != 0) {
            if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
                if(newCount > 1) {
                    paths = new TreePath[1];
                    paths[0] = pPaths[0];
                    newCount = 1;
                }
            }
            else if(selectionMode ==
                    TreeSelectionModel.CONTIGUOUS_TREE_SELECTION) {
                if(newCount > 0 && !arePathsContiguous(paths)) {
                    paths = new TreePath[1];
                    paths[0] = pPaths[0];
                    newCount = 1;
                }
            }
            TreePath         beginLeadPath = leadPath;
            Vector<PathPlaceHolder> cPaths = new Vector<PathPlaceHolder>(newCount + oldCount);
            List<TreePath> newSelectionAsList =
                    new ArrayList<TreePath>(newCount);
            lastPaths.clear();
            leadPath = null;
            for(newCounter = 0; newCounter < newCount; newCounter++) {
                TreePath path = paths[newCounter];
                if (path != null && lastPaths.get(path) == null) {
                    lastPaths.put(path, Boolean.TRUE);
                    if (uniquePaths.get(path) == null) {
                        cPaths.addElement(new PathPlaceHolder(path, true));
                    }
                    leadPath = path;
                    newSelectionAsList.add(path);
                }
            }
            TreePath[] newSelection = newSelectionAsList.toArray(
                    new TreePath[newSelectionAsList.size()]);
            for(oldCounter = 0; oldCounter < oldCount; oldCounter++)
                if(selection[oldCounter] != null &&
                    lastPaths.get(selection[oldCounter]) == null)
                    cPaths.addElement(new PathPlaceHolder
                                      (selection[oldCounter], false));
            selection = newSelection;
            Hashtable<TreePath, Boolean>  tempHT = uniquePaths;
            uniquePaths = lastPaths;
            lastPaths = tempHT;
            lastPaths.clear();
            insureUniqueness();
            updateLeadIndex();
            resetRowSelection();
            if(cPaths.size() > 0)
                notifyPathChange(cPaths, beginLeadPath);
        }
    }
    public void addSelectionPath(TreePath path) {
        if(path != null) {
            TreePath[]            toAdd = new TreePath[1];
            toAdd[0] = path;
            addSelectionPaths(toAdd);
        }
    }
    public void addSelectionPaths(TreePath[] paths) {
        int       newPathLength = ((paths == null) ? 0 : paths.length);
        if(newPathLength > 0) {
            if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
                setSelectionPaths(paths);
            }
            else if(selectionMode == TreeSelectionModel.
                    CONTIGUOUS_TREE_SELECTION && !canPathsBeAdded(paths)) {
                if(arePathsContiguous(paths)) {
                    setSelectionPaths(paths);
                }
                else {
                    TreePath[]          newPaths = new TreePath[1];
                    newPaths[0] = paths[0];
                    setSelectionPaths(newPaths);
                }
            }
            else {
                int               counter, validCount;
                int               oldCount;
                TreePath          beginLeadPath = leadPath;
                Vector<PathPlaceHolder>  cPaths = null;
                if(selection == null)
                    oldCount = 0;
                else
                    oldCount = selection.length;
                lastPaths.clear();
                for(counter = 0, validCount = 0; counter < newPathLength;
                    counter++) {
                    if(paths[counter] != null) {
                        if (uniquePaths.get(paths[counter]) == null) {
                            validCount++;
                            if(cPaths == null)
                                cPaths = new Vector<PathPlaceHolder>();
                            cPaths.addElement(new PathPlaceHolder
                                              (paths[counter], true));
                            uniquePaths.put(paths[counter], Boolean.TRUE);
                            lastPaths.put(paths[counter], Boolean.TRUE);
                        }
                        leadPath = paths[counter];
                    }
                }
                if(leadPath == null) {
                    leadPath = beginLeadPath;
                }
                if(validCount > 0) {
                    TreePath         newSelection[] = new TreePath[oldCount +
                                                                  validCount];
                    if(oldCount > 0)
                        System.arraycopy(selection, 0, newSelection, 0,
                                         oldCount);
                    if(validCount != paths.length) {
                        Enumeration<TreePath> newPaths = lastPaths.keys();
                        counter = oldCount;
                        while (newPaths.hasMoreElements()) {
                            newSelection[counter++] = newPaths.nextElement();
                        }
                    }
                    else {
                        System.arraycopy(paths, 0, newSelection, oldCount,
                                         validCount);
                    }
                    selection = newSelection;
                    insureUniqueness();
                    updateLeadIndex();
                    resetRowSelection();
                    notifyPathChange(cPaths, beginLeadPath);
                }
                else
                    leadPath = beginLeadPath;
                lastPaths.clear();
            }
        }
    }
    public void removeSelectionPath(TreePath path) {
        if(path != null) {
            TreePath[]             rPath = new TreePath[1];
            rPath[0] = path;
            removeSelectionPaths(rPath);
        }
    }
    public void removeSelectionPaths(TreePath[] paths) {
        if (paths != null && selection != null && paths.length > 0) {
            if(!canPathsBeRemoved(paths)) {
                clearSelection();
            }
            else {
                Vector<PathPlaceHolder> pathsToRemove = null;
                for (int removeCounter = paths.length - 1; removeCounter >= 0;
                     removeCounter--) {
                    if(paths[removeCounter] != null) {
                        if (uniquePaths.get(paths[removeCounter]) != null) {
                            if(pathsToRemove == null)
                                pathsToRemove = new Vector<PathPlaceHolder>(paths.length);
                            uniquePaths.remove(paths[removeCounter]);
                            pathsToRemove.addElement(new PathPlaceHolder
                                         (paths[removeCounter], false));
                        }
                    }
                }
                if(pathsToRemove != null) {
                    int         removeCount = pathsToRemove.size();
                    TreePath    beginLeadPath = leadPath;
                    if(removeCount == selection.length) {
                        selection = null;
                    }
                    else {
                        Enumeration<TreePath> pEnum = uniquePaths.keys();
                        int                  validCount = 0;
                        selection = new TreePath[selection.length -
                                                removeCount];
                        while (pEnum.hasMoreElements()) {
                            selection[validCount++] = pEnum.nextElement();
                        }
                    }
                    if (leadPath != null &&
                        uniquePaths.get(leadPath) == null) {
                        if (selection != null) {
                            leadPath = selection[selection.length - 1];
                        }
                        else {
                            leadPath = null;
                        }
                    }
                    else if (selection != null) {
                        leadPath = selection[selection.length - 1];
                    }
                    else {
                        leadPath = null;
                    }
                    updateLeadIndex();
                    resetRowSelection();
                    notifyPathChange(pathsToRemove, beginLeadPath);
                }
            }
        }
    }
    public TreePath getSelectionPath() {
        if (selection != null && selection.length > 0) {
            return selection[0];
        }
        return null;
    }
    public TreePath[] getSelectionPaths() {
        if(selection != null) {
            int                 pathSize = selection.length;
            TreePath[]          result = new TreePath[pathSize];
            System.arraycopy(selection, 0, result, 0, pathSize);
            return result;
        }
        return new TreePath[0];
    }
    public int getSelectionCount() {
        return (selection == null) ? 0 : selection.length;
    }
    public boolean isPathSelected(TreePath path) {
        return (path != null) ? (uniquePaths.get(path) != null) : false;
    }
    public boolean isSelectionEmpty() {
        return (selection == null || selection.length == 0);
    }
    public void clearSelection() {
        if (selection != null && selection.length > 0) {
            int                    selSize = selection.length;
            boolean[]              newness = new boolean[selSize];
            for(int counter = 0; counter < selSize; counter++)
                newness[counter] = false;
            TreeSelectionEvent     event = new TreeSelectionEvent
                (this, selection, newness, leadPath, null);
            leadPath = null;
            leadIndex = leadRow = -1;
            uniquePaths.clear();
            selection = null;
            resetRowSelection();
            fireValueChanged(event);
        }
    }
    public void addTreeSelectionListener(TreeSelectionListener x) {
        listenerList.add(TreeSelectionListener.class, x);
    }
    public void removeTreeSelectionListener(TreeSelectionListener x) {
        listenerList.remove(TreeSelectionListener.class, x);
    }
    public TreeSelectionListener[] getTreeSelectionListeners() {
        return listenerList.getListeners(TreeSelectionListener.class);
    }
    protected void fireValueChanged(TreeSelectionEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeSelectionListener.class) {
                ((TreeSelectionListener)listeners[i+1]).valueChanged(e);
            }
        }
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }
    public int[] getSelectionRows() {
        if (rowMapper != null && selection != null && selection.length > 0) {
            int[]      rows = rowMapper.getRowsForPaths(selection);
            if (rows != null) {
                int       invisCount = 0;
                for (int counter = rows.length - 1; counter >= 0; counter--) {
                    if (rows[counter] == -1) {
                        invisCount++;
                    }
                }
                if (invisCount > 0) {
                    if (invisCount == rows.length) {
                        rows = null;
                    }
                    else {
                        int[]    tempRows = new int[rows.length - invisCount];
                        for (int counter = rows.length - 1, visCounter = 0;
                             counter >= 0; counter--) {
                            if (rows[counter] != -1) {
                                tempRows[visCounter++] = rows[counter];
                            }
                        }
                        rows = tempRows;
                    }
                }
            }
            return rows;
        }
        return new int[0];
    }
    public int getMinSelectionRow() {
        return listSelectionModel.getMinSelectionIndex();
    }
    public int getMaxSelectionRow() {
        return listSelectionModel.getMaxSelectionIndex();
    }
    public boolean isRowSelected(int row) {
        return listSelectionModel.isSelectedIndex(row);
    }
    public void resetRowSelection() {
        listSelectionModel.clearSelection();
        if(selection != null && rowMapper != null) {
            int               aRow;
            int               validCount = 0;
            int[]             rows = rowMapper.getRowsForPaths(selection);
            for(int counter = 0, maxCounter = selection.length;
                counter < maxCounter; counter++) {
                aRow = rows[counter];
                if(aRow != -1) {
                    listSelectionModel.addSelectionInterval(aRow, aRow);
                }
            }
            if(leadIndex != -1 && rows != null) {
                leadRow = rows[leadIndex];
            }
            else if (leadPath != null) {
                tempPaths[0] = leadPath;
                rows = rowMapper.getRowsForPaths(tempPaths);
                leadRow = (rows != null) ? rows[0] : -1;
            }
            else {
                leadRow = -1;
            }
            insureRowContinuity();
        }
        else
            leadRow = -1;
    }
    public int getLeadSelectionRow() {
        return leadRow;
    }
    public TreePath getLeadSelectionPath() {
        return leadPath;
    }
    public synchronized void addPropertyChangeListener(
                                PropertyChangeListener listener) {
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
    public synchronized void removePropertyChangeListener(
                                PropertyChangeListener listener) {
        if (changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }
    public PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }
    protected void insureRowContinuity() {
        if(selectionMode == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION &&
           selection != null && rowMapper != null) {
            DefaultListSelectionModel lModel = listSelectionModel;
            int                       min = lModel.getMinSelectionIndex();
            if(min != -1) {
                for(int counter = min,
                        maxCounter = lModel.getMaxSelectionIndex();
                        counter <= maxCounter; counter++) {
                    if(!lModel.isSelectedIndex(counter)) {
                        if(counter == min) {
                            clearSelection();
                        }
                        else {
                            TreePath[] newSel = new TreePath[counter - min];
                            int selectionIndex[] = rowMapper.getRowsForPaths(selection);
                            for (int i = 0; i < selectionIndex.length; i++) {
                                if (selectionIndex[i]<counter) {
                                    newSel[selectionIndex[i]-min] = selection[i];
                                }
                            }
                            setSelectionPaths(newSel);
                            break;
                        }
                    }
                }
            }
        }
        else if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION &&
                selection != null && selection.length > 1) {
            setSelectionPath(selection[0]);
        }
    }
    protected boolean arePathsContiguous(TreePath[] paths) {
        if(rowMapper == null || paths.length < 2)
            return true;
        else {
            BitSet                             bitSet = new BitSet(32);
            int                                anIndex, counter, min;
            int                                pathCount = paths.length;
            int                                validCount = 0;
            TreePath[]                         tempPath = new TreePath[1];
            tempPath[0] = paths[0];
            min = rowMapper.getRowsForPaths(tempPath)[0];
            for(counter = 0; counter < pathCount; counter++) {
                if(paths[counter] != null) {
                    tempPath[0] = paths[counter];
                    int[] rows = rowMapper.getRowsForPaths(tempPath);
                    if (rows == null) {
                        return false;
                    }
                    anIndex = rows[0];
                    if(anIndex == -1 || anIndex < (min - pathCount) ||
                       anIndex > (min + pathCount))
                        return false;
                    if(anIndex < min)
                        min = anIndex;
                    if(!bitSet.get(anIndex)) {
                        bitSet.set(anIndex);
                        validCount++;
                    }
                }
            }
            int          maxCounter = validCount + min;
            for(counter = min; counter < maxCounter; counter++)
                if(!bitSet.get(counter))
                    return false;
        }
        return true;
    }
    protected boolean canPathsBeAdded(TreePath[] paths) {
        if(paths == null || paths.length == 0 || rowMapper == null ||
           selection == null || selectionMode ==
           TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
            return true;
        else {
            BitSet                       bitSet = new BitSet();
            DefaultListSelectionModel    lModel = listSelectionModel;
            int                          anIndex;
            int                          counter;
            int                          min = lModel.getMinSelectionIndex();
            int                          max = lModel.getMaxSelectionIndex();
            TreePath[]                   tempPath = new TreePath[1];
            if(min != -1) {
                for(counter = min; counter <= max; counter++) {
                    if(lModel.isSelectedIndex(counter))
                        bitSet.set(counter);
                }
            }
            else {
                tempPath[0] = paths[0];
                min = max = rowMapper.getRowsForPaths(tempPath)[0];
            }
            for(counter = paths.length - 1; counter >= 0; counter--) {
                if(paths[counter] != null) {
                    tempPath[0] = paths[counter];
                    int[]   rows = rowMapper.getRowsForPaths(tempPath);
                    if (rows == null) {
                        return false;
                    }
                    anIndex = rows[0];
                    min = Math.min(anIndex, min);
                    max = Math.max(anIndex, max);
                    if(anIndex == -1)
                        return false;
                    bitSet.set(anIndex);
                }
            }
            for(counter = min; counter <= max; counter++)
                if(!bitSet.get(counter))
                    return false;
        }
        return true;
    }
    protected boolean canPathsBeRemoved(TreePath[] paths) {
        if(rowMapper == null || selection == null ||
           selectionMode == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
            return true;
        else {
            BitSet               bitSet = new BitSet();
            int                  counter;
            int                  pathCount = paths.length;
            int                  anIndex;
            int                  min = -1;
            int                  validCount = 0;
            TreePath[]           tempPath = new TreePath[1];
            int[]                rows;
            lastPaths.clear();
            for (counter = 0; counter < pathCount; counter++) {
                if (paths[counter] != null) {
                    lastPaths.put(paths[counter], Boolean.TRUE);
                }
            }
            for(counter = selection.length - 1; counter >= 0; counter--) {
                if(lastPaths.get(selection[counter]) == null) {
                    tempPath[0] = selection[counter];
                    rows = rowMapper.getRowsForPaths(tempPath);
                    if(rows != null && rows[0] != -1 && !bitSet.get(rows[0])) {
                        validCount++;
                        if(min == -1)
                            min = rows[0];
                        else
                            min = Math.min(min, rows[0]);
                        bitSet.set(rows[0]);
                    }
                }
            }
            lastPaths.clear();
            if(validCount > 1) {
                for(counter = min + validCount - 1; counter >= min;
                    counter--)
                    if(!bitSet.get(counter))
                        return false;
            }
        }
        return true;
    }
    @Deprecated
    protected void notifyPathChange(Vector changedPaths,
                                    TreePath oldLeadSelection) {
        int                    cPathCount = changedPaths.size();
        boolean[]              newness = new boolean[cPathCount];
        TreePath[]            paths = new TreePath[cPathCount];
        PathPlaceHolder        placeholder;
        for(int counter = 0; counter < cPathCount; counter++) {
            placeholder = (PathPlaceHolder) changedPaths.elementAt(counter);
            newness[counter] = placeholder.isNew;
            paths[counter] = placeholder.path;
        }
        TreeSelectionEvent     event = new TreeSelectionEvent
                          (this, paths, newness, oldLeadSelection, leadPath);
        fireValueChanged(event);
    }
    protected void updateLeadIndex() {
        if(leadPath != null) {
            if(selection == null) {
                leadPath = null;
                leadIndex = leadRow = -1;
            }
            else {
                leadRow = leadIndex = -1;
                for(int counter = selection.length - 1; counter >= 0;
                    counter--) {
                    if(selection[counter] == leadPath) {
                        leadIndex = counter;
                        break;
                    }
                }
            }
        }
        else {
            leadIndex = -1;
        }
    }
    protected void insureUniqueness() {
    }
    public String toString() {
        int                selCount = getSelectionCount();
        StringBuffer       retBuffer = new StringBuffer();
        int[]              rows;
        if(rowMapper != null)
            rows = rowMapper.getRowsForPaths(selection);
        else
            rows = null;
        retBuffer.append(getClass().getName() + " " + hashCode() + " [ ");
        for(int counter = 0; counter < selCount; counter++) {
            if(rows != null)
                retBuffer.append(selection[counter].toString() + "@" +
                                 Integer.toString(rows[counter])+ " ");
            else
                retBuffer.append(selection[counter].toString() + " ");
        }
        retBuffer.append("]");
        return retBuffer.toString();
    }
    public Object clone() throws CloneNotSupportedException {
        DefaultTreeSelectionModel        clone = (DefaultTreeSelectionModel)
                            super.clone();
        clone.changeSupport = null;
        if(selection != null) {
            int              selLength = selection.length;
            clone.selection = new TreePath[selLength];
            System.arraycopy(selection, 0, clone.selection, 0, selLength);
        }
        clone.listenerList = new EventListenerList();
        clone.listSelectionModel = (DefaultListSelectionModel)
            listSelectionModel.clone();
        clone.uniquePaths = new Hashtable<TreePath, Boolean>();
        clone.lastPaths = new Hashtable<TreePath, Boolean>();
        clone.tempPaths = new TreePath[1];
        return clone;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        Object[]             tValues;
        s.defaultWriteObject();
        if(rowMapper != null && rowMapper instanceof Serializable) {
            tValues = new Object[2];
            tValues[0] = "rowMapper";
            tValues[1] = rowMapper;
        }
        else
            tValues = new Object[0];
        s.writeObject(tValues);
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        Object[]      tValues;
        s.defaultReadObject();
        tValues = (Object[])s.readObject();
        if(tValues.length > 0 && tValues[0].equals("rowMapper"))
            rowMapper = (RowMapper)tValues[1];
    }
}
class PathPlaceHolder {
    protected boolean             isNew;
    protected TreePath           path;
    PathPlaceHolder(TreePath path, boolean isNew) {
        this.path = path;
        this.isNew = isNew;
    }
}
