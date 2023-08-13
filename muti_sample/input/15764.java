public class DefaultSynthStyle extends SynthStyle implements Cloneable {
    private static final String PENDING = "Pending";
    private boolean opaque;
    private Insets insets;
    private StateInfo[] states;
    private Map data;
    private Font font;
    private SynthGraphicsUtils synthGraphics;
    private SynthPainter painter;
    public DefaultSynthStyle() {
    }
    public DefaultSynthStyle(DefaultSynthStyle style) {
        opaque = style.opaque;
        if (style.insets != null) {
            insets = new Insets(style.insets.top, style.insets.left,
                                style.insets.bottom, style.insets.right);
        }
        if (style.states != null) {
            states = new StateInfo[style.states.length];
            for (int counter = style.states.length - 1; counter >= 0;
                     counter--) {
                states[counter] = (StateInfo)style.states[counter].clone();
            }
        }
        if (style.data != null) {
            data = new HashMap();
            data.putAll(style.data);
        }
        font = style.font;
        synthGraphics = style.synthGraphics;
        painter = style.painter;
    }
    public DefaultSynthStyle(Insets insets, boolean opaque,
                             StateInfo[] states, Map data) {
        this.insets = insets;
        this.opaque = opaque;
        this.states = states;
        this.data = data;
    }
    public Color getColor(SynthContext context, ColorType type) {
        return getColor(context.getComponent(), context.getRegion(),
                        context.getComponentState(), type);
    }
    public Color getColor(JComponent c, Region id, int state,
                          ColorType type) {
        if (!id.isSubregion() && state == SynthConstants.ENABLED) {
            if (type == ColorType.BACKGROUND) {
                return c.getBackground();
            }
            else if (type == ColorType.FOREGROUND) {
                return c.getForeground();
            }
            else if (type == ColorType.TEXT_FOREGROUND) {
                Color color = c.getForeground();
                if (!(color instanceof UIResource)) {
                    return color;
                }
            }
        }
        Color color = getColorForState(c, id, state, type);
        if (color == null) {
            if (type == ColorType.BACKGROUND ||
                        type == ColorType.TEXT_BACKGROUND) {
                return c.getBackground();
            }
            else if (type == ColorType.FOREGROUND ||
                     type == ColorType.TEXT_FOREGROUND) {
                return c.getForeground();
            }
        }
        return color;
    }
    protected Color getColorForState(SynthContext context, ColorType type) {
        return getColorForState(context.getComponent(), context.getRegion(),
                                context.getComponentState(), type);
    }
    protected Color getColorForState(JComponent c, Region id, int state,
                                     ColorType type) {
        StateInfo si = getStateInfo(state);
        Color color;
        if (si != null && (color = si.getColor(type)) != null) {
            return color;
        }
        if (si == null || si.getComponentState() != 0) {
            si = getStateInfo(0);
            if (si != null) {
                return si.getColor(type);
            }
        }
        return null;
    }
    public void setFont(Font font) {
        this.font = font;
    }
    public Font getFont(SynthContext state) {
        return getFont(state.getComponent(), state.getRegion(),
                       state.getComponentState());
    }
    public Font getFont(JComponent c, Region id, int state) {
        if (!id.isSubregion() && state == SynthConstants.ENABLED) {
            return c.getFont();
        }
        Font cFont = c.getFont();
        if (cFont != null && !(cFont instanceof UIResource)) {
            return cFont;
        }
        return getFontForState(c, id, state);
    }
    protected Font getFontForState(JComponent c, Region id, int state) {
        if (c == null) {
            return this.font;
        }
        StateInfo si = getStateInfo(state);
        Font font;
        if (si != null && (font = si.getFont()) != null) {
            return font;
        }
        if (si == null || si.getComponentState() != 0) {
            si = getStateInfo(0);
            if (si != null && (font = si.getFont()) != null) {
                return font;
            }
        }
        return this.font;
    }
    protected Font getFontForState(SynthContext context) {
        return getFontForState(context.getComponent(), context.getRegion(),
                               context.getComponentState());
    }
    public void setGraphicsUtils(SynthGraphicsUtils graphics) {
        this.synthGraphics = graphics;
    }
    public SynthGraphicsUtils getGraphicsUtils(SynthContext context) {
        if (synthGraphics == null) {
            return super.getGraphicsUtils(context);
        }
        return synthGraphics;
    }
    public void setInsets(Insets insets) {
        this.insets = insets;
    }
    public Insets getInsets(SynthContext state, Insets to) {
        if (to == null) {
            to = new Insets(0, 0, 0, 0);
        }
        if (insets != null) {
            to.left = insets.left;
            to.right = insets.right;
            to.top = insets.top;
            to.bottom = insets.bottom;
        }
        else {
            to.left = to.right = to.top = to.bottom = 0;
        }
        return to;
    }
    public void setPainter(SynthPainter painter) {
        this.painter = painter;
    }
    public SynthPainter getPainter(SynthContext ss) {
        return painter;
    }
    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }
    public boolean isOpaque(SynthContext ss) {
        return opaque;
    }
    public void setData(Map data) {
        this.data = data;
    }
    public Map getData() {
        return data;
    }
    public Object get(SynthContext state, Object key) {
        StateInfo si = getStateInfo(state.getComponentState());
        if (si != null && si.getData() != null && getKeyFromData(si.getData(), key) != null) {
            return getKeyFromData(si.getData(), key);
        }
        si = getStateInfo(0);
        if (si != null && si.getData() != null && getKeyFromData(si.getData(), key) != null) {
            return getKeyFromData(si.getData(), key);
        }
        if(getKeyFromData(data, key) != null)
          return getKeyFromData(data, key);
        return getDefaultValue(state, key);
    }
    private Object getKeyFromData(Map stateData, Object key) {
          Object value = null;
          if (stateData != null) {
            synchronized(stateData) {
                value = stateData.get(key);
            }
            while (value == PENDING) {
                synchronized(stateData) {
                    try {
                        stateData.wait();
                    } catch (InterruptedException ie) {}
                    value = stateData.get(key);
                }
            }
            if (value instanceof UIDefaults.LazyValue) {
                synchronized(stateData) {
                    stateData.put(key, PENDING);
                }
                value = ((UIDefaults.LazyValue)value).createValue(null);
                synchronized(stateData) {
                    stateData.put(key, value);
                    stateData.notifyAll();
                }
            }
        }
        return value;
    }
    public Object getDefaultValue(SynthContext context, Object key) {
        return super.get(context, key);
    }
    public Object clone() {
        DefaultSynthStyle style;
        try {
            style = (DefaultSynthStyle)super.clone();
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
        if (states != null) {
            style.states = new StateInfo[states.length];
            for (int counter = states.length - 1; counter >= 0; counter--) {
                style.states[counter] = (StateInfo)states[counter].clone();
            }
        }
        if (data != null) {
            style.data = new HashMap();
            style.data.putAll(data);
        }
        return style;
    }
    public DefaultSynthStyle addTo(DefaultSynthStyle style) {
        if (insets != null) {
            style.insets = this.insets;
        }
        if (font != null) {
            style.font = this.font;
        }
        if (painter != null) {
            style.painter = this.painter;
        }
        if (synthGraphics != null) {
            style.synthGraphics = this.synthGraphics;
        }
        style.opaque = opaque;
        if (states != null) {
            if (style.states == null) {
                style.states = new StateInfo[states.length];
                for (int counter = states.length - 1; counter >= 0; counter--){
                    if (states[counter] != null) {
                        style.states[counter] = (StateInfo)states[counter].
                                                clone();
                    }
                }
            }
            else {
                int unique = 0;
                int matchCount = 0;
                int maxOStyles = style.states.length;
                for (int thisCounter = states.length - 1; thisCounter >= 0;
                         thisCounter--) {
                    int state = states[thisCounter].getComponentState();
                    boolean found = false;
                    for (int oCounter = maxOStyles - 1 - matchCount;
                             oCounter >= 0; oCounter--) {
                        if (state == style.states[oCounter].
                                           getComponentState()) {
                            style.states[oCounter] = states[thisCounter].
                                        addTo(style.states[oCounter]);
                            StateInfo tmp = style.states[maxOStyles - 1 -
                                                         matchCount];
                            style.states[maxOStyles - 1 - matchCount] =
                                  style.states[oCounter];
                            style.states[oCounter] = tmp;
                            matchCount++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        unique++;
                    }
                }
                if (unique != 0) {
                    StateInfo[] newStates = new StateInfo[
                                   unique + maxOStyles];
                    int newIndex = maxOStyles;
                    System.arraycopy(style.states, 0, newStates, 0,maxOStyles);
                    for (int thisCounter = states.length - 1; thisCounter >= 0;
                             thisCounter--) {
                        int state = states[thisCounter].getComponentState();
                        boolean found = false;
                        for (int oCounter = maxOStyles - 1; oCounter >= 0;
                                 oCounter--) {
                            if (state == style.states[oCounter].
                                               getComponentState()) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newStates[newIndex++] = (StateInfo)states[
                                      thisCounter].clone();
                        }
                    }
                    style.states = newStates;
                }
            }
        }
        if (data != null) {
            if (style.data == null) {
                style.data = new HashMap();
            }
            style.data.putAll(data);
        }
        return style;
    }
    public void setStateInfo(StateInfo[] states) {
        this.states = states;
    }
    public StateInfo[] getStateInfo() {
        return states;
    }
    public StateInfo getStateInfo(int state) {
        if (states != null) {
            int bestCount = 0;
            int bestIndex = -1;
            int wildIndex = -1;
            if (state == 0) {
                for (int counter = states.length - 1; counter >= 0;counter--) {
                    if (states[counter].getComponentState() == 0) {
                        return states[counter];
                    }
                }
                return null;
            }
            for (int counter = states.length - 1; counter >= 0; counter--) {
                int oState = states[counter].getComponentState();
                if (oState == 0) {
                    if (wildIndex == -1) {
                        wildIndex = counter;
                    }
                }
                else if ((state & oState) == oState) {
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
                return states[bestIndex];
            }
            if (wildIndex != -1) {
                return states[wildIndex];
            }
          }
          return null;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString()).append(',');
        buf.append("data=").append(data).append(',');
        buf.append("font=").append(font).append(',');
        buf.append("insets=").append(insets).append(',');
        buf.append("synthGraphics=").append(synthGraphics).append(',');
        buf.append("painter=").append(painter).append(',');
        StateInfo[] states = getStateInfo();
        if (states != null) {
            buf.append("states[");
            for (StateInfo state : states) {
                buf.append(state.toString()).append(',');
            }
            buf.append(']').append(',');
        }
        buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }
    public static class StateInfo {
        private Map data;
        private Font font;
        private Color[] colors;
        private int state;
        public StateInfo() {
        }
        public StateInfo(int state, Font font, Color[] colors) {
            this.state = state;
            this.font = font;
            this.colors = colors;
        }
        public StateInfo(StateInfo info) {
            this.state = info.state;
            this.font = info.font;
            if(info.data != null) {
               if(data == null) {
                  data = new HashMap();
               }
               data.putAll(info.data);
            }
            if (info.colors != null) {
                this.colors = new Color[info.colors.length];
                System.arraycopy(info.colors, 0, colors, 0,info.colors.length);
            }
        }
        public Map getData() {
            return data;
        }
        public void setData(Map data) {
            this.data = data;
        }
        public void setFont(Font font) {
            this.font = font;
        }
        public Font getFont() {
            return font;
        }
        public void setColors(Color[] colors) {
            this.colors = colors;
        }
        public Color[] getColors() {
            return colors;
        }
        public Color getColor(ColorType type) {
            if (colors != null) {
                int id = type.getID();
                if (id < colors.length) {
                    return colors[id];
                }
            }
            return null;
        }
        public StateInfo addTo(StateInfo info) {
            if (font != null) {
                info.font = font;
            }
            if(data != null) {
                if(info.data == null) {
                    info.data = new HashMap();
                }
                info.data.putAll(data);
            }
            if (colors != null) {
                if (info.colors == null) {
                    info.colors = new Color[colors.length];
                    System.arraycopy(colors, 0, info.colors, 0,
                                     colors.length);
                }
                else {
                    if (info.colors.length < colors.length) {
                        Color[] old = info.colors;
                        info.colors = new Color[colors.length];
                        System.arraycopy(old, 0, info.colors, 0, old.length);
                    }
                    for (int counter = colors.length - 1; counter >= 0;
                             counter--) {
                        if (colors[counter] != null) {
                            info.colors[counter] = colors[counter];
                        }
                    }
                }
            }
            return info;
        }
        public void setComponentState(int state) {
            this.state = state;
        }
        public int getComponentState() {
            return state;
        }
        private int getMatchCount(int val) {
            val &= state;
            val -= (0xaaaaaaaa & val) >>> 1;
            val = (val & 0x33333333) + ((val >>> 2) & 0x33333333);
            val = val + (val >>> 4) & 0x0f0f0f0f;
            val += val >>> 8;
            val += val >>> 16;
            return val & 0xff;
        }
        public Object clone() {
            return new StateInfo(this);
        }
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(super.toString()).append(',');
            buf.append("state=").append(Integer.toString(state)).append(',');
            buf.append("font=").append(font).append(',');
            if (colors != null) {
                buf.append("colors=").append(Arrays.asList(colors)).
                    append(',');
            }
            return buf.toString();
        }
    }
}
