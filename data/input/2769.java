class DefaultSwatchChooserPanel extends AbstractColorChooserPanel {
    SwatchPanel swatchPanel;
    RecentSwatchPanel recentSwatchPanel;
    MouseListener mainSwatchListener;
    MouseListener recentSwatchListener;
    public DefaultSwatchChooserPanel() {
        super();
        setInheritsPopupMenu(true);
    }
    public String getDisplayName() {
        return UIManager.getString("ColorChooser.swatchesNameText", getLocale());
    }
    public int getMnemonic() {
        return getInt("ColorChooser.swatchesMnemonic", -1);
    }
    public int getDisplayedMnemonicIndex() {
        return getInt("ColorChooser.swatchesDisplayedMnemonicIndex", -1);
    }
    public Icon getSmallDisplayIcon() {
        return null;
    }
    public Icon getLargeDisplayIcon() {
        return null;
    }
    public void installChooserPanel(JColorChooser enclosingChooser) {
        super.installChooserPanel(enclosingChooser);
    }
    protected void buildChooser() {
        String recentStr = UIManager.getString("ColorChooser.swatchesRecentText", getLocale());
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel superHolder = new JPanel(gb);
        swatchPanel =  new MainSwatchPanel();
        swatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                      getDisplayName());
        swatchPanel.setInheritsPopupMenu(true);
        recentSwatchPanel = new RecentSwatchPanel();
        recentSwatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                            recentStr);
        mainSwatchListener = new MainSwatchListener();
        swatchPanel.addMouseListener(mainSwatchListener);
        recentSwatchListener = new RecentSwatchListener();
        recentSwatchPanel.addMouseListener(recentSwatchListener);
        JPanel mainHolder = new JPanel(new BorderLayout());
        Border border = new CompoundBorder( new LineBorder(Color.black),
                                            new LineBorder(Color.white) );
        mainHolder.setBorder(border);
        mainHolder.add(swatchPanel, BorderLayout.CENTER);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        Insets oldInsets = gbc.insets;
        gbc.insets = new Insets(0, 0, 0, 10);
        superHolder.add(mainHolder, gbc);
        gbc.insets = oldInsets;
        recentSwatchPanel.setInheritsPopupMenu(true);
        JPanel recentHolder = new JPanel( new BorderLayout() );
        recentHolder.setBorder(border);
        recentHolder.setInheritsPopupMenu(true);
        recentHolder.add(recentSwatchPanel, BorderLayout.CENTER);
        JLabel l = new JLabel(recentStr);
        l.setLabelFor(recentSwatchPanel);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.weighty = 1.0;
        superHolder.add(l, gbc);
        gbc.weighty = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, 0, 0, 2);
        superHolder.add(recentHolder, gbc);
        superHolder.setInheritsPopupMenu(true);
        add(superHolder);
    }
    public void uninstallChooserPanel(JColorChooser enclosingChooser) {
        super.uninstallChooserPanel(enclosingChooser);
        swatchPanel.removeMouseListener(mainSwatchListener);
        recentSwatchPanel.removeMouseListener(recentSwatchListener);
        swatchPanel = null;
        recentSwatchPanel = null;
        mainSwatchListener = null;
        recentSwatchListener = null;
        removeAll();  
    }
    public void updateChooser() {
    }
    class RecentSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                Color color = recentSwatchPanel.getColorForLocation(e.getX(), e.getY());
                setSelectedColor(color);
            }
        }
    }
    class MainSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                Color color = swatchPanel.getColorForLocation(e.getX(), e.getY());
                setSelectedColor(color);
                recentSwatchPanel.setMostRecentColor(color);
            }
        }
    }
}
class SwatchPanel extends JPanel {
    protected Color[] colors;
    protected Dimension swatchSize;
    protected Dimension numSwatches;
    protected Dimension gap;
    public SwatchPanel() {
        initValues();
        initColors();
        setToolTipText(""); 
        setOpaque(true);
        setBackground(Color.white);
        setRequestFocusEnabled(false);
        setInheritsPopupMenu(true);
    }
    public boolean isFocusTraversable() {
        return false;
    }
    protected void initValues() {
    }
    public void paintComponent(Graphics g) {
         g.setColor(getBackground());
         g.fillRect(0,0,getWidth(), getHeight());
         for (int row = 0; row < numSwatches.height; row++) {
            int y = row * (swatchSize.height + gap.height);
            for (int column = 0; column < numSwatches.width; column++) {
              g.setColor( getColorForCell(column, row) );
                int x;
                if ((!this.getComponentOrientation().isLeftToRight()) &&
                    (this instanceof RecentSwatchPanel)) {
                    x = (numSwatches.width - column - 1) * (swatchSize.width + gap.width);
                } else {
                    x = column * (swatchSize.width + gap.width);
                }
                g.fillRect( x, y, swatchSize.width, swatchSize.height);
                g.setColor(Color.black);
                g.drawLine( x+swatchSize.width-1, y, x+swatchSize.width-1, y+swatchSize.height-1);
                g.drawLine( x, y+swatchSize.height-1, x+swatchSize.width-1, y+swatchSize.height-1);
            }
         }
    }
    public Dimension getPreferredSize() {
        int x = numSwatches.width * (swatchSize.width + gap.width) - 1;
        int y = numSwatches.height * (swatchSize.height + gap.height) - 1;
        return new Dimension( x, y );
    }
    protected void initColors() {
    }
    public String getToolTipText(MouseEvent e) {
        Color color = getColorForLocation(e.getX(), e.getY());
        return color.getRed()+", "+ color.getGreen() + ", " + color.getBlue();
    }
    public Color getColorForLocation( int x, int y ) {
        int column;
        if ((!this.getComponentOrientation().isLeftToRight()) &&
            (this instanceof RecentSwatchPanel)) {
            column = numSwatches.width - x / (swatchSize.width + gap.width) - 1;
        } else {
            column = x / (swatchSize.width + gap.width);
        }
        int row = y / (swatchSize.height + gap.height);
        return getColorForCell(column, row);
    }
    private Color getColorForCell( int column, int row) {
        return colors[ (row * numSwatches.width) + column ]; 
    }
}
class RecentSwatchPanel extends SwatchPanel {
    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize", getLocale());
        numSwatches = new Dimension( 5, 7 );
        gap = new Dimension(1, 1);
    }
    protected void initColors() {
        Color defaultRecentColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor", getLocale());
        int numColors = numSwatches.width * numSwatches.height;
        colors = new Color[numColors];
        for (int i = 0; i < numColors ; i++) {
            colors[i] = defaultRecentColor;
        }
    }
    public void setMostRecentColor(Color c) {
        System.arraycopy( colors, 0, colors, 1, colors.length-1);
        colors[0] = c;
        repaint();
    }
}
class MainSwatchPanel extends SwatchPanel {
    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize", getLocale());
        numSwatches = new Dimension( 31, 9 );
        gap = new Dimension(1, 1);
    }
    protected void initColors() {
        int[] rawValues = initRawValues();
        int numColors = rawValues.length / 3;
        colors = new Color[numColors];
        for (int i = 0; i < numColors ; i++) {
            colors[i] = new Color( rawValues[(i*3)], rawValues[(i*3)+1], rawValues[(i*3)+2] );
        }
    }
    private int[] initRawValues() {
        int[] rawValues = {
255, 255, 255, 
204, 255, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
255, 204, 255,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 204, 204,  
153, 255, 255,
153, 204, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
204, 153, 255,
255, 153, 255,
255, 153, 204,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 204, 153,
255, 255, 153,
204, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 204,
204, 204, 204,  
102, 255, 255,
102, 204, 255,
102, 153, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
153, 102, 255,
204, 102, 255,
255, 102, 255,
255, 102, 204,
255, 102, 153,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 153, 102,
255, 204, 102,
255, 255, 102,
204, 255, 102,
153, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 153,
102, 255, 204,
153, 153, 153, 
51, 255, 255,
51, 204, 255,
51, 153, 255,
51, 102, 255,
51, 51, 255,
51, 51, 255,
51, 51, 255,
102, 51, 255,
153, 51, 255,
204, 51, 255,
255, 51, 255,
255, 51, 204,
255, 51, 153,
255, 51, 102,
255, 51, 51,
255, 51, 51,
255, 51, 51,
255, 102, 51,
255, 153, 51,
255, 204, 51,
255, 255, 51,
204, 255, 51,
153, 255, 51,
102, 255, 51,
51, 255, 51,
51, 255, 51,
51, 255, 51,
51, 255, 102,
51, 255, 153,
51, 255, 204,
153, 153, 153, 
0, 255, 255,
0, 204, 255,
0, 153, 255,
0, 102, 255,
0, 51, 255,
0, 0, 255,
51, 0, 255,
102, 0, 255,
153, 0, 255,
204, 0, 255,
255, 0, 255,
255, 0, 204,
255, 0, 153,
255, 0, 102,
255, 0, 51,
255, 0 , 0,
255, 51, 0,
255, 102, 0,
255, 153, 0,
255, 204, 0,
255, 255, 0,
204, 255, 0,
153, 255, 0,
102, 255, 0,
51, 255, 0,
0, 255, 0,
0, 255, 51,
0, 255, 102,
0, 255, 153,
0, 255, 204,
102, 102, 102, 
0, 204, 204,
0, 204, 204,
0, 153, 204,
0, 102, 204,
0, 51, 204,
0, 0, 204,
51, 0, 204,
102, 0, 204,
153, 0, 204,
204, 0, 204,
204, 0, 204,
204, 0, 204,
204, 0, 153,
204, 0, 102,
204, 0, 51,
204, 0, 0,
204, 51, 0,
204, 102, 0,
204, 153, 0,
204, 204, 0,
204, 204, 0,
204, 204, 0,
153, 204, 0,
102, 204, 0,
51, 204, 0,
0, 204, 0,
0, 204, 51,
0, 204, 102,
0, 204, 153,
0, 204, 204,
102, 102, 102, 
0, 153, 153,
0, 153, 153,
0, 153, 153,
0, 102, 153,
0, 51, 153,
0, 0, 153,
51, 0, 153,
102, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 102,
153, 0, 51,
153, 0, 0,
153, 51, 0,
153, 102, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
102, 153, 0,
51, 153, 0,
0, 153, 0,
0, 153, 51,
0, 153, 102,
0, 153, 153,
0, 153, 153,
51, 51, 51, 
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 51, 102,
0, 0, 102,
51, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 51,
102, 0, 0,
102, 51, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
51, 102, 0,
0, 102, 0,
0, 102, 51,
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 0, 0, 
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
0, 51, 0,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
51, 51, 51 };
        return rawValues;
    }
}
