class UIStyle {
    public static enum CacheMode {
        NO_CACHING, FIXED_SIZES, NINE_SQUARE_SCALE
    }
    @XmlElement private UIColor textForeground = null;
    @XmlElement(name="inherit-textForeground")
    private boolean textForegroundInherited = true;
    @XmlElement private UIColor textBackground = null;
    @XmlElement(name="inherit-textBackground")
    private boolean textBackgroundInherited = true;
    @XmlElement private UIColor background = null;
    @XmlElement(name="inherit-background")
    private boolean backgroundInherited = true;
    @XmlElement private boolean cacheSettingsInherited = true;
    @XmlElement CacheMode cacheMode = CacheMode.FIXED_SIZES;
    @XmlElement String maxHozCachedImgScaling = "1.0";
    @XmlElement String maxVertCachedImgScaling = "1.0";
    @XmlElement(name="uiProperty")
    @XmlElementWrapper(name="uiproperties")
    private List<UIProperty> uiProperties = new ArrayList<UIProperty>();
    private UIStyle parentStyle = null;
    public void setParentStyle(UIStyle parentStyle) {
        this.parentStyle = parentStyle;
    }
    public CacheMode getCacheMode() {
        if (cacheSettingsInherited) {
            return (parentStyle == null ?
                CacheMode.FIXED_SIZES : parentStyle.getCacheMode());
        } else {
            return cacheMode;
        }
    }
    public String getMaxHozCachedImgScaling() {
        if (cacheSettingsInherited) {
            return (parentStyle == null ?
                "1.0" : parentStyle.getMaxHozCachedImgScaling());
        } else {
            return maxHozCachedImgScaling;
        }
    }
    public String getMaxVertCachedImgScaling() {
        if (cacheSettingsInherited) {
            return (parentStyle == null ?
                "1.0" : parentStyle.getMaxVertCachedImgScaling());
        } else {
            return maxVertCachedImgScaling;
        }
    }
    public String write(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (! textForegroundInherited) {
            sb.append(String.format("        addColor(d, \"%s%s\", %s);\n",
                    prefix, "textForeground", textForeground.getValue().write()));
        }
        if (! textBackgroundInherited) {
            sb.append(String.format("        addColor(d, \"%s%s\", %s);\n",
                    prefix, "textBackground", textBackground.getValue().write()));
        }
        if (! backgroundInherited) {
            sb.append(String.format("        addColor(d, \"%s%s\", %s);\n",
                    prefix, "background", background.getValue().write()));
        }
        for (UIProperty property : uiProperties) {
            sb.append(property.write(prefix));
        }
        return sb.toString();
    }
}
class UIRegion {
    @XmlAttribute protected String name;
    @XmlAttribute protected String key;
    @XmlAttribute private boolean opaque = false;
    @XmlElement private Insets contentMargins = new Insets(0, 0, 0, 0);
    @XmlElement(name="state")
    @XmlElementWrapper(name="backgroundStates")
    protected List<UIState> backgroundStates = new ArrayList<UIState>();
    public List<UIState> getBackgroundStates() { return backgroundStates; }
    @XmlElement(name="state")
    @XmlElementWrapper(name="foregroundStates")
    protected List<UIState> foregroundStates = new ArrayList<UIState>();
    public List<UIState> getForegroundStates() { return foregroundStates; }
    @XmlElement(name="state")
    @XmlElementWrapper(name="borderStates")
    protected List<UIState> borderStates = new ArrayList<UIState>();
    public List<UIState> getBorderStates() { return borderStates; }
    @XmlElement private UIStyle style = new UIStyle();
    @XmlElements({
        @XmlElement(name = "region", type = UIRegion.class),
        @XmlElement(name = "uiComponent", type = UIComponent.class),
        @XmlElement(name = "uiIconRegion", type = UIIconRegion.class)
    })
    @XmlElementWrapper(name="regions")
    private List<UIRegion> subRegions = new ArrayList<UIRegion>();
    public List<UIRegion> getSubRegions() { return subRegions; }
    protected void initStyles(UIStyle parentStyle) {
        style.setParentStyle(parentStyle);
        for (UIState state: backgroundStates) {
            state.getStyle().setParentStyle(this.style);
        }
        for (UIState state: foregroundStates) {
            state.getStyle().setParentStyle(this.style);
        }
        for (UIState state: borderStates) {
            state.getStyle().setParentStyle(this.style);
        }
        for (UIRegion region: subRegions) {
            region.initStyles(this.style);
        }
    }
    public String getKey() {
        return key == null || "".equals(key) ? name : key;
    }
    private boolean hasCanvas() {
        for (UIState s : backgroundStates) {
            if (s.hasCanvas()) return true;
        }
        for (UIState s : borderStates) {
            if (s.hasCanvas()) return true;
        }
        for (UIState s : foregroundStates) {
            if (s.hasCanvas()) return true;
        }
        for (UIRegion r: subRegions) {
            if (r.hasCanvas()) return true;
        }
        return false;
    }
    public void write(StringBuilder sb, StringBuilder styleBuffer,
                      UIComponent comp, String prefix, String pkg) {
        sb.append(String.format("        d.put(\"%s.contentMargins\", %s);\n",
                                prefix, contentMargins.write(true)));
        if (opaque) {
            sb.append(String.format("        d.put(\"%s.opaque\", Boolean.TRUE);\n", prefix));
        }
        String regionCode = "Region." + Utils.regionNameToCaps(name);
        styleBuffer.append(String.format("        register(%s, \"%s\");\n",
                                         regionCode, prefix));
        StringBuffer regString = new StringBuffer();
        List<UIStateType> types = comp.getStateTypes();
        if (types != null && types.size() > 0) {
            for (UIStateType type : types) {
                regString.append(type.getKey());
                regString.append(",");
            }
            regString.deleteCharAt(regString.length() - 1);
        }
        if (! regString.equals("Enabled,MouseOver,Pressed,Disabled,Focused,Selected,Default") && types.size() > 0) {
            sb.append(String.format("        d.put(\"%s.States\", \"%s\");\n",
                                    prefix, regString));
        }
        for (UIStateType type : types) {
            String synthState = type.getKey();
            if (! "Enabled".equals(synthState) &&
                ! "MouseOver".equals(synthState) &&
                ! "Pressed".equals(synthState) &&
                ! "Disabled".equals(synthState) &&
                ! "Focused".equals(synthState) &&
                ! "Selected".equals(synthState) &&
                ! "Default".equals(synthState)) {
                String className = Utils.normalize(prefix) + synthState + "State";
                sb.append(String.format("        d.put(\"%s.%s\", new %s());\n",
                                        prefix, synthState, className));
                String body = type.getCodeSnippet();
                Map<String, String> variables = Generator.getVariables();
                variables.put("STATE_NAME", className);
                variables.put("STATE_KEY", synthState);
                variables.put("BODY", body);
                Generator.writeSrcFile("StateImpl", variables, className);
            }
        }
        sb.append(style.write(prefix + '.'));
        String fileName = Utils.normalize(prefix) + "Painter";
        boolean hasCanvas = hasCanvas();
        if (hasCanvas) {
            PainterGenerator.writePainter(this, fileName);
        }
        for (UIState state : backgroundStates) {
            state.write(sb, prefix, pkg, fileName, "background");
        }
        for (UIState state : foregroundStates) {
            state.write(sb, prefix, pkg, fileName, "foreground");
        }
        for (UIState state : borderStates) {
            state.write(sb, prefix, pkg, fileName, "border");
        }
        for (UIRegion subreg : subRegions) {
            String p = prefix;
            if (! (subreg instanceof UIIconRegion)) {
                p = prefix + ":" + Utils.escape(subreg.getKey());
            }
            UIComponent c = comp;
            if (subreg instanceof UIComponent) {
                c = (UIComponent) subreg;
            }
            subreg.write(sb, styleBuffer, c, p, pkg);
        }
    }
}
class UIIconRegion extends UIRegion {
    @XmlAttribute private String basicKey;
    @Override public void write(StringBuilder sb, StringBuilder styleBuffer, UIComponent comp, String prefix, String pkg) {
        Dimension size = null;
        String fileNamePrefix = Utils.normalize(prefix) + "Painter";
        for (UIState state : backgroundStates) {
            Canvas canvas = state.getCanvas();
            if (!canvas.isBlank()) {
                state.write(sb, prefix, pkg, fileNamePrefix, getKey());
                size = canvas.getSize();
            }
        }
        if (size != null) {
            String k = (basicKey == null ? prefix + "." + getKey() : basicKey);
            sb.append(String.format(
                    "        d.put(\"%s\", new NimbusIcon(\"%s\", \"%sPainter\", %d, %d));\n",
                    k, prefix, getKey(), size.width, size.height));
        }
    }
}
class UIComponent extends UIRegion {
    @XmlAttribute private String componentName;
    @XmlElement(name="stateType")
    @XmlElementWrapper(name="stateTypes")
    private List<UIStateType> stateTypes = new ArrayList<UIStateType>();
    public List<UIStateType> getStateTypes() { return stateTypes; }
    @Override public String getKey() {
        if (key == null || "".equals(key)) {
            if (componentName == null || "".equals(componentName)) {
                return name;
            } else {
                return "\"" + componentName + "\"";
            }
        } else {
            return key;
        }
    }
}
class UIState {
    @XmlAttribute private String stateKeys;
    public String getStateKeys() { return stateKeys; }
    @XmlAttribute private boolean inverted;
    private String cachedName = null;
    @XmlElement private Canvas canvas;
    public Canvas getCanvas() { return canvas; }
    @XmlElement private UIStyle style;
    public UIStyle getStyle() { return style; }
    public boolean hasCanvas() {
        return ! canvas.isBlank();
    }
    public static List<String> stringToKeys(String keysString) {
        return Arrays.asList(keysString.split("\\+"));
    }
    public String getName() {
        if (cachedName == null) {
            StringBuilder buf = new StringBuilder();
            List<String> keys = stringToKeys(stateKeys);
            Collections.sort(keys);
            for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
                buf.append(iter.next());
                if (iter.hasNext()) {
                    buf.append('+');
                }
            }
            cachedName = buf.toString();
        }
        return cachedName;
    }
    public void write(StringBuilder sb, String prefix, String pkg, String fileNamePrefix, String painterPrefix) {
        String statePrefix = prefix + "[" + getName() + "]";
        sb.append(style.write(statePrefix + '.'));
        if (hasCanvas()) {
            writeLazyPainter(sb, statePrefix, pkg, fileNamePrefix, painterPrefix);
        }
    }
    private void writeLazyPainter(StringBuilder sb, String statePrefix, String packageNamePrefix, String fileNamePrefix, String painterPrefix) {
        String cacheModeString = "AbstractRegionPainter.PaintContext.CacheMode." + style.getCacheMode();
        String stateConstant = Utils.statesToConstantName(painterPrefix + "_" + stateKeys);
        sb.append(String.format(
                "        d.put(\"%s.%sPainter\", new LazyPainter(\"%s.%s\", %s.%s, %s, %s, %b, %s, %s, %s));\n",
                statePrefix, painterPrefix, packageNamePrefix, fileNamePrefix,
                fileNamePrefix, stateConstant, canvas.getStretchingInsets().write(false),
                canvas.getSize().write(false), inverted, cacheModeString,
                Utils.formatDouble(style.getMaxHozCachedImgScaling()),
                Utils.formatDouble(style.getMaxVertCachedImgScaling())));
    }
}
class UIStateType {
    @XmlAttribute private String key;
    public String getKey() { return key; }
    @XmlElement private String codeSnippet;
    public String getCodeSnippet() { return codeSnippet; }
}
