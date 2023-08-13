public final class NimbusStyle extends SynthStyle {
    public static final String LARGE_KEY = "large";
    public static final String SMALL_KEY = "small";
    public static final String MINI_KEY = "mini";
    public static final double LARGE_SCALE = 1.15;
    public static final double SMALL_SCALE = 0.857;
    public static final double MINI_SCALE = 0.714;
    private static final Object NULL = '\0';
    private static final Color DEFAULT_COLOR = new ColorUIResource(Color.BLACK);
    private static final Comparator<RuntimeState> STATE_COMPARATOR =
        new Comparator<RuntimeState>() {
            @Override
            public int compare(RuntimeState a, RuntimeState b) {
                return a.state - b.state;
            }
        };
    private String prefix;
    private SynthPainter painter;
    private Values values;
    private CacheKey tmpKey = new CacheKey("", 0);
    private WeakReference<JComponent> component;
    NimbusStyle(String prefix, JComponent c) {
        if (c != null) {
            this.component = new WeakReference<JComponent>(c);
        }
        this.prefix = prefix;
        this.painter = new SynthPainterImpl(this);
    }
    @Override public void installDefaults(SynthContext ctx) {
        validate();
        super.installDefaults(ctx);
    }
    private void validate() {
        if (values != null) return;
        values = new Values();
        Map<String, Object> defaults =
                ((NimbusLookAndFeel) UIManager.getLookAndFeel()).
                        getDefaultsForPrefix(prefix);
        if (component != null) {
            Object o = component.get().getClientProperty("Nimbus.Overrides");
            if (o instanceof UIDefaults) {
                Object i = component.get().getClientProperty(
                        "Nimbus.Overrides.InheritDefaults");
                boolean inherit = i instanceof Boolean ? (Boolean)i : true;
                UIDefaults d = (UIDefaults)o;
                TreeMap<String, Object> map = new TreeMap<String, Object>();
                for (Object obj : d.keySet()) {
                    if (obj instanceof String) {
                        String key = (String)obj;
                        if (key.startsWith(prefix)) {
                            map.put(key, d.get(key));
                        }
                    }
                }
                if (inherit) {
                    defaults.putAll(map);
                } else {
                    defaults = map;
                }
            }
        }
        List<State> states = new ArrayList<State>();
        Map<String,Integer> stateCodes = new HashMap<String,Integer>();
        List<RuntimeState> runtimeStates = new ArrayList<RuntimeState>();
        String statesString = (String)defaults.get(prefix + ".States");
        if (statesString != null) {
            String s[] = statesString.split(",");
            for (int i=0; i<s.length; i++) {
                s[i] = s[i].trim();
                if (!State.isStandardStateName(s[i])) {
                    String stateName = prefix + "." + s[i];
                    State customState = (State)defaults.get(stateName);
                    if (customState != null) {
                        states.add(customState);
                    }
                } else {
                    states.add(State.getStandardState(s[i]));
                }
            }
            if (states.size() > 0) {
                values.stateTypes = states.toArray(new State[states.size()]);
            }
            int code = 1;
            for (State state : states) {
                stateCodes.put(state.getName(), code);
                code <<= 1;
            }
        } else {
            states.add(State.Enabled);
            states.add(State.MouseOver);
            states.add(State.Pressed);
            states.add(State.Disabled);
            states.add(State.Focused);
            states.add(State.Selected);
            states.add(State.Default);
            stateCodes.put("Enabled", ENABLED);
            stateCodes.put("MouseOver", MOUSE_OVER);
            stateCodes.put("Pressed", PRESSED);
            stateCodes.put("Disabled", DISABLED);
            stateCodes.put("Focused", FOCUSED);
            stateCodes.put("Selected", SELECTED);
            stateCodes.put("Default", DEFAULT);
        }
        for (String key : defaults.keySet()) {
            String temp = key.substring(prefix.length());
            if (temp.indexOf('"') != -1 || temp.indexOf(':') != -1) continue;
            temp = temp.substring(1);
            String stateString = null;
            String property = null;
            int bracketIndex = temp.indexOf(']');
            if (bracketIndex < 0) {
                property = temp;
            } else {
                stateString = temp.substring(0, bracketIndex);
                property = temp.substring(bracketIndex + 2);
            }
            if (stateString == null) {
                if ("contentMargins".equals(property)) {
                    values.contentMargins = (Insets)defaults.get(key);
                } else if ("States".equals(property)) {
                } else {
                    values.defaults.put(property, defaults.get(key));
                }
            } else {
                boolean skip = false;
                int componentState = 0;
                String[] stateParts = stateString.split("\\+");
                for (String s : stateParts) {
                    if (stateCodes.containsKey(s)) {
                        componentState |= stateCodes.get(s);
                    } else {
                        skip = true;
                        break;
                    }
                }
                if (skip) continue;
                RuntimeState rs = null;
                for (RuntimeState s : runtimeStates) {
                    if (s.state == componentState) {
                        rs = s;
                        break;
                    }
                }
                if (rs == null) {
                    rs = new RuntimeState(componentState, stateString);
                    runtimeStates.add(rs);
                }
                if ("backgroundPainter".equals(property)) {
                    rs.backgroundPainter = getPainter(defaults, key);
                } else if ("foregroundPainter".equals(property)) {
                    rs.foregroundPainter = getPainter(defaults, key);
                } else if ("borderPainter".equals(property)) {
                    rs.borderPainter = getPainter(defaults, key);
                } else {
                    rs.defaults.put(property, defaults.get(key));
                }
            }
        }
        Collections.sort(runtimeStates, STATE_COMPARATOR);
        values.states = runtimeStates.toArray(new RuntimeState[runtimeStates.size()]);
    }
    private Painter getPainter(Map<String, Object> defaults, String key) {
        Object p = defaults.get(key);
        if (p instanceof UIDefaults.LazyValue) {
            p = ((UIDefaults.LazyValue)p).createValue(UIManager.getDefaults());
        }
        return (p instanceof Painter ? (Painter)p : null);
    }
    @Override public Insets getInsets(SynthContext ctx, Insets in) {
        if (in == null) {
            in = new Insets(0, 0, 0, 0);
        }
        Values v = getValues(ctx);
        if (v.contentMargins == null) {
            in.bottom = in.top = in.left = in.right = 0;
            return in;
        } else {
            in.bottom = v.contentMargins.bottom;
            in.top = v.contentMargins.top;
            in.left = v.contentMargins.left;
            in.right = v.contentMargins.right;
            String scaleKey = (String)ctx.getComponent().getClientProperty(
                    "JComponent.sizeVariant");
            if (scaleKey != null){
                if (LARGE_KEY.equals(scaleKey)){
                    in.bottom *= LARGE_SCALE;
                    in.top *= LARGE_SCALE;
                    in.left *= LARGE_SCALE;
                    in.right *= LARGE_SCALE;
                } else if (SMALL_KEY.equals(scaleKey)){
                    in.bottom *= SMALL_SCALE;
                    in.top *= SMALL_SCALE;
                    in.left *= SMALL_SCALE;
                    in.right *= SMALL_SCALE;
                } else if (MINI_KEY.equals(scaleKey)){
                    in.bottom *= MINI_SCALE;
                    in.top *= MINI_SCALE;
                    in.left *= MINI_SCALE;
                    in.right *= MINI_SCALE;
                }
            }
            return in;
        }
    }
    @Override protected Color getColorForState(SynthContext ctx, ColorType type) {
        String key = null;
        if (type == ColorType.BACKGROUND) {
            key = "background";
        } else if (type == ColorType.FOREGROUND) {
            key = "textForeground";
        } else if (type == ColorType.TEXT_BACKGROUND) {
            key = "textBackground";
        } else if (type == ColorType.TEXT_FOREGROUND) {
            key = "textForeground";
        } else if (type == ColorType.FOCUS) {
            key = "focus";
        } else if (type != null) {
            key = type.toString();
        } else {
            return DEFAULT_COLOR;
        }
        Color c = (Color) get(ctx, key);
        if (c == null) c = DEFAULT_COLOR;
        return c;
    }
    @Override protected Font getFontForState(SynthContext ctx) {
        Font f = (Font)get(ctx, "font");
        if (f == null) f = UIManager.getFont("defaultFont");
        String scaleKey = (String)ctx.getComponent().getClientProperty(
                "JComponent.sizeVariant");
        if (scaleKey != null){
            if (LARGE_KEY.equals(scaleKey)){
                f = f.deriveFont(Math.round(f.getSize2D()*LARGE_SCALE));
            } else if (SMALL_KEY.equals(scaleKey)){
                f = f.deriveFont(Math.round(f.getSize2D()*SMALL_SCALE));
            } else if (MINI_KEY.equals(scaleKey)){
                f = f.deriveFont(Math.round(f.getSize2D()*MINI_SCALE));
            }
        }
        return f;
    }
    @Override public SynthPainter getPainter(SynthContext ctx) {
        return painter;
    }
    @Override public boolean isOpaque(SynthContext ctx) {
        if ("Table.cellRenderer".equals(ctx.getComponent().getName())) {
            return true;
        }
        Boolean opaque = (Boolean)get(ctx, "opaque");
        return opaque == null ? false : opaque;
    }
    @Override public Object get(SynthContext ctx, Object key) {
        Values v = getValues(ctx);
        String fullKey = key.toString();
        String partialKey = fullKey.substring(fullKey.indexOf(".") + 1);
        Object obj = null;
        int xstate = getExtendedState(ctx, v);
        tmpKey.init(partialKey, xstate);
        obj = v.cache.get(tmpKey);
        boolean wasInCache = obj != null;
        if (!wasInCache){
            RuntimeState s = null;
            int[] lastIndex = new int[] {-1};
            while (obj == null &&
                    (s = getNextState(v.states, lastIndex, xstate)) != null) {
                obj = s.defaults.get(partialKey);
            }
            if (obj == null && v.defaults != null) {
                obj = v.defaults.get(partialKey);
            }
            if (obj == null) obj = UIManager.get(fullKey);
            if (obj == null && partialKey.equals("focusInputMap")) {
                obj = super.get(ctx, fullKey);
            }
            v.cache.put(new CacheKey(partialKey, xstate),
                    obj == null ? NULL : obj);
        }
        return obj == NULL ? null : obj;
    }
    public Painter getBackgroundPainter(SynthContext ctx) {
        Values v = getValues(ctx);
        int xstate = getExtendedState(ctx, v);
        Painter p = null;
        tmpKey.init("backgroundPainter$$instance", xstate);
        p = (Painter)v.cache.get(tmpKey);
        if (p != null) return p;
        RuntimeState s = null;
        int[] lastIndex = new int[] {-1};
        while ((s = getNextState(v.states, lastIndex, xstate)) != null) {
            if (s.backgroundPainter != null) {
                p = s.backgroundPainter;
                break;
            }
        }
        if (p == null) p = (Painter)get(ctx, "backgroundPainter");
        if (p != null) {
            v.cache.put(new CacheKey("backgroundPainter$$instance", xstate), p);
        }
        return p;
    }
    public Painter getForegroundPainter(SynthContext ctx) {
        Values v = getValues(ctx);
        int xstate = getExtendedState(ctx, v);
        Painter p = null;
        tmpKey.init("foregroundPainter$$instance", xstate);
        p = (Painter)v.cache.get(tmpKey);
        if (p != null) return p;
        RuntimeState s = null;
        int[] lastIndex = new int[] {-1};
        while ((s = getNextState(v.states, lastIndex, xstate)) != null) {
            if (s.foregroundPainter != null) {
                p = s.foregroundPainter;
                break;
            }
        }
        if (p == null) p = (Painter)get(ctx, "foregroundPainter");
        if (p != null) {
            v.cache.put(new CacheKey("foregroundPainter$$instance", xstate), p);
        }
        return p;
    }
    public Painter getBorderPainter(SynthContext ctx) {
        Values v = getValues(ctx);
        int xstate = getExtendedState(ctx, v);
        Painter p = null;
        tmpKey.init("borderPainter$$instance", xstate);
        p = (Painter)v.cache.get(tmpKey);
        if (p != null) return p;
        RuntimeState s = null;
        int[] lastIndex = new int[] {-1};
        while ((s = getNextState(v.states, lastIndex, xstate)) != null) {
            if (s.borderPainter != null) {
                p = s.borderPainter;
                break;
            }
        }
        if (p == null) p = (Painter)get(ctx, "borderPainter");
        if (p != null) {
            v.cache.put(new CacheKey("borderPainter$$instance", xstate), p);
        }
        return p;
    }
    private Values getValues(SynthContext ctx) {
        validate();
        return values;
    }
    private boolean contains(String[] names, String name) {
        assert name != null;
        for (int i=0; i<names.length; i++) {
            if (name.equals(names[i])) {
                return true;
            }
        }
        return false;
    }
    private int getExtendedState(SynthContext ctx, Values v) {
        JComponent c = ctx.getComponent();
        int xstate = 0;
        int mask = 1;
        Object property = c.getClientProperty("Nimbus.State");
        if (property != null) {
            String stateNames = property.toString();
            String[] states = stateNames.split("\\+");
            if (v.stateTypes == null){
                for (String stateStr : states) {
                    State.StandardState s = State.getStandardState(stateStr);
                    if (s != null) xstate |= s.getState();
                }
            } else {
                for (State s : v.stateTypes) {
                    if (contains(states, s.getName())) {
                        xstate |= mask;
                    }
                    mask <<= 1;
                }
            }
        } else {
            if (v.stateTypes == null) return ctx.getComponentState();
            int state = ctx.getComponentState();
            for (State s : v.stateTypes) {
                if (s.isInState(c, state)) {
                    xstate |= mask;
                }
                mask <<= 1;
            }
        }
        return xstate;
    }
    private RuntimeState getNextState(RuntimeState[] states,
                                      int[] lastState,
                                      int xstate) {
        if (states != null && states.length > 0) {
            int bestCount = 0;
            int bestIndex = -1;
            int wildIndex = -1;
            if (xstate == 0) {
                for (int counter = states.length - 1; counter >= 0; counter--) {
                    if (states[counter].state == 0) {
                        lastState[0] = counter;
                        return states[counter];
                    }
                }
                lastState[0] = -1;
                return null;
            }
            int lastStateIndex = lastState == null || lastState[0] == -1 ?
                states.length : lastState[0];
            for (int counter = lastStateIndex - 1; counter >= 0; counter--) {
                int oState = states[counter].state;
                if (oState == 0) {
                    if (wildIndex == -1) {
                        wildIndex = counter;
                    }
                } else if ((xstate & oState) == oState) {
                    int bitCount = oState;
                    bitCount -= (0xaaaaaaaa & bitCount) >>> 1;
                    bitCount = (bitCount & 0x33333333) + ((bitCount >>> 2) &
                            0x33333333);
                    bitCount = bitCount + (bitCount >>> 4) & 0x0f0f0f0f;
                    bitCount += bitCount >>> 8;
                    bitCount += bitCount >>> 16;
                    bitCount = bitCount & 0xff;
                    if (bitCount > bestCount) {
                        bestIndex = counter;
                        bestCount = bitCount;
                    }
                }
            }
            if (bestIndex != -1) {
                lastState[0] = bestIndex;
                return states[bestIndex];
            }
            if (wildIndex != -1) {
                lastState[0] = wildIndex;
                return states[wildIndex];
            }
        }
        lastState[0] = -1;
        return null;
    }
    private final class RuntimeState implements Cloneable {
        int state;
        Painter backgroundPainter;
        Painter foregroundPainter;
        Painter borderPainter;
        String stateName;
        UIDefaults defaults = new UIDefaults(10, .7f);
        private RuntimeState(int state, String stateName) {
            this.state = state;
            this.stateName = stateName;
        }
        @Override
        public String toString() {
            return stateName;
        }
        @Override
        public RuntimeState clone() {
            RuntimeState clone = new RuntimeState(state, stateName);
            clone.backgroundPainter = backgroundPainter;
            clone.foregroundPainter = foregroundPainter;
            clone.borderPainter = borderPainter;
            clone.defaults.putAll(defaults);
            return clone;
        }
    }
    private static final class Values {
        State[] stateTypes = null;
        RuntimeState[] states = null;
        Insets contentMargins;
        UIDefaults defaults = new UIDefaults(10, .7f);
        Map<CacheKey,Object> cache = new HashMap<CacheKey,Object>();
    }
    private static final class CacheKey {
        private String key;
        private int xstate;
        CacheKey(Object key, int xstate) {
            init(key, xstate);
        }
        void init(Object key, int xstate) {
            this.key = key.toString();
            this.xstate = xstate;
        }
        @Override
        public boolean equals(Object obj) {
            final CacheKey other = (CacheKey) obj;
            if (obj == null) return false;
            if (this.xstate != other.xstate) return false;
            if (!this.key.equals(other.key)) return false;
            return true;
        }
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.key.hashCode();
            hash = 29 * hash + this.xstate;
            return hash;
        }
    }
}
