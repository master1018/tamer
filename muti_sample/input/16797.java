public class Label extends Component implements Accessible {
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    public static final int LEFT        = 0;
    public static final int CENTER      = 1;
    public static final int RIGHT       = 2;
    String text;
    int    alignment = LEFT;
    private static final String base = "label";
    private static int nameCounter = 0;
     private static final long serialVersionUID = 3094126758329070636L;
    public Label() throws HeadlessException {
        this("", LEFT);
    }
    public Label(String text) throws HeadlessException {
        this(text, LEFT);
    }
    public Label(String text, int alignment) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.text = text;
        setAlignment(alignment);
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
    }
    String constructComponentName() {
        synchronized (Label.class) {
            return base + nameCounter++;
        }
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createLabel(this);
            super.addNotify();
        }
    }
    public int getAlignment() {
        return alignment;
    }
    public synchronized void setAlignment(int alignment) {
        switch (alignment) {
          case LEFT:
          case CENTER:
          case RIGHT:
            this.alignment = alignment;
            LabelPeer peer = (LabelPeer)this.peer;
            if (peer != null) {
                peer.setAlignment(alignment);
            }
            return;
        }
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        boolean testvalid = false;
        synchronized (this) {
            if (text != this.text && (this.text == null ||
                                      !this.text.equals(text))) {
                this.text = text;
                LabelPeer peer = (LabelPeer)this.peer;
                if (peer != null) {
                    peer.setText(text);
                }
                testvalid = true;
            }
        }
        if (testvalid) {
            invalidateIfValid();
        }
    }
    protected String paramString() {
        String str = ",align=";
        switch (alignment) {
          case LEFT:   str += "left"; break;
          case CENTER: str += "center"; break;
          case RIGHT:  str += "right"; break;
        }
        return super.paramString() + str + ",text=" + text;
    }
    private static native void initIDs();
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTLabel();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTLabel extends AccessibleAWTComponent
    {
        private static final long serialVersionUID = -3568967560160480438L;
        public AccessibleAWTLabel() {
            super();
        }
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getText() == null) {
                    return super.getAccessibleName();
                } else {
                    return getText();
                }
            }
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LABEL;
        }
    } 
}
