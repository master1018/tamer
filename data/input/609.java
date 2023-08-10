public abstract class Enumeration {
    protected Iterator<TaskControlBlock> listIt;
    public void setIterator(Iterator<TaskControlBlock> it) {
        listIt = it;
    }
    public abstract GameObject getNext();
}
