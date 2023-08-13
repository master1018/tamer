public class DefaultDesktopManager implements DesktopManager, java.io.Serializable {
    final static String HAS_BEEN_ICONIFIED_PROPERTY = "wasIconOnce";
    final static int DEFAULT_DRAG_MODE = 0;
    final static int OUTLINE_DRAG_MODE = 1;
    final static int FASTER_DRAG_MODE = 2;
    int dragMode = DEFAULT_DRAG_MODE;
    private transient Rectangle currentBounds = null;
    private transient Graphics desktopGraphics = null;
    private transient Rectangle desktopBounds = null;
    private transient Rectangle[] floatingItems = {};
    private transient boolean didDrag;
    public void openFrame(JInternalFrame f) {
        if(f.getDesktopIcon().getParent() != null) {
            f.getDesktopIcon().getParent().add(f);
            removeIconFor(f);
        }
    }
    public void closeFrame(JInternalFrame f) {
        JDesktopPane d = f.getDesktopPane();
        if (d == null) {
            return;
        }
        boolean findNext = f.isSelected();
        Container c = f.getParent();
        JInternalFrame nextFrame = null;
        if (findNext) {
            nextFrame = d.getNextFrame(f);
            try { f.setSelected(false); } catch (PropertyVetoException e2) { }
        }
        if(c != null) {
            c.remove(f); 
            c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
        }
        removeIconFor(f);
        if(f.getNormalBounds() != null)
            f.setNormalBounds(null);
        if(wasIcon(f))
            setWasIcon(f, null);
        if (nextFrame != null) {
            try { nextFrame.setSelected(true); }
            catch (PropertyVetoException e2) { }
        } else if (findNext && d.getComponentCount() == 0) {
            d.requestFocus();
        }
    }
    public void maximizeFrame(JInternalFrame f) {
        if (f.isIcon()) {
            try {
                f.setIcon(false);
            } catch (PropertyVetoException e2) {
            }
        } else {
            f.setNormalBounds(f.getBounds());
            Rectangle desktopBounds = f.getParent().getBounds();
            setBoundsForFrame(f, 0, 0,
                desktopBounds.width, desktopBounds.height);
        }
        try {
            f.setSelected(true);
        } catch (PropertyVetoException e2) {
        }
    }
    public void minimizeFrame(JInternalFrame f) {
        if (f.isIcon()) {
            iconifyFrame(f);
            return;
        }
        if ((f.getNormalBounds()) != null) {
            Rectangle r = f.getNormalBounds();
            f.setNormalBounds(null);
            try { f.setSelected(true); } catch (PropertyVetoException e2) { }
            setBoundsForFrame(f, r.x, r.y, r.width, r.height);
        }
    }
    public void iconifyFrame(JInternalFrame f) {
        JInternalFrame.JDesktopIcon desktopIcon;
        Container c = f.getParent();
        JDesktopPane d = f.getDesktopPane();
        boolean findNext = f.isSelected();
        desktopIcon = f.getDesktopIcon();
        if(!wasIcon(f)) {
            Rectangle r = getBoundsForIconOf(f);
            desktopIcon.setBounds(r.x, r.y, r.width, r.height);
            desktopIcon.revalidate();
            setWasIcon(f, Boolean.TRUE);
        }
        if (c == null || d == null) {
            return;
        }
        if (c instanceof JLayeredPane) {
            JLayeredPane lp = (JLayeredPane)c;
            int layer = lp.getLayer(f);
            lp.putLayer(desktopIcon, layer);
        }
        if (!f.isMaximum()) {
            f.setNormalBounds(f.getBounds());
        }
        d.setComponentOrderCheckingEnabled(false);
        c.remove(f);
        c.add(desktopIcon);
        d.setComponentOrderCheckingEnabled(true);
        c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
        if (findNext) {
            if (d.selectFrame(true) == null) {
                f.restoreSubcomponentFocus();
            }
        }
    }
    public void deiconifyFrame(JInternalFrame f) {
        JInternalFrame.JDesktopIcon desktopIcon = f.getDesktopIcon();
        Container c = desktopIcon.getParent();
        JDesktopPane d = f.getDesktopPane();
        if (c != null && d != null) {
            c.add(f);
            if (f.isMaximum()) {
                Rectangle desktopBounds = c.getBounds();
                if (f.getWidth() != desktopBounds.width ||
                        f.getHeight() != desktopBounds.height) {
                    setBoundsForFrame(f, 0, 0,
                        desktopBounds.width, desktopBounds.height);
                }
            }
            removeIconFor(f);
            if (f.isSelected()) {
                f.moveToFront();
                f.restoreSubcomponentFocus();
            }
            else {
                try {
                    f.setSelected(true);
                } catch (PropertyVetoException e2) {}
            }
        }
    }
    public void activateFrame(JInternalFrame f) {
        Container p = f.getParent();
        Component[] c;
        JDesktopPane d = f.getDesktopPane();
        JInternalFrame currentlyActiveFrame =
          (d == null) ? null : d.getSelectedFrame();
        if(p == null) {
            p = f.getDesktopIcon().getParent();
            if(p == null)
                return;
        }
        if (currentlyActiveFrame == null){
          if (d != null) { d.setSelectedFrame(f);}
        } else if (currentlyActiveFrame != f) {
          if (currentlyActiveFrame.isSelected()) {
            try {
              currentlyActiveFrame.setSelected(false);
            }
            catch(PropertyVetoException e2) {}
          }
          if (d != null) { d.setSelectedFrame(f);}
        }
        f.moveToFront();
    }
    public void deactivateFrame(JInternalFrame f) {
      JDesktopPane d = f.getDesktopPane();
      JInternalFrame currentlyActiveFrame =
          (d == null) ? null : d.getSelectedFrame();
      if (currentlyActiveFrame == f)
        d.setSelectedFrame(null);
    }
    public void beginDraggingFrame(JComponent f) {
        setupDragMode(f);
        if (dragMode == FASTER_DRAG_MODE) {
          Component desktop = f.getParent();
          floatingItems = findFloatingItems(f);
          currentBounds = f.getBounds();
          if (desktop instanceof JComponent) {
              desktopBounds = ((JComponent)desktop).getVisibleRect();
          }
          else {
              desktopBounds = desktop.getBounds();
              desktopBounds.x = desktopBounds.y = 0;
          }
          desktopGraphics = JComponent.safelyGetGraphics(desktop);
          ((JInternalFrame)f).isDragging = true;
          didDrag = false;
        }
    }
    private void setupDragMode(JComponent f) {
        JDesktopPane p = getDesktopPane(f);
        Container parent = f.getParent();
        dragMode = DEFAULT_DRAG_MODE;
        if (p != null) {
            String mode = (String)p.getClientProperty("JDesktopPane.dragMode");
            Window window = SwingUtilities.getWindowAncestor(f);
            if (window != null && !AWTUtilities.isWindowOpaque(window)) {
                dragMode = DEFAULT_DRAG_MODE;
            } else if (mode != null && mode.equals("outline")) {
                dragMode = OUTLINE_DRAG_MODE;
            } else if (mode != null && mode.equals("faster")
                    && f instanceof JInternalFrame
                    && ((JInternalFrame)f).isOpaque() &&
                       (parent == null || parent.isOpaque())) {
                dragMode = FASTER_DRAG_MODE;
            } else {
                if (p.getDragMode() == JDesktopPane.OUTLINE_DRAG_MODE ) {
                    dragMode = OUTLINE_DRAG_MODE;
                } else if ( p.getDragMode() == JDesktopPane.LIVE_DRAG_MODE
                        && f instanceof JInternalFrame
                        && ((JInternalFrame)f).isOpaque()) {
                    dragMode = FASTER_DRAG_MODE;
                } else {
                    dragMode = DEFAULT_DRAG_MODE;
                }
            }
        }
    }
    private transient Point currentLoc = null;
    public void dragFrame(JComponent f, int newX, int newY) {
        if (dragMode == OUTLINE_DRAG_MODE) {
            JDesktopPane desktopPane = getDesktopPane(f);
            if (desktopPane != null){
              Graphics g = JComponent.safelyGetGraphics(desktopPane);
              g.setXORMode(Color.white);
              if (currentLoc != null) {
                g.drawRect(currentLoc.x, currentLoc.y,
                        f.getWidth()-1, f.getHeight()-1);
              }
              g.drawRect( newX, newY, f.getWidth()-1, f.getHeight()-1);
              sun.java2d.SurfaceData sData =
                  ((sun.java2d.SunGraphics2D)g).getSurfaceData();
              if (!sData.isSurfaceLost()) {
                  currentLoc = new Point (newX, newY);
              }
;
              g.dispose();
            }
        } else if (dragMode == FASTER_DRAG_MODE) {
            dragFrameFaster(f, newX, newY);
        } else {
            setBoundsForFrame(f, newX, newY, f.getWidth(), f.getHeight());
        }
    }
    public void endDraggingFrame(JComponent f) {
        if ( dragMode == OUTLINE_DRAG_MODE && currentLoc != null) {
            setBoundsForFrame(f, currentLoc.x, currentLoc.y, f.getWidth(), f.getHeight() );
            currentLoc = null;
        } else if (dragMode == FASTER_DRAG_MODE) {
            currentBounds = null;
            if (desktopGraphics != null) {
                desktopGraphics.dispose();
                desktopGraphics = null;
            }
            desktopBounds = null;
            ((JInternalFrame)f).isDragging = false;
        }
    }
    public void beginResizingFrame(JComponent f, int direction) {
        setupDragMode(f);
    }
    public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
        if ( dragMode == DEFAULT_DRAG_MODE || dragMode == FASTER_DRAG_MODE ) {
            setBoundsForFrame(f, newX, newY, newWidth, newHeight);
        } else {
            JDesktopPane desktopPane = getDesktopPane(f);
            if (desktopPane != null){
              Graphics g = JComponent.safelyGetGraphics(desktopPane);
              g.setXORMode(Color.white);
              if (currentBounds != null) {
                g.drawRect( currentBounds.x, currentBounds.y, currentBounds.width-1, currentBounds.height-1);
              }
              g.drawRect( newX, newY, newWidth-1, newHeight-1);
              sun.java2d.SurfaceData sData =
                  ((sun.java2d.SunGraphics2D)g).getSurfaceData();
              if (!sData.isSurfaceLost()) {
                  currentBounds = new Rectangle (newX, newY, newWidth, newHeight);
              }
              g.setPaintMode();
              g.dispose();
            }
        }
    }
    public void endResizingFrame(JComponent f) {
        if ( dragMode == OUTLINE_DRAG_MODE && currentBounds != null) {
            setBoundsForFrame(f, currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height );
            currentBounds = null;
        }
    }
    public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
        f.setBounds(newX, newY, newWidth, newHeight);
        f.revalidate();
    }
    protected void removeIconFor(JInternalFrame f) {
        JInternalFrame.JDesktopIcon di = f.getDesktopIcon();
        Container c = di.getParent();
        if(c != null) {
            c.remove(di);
            c.repaint(di.getX(), di.getY(), di.getWidth(), di.getHeight());
        }
    }
    protected Rectangle getBoundsForIconOf(JInternalFrame f) {
      JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
      Dimension prefSize = icon.getPreferredSize();
      Container c = f.getParent();
      if (c == null) {
          c = f.getDesktopIcon().getParent();
      }
      if (c == null) {
        return new Rectangle(0, 0, prefSize.width, prefSize.height);
      }
      Rectangle parentBounds = c.getBounds();
      Component [] components = c.getComponents();
      Rectangle availableRectangle = null;
      JInternalFrame.JDesktopIcon currentIcon = null;
      int x = 0;
      int y = parentBounds.height - prefSize.height;
      int w = prefSize.width;
      int h = prefSize.height;
      boolean found = false;
      while (!found) {
        availableRectangle = new Rectangle(x,y,w,h);
        found = true;
        for ( int i=0; i<components.length; i++ ) {
          if ( components[i] instanceof JInternalFrame ) {
            currentIcon = ((JInternalFrame)components[i]).getDesktopIcon();
          }
          else if ( components[i] instanceof JInternalFrame.JDesktopIcon ){
            currentIcon = (JInternalFrame.JDesktopIcon)components[i];
          } else
            continue;
          if ( !currentIcon.equals(icon) ) {
            if ( availableRectangle.intersects(currentIcon.getBounds()) ) {
              found = false;
              break;
            }
          }
        }
        if (currentIcon == null)
          return availableRectangle;
        x += currentIcon.getBounds().width;
        if ( x + w > parentBounds.width ) {
          x = 0;
          y -= h;
        }
      }
      return(availableRectangle);
    }
    protected void setPreviousBounds(JInternalFrame f, Rectangle r)     {
        f.setNormalBounds(r);
    }
    protected Rectangle getPreviousBounds(JInternalFrame f)     {
        return f.getNormalBounds();
    }
    protected void setWasIcon(JInternalFrame f, Boolean value)  {
        if (value != null) {
            f.putClientProperty(HAS_BEEN_ICONIFIED_PROPERTY, value);
        }
    }
    protected boolean wasIcon(JInternalFrame f) {
        return (f.getClientProperty(HAS_BEEN_ICONIFIED_PROPERTY) == Boolean.TRUE);
    }
    JDesktopPane getDesktopPane( JComponent frame ) {
        JDesktopPane pane = null;
        Component c = frame.getParent();
        while ( pane == null ) {
            if ( c instanceof JDesktopPane ) {
                pane = (JDesktopPane)c;
            }
            else if ( c == null ) {
                break;
            }
            else {
                c = c.getParent();
            }
        }
        return pane;
    }
   private void dragFrameFaster(JComponent f, int newX, int newY) {
      Rectangle previousBounds = new Rectangle(currentBounds.x,
                                               currentBounds.y,
                                               currentBounds.width,
                                               currentBounds.height);
      currentBounds.x = newX;
      currentBounds.y = newY;
      if (didDrag) {
          emergencyCleanup(f);
      }
      else {
          didDrag = true;
          ((JInternalFrame)f).danger = false;
      }
      boolean floaterCollision = isFloaterCollision(previousBounds, currentBounds);
      JComponent parent = (JComponent)f.getParent();
      Rectangle visBounds = previousBounds.intersection(desktopBounds);
      RepaintManager currentManager = RepaintManager.currentManager(f);
      currentManager.beginPaint();
      try {
          if(!floaterCollision) {
              currentManager.copyArea(parent, desktopGraphics, visBounds.x,
                                      visBounds.y,
                                      visBounds.width,
                                      visBounds.height,
                                      newX - previousBounds.x,
                                      newY - previousBounds.y,
                                      true);
          }
          f.setBounds(currentBounds);
          if(floaterCollision) {
              ((JInternalFrame)f).isDragging = false;
              parent.paintImmediately(currentBounds);
              ((JInternalFrame)f).isDragging = true;
          }
          currentManager.markCompletelyClean(parent);
          currentManager.markCompletelyClean(f);
          Rectangle[] dirtyRects = null;
          if ( previousBounds.intersects(currentBounds) ) {
              dirtyRects = SwingUtilities.computeDifference(previousBounds,
                                                            currentBounds);
          } else {
              dirtyRects = new Rectangle[1];
              dirtyRects[0] = previousBounds;
          };
          for (int i = 0; i < dirtyRects.length; i++) {
              parent.paintImmediately(dirtyRects[i]);
          }
          if ( !(visBounds.equals(previousBounds)) ) {
              dirtyRects = SwingUtilities.computeDifference(previousBounds,
                                                            desktopBounds);
              for (int i = 0; i < dirtyRects.length; i++) {
                  dirtyRects[i].x += newX - previousBounds.x;
                  dirtyRects[i].y += newY - previousBounds.y;
                  ((JInternalFrame)f).isDragging = false;
                  parent.paintImmediately(dirtyRects[i]);
                  ((JInternalFrame)f).isDragging = true;
              }
          }
      } finally {
          currentManager.endPaint();
      }
      Window topLevel = SwingUtilities.getWindowAncestor(f);
      Toolkit tk = Toolkit.getDefaultToolkit();
      if (!topLevel.isOpaque() &&
          (tk instanceof SunToolkit) &&
          ((SunToolkit)tk).needUpdateWindow())
      {
          AWTAccessor.getWindowAccessor().updateWindow(topLevel);
      }
   }
   private boolean isFloaterCollision(Rectangle moveFrom, Rectangle moveTo) {
      if (floatingItems.length == 0) {
         return false;
      }
      for (int i = 0; i < floatingItems.length; i++) {
         boolean intersectsFrom = moveFrom.intersects(floatingItems[i]);
         if (intersectsFrom) {
            return true;
         }
         boolean intersectsTo = moveTo.intersects(floatingItems[i]);
         if (intersectsTo) {
            return true;
         }
      }
      return false;
   }
   private Rectangle[] findFloatingItems(JComponent f) {
      Container desktop = f.getParent();
      Component[] children = desktop.getComponents();
      int i = 0;
      for (i = 0; i < children.length; i++) {
         if (children[i] == f) {
            break;
         }
      }
      Rectangle[] floaters = new Rectangle[i];
      for (i = 0; i < floaters.length; i++) {
         floaters[i] = children[i].getBounds();
      }
      return floaters;
   }
   private void emergencyCleanup(final JComponent f) {
        if ( ((JInternalFrame)f).danger ) {
           SwingUtilities.invokeLater( new Runnable(){
                                       public void run(){
                                       ((JInternalFrame)f).isDragging = false;
                                       f.paintImmediately(0,0,
                                                          f.getWidth(),
                                                          f.getHeight());
                                        ((JInternalFrame)f).isDragging = true;
                                       }});
             ((JInternalFrame)f).danger = false;
        }
   }
}
