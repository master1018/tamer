public abstract class MenuComponent implements Serializable {
    private static final long serialVersionUID = -4536902356223894379L;
    private String name;
    private Font font;
    MenuContainer parent;
    boolean deprecatedEventHandler = true;
    private int selectedItemIndex;
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    public MenuComponent() throws HeadlessException {
        toolkit.lockAWT();
        try {
            Toolkit.checkHeadless();
            name = autoName();
            selectedItemIndex = -1;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public String getName() {
        toolkit.lockAWT();
        try {
            return name;
        } finally {
            toolkit.unlockAWT();
        }
    }
    @Override
    public String toString() {
        toolkit.lockAWT();
        try {
            return getClass().getName() + "[" + paramString() + "]"; 
        } finally {
            toolkit.unlockAWT();
        }
    }
    public MenuContainer getParent() {
        toolkit.lockAWT();
        try {
            return parent;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setName(String name) {
        toolkit.lockAWT();
        try {
            this.name = name;
        } finally {
            toolkit.unlockAWT();
        }
    }
    public final void dispatchEvent(AWTEvent event) {
        toolkit.lockAWT();
        try {
            processEvent(event);
            if (deprecatedEventHandler) {
                postDeprecatedEvent(event);
            }
        } finally {
            toolkit.unlockAWT();
        }
    }
    void postDeprecatedEvent(AWTEvent event) {
        Event evt = event.getEvent();
        if (evt != null) {
            postEvent(evt);
        }
    }
    @Deprecated
    public MenuComponentPeer getPeer() throws org.apache.harmony.luni.util.NotImplementedException {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
        if (true) {
            throw new RuntimeException("Method is not implemented"); 
        }
        return null;
    }
    protected final Object getTreeLock() {
        return toolkit.awtTreeLock;
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    public boolean postEvent(Event e) {
        toolkit.lockAWT();
        try {
            if (parent != null) {
                return parent.postEvent(e);
            }
            return false;
        } finally {
            toolkit.unlockAWT();
        }
    }
    protected String paramString() {
        toolkit.lockAWT();
        try {
            return getName();
        } finally {
            toolkit.unlockAWT();
        }
    }
    public Font getFont() {
        toolkit.lockAWT();
        try {
            if (font == null && hasDefaultFont()) {
                return toolkit.getDefaultFont();
            }
            if (font == null && parent != null) {
                return parent.getFont();
            }
            return font;
        } finally {
            toolkit.unlockAWT();
        }
    }
    boolean isFontSet() {
        return font != null
                || ((parent instanceof MenuComponent) && ((MenuComponent)parent).isFontSet());
    }
    boolean hasDefaultFont() {
        return false;
    }
    protected void processEvent(AWTEvent event) {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void removeNotify() {
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }
    public void setFont(Font font) {
        toolkit.lockAWT();
        try {
            this.font = font;
        } finally {
            toolkit.unlockAWT();
        }
    }
    void setParent(MenuContainer parent) {
        this.parent = parent;
    }
    Point getLocation() {
        return new Point(0, 0);
    }
    int getWidth() {
        return 1;
    }
    int getHeight() {
        return 1;
    }
    void paint(Graphics gr) {
        gr.setColor(Color.LIGHT_GRAY);
        gr.fillRect(0, 0, getWidth(), getHeight());
        gr.setColor(Color.BLACK);
    }
    void onMouseEvent(int eventId, Point where, int mouseButton, long when, int modifiers) {
    }
    void onKeyEvent(int eventId, int vKey, long when, int modifiers) {
    }
    Rectangle getItemRect(int index) {
        return null;
    }
    final MultiRectArea getUpdateClip(int index1, int index2) {
        MultiRectArea clip = new MultiRectArea();
        if (index1 >= 0) {
            clip.add(getItemRect(index1));
        }
        if (index2 >= 0) {
            clip.add(getItemRect(index2));
        }
        return clip;
    }
    Point getSubmenuLocation(int index) {
        return new Point(0, 0);
    }
    int getSelectedItemIndex() {
        return selectedItemIndex;
    }
    void hide() {
        selectedItemIndex = -1;
        if (parent instanceof MenuComponent) {
            ((MenuComponent)parent).itemHidden(this);
        }
    }
    void itemHidden(MenuComponent mc) {
    }
    boolean isVisible() {
        return true;
    }
    boolean isActive() {
        return true;
    }
    void endMenu() {
    }
    void itemSelected(long when, int modifiers) {
        endMenu();
    }
    String autoName() {
        String name = getClass().getName();
        if (name.indexOf("$") != -1) { 
            return null;
        }
        int number = 0;
        name = name.substring(name.lastIndexOf(".") + 1) + Integer.toString(number); 
        return name;
    }
    Graphics getGraphics(MultiRectArea clip) {
        return null;
    }
}
