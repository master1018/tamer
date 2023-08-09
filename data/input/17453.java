public class SpringLayout implements LayoutManager2 {
    private Map<Component, Constraints> componentConstraints = new HashMap<Component, Constraints>();
    private Spring cyclicReference = Spring.constant(Spring.UNSET);
    private Set<Spring> cyclicSprings;
    private Set<Spring> acyclicSprings;
    public static final String NORTH  = "North";
    public static final String SOUTH  = "South";
    public static final String EAST   = "East";
    public static final String WEST   = "West";
    public static final String HORIZONTAL_CENTER   = "HorizontalCenter";
    public static final String VERTICAL_CENTER   = "VerticalCenter";
    public static final String BASELINE   = "Baseline";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    private static String[] ALL_HORIZONTAL = {WEST, WIDTH, EAST, HORIZONTAL_CENTER};
    private static String[] ALL_VERTICAL = {NORTH, HEIGHT, SOUTH, VERTICAL_CENTER, BASELINE};
    public static class Constraints {
       private Spring x;
       private Spring y;
       private Spring width;
       private Spring height;
       private Spring east;
       private Spring south;
        private Spring horizontalCenter;
        private Spring verticalCenter;
        private Spring baseline;
        private List<String> horizontalHistory = new ArrayList<String>(2);
        private List<String> verticalHistory = new ArrayList<String>(2);
        private Component c;
       public Constraints() {
       }
       public Constraints(Spring x, Spring y) {
           setX(x);
           setY(y);
       }
       public Constraints(Spring x, Spring y, Spring width, Spring height) {
           setX(x);
           setY(y);
           setWidth(width);
           setHeight(height);
       }
        public Constraints(Component c) {
            this.c = c;
            setX(Spring.constant(c.getX()));
            setY(Spring.constant(c.getY()));
            setWidth(Spring.width(c));
            setHeight(Spring.height(c));
        }
        private void pushConstraint(String name, Spring value, boolean horizontal) {
            boolean valid = true;
            List<String> history = horizontal ? horizontalHistory :
                                                verticalHistory;
            if (history.contains(name)) {
                history.remove(name);
                valid = false;
            } else if (history.size() == 2 && value != null) {
                history.remove(0);
                valid = false;
            }
            if (value != null) {
                history.add(name);
            }
            if (!valid) {
                String[] all = horizontal ? ALL_HORIZONTAL : ALL_VERTICAL;
                for (String s : all) {
                    if (!history.contains(s)) {
                        setConstraint(s, null);
                    }
                }
            }
        }
       private Spring sum(Spring s1, Spring s2) {
           return (s1 == null || s2 == null) ? null : Spring.sum(s1, s2);
       }
       private Spring difference(Spring s1, Spring s2) {
           return (s1 == null || s2 == null) ? null : Spring.difference(s1, s2);
       }
        private Spring scale(Spring s, float factor) {
            return (s == null) ? null : Spring.scale(s, factor);
        }
        private int getBaselineFromHeight(int height) {
            if (height < 0) {
                return -c.getBaseline(c.getPreferredSize().width,
                                      -height);
            }
            return c.getBaseline(c.getPreferredSize().width, height);
        }
        private int getHeightFromBaseLine(int baseline) {
            Dimension prefSize = c.getPreferredSize();
            int prefHeight = prefSize.height;
            int prefBaseline = c.getBaseline(prefSize.width, prefHeight);
            if (prefBaseline == baseline) {
                return prefHeight;
            }
            switch(c.getBaselineResizeBehavior()) {
            case CONSTANT_DESCENT:
                return prefHeight + (baseline - prefBaseline);
            case CENTER_OFFSET:
                return prefHeight + 2 * (baseline - prefBaseline);
            case CONSTANT_ASCENT:
            default: 
            }
            return Integer.MIN_VALUE;
        }
         private Spring heightToRelativeBaseline(Spring s) {
            return new Spring.SpringMap(s) {
                 protected int map(int i) {
                    return getBaselineFromHeight(i);
                 }
                 protected int inv(int i) {
                     return getHeightFromBaseLine(i);
                 }
            };
        }
        private Spring relativeBaselineToHeight(Spring s) {
            return new Spring.SpringMap(s) {
                protected int map(int i) {
                    return getHeightFromBaseLine(i);
                 }
                 protected int inv(int i) {
                    return getBaselineFromHeight(i);
                 }
            };
        }
        private boolean defined(List history, String s1, String s2) {
            return history.contains(s1) && history.contains(s2);
        }
       public void setX(Spring x) {
           this.x = x;
           pushConstraint(WEST, x, true);
       }
       public Spring getX() {
           if (x == null) {
               if (defined(horizontalHistory, EAST, WIDTH)) {
                   x = difference(east, width);
               } else if (defined(horizontalHistory, HORIZONTAL_CENTER, WIDTH)) {
                   x = difference(horizontalCenter, scale(width, 0.5f));
               } else if (defined(horizontalHistory, HORIZONTAL_CENTER, EAST)) {
                   x = difference(scale(horizontalCenter, 2f), east);
               }
           }
           return x;
       }
       public void setY(Spring y) {
           this.y = y;
           pushConstraint(NORTH, y, false);
       }
       public Spring getY() {
           if (y == null) {
               if (defined(verticalHistory, SOUTH, HEIGHT)) {
                   y = difference(south, height);
               } else if (defined(verticalHistory, VERTICAL_CENTER, HEIGHT)) {
                   y = difference(verticalCenter, scale(height, 0.5f));
               } else if (defined(verticalHistory, VERTICAL_CENTER, SOUTH)) {
                   y = difference(scale(verticalCenter, 2f), south);
               } else if (defined(verticalHistory, BASELINE, HEIGHT)) {
                   y = difference(baseline, heightToRelativeBaseline(height));
               } else if (defined(verticalHistory, BASELINE, SOUTH)) {
                   y = scale(difference(baseline, heightToRelativeBaseline(south)), 2f);
               }
           }
           return y;
       }
       public void setWidth(Spring width) {
           this.width = width;
           pushConstraint(WIDTH, width, true);
       }
       public Spring getWidth() {
           if (width == null) {
               if (horizontalHistory.contains(EAST)) {
                   width = difference(east, getX());
               } else if (horizontalHistory.contains(HORIZONTAL_CENTER)) {
                   width = scale(difference(horizontalCenter, getX()), 2f);
               }
           }
           return width;
       }
       public void setHeight(Spring height) {
           this.height = height;
           pushConstraint(HEIGHT, height, false);
       }
       public Spring getHeight() {
           if (height == null) {
               if (verticalHistory.contains(SOUTH)) {
                   height = difference(south, getY());
               } else if (verticalHistory.contains(VERTICAL_CENTER)) {
                   height = scale(difference(verticalCenter, getY()), 2f);
               } else if (verticalHistory.contains(BASELINE)) {
                   height = relativeBaselineToHeight(difference(baseline, getY()));
               }
           }
           return height;
       }
       private void setEast(Spring east) {
           this.east = east;
           pushConstraint(EAST, east, true);
       }
       private Spring getEast() {
           if (east == null) {
               east = sum(getX(), getWidth());
           }
           return east;
       }
       private void setSouth(Spring south) {
           this.south = south;
           pushConstraint(SOUTH, south, false);
       }
       private Spring getSouth() {
           if (south == null) {
               south = sum(getY(), getHeight());
           }
           return south;
       }
        private Spring getHorizontalCenter() {
            if (horizontalCenter == null) {
                horizontalCenter = sum(getX(), scale(getWidth(), 0.5f));
            }
            return horizontalCenter;
        }
        private void setHorizontalCenter(Spring horizontalCenter) {
            this.horizontalCenter = horizontalCenter;
            pushConstraint(HORIZONTAL_CENTER, horizontalCenter, true);
        }
        private Spring getVerticalCenter() {
            if (verticalCenter == null) {
                verticalCenter = sum(getY(), scale(getHeight(), 0.5f));
            }
            return verticalCenter;
        }
        private void setVerticalCenter(Spring verticalCenter) {
            this.verticalCenter = verticalCenter;
            pushConstraint(VERTICAL_CENTER, verticalCenter, false);
        }
        private Spring getBaseline() {
            if (baseline == null) {
                baseline = sum(getY(), heightToRelativeBaseline(getHeight()));
            }
            return baseline;
        }
        private void setBaseline(Spring baseline) {
            this.baseline = baseline;
            pushConstraint(BASELINE, baseline, false);
        }
       public void setConstraint(String edgeName, Spring s) {
           edgeName = edgeName.intern();
           if (edgeName == WEST) {
               setX(s);
           } else if (edgeName == NORTH) {
               setY(s);
           } else if (edgeName == EAST) {
               setEast(s);
           } else if (edgeName == SOUTH) {
               setSouth(s);
           } else if (edgeName == HORIZONTAL_CENTER) {
               setHorizontalCenter(s);
           } else if (edgeName == WIDTH) {
               setWidth(s);
           } else if (edgeName == HEIGHT) {
               setHeight(s);
           } else if (edgeName == VERTICAL_CENTER) {
               setVerticalCenter(s);
           } else if (edgeName == BASELINE) {
               setBaseline(s);
           }
       }
       public Spring getConstraint(String edgeName) {
           edgeName = edgeName.intern();
           return (edgeName == WEST)  ? getX() :
                   (edgeName == NORTH) ? getY() :
                   (edgeName == EAST)  ? getEast() :
                   (edgeName == SOUTH) ? getSouth() :
                   (edgeName == WIDTH)  ? getWidth() :
                   (edgeName == HEIGHT) ? getHeight() :
                   (edgeName == HORIZONTAL_CENTER) ? getHorizontalCenter() :
                   (edgeName == VERTICAL_CENTER)  ? getVerticalCenter() :
                   (edgeName == BASELINE) ? getBaseline() :
                  null;
       }
        void reset() {
           Spring[] allSprings = {x, y, width, height, east, south,
               horizontalCenter, verticalCenter, baseline};
           for (Spring s : allSprings) {
               if (s != null) {
                   s.setValue(Spring.UNSET);
               }
           }
       }
   }
   private static class SpringProxy extends Spring {
       private String edgeName;
       private Component c;
       private SpringLayout l;
       public SpringProxy(String edgeName, Component c, SpringLayout l) {
           this.edgeName = edgeName;
           this.c = c;
           this.l = l;
       }
       private Spring getConstraint() {
           return l.getConstraints(c).getConstraint(edgeName);
       }
       public int getMinimumValue() {
           return getConstraint().getMinimumValue();
       }
       public int getPreferredValue() {
           return getConstraint().getPreferredValue();
       }
       public int getMaximumValue() {
           return getConstraint().getMaximumValue();
       }
       public int getValue() {
           return getConstraint().getValue();
       }
       public void setValue(int size) {
           getConstraint().setValue(size);
       }
        boolean isCyclic(SpringLayout l) {
           return l.isCyclic(getConstraint());
       }
       public String toString() {
           return "SpringProxy for " + edgeName + " edge of " + c.getName() + ".";
       }
    }
    public SpringLayout() {}
    private void resetCyclicStatuses() {
        cyclicSprings = new HashSet<Spring>();
        acyclicSprings = new HashSet<Spring>();
    }
    private void setParent(Container p) {
        resetCyclicStatuses();
        Constraints pc = getConstraints(p);
        pc.setX(Spring.constant(0));
        pc.setY(Spring.constant(0));
        Spring width = pc.getWidth();
        if (width instanceof Spring.WidthSpring && ((Spring.WidthSpring)width).c == p) {
            pc.setWidth(Spring.constant(0, 0, Integer.MAX_VALUE));
        }
        Spring height = pc.getHeight();
        if (height instanceof Spring.HeightSpring && ((Spring.HeightSpring)height).c == p) {
            pc.setHeight(Spring.constant(0, 0, Integer.MAX_VALUE));
        }
    }
     boolean isCyclic(Spring s) {
        if (s == null) {
            return false;
        }
        if (cyclicSprings.contains(s)) {
            return true;
        }
        if (acyclicSprings.contains(s)) {
            return false;
        }
        cyclicSprings.add(s);
        boolean result = s.isCyclic(this);
        if (!result) {
            acyclicSprings.add(s);
            cyclicSprings.remove(s);
        }
        else {
            System.err.println(s + " is cyclic. ");
        }
        return result;
    }
    private Spring abandonCycles(Spring s) {
        return isCyclic(s) ? cyclicReference : s;
    }
    public void addLayoutComponent(String name, Component c) {}
    public void removeLayoutComponent(Component c) {
        componentConstraints.remove(c);
    }
    private static Dimension addInsets(int width, int height, Container p) {
        Insets i = p.getInsets();
        return new Dimension(width + i.left + i.right, height + i.top + i.bottom);
    }
    public Dimension minimumLayoutSize(Container parent) {
        setParent(parent);
        Constraints pc = getConstraints(parent);
        return addInsets(abandonCycles(pc.getWidth()).getMinimumValue(),
                         abandonCycles(pc.getHeight()).getMinimumValue(),
                         parent);
    }
    public Dimension preferredLayoutSize(Container parent) {
        setParent(parent);
        Constraints pc = getConstraints(parent);
        return addInsets(abandonCycles(pc.getWidth()).getPreferredValue(),
                         abandonCycles(pc.getHeight()).getPreferredValue(),
                         parent);
    }
    public Dimension maximumLayoutSize(Container parent) {
        setParent(parent);
        Constraints pc = getConstraints(parent);
        return addInsets(abandonCycles(pc.getWidth()).getMaximumValue(),
                         abandonCycles(pc.getHeight()).getMaximumValue(),
                         parent);
    }
    public void addLayoutComponent(Component component, Object constraints) {
        if (constraints instanceof Constraints) {
            putConstraints(component, (Constraints)constraints);
        }
    }
    public float getLayoutAlignmentX(Container p) {
        return 0.5f;
    }
    public float getLayoutAlignmentY(Container p) {
        return 0.5f;
    }
    public void invalidateLayout(Container p) {}
    public void putConstraint(String e1, Component c1, int pad, String e2, Component c2) {
        putConstraint(e1, c1, Spring.constant(pad), e2, c2);
    }
    public void putConstraint(String e1, Component c1, Spring s, String e2, Component c2) {
        putConstraint(e1, c1, Spring.sum(s, getConstraint(e2, c2)));
    }
    private void putConstraint(String e, Component c, Spring s) {
        if (s != null) {
            getConstraints(c).setConstraint(e, s);
        }
     }
    private Constraints applyDefaults(Component c, Constraints constraints) {
        if (constraints == null) {
            constraints = new Constraints();
        }
        if (constraints.c == null) {
            constraints.c = c;
        }
        if (constraints.horizontalHistory.size() < 2) {
            applyDefaults(constraints, WEST, Spring.constant(0), WIDTH,
                          Spring.width(c), constraints.horizontalHistory);
        }
        if (constraints.verticalHistory.size() < 2) {
            applyDefaults(constraints, NORTH, Spring.constant(0), HEIGHT,
                          Spring.height(c), constraints.verticalHistory);
        }
        return constraints;
    }
    private void applyDefaults(Constraints constraints, String name1,
                               Spring spring1, String name2, Spring spring2,
                               List<String> history) {
        if (history.size() == 0) {
            constraints.setConstraint(name1, spring1);
            constraints.setConstraint(name2, spring2);
        } else {
            if (constraints.getConstraint(name2) == null) {
                constraints.setConstraint(name2, spring2);
            } else {
                constraints.setConstraint(name1, spring1);
            }
            Collections.rotate(history, 1);
        }
    }
    private void putConstraints(Component component, Constraints constraints) {
        componentConstraints.put(component, applyDefaults(component, constraints));
    }
    public Constraints getConstraints(Component c) {
       Constraints result = componentConstraints.get(c);
       if (result == null) {
           if (c instanceof javax.swing.JComponent) {
                Object cp = ((javax.swing.JComponent)c).getClientProperty(SpringLayout.class);
                if (cp instanceof Constraints) {
                    return applyDefaults(c, (Constraints)cp);
                }
            }
            result = new Constraints();
            putConstraints(c, result);
       }
       return result;
    }
    public Spring getConstraint(String edgeName, Component c) {
        edgeName = edgeName.intern();
        return new SpringProxy(edgeName, c, this);
    }
    public void layoutContainer(Container parent) {
        setParent(parent);
        int n = parent.getComponentCount();
        getConstraints(parent).reset();
        for (int i = 0 ; i < n ; i++) {
            getConstraints(parent.getComponent(i)).reset();
        }
        Insets insets = parent.getInsets();
        Constraints pc = getConstraints(parent);
        abandonCycles(pc.getX()).setValue(0);
        abandonCycles(pc.getY()).setValue(0);
        abandonCycles(pc.getWidth()).setValue(parent.getWidth() -
                                              insets.left - insets.right);
        abandonCycles(pc.getHeight()).setValue(parent.getHeight() -
                                               insets.top - insets.bottom);
        for (int i = 0 ; i < n ; i++) {
            Component c = parent.getComponent(i);
            Constraints cc = getConstraints(c);
            int x = abandonCycles(cc.getX()).getValue();
            int y = abandonCycles(cc.getY()).getValue();
            int width = abandonCycles(cc.getWidth()).getValue();
            int height = abandonCycles(cc.getHeight()).getValue();
            c.setBounds(insets.left + x, insets.top + y, width, height);
        }
    }
}
