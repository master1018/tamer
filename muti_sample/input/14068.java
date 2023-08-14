public class ClearChanged extends Observable {
    boolean clearChangedCalled = false;
    public static void main(String[] args) {
        ClearChanged test = new ClearChanged();
        test.setChanged();
        test.notifyObservers(new Object());
        if (!test.clearChangedCalled)
           throw new RuntimeException("setChanged method not called.");
    }
    public void clearChanged() {
        clearChangedCalled = true;
        super.clearChanged();
    }
}
