public class WindowsInternalFrameTitlePane extends BasicInternalFrameTitlePane {
    private Color selectedTitleGradientColor;
    private Color notSelectedTitleGradientColor;
    private JPopupMenu systemPopupMenu;
    private JLabel systemLabel;
    private Font titleFont;
    private int titlePaneHeight;
    private int buttonWidth, buttonHeight;
    private boolean hotTrackingOn;
    public WindowsInternalFrameTitlePane(JInternalFrame f) {
        super(f);
    }
    protected void addSubComponents() {
        add(systemLabel);
        add(iconButton);
        add(maxButton);
        add(closeButton);
    }
    protected void installDefaults() {
        super.installDefaults();
        titlePaneHeight = UIManager.getInt("InternalFrame.titlePaneHeight");
        buttonWidth     = UIManager.getInt("InternalFrame.titleButtonWidth")  - 4;
        buttonHeight    = UIManager.getInt("InternalFrame.titleButtonHeight") - 4;
        Object obj      = UIManager.get("InternalFrame.titleButtonToolTipsOn");
        hotTrackingOn = (obj instanceof Boolean) ? (Boolean)obj : true;
        if (XPStyle.getXP() != null) {
            buttonWidth = buttonHeight;
            Dimension d = XPStyle.getPartSize(Part.WP_CLOSEBUTTON, State.NORMAL);
            if (d != null && d.width != 0 && d.height != 0) {
                buttonWidth = (int) ((float) buttonWidth * d.width / d.height);
            }
        } else {
            buttonWidth += 2;
            selectedTitleGradientColor =
                    UIManager.getColor("InternalFrame.activeTitleGradient");
            notSelectedTitleGradientColor =
                    UIManager.getColor("InternalFrame.inactiveTitleGradient");
            Color activeBorderColor =
                    UIManager.getColor("InternalFrame.activeBorderColor");
            setBorder(BorderFactory.createLineBorder(activeBorderColor, 1));
        }
    }
    protected void uninstallListeners() {
        super.uninstallListeners();
    }
    protected void createButtons() {
        super.createButtons();
        if (XPStyle.getXP() != null) {
            iconButton.setContentAreaFilled(false);
            maxButton.setContentAreaFilled(false);
            closeButton.setContentAreaFilled(false);
        }
    }
    protected void setButtonIcons() {
        super.setButtonIcons();
        if (!hotTrackingOn) {
            iconButton.setToolTipText(null);
            maxButton.setToolTipText(null);
            closeButton.setToolTipText(null);
        }
    }
    public void paintComponent(Graphics g)  {
        XPStyle xp = XPStyle.getXP();
        paintTitleBackground(g);
        String title = frame.getTitle();
        if (title != null) {
            boolean isSelected = frame.isSelected();
            Font oldFont = g.getFont();
            Font newFont = (titleFont != null) ? titleFont : getFont();
            g.setFont(newFont);
            FontMetrics fm = SwingUtilities2.getFontMetrics(frame, g, newFont);
            int baseline = (getHeight() + fm.getAscent() - fm.getLeading() -
                    fm.getDescent()) / 2;
            Rectangle lastIconBounds = new Rectangle(0, 0, 0, 0);
            if (frame.isIconifiable()) {
                lastIconBounds = iconButton.getBounds();
            } else if (frame.isMaximizable()) {
                lastIconBounds = maxButton.getBounds();
            } else if (frame.isClosable()) {
                lastIconBounds = closeButton.getBounds();
            }
            int titleX;
            int titleW;
            int gap = 2;
            if (WindowsGraphicsUtils.isLeftToRight(frame)) {
                if (lastIconBounds.x == 0) { 
                    lastIconBounds.x = frame.getWidth() - frame.getInsets().right;
                }
                titleX = systemLabel.getX() + systemLabel.getWidth() + gap;
                if (xp != null) {
                    titleX += 2;
                }
                titleW = lastIconBounds.x - titleX - gap;
            } else {
                if (lastIconBounds.x == 0) { 
                    lastIconBounds.x = frame.getInsets().left;
                }
                titleW = SwingUtilities2.stringWidth(frame, fm, title);
                int minTitleX = lastIconBounds.x + lastIconBounds.width + gap;
                if (xp != null) {
                    minTitleX += 2;
                }
                int availableWidth = systemLabel.getX() - gap - minTitleX;
                if (availableWidth > titleW) {
                    titleX = systemLabel.getX() - gap - titleW;
                } else {
                    titleX = minTitleX;
                    titleW = availableWidth;
                }
            }
            title = getTitle(frame.getTitle(), fm, titleW);
            if (xp != null) {
                String shadowType = null;
                if (isSelected) {
                    shadowType = xp.getString(this, Part.WP_CAPTION,
                                              State.ACTIVE, Prop.TEXTSHADOWTYPE);
                }
                if ("single".equalsIgnoreCase(shadowType)) {
                    Point shadowOffset = xp.getPoint(this, Part.WP_WINDOW, State.ACTIVE,
                                                     Prop.TEXTSHADOWOFFSET);
                    Color shadowColor  = xp.getColor(this, Part.WP_WINDOW, State.ACTIVE,
                                                     Prop.TEXTSHADOWCOLOR, null);
                    if (shadowOffset != null && shadowColor != null) {
                        g.setColor(shadowColor);
                        SwingUtilities2.drawString(frame, g, title,
                                     titleX + shadowOffset.x,
                                     baseline + shadowOffset.y);
                    }
                }
            }
            g.setColor(isSelected ? selectedTextColor : notSelectedTextColor);
            SwingUtilities2.drawString(frame, g, title, titleX, baseline);
            g.setFont(oldFont);
        }
    }
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    public Dimension getMinimumSize() {
        Dimension d = new Dimension(super.getMinimumSize());
        d.height = titlePaneHeight + 2;
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            if (frame.isMaximum()) {
                d.height -= 1;
            } else {
                d.height += 3;
            }
        }
        return d;
    }
    protected void paintTitleBackground(Graphics g) {
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            Part part = frame.isIcon() ? Part.WP_MINCAPTION
                                       : (frame.isMaximum() ? Part.WP_MAXCAPTION
                                                            : Part.WP_CAPTION);
            State state = frame.isSelected() ? State.ACTIVE : State.INACTIVE;
            Skin skin = xp.getSkin(this, part);
            skin.paintSkin(g, 0,  0, getWidth(), getHeight(), state);
        } else {
            Boolean gradientsOn = (Boolean)LookAndFeel.getDesktopPropertyValue(
                "win.frame.captionGradientsOn", Boolean.valueOf(false));
            if (gradientsOn.booleanValue() && g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D)g;
                Paint savePaint = g2.getPaint();
                boolean isSelected = frame.isSelected();
                int w = getWidth();
                if (isSelected) {
                    GradientPaint titleGradient = new GradientPaint(0,0,
                            selectedTitleColor,
                            (int)(w*.75),0,
                            selectedTitleGradientColor);
                    g2.setPaint(titleGradient);
                } else {
                    GradientPaint titleGradient = new GradientPaint(0,0,
                            notSelectedTitleColor,
                            (int)(w*.75),0,
                            notSelectedTitleGradientColor);
                    g2.setPaint(titleGradient);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(savePaint);
            } else {
                super.paintTitleBackground(g);
            }
        }
    }
    protected void assembleSystemMenu() {
        systemPopupMenu = new JPopupMenu();
        addSystemMenuItems(systemPopupMenu);
        enableActions();
        systemLabel = new JLabel(frame.getFrameIcon()) {
            protected void paintComponent(Graphics g) {
                int x = 0;
                int y = 0;
                int w = getWidth();
                int h = getHeight();
                g = g.create();  
                if (isOpaque()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, w, h);
                }
                Icon icon = getIcon();
                int iconWidth;
                int iconHeight;
                if (icon != null &&
                    (iconWidth = icon.getIconWidth()) > 0 &&
                    (iconHeight = icon.getIconHeight()) > 0) {
                    double drawScale;
                    if (iconWidth > iconHeight) {
                        y = (h - w*iconHeight/iconWidth) / 2;
                        drawScale = w / (double)iconWidth;
                    } else {
                        x = (w - h*iconWidth/iconHeight) / 2;
                        drawScale = h / (double)iconHeight;
                    }
                    ((Graphics2D)g).translate(x, y);
                    ((Graphics2D)g).scale(drawScale, drawScale);
                    icon.paintIcon(this, g, 0, 0);
                }
                g.dispose();
            }
        };
        systemLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && frame.isClosable() &&
                    !frame.isIcon()) {
                    systemPopupMenu.setVisible(false);
                    frame.doDefaultCloseAction();
                }
                else {
                    super.mouseClicked(e);
                }
            }
            public void mousePressed(MouseEvent e) {
                try {
                    frame.setSelected(true);
                } catch(PropertyVetoException pve) {
                }
                showSystemPopupMenu(e.getComponent());
            }
        });
    }
    protected void addSystemMenuItems(JPopupMenu menu) {
        JMenuItem mi = menu.add(restoreAction);
        mi.setMnemonic('R');
        mi = menu.add(moveAction);
        mi.setMnemonic('M');
        mi = menu.add(sizeAction);
        mi.setMnemonic('S');
        mi = menu.add(iconifyAction);
        mi.setMnemonic('n');
        mi = menu.add(maximizeAction);
        mi.setMnemonic('x');
        systemPopupMenu.add(new JSeparator());
        mi = menu.add(closeAction);
        mi.setMnemonic('C');
    }
    protected void showSystemMenu(){
        showSystemPopupMenu(systemLabel);
    }
    private void showSystemPopupMenu(Component invoker){
        Dimension dim = new Dimension();
        Border border = frame.getBorder();
        if (border != null) {
            dim.width += border.getBorderInsets(frame).left +
                border.getBorderInsets(frame).right;
            dim.height += border.getBorderInsets(frame).bottom +
                border.getBorderInsets(frame).top;
        }
        if (!frame.isIcon()) {
            systemPopupMenu.show(invoker,
                getX() - dim.width,
                getY() + getHeight() - dim.height);
        } else {
            systemPopupMenu.show(invoker,
                getX() - dim.width,
                getY() - systemPopupMenu.getPreferredSize().height -
                     dim.height);
        }
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return new WindowsPropertyChangeHandler();
    }
    protected LayoutManager createLayout() {
        return new WindowsTitlePaneLayout();
    }
    public class WindowsTitlePaneLayout extends BasicInternalFrameTitlePane.TitlePaneLayout {
        private Insets captionMargin = null;
        private Insets contentMargin = null;
        private XPStyle xp = XPStyle.getXP();
        WindowsTitlePaneLayout() {
            if (xp != null) {
                Component c = WindowsInternalFrameTitlePane.this;
                captionMargin = xp.getMargin(c, Part.WP_CAPTION, null, Prop.CAPTIONMARGINS);
                contentMargin = xp.getMargin(c, Part.WP_CAPTION, null, Prop.CONTENTMARGINS);
            }
            if (captionMargin == null) {
                captionMargin = new Insets(0, 2, 0, 2);
            }
            if (contentMargin == null) {
                contentMargin = new Insets(0, 0, 0, 0);
            }
        }
        private int layoutButton(JComponent button, Part part,
                                 int x, int y, int w, int h, int gap,
                                 boolean leftToRight) {
            if (!leftToRight) {
                x -= w;
            }
            button.setBounds(x, y, w, h);
            if (leftToRight) {
                x += w + 2;
            } else {
                x -= 2;
            }
            return x;
        }
        public void layoutContainer(Container c) {
            boolean leftToRight = WindowsGraphicsUtils.isLeftToRight(frame);
            int x, y;
            int w = getWidth();
            int h = getHeight();
            int iconSize = (xp != null) ? (h-2)*6/10 : h-4;
            if (xp != null) {
                x = (leftToRight) ? captionMargin.left + 2 : w - captionMargin.right - 2;
            } else {
                x = (leftToRight) ? captionMargin.left : w - captionMargin.right;
            }
            y = (h - iconSize) / 2;
            layoutButton(systemLabel, Part.WP_SYSBUTTON,
                         x, y, iconSize, iconSize, 0,
                         leftToRight);
            if (xp != null) {
                x = (leftToRight) ? w - captionMargin.right - 2 : captionMargin.left + 2;
                y = 1;  
                if (frame.isMaximum()) {
                    y += 1;
                } else {
                    y += 5;
                }
            } else {
                x = (leftToRight) ? w - captionMargin.right : captionMargin.left;
                y = (h - buttonHeight) / 2;
            }
            if(frame.isClosable()) {
                x = layoutButton(closeButton, Part.WP_CLOSEBUTTON,
                                 x, y, buttonWidth, buttonHeight, 2,
                                 !leftToRight);
            }
            if(frame.isMaximizable()) {
                x = layoutButton(maxButton, Part.WP_MAXBUTTON,
                                 x, y, buttonWidth, buttonHeight, (xp != null) ? 2 : 0,
                                 !leftToRight);
            }
            if(frame.isIconifiable()) {
                layoutButton(iconButton, Part.WP_MINBUTTON,
                             x, y, buttonWidth, buttonHeight, 0,
                             !leftToRight);
            }
        }
    } 
    public class WindowsPropertyChangeHandler extends PropertyChangeHandler {
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (JInternalFrame.FRAME_ICON_PROPERTY.equals(prop) &&
                    systemLabel != null) {
                systemLabel.setIcon(frame.getFrameIcon());
            }
            super.propertyChange(evt);
        }
    }
    public static class ScalableIconUIResource implements Icon, UIResource {
        private static final int SIZE = 16;
        private Icon[] icons;
        public ScalableIconUIResource(Object[] objects) {
            this.icons = new Icon[objects.length];
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof UIDefaults.LazyValue) {
                    icons[i] = (Icon)((UIDefaults.LazyValue)objects[i]).createValue(null);
                } else {
                    icons[i] = (Icon)objects[i];
                }
            }
        }
        protected Icon getBestIcon(int size) {
            if (icons != null && icons.length > 0) {
                int bestIndex = 0;
                int minDiff = Integer.MAX_VALUE;
                for (int i=0; i < icons.length; i++) {
                    Icon icon = icons[i];
                    int iconSize;
                    if (icon != null && (iconSize = icon.getIconWidth()) > 0) {
                        int diff = Math.abs(iconSize - size);
                        if (diff < minDiff) {
                            minDiff = diff;
                            bestIndex = i;
                        }
                    }
                }
                return icons[bestIndex];
            } else {
                return null;
            }
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            int size = getIconWidth();
            double scale = g2d.getTransform().getScaleX();
            Icon icon = getBestIcon((int)(size * scale));
            int iconSize;
            if (icon != null && (iconSize = icon.getIconWidth()) > 0) {
                double drawScale = size / (double)iconSize;
                g2d.translate(x, y);
                g2d.scale(drawScale, drawScale);
                icon.paintIcon(c, g2d, 0, 0);
            }
            g2d.dispose();
        }
        public int getIconWidth() {
            return SIZE;
        }
        public int getIconHeight() {
            return SIZE;
        }
    }
}
