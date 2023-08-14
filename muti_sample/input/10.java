public class JToolBar extends JComponent implements SwingConstants, Accessible
{
    private static final String uiClassID = "ToolBarUI";
    private    boolean   paintBorder              = true;
    private    Insets    margin                   = null;
    private    boolean   floatable                = true;
    private    int       orientation              = HORIZONTAL;
    public JToolBar()
    {
        this( HORIZONTAL );
    }
    public JToolBar( int orientation )
    {
        this(null, orientation);
    }
    public JToolBar( String name ) {
        this(name, HORIZONTAL);
    }
    public JToolBar( String name , int orientation) {
        setName(name);
        checkOrientation( orientation );
        this.orientation = orientation;
        DefaultToolBarLayout layout =  new DefaultToolBarLayout( orientation );
        setLayout( layout );
        addPropertyChangeListener( layout );
        updateUI();
    }
    public ToolBarUI getUI() {
        return (ToolBarUI)ui;
    }
    public void setUI(ToolBarUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((ToolBarUI)UIManager.getUI(this));
        if (getLayout() == null) {
            setLayout(new DefaultToolBarLayout(getOrientation()));
        }
        invalidate();
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public int getComponentIndex(Component c) {
        int ncomponents = this.getComponentCount();
        Component[] component = this.getComponents();
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = component[i];
            if (comp == c)
                return i;
        }
        return -1;
    }
    public Component getComponentAtIndex(int i) {
        int ncomponents = this.getComponentCount();
        if ( i >= 0 && i < ncomponents) {
            Component[] component = this.getComponents();
            return component[i];
        }
        return null;
    }
     public void setMargin(Insets m)
     {
         Insets old = margin;
         margin = m;
         firePropertyChange("margin", old, m);
         revalidate();
         repaint();
     }
     public Insets getMargin()
     {
         if(margin == null) {
             return new Insets(0,0,0,0);
         } else {
             return margin;
         }
     }
     public boolean isBorderPainted()
     {
         return paintBorder;
     }
     public void setBorderPainted(boolean b)
     {
         if ( paintBorder != b )
         {
             boolean old = paintBorder;
             paintBorder = b;
             firePropertyChange("borderPainted", old, b);
             revalidate();
             repaint();
         }
     }
     protected void paintBorder(Graphics g)
     {
         if (isBorderPainted())
         {
             super.paintBorder(g);
         }
     }
    public boolean isFloatable()
    {
        return floatable;
    }
    public void setFloatable( boolean b )
    {
        if ( floatable != b )
        {
            boolean old = floatable;
            floatable = b;
            firePropertyChange("floatable", old, b);
            revalidate();
            repaint();
        }
    }
    public int getOrientation()
    {
        return this.orientation;
    }
    public void setOrientation( int o )
    {
        checkOrientation( o );
        if ( orientation != o )
        {
            int old = orientation;
            orientation = o;
            firePropertyChange("orientation", old, o);
            revalidate();
            repaint();
        }
    }
    public void setRollover(boolean rollover) {
        putClientProperty("JToolBar.isRollover",
                          rollover ? Boolean.TRUE : Boolean.FALSE);
    }
    public boolean isRollover() {
        Boolean rollover = (Boolean)getClientProperty("JToolBar.isRollover");
        if (rollover != null) {
            return rollover.booleanValue();
        }
        return false;
    }
    private void checkOrientation( int orientation )
    {
        switch ( orientation )
        {
            case VERTICAL:
            case HORIZONTAL:
                break;
            default:
                throw new IllegalArgumentException( "orientation must be one of: VERTICAL, HORIZONTAL" );
        }
    }
    public void addSeparator()
    {
        addSeparator(null);
    }
    public void addSeparator( Dimension size )
    {
        JToolBar.Separator s = new JToolBar.Separator( size );
        add(s);
    }
    public JButton add(Action a) {
        JButton b = createActionComponent(a);
        b.setAction(a);
        add(b);
        return b;
    }
    protected JButton createActionComponent(Action a) {
        JButton b = new JButton() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = createActionChangeListener(this);
                if (pcl==null) {
                    pcl = super.createActionPropertyChangeListener(a);
                }
                return pcl;
            }
        };
        if (a != null && (a.getValue(Action.SMALL_ICON) != null ||
                          a.getValue(Action.LARGE_ICON_KEY) != null)) {
            b.setHideActionText(true);
        }
        b.setHorizontalTextPosition(JButton.CENTER);
        b.setVerticalTextPosition(JButton.BOTTOM);
        return b;
    }
    protected PropertyChangeListener createActionChangeListener(JButton b) {
        return null;
    }
    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof Separator) {
            if (getOrientation() == VERTICAL) {
                ( (Separator)comp ).setOrientation(JSeparator.HORIZONTAL);
            } else {
                ( (Separator)comp ).setOrientation(JSeparator.VERTICAL);
            }
        }
        super.addImpl(comp, constraints, index);
        if (comp instanceof JButton) {
            ((JButton)comp).setDefaultCapable(false);
        }
    }
    static public class Separator extends JSeparator
    {
        private Dimension separatorSize;
        public Separator()
        {
            this( null );  
        }
        public Separator( Dimension size )
        {
            super( JSeparator.HORIZONTAL );
            setSeparatorSize(size);
        }
        public String getUIClassID()
        {
            return "ToolBarSeparatorUI";
        }
        public void setSeparatorSize( Dimension size )
        {
            if (size != null) {
                separatorSize = size;
            } else {
                super.updateUI();
            }
            this.invalidate();
        }
        public Dimension getSeparatorSize()
        {
            return separatorSize;
        }
        public Dimension getMinimumSize()
        {
            if (separatorSize != null) {
                return separatorSize.getSize();
            } else {
                return super.getMinimumSize();
            }
        }
        public Dimension getMaximumSize()
        {
            if (separatorSize != null) {
                return separatorSize.getSize();
            } else {
                return super.getMaximumSize();
            }
        }
        public Dimension getPreferredSize()
        {
            if (separatorSize != null) {
                return separatorSize.getSize();
            } else {
                return super.getPreferredSize();
            }
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    protected String paramString() {
        String paintBorderString = (paintBorder ?
                                    "true" : "false");
        String marginString = (margin != null ?
                               margin.toString() : "");
        String floatableString = (floatable ?
                                  "true" : "false");
        String orientationString = (orientation == HORIZONTAL ?
                                    "HORIZONTAL" : "VERTICAL");
        return super.paramString() +
        ",floatable=" + floatableString +
        ",margin=" + marginString +
        ",orientation=" + orientationString +
        ",paintBorder=" + paintBorderString;
    }
    private class DefaultToolBarLayout
        implements LayoutManager2, Serializable, PropertyChangeListener, UIResource {
        BoxLayout lm;
        DefaultToolBarLayout(int orientation) {
            if (orientation == JToolBar.VERTICAL) {
                lm = new BoxLayout(JToolBar.this, BoxLayout.PAGE_AXIS);
            } else {
                lm = new BoxLayout(JToolBar.this, BoxLayout.LINE_AXIS);
            }
        }
        public void addLayoutComponent(String name, Component comp) {
            lm.addLayoutComponent(name, comp);
        }
        public void addLayoutComponent(Component comp, Object constraints) {
            lm.addLayoutComponent(comp, constraints);
        }
        public void removeLayoutComponent(Component comp) {
            lm.removeLayoutComponent(comp);
        }
        public Dimension preferredLayoutSize(Container target) {
            return lm.preferredLayoutSize(target);
        }
        public Dimension minimumLayoutSize(Container target) {
            return lm.minimumLayoutSize(target);
        }
        public Dimension maximumLayoutSize(Container target) {
            return lm.maximumLayoutSize(target);
        }
        public void layoutContainer(Container target) {
            lm.layoutContainer(target);
        }
        public float getLayoutAlignmentX(Container target) {
            return lm.getLayoutAlignmentX(target);
        }
        public float getLayoutAlignmentY(Container target) {
            return lm.getLayoutAlignmentY(target);
        }
        public void invalidateLayout(Container target) {
            lm.invalidateLayout(target);
        }
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if( name.equals("orientation") ) {
                int o = ((Integer)e.getNewValue()).intValue();
                if (o == JToolBar.VERTICAL)
                    lm = new BoxLayout(JToolBar.this, BoxLayout.PAGE_AXIS);
                else {
                    lm = new BoxLayout(JToolBar.this, BoxLayout.LINE_AXIS);
                }
            }
        }
    }
    public void setLayout(LayoutManager mgr) {
        LayoutManager oldMgr = getLayout();
        if (oldMgr instanceof PropertyChangeListener) {
            removePropertyChangeListener((PropertyChangeListener)oldMgr);
        }
        super.setLayout(mgr);
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJToolBar();
        }
        return accessibleContext;
    }
    protected class AccessibleJToolBar extends AccessibleJComponent {
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            return states;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOOL_BAR;
        }
    } 
}
