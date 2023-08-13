public class Button extends Component implements Accessible {
    String label;
    String actionCommand;
    transient ActionListener actionListener;
    private static final String base = "button";
    private static int nameCounter = 0;
    private static final long serialVersionUID = -8774683716313001058L;
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    private static native void initIDs();
    public Button() throws HeadlessException {
        this("");
    }
    public Button(String label) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.label = label;
    }
    String constructComponentName() {
        synchronized (Button.class) {
            return base + nameCounter++;
        }
    }
    public void addNotify() {
        synchronized(getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createButton(this);
            super.addNotify();
        }
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        boolean testvalid = false;
        synchronized (this) {
            if (label != this.label && (this.label == null ||
                                        !this.label.equals(label))) {
                this.label = label;
                ButtonPeer peer = (ButtonPeer)this.peer;
                if (peer != null) {
                    peer.setLabel(label);
                }
                testvalid = true;
            }
        }
        if (testvalid) {
            invalidateIfValid();
        }
    }
    public void setActionCommand(String command) {
        actionCommand = command;
    }
    public String getActionCommand() {
        return (actionCommand == null? label : actionCommand);
    }
    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }
    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }
    public synchronized ActionListener[] getActionListeners() {
        return (ActionListener[]) (getListeners(ActionListener.class));
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ActionEvent.ACTION_PERFORMED) {
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }
    protected void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }
    protected String paramString() {
        return super.paramString() + ",label=" + label;
    }
    private int buttonSerializedDataVersion = 1;
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      s.defaultWriteObject();
      AWTEventMulticaster.save(s, actionListenerK, actionListener);
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
        if (actionListenerK == key)
          addActionListener((ActionListener)(s.readObject()));
        else 
          s.readObject();
      }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTButton();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTButton extends AccessibleAWTComponent
        implements AccessibleAction, AccessibleValue
    {
        private static final long serialVersionUID = -5932203980244017102L;
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getLabel() == null) {
                    return super.getAccessibleName();
                } else {
                    return getLabel();
                }
            }
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public AccessibleValue getAccessibleValue() {
            return this;
        }
        public int getAccessibleActionCount() {
            return 1;
        }
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return "click";
            } else {
                return null;
            }
        }
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                Toolkit.getEventQueue().postEvent(
                        new ActionEvent(Button.this,
                                        ActionEvent.ACTION_PERFORMED,
                                        Button.this.getActionCommand()));
                return true;
            } else {
                return false;
            }
        }
        public Number getCurrentAccessibleValue() {
            return Integer.valueOf(0);
        }
        public boolean setCurrentAccessibleValue(Number n) {
            return false;
        }
        public Number getMinimumAccessibleValue() {
            return Integer.valueOf(0);
        }
        public Number getMaximumAccessibleValue() {
            return Integer.valueOf(0);
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
    } 
}
