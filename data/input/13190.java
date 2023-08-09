public class MotifGraphicsUtils implements SwingConstants
{
    private static final String MAX_ACC_WIDTH  =  "maxAccWidth";
    static void drawPoint(Graphics g, int x, int y) {
        g.drawLine(x, y, x, y);
    }
    public static void drawGroove(Graphics g, int x, int y, int w, int h,
                                  Color shadow, Color highlight)
    {
        Color oldColor = g.getColor();  
        g.translate(x, y);
        g.setColor(shadow);
        g.drawRect(0, 0, w-2, h-2);
        g.setColor(highlight);
        g.drawLine(1, h-3, 1, 1);
        g.drawLine(1, 1, w-3, 1);
        g.drawLine(0, h-1, w-1, h-1);
        g.drawLine(w-1, h-1, w-1, 0);
        g.translate(-x, -y);
        g.setColor(oldColor);
    }
    public static void drawStringInRect(Graphics g, String aString, int x, int y,
                                 int width, int height, int justification) {
        drawStringInRect(null, g, aString, x, y, width, height, justification);
    }
    static void drawStringInRect(JComponent c, Graphics g, String aString,
                                 int x, int y, int width, int height,
                                 int justification) {
        FontMetrics  fontMetrics;
        int          drawWidth, startX, startY, delta;
        if (g.getFont() == null) {
            return;
        }
        fontMetrics = SwingUtilities2.getFontMetrics(c, g);
        if (fontMetrics == null) {
            return;
        }
        if (justification == CENTER) {
            drawWidth = SwingUtilities2.stringWidth(c, fontMetrics, aString);
            if (drawWidth > width) {
                drawWidth = width;
            }
            startX = x + (width - drawWidth) / 2;
        } else if (justification == RIGHT) {
            drawWidth = SwingUtilities2.stringWidth(c, fontMetrics, aString);
            if (drawWidth > width) {
                drawWidth = width;
            }
            startX = x + width - drawWidth;
        } else {
            startX = x;
        }
        delta = (height - fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
        if (delta < 0) {
            delta = 0;
        }
        startY = y + height - delta - fontMetrics.getDescent();
        SwingUtilities2.drawString(c, g, aString, startX, startY);
    }
  public static void paintMenuItem(Graphics g, JComponent c,
                                   Icon checkIcon, Icon arrowIcon,
                                   Color background, Color foreground,
                                   int defaultTextIconGap)
    {
        JMenuItem b = (JMenuItem) c;
        ButtonModel model = b.getModel();
        Dimension size = b.getSize();
        Insets i = c.getInsets();
        Rectangle viewRect = new Rectangle(size);
        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle acceleratorRect = new Rectangle();
        Rectangle checkRect = new Rectangle();
        Rectangle arrowRect = new Rectangle();
        Font holdf = g.getFont();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g, f);
        FontMetrics fmAccel = SwingUtilities2.getFontMetrics(
            c, g, UIManager.getFont("MenuItem.acceleratorFont"));
        if (c.isOpaque()) {
            if (model.isArmed()|| (c instanceof JMenu && model.isSelected())) {
                g.setColor(background);
            } else {
                g.setColor(c.getBackground());
            }
            g.fillRect(0,0, size.width, size.height);
        }
        KeyStroke accelerator =  b.getAccelerator();
        String acceleratorText = "";
        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
                acceleratorText += "+";
            }
            acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
        }
        String text = layoutMenuItem(c, fm, b.getText(), fmAccel,
                                     acceleratorText, b.getIcon(),
                                     checkIcon, arrowIcon,
                                     b.getVerticalAlignment(),
                                     b.getHorizontalAlignment(),
                                     b.getVerticalTextPosition(),
                                     b.getHorizontalTextPosition(),
                                     viewRect, iconRect,
                                     textRect, acceleratorRect,
                                     checkRect, arrowRect,
                                     b.getText() == null
                                     ? 0 : defaultTextIconGap,
                                     defaultTextIconGap
                                     );
        Color holdc = g.getColor();
        if (checkIcon != null) {
            if(model.isArmed() || (c instanceof JMenu && model.isSelected()))
                g.setColor(foreground);
            checkIcon.paintIcon(c, g, checkRect.x, checkRect.y);
            g.setColor(holdc);
        }
        if(b.getIcon() != null) {
            Icon icon;
            if(!model.isEnabled()) {
                icon = b.getDisabledIcon();
            } else if(model.isPressed() && model.isArmed()) {
                icon = b.getPressedIcon();
                if(icon == null) {
                    icon = b.getIcon();
                }
            } else {
                icon = b.getIcon();
            }
            if (icon!=null) {
                icon.paintIcon(c, g, iconRect.x, iconRect.y);
            }
        }
        if(text != null && !text.equals("")) {
            View v = (View) c.getClientProperty("html");
            if (v != null) {
                v.paint(g, textRect);
            } else {
                int mnemIndex = b.getDisplayedMnemonicIndex();
                if(!model.isEnabled()) {
                    g.setColor(b.getBackground().brighter());
                    SwingUtilities2.drawStringUnderlineCharAt(b, g,text,
                        mnemIndex,
                        textRect.x, textRect.y + fmAccel.getAscent());
                    g.setColor(b.getBackground().darker());
                    SwingUtilities2.drawStringUnderlineCharAt(b, g,text,
                        mnemIndex,
                        textRect.x - 1, textRect.y + fmAccel.getAscent() - 1);
                } else {
                    if (model.isArmed()|| (c instanceof JMenu && model.isSelected())) {
                        g.setColor(foreground);
                    } else {
                        g.setColor(b.getForeground());
                    }
                    SwingUtilities2.drawStringUnderlineCharAt(b, g,text,
                                                  mnemIndex,
                                                  textRect.x,
                                                  textRect.y + fm.getAscent());
                }
            }
        }
        if(acceleratorText != null && !acceleratorText.equals("")) {
            int accOffset = 0;
            Container parent = b.getParent();
            if (parent != null && parent instanceof JComponent) {
                JComponent p = (JComponent) parent;
                Integer maxValueInt = (Integer) p.getClientProperty(MotifGraphicsUtils.MAX_ACC_WIDTH);
                int maxValue = maxValueInt != null ?
                    maxValueInt.intValue() : acceleratorRect.width;
                accOffset = maxValue - acceleratorRect.width;
            }
            g.setFont( UIManager.getFont("MenuItem.acceleratorFont") );
            if(!model.isEnabled()) {
                g.setColor(b.getBackground().brighter());
                SwingUtilities2.drawString(c, g,acceleratorText,
                                              acceleratorRect.x - accOffset, acceleratorRect.y + fm.getAscent());
                g.setColor(b.getBackground().darker());
                SwingUtilities2.drawString(c, g,acceleratorText,
                                              acceleratorRect.x - accOffset - 1, acceleratorRect.y + fm.getAscent() - 1);
            } else {
                if (model.isArmed()|| (c instanceof JMenu && model.isSelected()))
                    {
                        g.setColor(foreground);
                    } else {
                        g.setColor(b.getForeground());
                    }
                SwingUtilities2.drawString(c, g,acceleratorText,
                                              acceleratorRect.x - accOffset,
                                              acceleratorRect.y + fmAccel.getAscent());
            }
        }
        if (arrowIcon != null) {
            if(model.isArmed() || (c instanceof JMenu && model.isSelected()))
                g.setColor(foreground);
            if( !(b.getParent() instanceof JMenuBar) )
                arrowIcon.paintIcon(c, g, arrowRect.x, arrowRect.y);
        }
        g.setColor(holdc);
        g.setFont(holdf);
    }
    private static String layoutMenuItem(
        JComponent c,
        FontMetrics fm,
        String text,
        FontMetrics fmAccel,
        String acceleratorText,
        Icon icon,
        Icon checkIcon,
        Icon arrowIcon,
        int verticalAlignment,
        int horizontalAlignment,
        int verticalTextPosition,
        int horizontalTextPosition,
        Rectangle viewR,
        Rectangle iconR,
        Rectangle textR,
        Rectangle acceleratorR,
        Rectangle checkIconR,
        Rectangle arrowIconR,
        int textIconGap,
        int menuItemGap
        )
    {
        SwingUtilities.layoutCompoundLabel(c,
                                           fm,
                                           text,
                                           icon,
                                           verticalAlignment,
                                           horizontalAlignment,
                                           verticalTextPosition,
                                           horizontalTextPosition,
                                           viewR,
                                           iconR,
                                           textR,
                                           textIconGap);
        if( (acceleratorText == null) || acceleratorText.equals("") ) {
            acceleratorR.width = acceleratorR.height = 0;
            acceleratorText = "";
        }
        else {
            acceleratorR.width
                = SwingUtilities2.stringWidth(c, fmAccel, acceleratorText);
            acceleratorR.height = fmAccel.getHeight();
        }
        if (checkIcon != null) {
            checkIconR.width = checkIcon.getIconWidth();
            checkIconR.height = checkIcon.getIconHeight();
        }
        else {
            checkIconR.width = checkIconR.height = 0;
        }
        if (arrowIcon != null) {
            arrowIconR.width = arrowIcon.getIconWidth();
            arrowIconR.height = arrowIcon.getIconHeight();
        }
        else {
            arrowIconR.width = arrowIconR.height = 0;
        }
        Rectangle labelR = iconR.union(textR);
        if( MotifGraphicsUtils.isLeftToRight(c) ) {
            textR.x += checkIconR.width + menuItemGap;
            iconR.x += checkIconR.width + menuItemGap;
            acceleratorR.x = viewR.x + viewR.width - arrowIconR.width
                             - menuItemGap - acceleratorR.width;
            checkIconR.x = viewR.x;
            arrowIconR.x = viewR.x + viewR.width - menuItemGap
                           - arrowIconR.width;
        } else {
            textR.x -= (checkIconR.width + menuItemGap);
            iconR.x -= (checkIconR.width + menuItemGap);
            acceleratorR.x = viewR.x + arrowIconR.width + menuItemGap;
            checkIconR.x = viewR.x + viewR.width - checkIconR.width;
            arrowIconR.x = viewR.x + menuItemGap;
        }
        acceleratorR.y = labelR.y + (labelR.height/2) - (acceleratorR.height/2);
        arrowIconR.y = labelR.y + (labelR.height/2) - (arrowIconR.height/2);
        checkIconR.y = labelR.y + (labelR.height/2) - (checkIconR.height/2);
        return text;
    }
  private static void drawMenuBezel(Graphics g, Color background,
                                    int x, int y,
                                    int width, int height)
    {
      g.setColor(background);
      g.fillRect(x,y,width,height);
      g.setColor(background.brighter().brighter());
      g.drawLine(x+1,       y+height-1,  x+width-1, y+height-1);
      g.drawLine(x+width-1, y+height-2,  x+width-1, y+1);
      g.setColor(background.darker().darker());
      g.drawLine(x,   y,   x+width-2, y);
      g.drawLine(x,   y+1, x,         y+height-2);
    }
    static boolean isLeftToRight( Component c ) {
        return c.getComponentOrientation().isLeftToRight();
    }
}
