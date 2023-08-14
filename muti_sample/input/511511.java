public class SelectionController extends Observable {
    private ArrayList<Selection> mSelections;
    public void change(ArrayList<Selection> selections, Object arg) {
        this.mSelections = selections;
        setChanged();
        notifyObservers(arg);
    }
    public ArrayList<Selection> getSelections() {
        return mSelections;
    }
}
