public class Model {
    protected ArrayList<IModelChangedObserver> mModelChangedObservers =
            new ArrayList<IModelChangedObserver>();
    public void registerModelChangedObserver(IModelChangedObserver observer) {
        if (!mModelChangedObservers.contains(observer)) {
            mModelChangedObservers.add(observer);
            registerModelChangedObserverInDescendants(observer);
        }
    }
    public void unregisterModelChangedObserver(IModelChangedObserver observer) {
        mModelChangedObservers.remove(observer);
        unregisterModelChangedObserverInDescendants(observer);
    }
    public void unregisterAllModelChangedObservers() {
        unregisterAllModelChangedObserversInDescendants();
        mModelChangedObservers.clear();
    }
    protected void notifyModelChanged(boolean dataChanged) {
        for (IModelChangedObserver observer : mModelChangedObservers) {
            observer.onModelChanged(this, dataChanged);
        }
    }
    protected void registerModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
    }
    protected void unregisterModelChangedObserverInDescendants(
            IModelChangedObserver observer) {
    }
    protected void unregisterAllModelChangedObserversInDescendants() {
    }
}
