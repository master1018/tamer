public abstract class Spring {
    public static final int UNSET = Integer.MIN_VALUE;
    protected Spring() {}
    public abstract int getMinimumValue();
    public abstract int getPreferredValue();
    public abstract int getMaximumValue();
    public abstract int getValue();
    public abstract void setValue(int value);
    private double range(boolean contract) {
        return contract ? (getPreferredValue() - getMinimumValue()) :
                          (getMaximumValue() - getPreferredValue());
    }
     double getStrain() {
        double delta = (getValue() - getPreferredValue());
        return delta/range(getValue() < getPreferredValue());
    }
     void setStrain(double strain) {
        setValue(getPreferredValue() + (int)(strain * range(strain < 0)));
    }
     boolean isCyclic(SpringLayout l) {
        return false;
    }
     static abstract class AbstractSpring extends Spring {
        protected int size = UNSET;
        public int getValue() {
            return size != UNSET ? size : getPreferredValue();
        }
        public final void setValue(int size) {
            if (this.size == size) {
                return;
            }
            if (size == UNSET) {
                clear();
            } else {
                setNonClearValue(size);
            }
        }
        protected void clear() {
            size = UNSET;
        }
        protected void setNonClearValue(int size) {
            this.size = size;
        }
    }
    private static class StaticSpring extends AbstractSpring {
        protected int min;
        protected int pref;
        protected int max;
        public StaticSpring(int pref) {
            this(pref, pref, pref);
        }
        public StaticSpring(int min, int pref, int max) {
            this.min = min;
            this.pref = pref;
            this.max = max;
        }
         public String toString() {
             return "StaticSpring [" + min + ", " + pref + ", " + max + "]";
         }
         public int getMinimumValue() {
            return min;
        }
        public int getPreferredValue() {
            return pref;
        }
        public int getMaximumValue() {
            return max;
        }
    }
    private static class NegativeSpring extends Spring {
        private Spring s;
        public NegativeSpring(Spring s) {
            this.s = s;
        }
        public int getMinimumValue() {
            return -s.getMaximumValue();
        }
        public int getPreferredValue() {
            return -s.getPreferredValue();
        }
        public int getMaximumValue() {
            return -s.getMinimumValue();
        }
        public int getValue() {
            return -s.getValue();
        }
        public void setValue(int size) {
            s.setValue(-size);
        }
         boolean isCyclic(SpringLayout l) {
            return s.isCyclic(l);
        }
    }
    private static class ScaleSpring extends Spring {
        private Spring s;
        private float factor;
        private ScaleSpring(Spring s, float factor) {
            this.s = s;
            this.factor = factor;
        }
        public int getMinimumValue() {
            return Math.round((factor < 0 ? s.getMaximumValue() : s.getMinimumValue()) * factor);
        }
        public int getPreferredValue() {
            return Math.round(s.getPreferredValue() * factor);
        }
        public int getMaximumValue() {
            return Math.round((factor < 0 ? s.getMinimumValue() : s.getMaximumValue()) * factor);
        }
        public int getValue() {
            return Math.round(s.getValue() * factor);
        }
        public void setValue(int value) {
            if (value == UNSET) {
                s.setValue(UNSET);
            } else {
                s.setValue(Math.round(value / factor));
            }
        }
         boolean isCyclic(SpringLayout l) {
            return s.isCyclic(l);
        }
    }
     static class WidthSpring extends AbstractSpring {
         Component c;
        public WidthSpring(Component c) {
            this.c = c;
        }
        public int getMinimumValue() {
            return c.getMinimumSize().width;
        }
        public int getPreferredValue() {
            return c.getPreferredSize().width;
        }
        public int getMaximumValue() {
            return Math.min(Short.MAX_VALUE, c.getMaximumSize().width);
        }
    }
       static class HeightSpring extends AbstractSpring {
         Component c;
        public HeightSpring(Component c) {
            this.c = c;
        }
        public int getMinimumValue() {
            return c.getMinimumSize().height;
        }
        public int getPreferredValue() {
            return c.getPreferredSize().height;
        }
        public int getMaximumValue() {
            return Math.min(Short.MAX_VALUE, c.getMaximumSize().height);
        }
    }
    static abstract class SpringMap extends Spring {
       private Spring s;
       public SpringMap(Spring s) {
           this.s = s;
       }
       protected abstract int map(int i);
       protected abstract int inv(int i);
       public int getMinimumValue() {
           return map(s.getMinimumValue());
       }
       public int getPreferredValue() {
           return map(s.getPreferredValue());
       }
       public int getMaximumValue() {
           return Math.min(Short.MAX_VALUE, map(s.getMaximumValue()));
       }
       public int getValue() {
           return map(s.getValue());
       }
       public void setValue(int value) {
           if (value == UNSET) {
               s.setValue(UNSET);
           } else {
               s.setValue(inv(value));
           }
       }
        boolean isCyclic(SpringLayout l) {
           return s.isCyclic(l);
       }
   }
     static abstract class CompoundSpring extends StaticSpring {
        protected Spring s1;
        protected Spring s2;
        public CompoundSpring(Spring s1, Spring s2) {
            super(UNSET);
            this.s1 = s1;
            this.s2 = s2;
        }
        public String toString() {
            return "CompoundSpring of " + s1 + " and " + s2;
        }
        protected void clear() {
            super.clear();
            min = pref = max = UNSET;
            s1.setValue(UNSET);
            s2.setValue(UNSET);
        }
        protected abstract int op(int x, int y);
        public int getMinimumValue() {
            if (min == UNSET) {
                min = op(s1.getMinimumValue(), s2.getMinimumValue());
            }
            return min;
        }
        public int getPreferredValue() {
            if (pref == UNSET) {
                pref = op(s1.getPreferredValue(), s2.getPreferredValue());
            }
            return pref;
        }
        public int getMaximumValue() {
            if (max == UNSET) {
                max = op(s1.getMaximumValue(), s2.getMaximumValue());
            }
            return max;
        }
        public int getValue() {
            if (size == UNSET) {
                size = op(s1.getValue(), s2.getValue());
            }
            return size;
        }
         boolean isCyclic(SpringLayout l) {
            return l.isCyclic(s1) || l.isCyclic(s2);
        }
    };
     private static class SumSpring extends CompoundSpring {
         public SumSpring(Spring s1, Spring s2) {
             super(s1, s2);
         }
         protected int op(int x, int y) {
             return x + y;
         }
         protected void setNonClearValue(int size) {
             super.setNonClearValue(size);
             s1.setStrain(this.getStrain());
             s2.setValue(size - s1.getValue());
         }
     }
    private static class MaxSpring extends CompoundSpring {
        public MaxSpring(Spring s1, Spring s2) {
            super(s1, s2);
        }
        protected int op(int x, int y) {
            return Math.max(x, y);
        }
        protected void setNonClearValue(int size) {
            super.setNonClearValue(size);
            s1.setValue(size);
            s2.setValue(size);
        }
    }
     public static Spring constant(int pref) {
         return constant(pref, pref, pref);
     }
     public static Spring constant(int min, int pref, int max) {
         return new StaticSpring(min, pref, max);
     }
    public static Spring minus(Spring s) {
        return new NegativeSpring(s);
    }
     public static Spring sum(Spring s1, Spring s2) {
         return new SumSpring(s1, s2);
     }
    public static Spring max(Spring s1, Spring s2) {
        return new MaxSpring(s1, s2);
    }
     static Spring difference(Spring s1, Spring s2) {
        return sum(s1, minus(s2));
    }
    public static Spring scale(Spring s, float factor) {
        checkArg(s);
        return new ScaleSpring(s, factor);
    }
    public static Spring width(Component c) {
        checkArg(c);
        return new WidthSpring(c);
    }
    public static Spring height(Component c) {
        checkArg(c);
        return new HeightSpring(c);
    }
    private static void checkArg(Object s) {
        if (s == null) {
            throw new NullPointerException("Argument must not be null");
        }
    }
}
