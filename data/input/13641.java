public class SynthGraphicsUtils {
    private Rectangle paintIconR = new Rectangle();
    private Rectangle paintTextR = new Rectangle();
    private Rectangle paintViewR = new Rectangle();
    private Insets paintInsets = new Insets(0, 0, 0, 0);
    private Rectangle iconR = new Rectangle();
    private Rectangle textR = new Rectangle();
    private Rectangle viewR = new Rectangle();
    private Insets viewSizingInsets = new Insets(0, 0, 0, 0);
    public SynthGraphicsUtils() {
    }
    public void drawLine(SynthContext context, Object paintKey,
                         Graphics g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }
    public void drawLine(SynthContext context, Object paintKey,
                         Graphics g, int x1, int y1, int x2, int y2,
                         Object styleKey) {
        if ("dashed".equals(styleKey)) {
            if (x1 == x2) {
                y1 += (y1 % 2);
                for (int y = y1; y <= y2; y+=2) {
                    g.drawLine(x1, y, x2, y);
                }
            } else if (y1 == y2) {
                x1 += (x1 % 2);
                for (int x = x1; x <= x2; x+=2) {
                    g.drawLine(x, y1, x, y2);
                }
            }
        } else {
            drawLine(context, paintKey, g, x1, y1, x2, y2);
        }
    }
    public String layoutText(SynthContext ss, FontMetrics fm,
                         String text, Icon icon, int hAlign,
                         int vAlign, int hTextPosition,
                         int vTextPosition, Rectangle viewR,
                         Rectangle iconR, Rectangle textR, int iconTextGap) {
        if (icon instanceof SynthIcon) {
            SynthIconWrapper wrapper = SynthIconWrapper.get((SynthIcon)icon,
                                                            ss);
            String formattedText = SwingUtilities.layoutCompoundLabel(
                      ss.getComponent(), fm, text, wrapper, vAlign, hAlign,
                      vTextPosition, hTextPosition, viewR, iconR, textR,
                      iconTextGap);
            SynthIconWrapper.release(wrapper);
            return formattedText;
        }
        return SwingUtilities.layoutCompoundLabel(
                      ss.getComponent(), fm, text, icon, vAlign, hAlign,
                      vTextPosition, hTextPosition, viewR, iconR, textR,
                      iconTextGap);
    }
    public int computeStringWidth(SynthContext ss, Font font,
                                  FontMetrics metrics, String text) {
        return SwingUtilities2.stringWidth(ss.getComponent(), metrics,
                                          text);
    }
    public Dimension getMinimumSize(SynthContext ss, Font font, String text,
                      Icon icon, int hAlign, int vAlign, int hTextPosition,
                      int vTextPosition, int iconTextGap, int mnemonicIndex) {
        JComponent c = ss.getComponent();
        Dimension size = getPreferredSize(ss, font, text, icon, hAlign,
                                          vAlign, hTextPosition, vTextPosition,
                                          iconTextGap, mnemonicIndex);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            size.width -= v.getPreferredSpan(View.X_AXIS) -
                          v.getMinimumSpan(View.X_AXIS);
        }
        return size;
    }
    public Dimension getMaximumSize(SynthContext ss, Font font, String text,
                      Icon icon, int hAlign, int vAlign, int hTextPosition,
                      int vTextPosition, int iconTextGap, int mnemonicIndex) {
        JComponent c = ss.getComponent();
        Dimension size = getPreferredSize(ss, font, text, icon, hAlign,
                                          vAlign, hTextPosition, vTextPosition,
                                          iconTextGap, mnemonicIndex);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            size.width += v.getMaximumSpan(View.X_AXIS) -
                          v.getPreferredSpan(View.X_AXIS);
        }
        return size;
    }
    public int getMaximumCharHeight(SynthContext context) {
        FontMetrics fm = context.getComponent().getFontMetrics(
            context.getStyle().getFont(context));
        return (fm.getAscent() + fm.getDescent());
    }
    public Dimension getPreferredSize(SynthContext ss, Font font, String text,
                      Icon icon, int hAlign, int vAlign, int hTextPosition,
                      int vTextPosition, int iconTextGap, int mnemonicIndex) {
        JComponent c = ss.getComponent();
        Insets insets = c.getInsets(viewSizingInsets);
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        if (icon == null && (text == null || font == null)) {
            return new Dimension(dx, dy);
        }
        else if ((text == null) || ((icon != null) && (font == null))) {
            return new Dimension(SynthIcon.getIconWidth(icon, ss) + dx,
                                 SynthIcon.getIconHeight(icon, ss) + dy);
        }
        else {
            FontMetrics fm = c.getFontMetrics(font);
            iconR.x = iconR.y = iconR.width = iconR.height = 0;
            textR.x = textR.y = textR.width = textR.height = 0;
            viewR.x = dx;
            viewR.y = dy;
            viewR.width = viewR.height = Short.MAX_VALUE;
            layoutText(ss, fm, text, icon, hAlign, vAlign,
                   hTextPosition, vTextPosition, viewR, iconR, textR,
                   iconTextGap);
            int x1 = Math.min(iconR.x, textR.x);
            int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
            int y1 = Math.min(iconR.y, textR.y);
            int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
            Dimension rv = new Dimension(x2 - x1, y2 - y1);
            rv.width += dx;
            rv.height += dy;
            return rv;
        }
    }
    public void paintText(SynthContext ss, Graphics g, String text,
                          Rectangle bounds, int mnemonicIndex) {
        paintText(ss, g, text, bounds.x, bounds.y, mnemonicIndex);
    }
    public void paintText(SynthContext ss, Graphics g, String text,
                          int x, int y, int mnemonicIndex) {
        if (text != null) {
            JComponent c = ss.getComponent();
            FontMetrics fm = SwingUtilities2.getFontMetrics(c, g);
            y += fm.getAscent();
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text,
                                                      mnemonicIndex, x, y);
        }
    }
    public void paintText(SynthContext ss, Graphics g, String text,
                      Icon icon, int hAlign, int vAlign, int hTextPosition,
                      int vTextPosition, int iconTextGap, int mnemonicIndex,
                      int textOffset) {
        if ((icon == null) && (text == null)) {
            return;
        }
        JComponent c = ss.getComponent();
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g);
        Insets insets = SynthLookAndFeel.getPaintingInsets(ss, paintInsets);
        paintViewR.x = insets.left;
        paintViewR.y = insets.top;
        paintViewR.width = c.getWidth() - (insets.left + insets.right);
        paintViewR.height = c.getHeight() - (insets.top + insets.bottom);
        paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
        paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
        String clippedText =
            layoutText(ss, fm, text, icon, hAlign, vAlign,
                   hTextPosition, vTextPosition, paintViewR, paintIconR,
                   paintTextR, iconTextGap);
        if (icon != null) {
            Color color = g.getColor();
            if (ss.getStyle().getBoolean(ss, "TableHeader.alignSorterArrow", false) &&
                "TableHeader.renderer".equals(c.getName())) {
                paintIconR.x = paintViewR.width - paintIconR.width;
            } else {
                paintIconR.x += textOffset;
            }
            paintIconR.y += textOffset;
            SynthIcon.paintIcon(icon, ss, g, paintIconR.x, paintIconR.y,
                                paintIconR.width, paintIconR.height);
            g.setColor(color);
        }
        if (text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, paintTextR);
            } else {
                paintTextR.x += textOffset;
                paintTextR.y += textOffset;
                paintText(ss, g, clippedText, paintTextR, mnemonicIndex);
            }
        }
    }
    static Dimension getPreferredMenuItemSize(SynthContext context,
           SynthContext accContext, JComponent c,
           Icon checkIcon, Icon arrowIcon, int defaultTextIconGap,
           String acceleratorDelimiter, boolean useCheckAndArrow,
           String propertyPrefix) {
         JMenuItem mi = (JMenuItem) c;
         SynthMenuItemLayoutHelper lh = new SynthMenuItemLayoutHelper(
                 context, accContext, mi, checkIcon, arrowIcon,
                 MenuItemLayoutHelper.createMaxRect(), defaultTextIconGap,
                 acceleratorDelimiter, SynthLookAndFeel.isLeftToRight(mi),
                 useCheckAndArrow, propertyPrefix);
         Dimension result = new Dimension();
         int gap = lh.getGap();
         result.width = 0;
         MenuItemLayoutHelper.addMaxWidth(lh.getCheckSize(), gap, result);
         MenuItemLayoutHelper.addMaxWidth(lh.getLabelSize(), gap, result);
         MenuItemLayoutHelper.addWidth(lh.getMaxAccOrArrowWidth(), 5 * gap, result);
         result.width -= gap;
         result.height = MenuItemLayoutHelper.max(lh.getCheckSize().getHeight(),
                 lh.getLabelSize().getHeight(), lh.getAccSize().getHeight(),
                 lh.getArrowSize().getHeight());
         Insets insets = lh.getMenuItem().getInsets();
         if (insets != null) {
             result.width += insets.left + insets.right;
             result.height += insets.top + insets.bottom;
         }
         if (result.width % 2 == 0) {
             result.width++;
         }
         if (result.height % 2 == 0) {
             result.height++;
         }
         return result;
     }
    static void applyInsets(Rectangle rect, Insets insets, boolean leftToRight) {
        if (insets != null) {
            rect.x += (leftToRight ? insets.left : insets.right);
            rect.y += insets.top;
            rect.width -= (leftToRight ? insets.right : insets.left) + rect.x;
            rect.height -= (insets.bottom + rect.y);
        }
    }
    static void paint(SynthContext context, SynthContext accContext, Graphics g,
               Icon checkIcon, Icon arrowIcon, String acceleratorDelimiter,
               int defaultTextIconGap, String propertyPrefix) {
        JMenuItem mi = (JMenuItem) context.getComponent();
        SynthStyle style = context.getStyle();
        g.setFont(style.getFont(context));
        Rectangle viewRect = new Rectangle(0, 0, mi.getWidth(), mi.getHeight());
        boolean leftToRight = SynthLookAndFeel.isLeftToRight(mi);
        applyInsets(viewRect, mi.getInsets(), leftToRight);
        SynthMenuItemLayoutHelper lh = new SynthMenuItemLayoutHelper(
                context, accContext, mi, checkIcon, arrowIcon, viewRect,
                defaultTextIconGap, acceleratorDelimiter, leftToRight,
                MenuItemLayoutHelper.useCheckAndArrow(mi), propertyPrefix);
        MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();
        paintMenuItem(g, lh, lr);
    }
    static void paintMenuItem(Graphics g, SynthMenuItemLayoutHelper lh,
                              MenuItemLayoutHelper.LayoutResult lr) {
        Font holdf = g.getFont();
        Color holdc = g.getColor();
        paintCheckIcon(g, lh, lr);
        paintIcon(g, lh, lr);
        paintText(g, lh, lr);
        paintAccText(g, lh, lr);
        paintArrowIcon(g, lh, lr);
        g.setColor(holdc);
        g.setFont(holdf);
    }
    static void paintBackground(Graphics g, SynthMenuItemLayoutHelper lh) {
        paintBackground(lh.getContext(), g, lh.getMenuItem());
    }
    static void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintMenuItemBackground(context, g, 0, 0,
                c.getWidth(), c.getHeight());
    }
    static void paintIcon(Graphics g, SynthMenuItemLayoutHelper lh,
                          MenuItemLayoutHelper.LayoutResult lr) {
        if (lh.getIcon() != null) {
            Icon icon;
            JMenuItem mi = lh.getMenuItem();
            ButtonModel model = mi.getModel();
            if (!model.isEnabled()) {
                icon = mi.getDisabledIcon();
            } else if (model.isPressed() && model.isArmed()) {
                icon = mi.getPressedIcon();
                if (icon == null) {
                    icon = mi.getIcon();
                }
            } else {
                icon = mi.getIcon();
            }
            if (icon != null) {
                Rectangle iconRect = lr.getIconRect();
                SynthIcon.paintIcon(icon, lh.getContext(), g, iconRect.x,
                        iconRect.y, iconRect.width, iconRect.height);
            }
        }
    }
    static void paintCheckIcon(Graphics g, SynthMenuItemLayoutHelper lh,
                               MenuItemLayoutHelper.LayoutResult lr) {
        if (lh.getCheckIcon() != null) {
            Rectangle checkRect = lr.getCheckRect();
            SynthIcon.paintIcon(lh.getCheckIcon(), lh.getContext(), g,
                    checkRect.x, checkRect.y, checkRect.width, checkRect.height);
        }
    }
    static void paintAccText(Graphics g, SynthMenuItemLayoutHelper lh,
                             MenuItemLayoutHelper.LayoutResult lr) {
        String accText = lh.getAccText();
        if (accText != null && !accText.equals("")) {
            g.setColor(lh.getAccStyle().getColor(lh.getAccContext(),
                    ColorType.TEXT_FOREGROUND));
            g.setFont(lh.getAccStyle().getFont(lh.getAccContext()));
            lh.getAccGraphicsUtils().paintText(lh.getAccContext(), g, accText,
                    lr.getAccRect().x, lr.getAccRect().y, -1);
        }
    }
    static void paintText(Graphics g, SynthMenuItemLayoutHelper lh,
                          MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getText().equals("")) {
            if (lh.getHtmlView() != null) {
                lh.getHtmlView().paint(g, lr.getTextRect());
            } else {
                g.setColor(lh.getStyle().getColor(
                        lh.getContext(), ColorType.TEXT_FOREGROUND));
                g.setFont(lh.getStyle().getFont(lh.getContext()));
                lh.getGraphicsUtils().paintText(lh.getContext(), g, lh.getText(),
                        lr.getTextRect().x, lr.getTextRect().y,
                        lh.getMenuItem().getDisplayedMnemonicIndex());
            }
        }
    }
    static void paintArrowIcon(Graphics g, SynthMenuItemLayoutHelper lh,
                               MenuItemLayoutHelper.LayoutResult lr) {
        if (lh.getArrowIcon() != null) {
            Rectangle arrowRect = lr.getArrowRect();
            SynthIcon.paintIcon(lh.getArrowIcon(), lh.getContext(), g,
                    arrowRect.x, arrowRect.y, arrowRect.width, arrowRect.height);
        }
    }
    private static class SynthIconWrapper implements Icon {
        private static final java.util.List<SynthIconWrapper> CACHE = new java.util.ArrayList<SynthIconWrapper>(1);
        private SynthIcon synthIcon;
        private SynthContext context;
        static SynthIconWrapper get(SynthIcon icon, SynthContext context) {
            synchronized(CACHE) {
                int size = CACHE.size();
                if (size > 0) {
                    SynthIconWrapper wrapper = CACHE.remove(size - 1);
                    wrapper.reset(icon, context);
                    return wrapper;
                }
            }
            return new SynthIconWrapper(icon, context);
        }
        static void release(SynthIconWrapper wrapper) {
            wrapper.reset(null, null);
            synchronized(CACHE) {
                CACHE.add(wrapper);
            }
        }
        SynthIconWrapper(SynthIcon icon, SynthContext context) {
            reset(icon, context);
        }
        void reset(SynthIcon icon, SynthContext context) {
            synthIcon = icon;
            this.context = context;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }
        public int getIconWidth() {
            return synthIcon.getIconWidth(context);
        }
        public int getIconHeight() {
            return synthIcon.getIconHeight(context);
        }
    }
}
