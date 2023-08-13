public class SynthContext {
    private static final Map<Class, List<SynthContext>> contextMap;
    private JComponent component;
    private Region region;
    private SynthStyle style;
    private int state;
    static {
        contextMap = new HashMap<Class, List<SynthContext>>();
    }
    static SynthContext getContext(Class type, JComponent component,
                                   Region region, SynthStyle style,
                                   int state) {
        SynthContext context = null;
        synchronized(contextMap) {
            List<SynthContext> instances = contextMap.get(type);
            if (instances != null) {
                int size = instances.size();
                if (size > 0) {
                    context = instances.remove(size - 1);
                }
            }
        }
        if (context == null) {
            try {
                context = (SynthContext)type.newInstance();
            } catch (IllegalAccessException iae) {
            } catch (InstantiationException ie) {
            }
        }
        context.reset(component, region, style, state);
        return context;
    }
    static void releaseContext(SynthContext context) {
        synchronized(contextMap) {
            List<SynthContext> instances = contextMap.get(context.getClass());
            if (instances == null) {
                instances = new ArrayList<SynthContext>(5);
                contextMap.put(context.getClass(), instances);
            }
            instances.add(context);
        }
    }
    SynthContext() {
    }
    public SynthContext(JComponent component, Region region, SynthStyle style,
                        int state) {
        if (component == null || region == null || style == null) {
            throw new NullPointerException(
                "You must supply a non-null component, region and style");
        }
        reset(component, region, style, state);
    }
    public JComponent getComponent() {
        return component;
    }
    public Region getRegion() {
        return region;
    }
    boolean isSubregion() {
        return getRegion().isSubregion();
    }
    void setStyle(SynthStyle style) {
        this.style = style;
    }
    public SynthStyle getStyle() {
        return style;
    }
    void setComponentState(int state) {
        this.state = state;
    }
    public int getComponentState() {
        return state;
    }
    void reset(JComponent component, Region region, SynthStyle style,
               int state) {
        this.component = component;
        this.region = region;
        this.style = style;
        this.state = state;
    }
    void dispose() {
        this.component = null;
        this.style = null;
        releaseContext(this);
    }
    SynthPainter getPainter() {
        SynthPainter painter = getStyle().getPainter(this);
        if (painter != null) {
            return painter;
        }
        return SynthPainter.NULL_PAINTER;
    }
}
