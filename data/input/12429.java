public class Choice extends Component implements ItemSelectable, Accessible {
    Vector pItems;
    int selectedIndex = -1;
    transient ItemListener itemListener;
    private static final String base = "choice";
    private static int nameCounter = 0;
     private static final long serialVersionUID = -4075310674757313071L;
    public Choice() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        pItems = new Vector();
    }
    String constructComponentName() {
        synchronized (Choice.class) {
            return base + nameCounter++;
        }
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createChoice(this);
            super.addNotify();
        }
    }
    public int getItemCount() {
        return countItems();
    }
    @Deprecated
    public int countItems() {
        return pItems.size();
    }
    public String getItem(int index) {
        return getItemImpl(index);
    }
    final String getItemImpl(int index) {
        return (String)pItems.elementAt(index);
    }
    public void add(String item) {
        addItem(item);
    }
    public void addItem(String item) {
        synchronized (this) {
            insertNoInvalidate(item, pItems.size());
        }
        invalidateIfValid();
    }
    private void insertNoInvalidate(String item, int index) {
        if (item == null) {
            throw new
                NullPointerException("cannot add null item to Choice");
        }
        pItems.insertElementAt(item, index);
        ChoicePeer peer = (ChoicePeer)this.peer;
        if (peer != null) {
            peer.add(item, index);
        }
        if (selectedIndex < 0 || selectedIndex >= index) {
            select(0);
        }
    }
    public void insert(String item, int index) {
        synchronized (this) {
            if (index < 0) {
                throw new IllegalArgumentException("index less than zero.");
            }
            index = Math.min(index, pItems.size());
            insertNoInvalidate(item, index);
        }
        invalidateIfValid();
    }
    public void remove(String item) {
        synchronized (this) {
            int index = pItems.indexOf(item);
            if (index < 0) {
                throw new IllegalArgumentException("item " + item +
                                                   " not found in choice");
            } else {
                removeNoInvalidate(index);
            }
        }
        invalidateIfValid();
    }
    public void remove(int position) {
        synchronized (this) {
            removeNoInvalidate(position);
        }
        invalidateIfValid();
    }
    private void removeNoInvalidate(int position) {
        pItems.removeElementAt(position);
        ChoicePeer peer = (ChoicePeer)this.peer;
        if (peer != null) {
            peer.remove(position);
        }
        if (pItems.size() == 0) {
            selectedIndex = -1;
        } else if (selectedIndex == position) {
            select(0);
        } else if (selectedIndex > position) {
            select(selectedIndex-1);
        }
    }
    public void removeAll() {
        synchronized (this) {
            if (peer != null) {
                ((ChoicePeer)peer).removeAll();
            }
            pItems.removeAllElements();
            selectedIndex = -1;
        }
        invalidateIfValid();
    }
    public synchronized String getSelectedItem() {
        return (selectedIndex >= 0) ? getItem(selectedIndex) : null;
    }
    public synchronized Object[] getSelectedObjects() {
        if (selectedIndex >= 0) {
            Object[] items = new Object[1];
            items[0] = getItem(selectedIndex);
            return items;
        }
        return null;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }
    public synchronized void select(int pos) {
        if ((pos >= pItems.size()) || (pos < 0)) {
            throw new IllegalArgumentException("illegal Choice item position: " + pos);
        }
        if (pItems.size() > 0) {
            selectedIndex = pos;
            ChoicePeer peer = (ChoicePeer)this.peer;
            if (peer != null) {
                peer.select(pos);
            }
        }
    }
    public synchronized void select(String str) {
        int index = pItems.indexOf(str);
        if (index >= 0) {
            select(index);
        }
    }
    public synchronized void addItemListener(ItemListener l) {
        if (l == null) {
           return;
        }
        itemListener = AWTEventMulticaster.add(itemListener, l);
        newEventsOnly = true;
    }
    public synchronized void removeItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }
    public synchronized ItemListener[] getItemListeners() {
        return (ItemListener[])(getListeners(ItemListener.class));
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ItemListener.class) {
            l = itemListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ItemEvent.ITEM_STATE_CHANGED) {
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        }
        super.processEvent(e);
    }
    protected void processItemEvent(ItemEvent e) {
        ItemListener listener = itemListener;
        if (listener != null) {
            listener.itemStateChanged(e);
        }
    }
    protected String paramString() {
        return super.paramString() + ",current=" + getSelectedItem();
    }
    private int choiceSerializedDataVersion = 1;
    private void writeObject(ObjectOutputStream s)
      throws java.io.IOException
    {
      s.defaultWriteObject();
      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      s.writeObject(null);
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      GraphicsEnvironment.checkHeadless();
      s.defaultReadObject();
      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
        String key = ((String)keyOrNull).intern();
        if (itemListenerK == key)
          addItemListener((ItemListener)(s.readObject()));
        else 
          s.readObject();
      }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTChoice();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTChoice extends AccessibleAWTComponent
        implements AccessibleAction
    {
        private static final long serialVersionUID = 7175603582428509322L;
        public AccessibleAWTChoice() {
            super();
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COMBO_BOX;
        }
        public int getAccessibleActionCount() {
            return 0;  
        }
        public String getAccessibleActionDescription(int i) {
            return null;  
        }
        public boolean doAccessibleAction(int i) {
            return false;  
        }
    } 
}
