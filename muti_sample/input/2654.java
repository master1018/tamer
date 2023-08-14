class GTKStyleFactory extends SynthStyleFactory {
    private final Map<Object, GTKStyle> stylesCache;
    private Font defaultFont;
    GTKStyleFactory() {
        stylesCache = new HashMap<Object, GTKStyle>();
    }
    public synchronized SynthStyle getStyle(JComponent c, Region id) {
        WidgetType wt = GTKEngine.getWidgetType(c, id);
        Object key = null;
        if (id == Region.SCROLL_BAR) {
            if (c != null) {
                JScrollBar sb = (JScrollBar)c;
                boolean sp = (sb.getParent() instanceof JScrollPane);
                boolean horiz = (sb.getOrientation() == JScrollBar.HORIZONTAL);
                boolean ltr = sb.getComponentOrientation().isLeftToRight();
                boolean focusable = sb.isFocusable();
                key = new ComplexKey(wt, sp, horiz, ltr, focusable);
            }
        }
        else if (id == Region.CHECK_BOX || id == Region.RADIO_BUTTON) {
            if (c != null) {
                boolean ltr = c.getComponentOrientation().isLeftToRight();
                key = new ComplexKey(wt, ltr);
            }
        }
        else if (id == Region.BUTTON) {
            if (c != null) {
                JButton btn = (JButton)c;
                boolean toolButton = (btn.getParent() instanceof JToolBar);
                boolean defaultCapable = btn.isDefaultCapable();
                key = new ComplexKey(wt, toolButton, defaultCapable);
            }
        }
        if (key == null) {
            key = wt;
        }
        GTKStyle result = stylesCache.get(key);
        if (result == null) {
            result = new GTKStyle(defaultFont, wt);
            stylesCache.put(key, result);
        }
        return result;
    }
    void initStyles(Font defaultFont) {
        this.defaultFont = defaultFont;
        stylesCache.clear();
    }
    private static class ComplexKey {
        private final WidgetType wt;
        private final Object[] args;
        ComplexKey(WidgetType wt, Object... args) {
            this.wt = wt;
            this.args = args;
        }
        @Override
        public int hashCode() {
            int hash = wt.hashCode();
            if (args != null) {
                for (Object arg : args) {
                    hash = hash*29 + (arg == null ? 0 : arg.hashCode());
                }
            }
            return hash;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ComplexKey)) {
                return false;
            }
            ComplexKey that = (ComplexKey)o;
            if (this.wt == that.wt) {
                if (this.args == null && that.args == null) {
                    return true;
                }
                if (this.args != null && that.args != null &&
                    this.args.length == that.args.length)
                {
                    for (int i = 0; i < this.args.length; i++) {
                        Object a1 = this.args[i];
                        Object a2 = that.args[i];
                        if (!(a1==null ? a2==null : a1.equals(a2))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        @Override
        public String toString() {
            String str = "ComplexKey[wt=" + wt;
            if (args != null) {
                str += ",args=[";
                for (int i = 0; i < args.length; i++) {
                    str += args[i];
                    if (i < args.length-1) str += ",";
                }
                str += "]";
            }
            str += "]";
            return str;
        }
    }
}
