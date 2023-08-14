abstract class ActionPropertyChangeListener<T extends JComponent>
        implements PropertyChangeListener, Serializable {
    private static ReferenceQueue<JComponent> queue;
    private transient OwnedWeakReference<T> target;
    private Action action;
    private static ReferenceQueue<JComponent> getQueue() {
        synchronized(ActionPropertyChangeListener.class) {
            if (queue == null) {
                queue = new ReferenceQueue<JComponent>();
            }
        }
        return queue;
    }
    public ActionPropertyChangeListener(T c, Action a) {
        super();
        setTarget(c);
        this.action = a;
    }
    public final void propertyChange(PropertyChangeEvent e) {
        T target = getTarget();
        if (target == null) {
            getAction().removePropertyChangeListener(this);
        } else {
            actionPropertyChanged(target, getAction(), e);
        }
    }
    protected abstract void actionPropertyChanged(T target, Action action,
                                                  PropertyChangeEvent e);
    private void setTarget(T c) {
        ReferenceQueue<JComponent> queue = getQueue();
        OwnedWeakReference r;
        while ((r = (OwnedWeakReference)queue.poll()) != null) {
            ActionPropertyChangeListener oldPCL = r.getOwner();
            Action oldAction = oldPCL.getAction();
            if (oldAction!=null) {
                oldAction.removePropertyChangeListener(oldPCL);
            }
        }
        this.target = new OwnedWeakReference<T>(c, queue, this);
    }
    public T getTarget() {
        if (target == null) {
            return null;
        }
        return this.target.get();
    }
    public Action getAction() {
          return action;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(getTarget());
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
                     throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        T target = (T)s.readObject();
        if (target != null) {
            setTarget(target);
        }
    }
    private static class OwnedWeakReference<U extends JComponent> extends
                              WeakReference<U> {
        private ActionPropertyChangeListener owner;
        OwnedWeakReference(U target, ReferenceQueue<? super U> queue,
                           ActionPropertyChangeListener owner) {
            super(target, queue);
            this.owner = owner;
        }
        public ActionPropertyChangeListener getOwner() {
            return owner;
        }
    }
}
