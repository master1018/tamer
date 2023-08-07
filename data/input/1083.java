public abstract class AbstractObservableNavigableMap<K, V> extends AbstractNavigableMapDecorator<K, V> implements ObservableNavigableMap<K, V> {
    private ObservableNavigableMapChangeSupport<K, V> support;
    protected AbstractObservableNavigableMap(final NavigableMap<K, V> navigableMap) {
        super(navigableMap);
        support = new ObservableNavigableMapChangeSupport(this);
    }
    protected final ObservableNavigableMapChangeSupport<K, V> getObservableNavigableMapChangeSupport() {
        return support;
    }
    public final void addNavigableMapChangeListener(final NavigableMapChangeListener<K, V> l) {
        support.addNavigableMapChangeListener(l);
    }
    public final void removeNavigableMapChangeListener(final NavigableMapChangeListener<K, V> l) {
        support.removeNavigableMapChangeListener(l);
    }
    public final void addVetoableNavigableMapChangeListener(final VetoableNavigableMapChangeListener<K, V> l) {
        support.addVetoableNavigableMapChangeListener(l);
    }
    public final void removeVetoableNavigableMapChangeListener(final VetoableNavigableMapChangeListener<K, V> l) {
        support.removeVetoableNavigableMapChangeListener(l);
    }
    public final NavigableMapChangeListener<K, V>[] getNavigableMapChangeListeners() {
        return support.getNavigableMapChangeListeners();
    }
    public final int getNavigableMapChangeListenerCount() {
        return support.getNavigableMapChangeListenerCount();
    }
    public final VetoableNavigableMapChangeListener<K, V>[] getVetoableNavigableMapChangeListeners() {
        return support.getVetoableNavigableMapChangeListeners();
    }
    public final int getVetoableNavigableMapChangeListenerCount() {
        return support.getVetoableNavigableMapChangeListenerCount();
    }
    public void fireNavigableMapWillChange() throws NavigableMapChangeVetoException {
        support.fireNavigableMapWillChange();
    }
    public void fireNavigableMapWillChange(final VetoableNavigableMapChangeEvent<K, V> e) throws NavigableMapChangeVetoException {
        support.fireNavigableMapWillChange(e);
    }
    public void fireNavigableMapChanged() {
        support.fireNavigableMapChanged();
    }
    public void fireNavigableMapChanged(final NavigableMapChangeEvent<K, V> e) {
        support.fireNavigableMapChanged(e);
    }
}
