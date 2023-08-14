public class WInputMethod extends InputMethodAdapter
{
    private InputMethodContext inputContext;
    private Component awtFocussedComponent;
    private WComponentPeer awtFocussedComponentPeer = null;
    private WComponentPeer lastFocussedComponentPeer = null;
    private boolean isLastFocussedActiveClient = false;
    private boolean isActive;
    private int context;
    private boolean open; 
    private int cmode;    
    private Locale currentLocale;
    private boolean statusWindowHidden = false;
    public final static byte ATTR_INPUT                 = 0x00;
    public final static byte ATTR_TARGET_CONVERTED      = 0x01;
    public final static byte ATTR_CONVERTED             = 0x02;
    public final static byte ATTR_TARGET_NOTCONVERTED   = 0x03;
    public final static byte ATTR_INPUT_ERROR           = 0x04;
    public final static int  IME_CMODE_ALPHANUMERIC     = 0x0000;
    public final static int  IME_CMODE_NATIVE           = 0x0001;
    public final static int  IME_CMODE_KATAKANA         = 0x0002;
    public final static int  IME_CMODE_LANGUAGE         = 0x0003;
    public final static int  IME_CMODE_FULLSHAPE        = 0x0008;
    public final static int  IME_CMODE_HANJACONVERT     = 0x0040;
    public final static int  IME_CMODE_ROMAN            = 0x0010;
    private final static boolean COMMIT_INPUT           = true;
    private final static boolean DISCARD_INPUT          = false;
    private static Map[] highlightStyles;
    static {
        Map styles[] = new Map[4];
        HashMap map;
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
        styles[0] = Collections.unmodifiableMap(map);
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_GRAY);
        styles[1] = Collections.unmodifiableMap(map);
        map = new HashMap(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
        styles[2] = Collections.unmodifiableMap(map);
        map = new HashMap(4);
        Color navyBlue = new Color(0, 0, 128);
        map.put(TextAttribute.FOREGROUND, navyBlue);
        map.put(TextAttribute.BACKGROUND, Color.white);
        map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        styles[3] = Collections.unmodifiableMap(map);
        highlightStyles = styles;
    }
    public WInputMethod()
    {
        context = createNativeContext();
        cmode = getConversionStatus(context);
        open = getOpenStatus(context);
        currentLocale = getNativeLocale();
        if (currentLocale == null) {
            currentLocale = Locale.getDefault();
        }
    }
    protected void finalize() throws Throwable
    {
        if (context!=0) {
            destroyNativeContext(context);
            context=0;
        }
        super.finalize();
    }
    public synchronized void setInputMethodContext(InputMethodContext context) {
        inputContext = context;
    }
    public final void dispose() {
    }
    public Object getControlObject() {
        return null;
    }
    public boolean setLocale(Locale lang) {
        return setLocale(lang, false);
    }
    private boolean setLocale(Locale lang, boolean onActivate) {
        Locale[] available = WInputMethodDescriptor.getAvailableLocalesInternal();
        for (int i = 0; i < available.length; i++) {
            Locale locale = available[i];
            if (lang.equals(locale) ||
                    locale.equals(Locale.JAPAN) && lang.equals(Locale.JAPANESE) ||
                    locale.equals(Locale.KOREA) && lang.equals(Locale.KOREAN)) {
                if (isActive) {
                    setNativeLocale(locale.toLanguageTag(), onActivate);
                }
                currentLocale = locale;
                return true;
            }
        }
        return false;
    }
    public Locale getLocale() {
        if (isActive) {
            currentLocale = getNativeLocale();
            if (currentLocale == null) {
                currentLocale = Locale.getDefault();
            }
        }
        return currentLocale;
    }
    public void setCharacterSubsets(Subset[] subsets) {
        if (subsets == null){
            setConversionStatus(context, cmode);
            setOpenStatus(context, open);
            return;
        }
        Subset subset1 = subsets[0];
        Locale locale = getNativeLocale();
        int newmode;
        if (locale == null) {
            return;
        }
        if (locale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            if (subset1 == UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                setOpenStatus(context, false);
            } else {
                if (subset1 == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || subset1 == InputSubset.KANJI
                    || subset1 == UnicodeBlock.HIRAGANA)
                    newmode = IME_CMODE_NATIVE | IME_CMODE_FULLSHAPE;
                else if (subset1 == UnicodeBlock.KATAKANA)
                    newmode = IME_CMODE_NATIVE | IME_CMODE_KATAKANA| IME_CMODE_FULLSHAPE;
                else if (subset1 == InputSubset.HALFWIDTH_KATAKANA)
                    newmode = IME_CMODE_NATIVE | IME_CMODE_KATAKANA;
                else if (subset1 == InputSubset.FULLWIDTH_LATIN)
                    newmode = IME_CMODE_FULLSHAPE;
                else
                    return;
                setOpenStatus(context, true);
                newmode |= (getConversionStatus(context)&IME_CMODE_ROMAN);   
                setConversionStatus(context, newmode);
            }
        } else if (locale.getLanguage().equals(Locale.KOREAN.getLanguage())) {
            if (subset1 == UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                setOpenStatus(context, false);
            } else {
                if (subset1 == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || subset1 == InputSubset.HANJA
                    || subset1 == UnicodeBlock.HANGUL_SYLLABLES
                    || subset1 == UnicodeBlock.HANGUL_JAMO
                    || subset1 == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO)
                    newmode = IME_CMODE_NATIVE;
                else if (subset1 == InputSubset.FULLWIDTH_LATIN)
                    newmode = IME_CMODE_FULLSHAPE;
                else
                    return;
                setOpenStatus(context, true);
                setConversionStatus(context, newmode);
            }
        } else if (locale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
            if (subset1 == UnicodeBlock.BASIC_LATIN || subset1 == InputSubset.LATIN_DIGITS) {
                setOpenStatus(context, false);
            } else {
                if (subset1 == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || subset1 == InputSubset.TRADITIONAL_HANZI
                    || subset1 == InputSubset.SIMPLIFIED_HANZI)
                    newmode = IME_CMODE_NATIVE;
                else if (subset1 == InputSubset.FULLWIDTH_LATIN)
                    newmode = IME_CMODE_FULLSHAPE;
                else
                    return;
                setOpenStatus(context, true);
                setConversionStatus(context, newmode);
            }
        }
    }
    public void dispatchEvent(AWTEvent e) {
        if (e instanceof ComponentEvent) {
            Component comp = ((ComponentEvent) e).getComponent();
            if (comp == awtFocussedComponent) {
                if (awtFocussedComponentPeer == null ||
                    awtFocussedComponentPeer.isDisposed()) {
                    awtFocussedComponentPeer = getNearestNativePeer(comp);
                }
                if (awtFocussedComponentPeer != null) {
                    handleNativeIMEEvent(awtFocussedComponentPeer, e);
                }
            }
        }
    }
    public void activate() {
        boolean isAc = haveActiveClient();
        if (lastFocussedComponentPeer != awtFocussedComponentPeer ||
            isLastFocussedActiveClient != isAc) {
            if (lastFocussedComponentPeer != null) {
                disableNativeIME(lastFocussedComponentPeer);
            }
            if (awtFocussedComponentPeer != null) {
                enableNativeIME(awtFocussedComponentPeer, context, !isAc);
            }
            lastFocussedComponentPeer = awtFocussedComponentPeer;
            isLastFocussedActiveClient = isAc;
        }
        isActive = true;
        if (currentLocale != null) {
            setLocale(currentLocale, true);
        }
        if (statusWindowHidden) {
            setStatusWindowVisible(awtFocussedComponentPeer, true);
            statusWindowHidden = false;
        }
    }
    public void deactivate(boolean isTemporary)
    {
        getLocale();
        if (awtFocussedComponentPeer != null) {
            lastFocussedComponentPeer = awtFocussedComponentPeer;
            isLastFocussedActiveClient = haveActiveClient();
        }
        isActive = false;
    }
    public void disableInputMethod() {
        if (lastFocussedComponentPeer != null) {
            disableNativeIME(lastFocussedComponentPeer);
            lastFocussedComponentPeer = null;
            isLastFocussedActiveClient = false;
        }
    }
    public String getNativeInputMethodInfo() {
        return getNativeIMMDescription();
    }
    protected void stopListening() {
        disableInputMethod();
    }
    protected void setAWTFocussedComponent(Component component) {
        if (component == null) {
            return;
        }
        WComponentPeer peer = getNearestNativePeer(component);
        if (isActive) {
            if (awtFocussedComponentPeer != null) {
                disableNativeIME(awtFocussedComponentPeer);
            }
            if (peer != null) {
                enableNativeIME(peer, context, !haveActiveClient());
            }
        }
        awtFocussedComponent = component;
        awtFocussedComponentPeer = peer;
    }
    public void hideWindows() {
        if (awtFocussedComponentPeer != null) {
            setStatusWindowVisible(awtFocussedComponentPeer, false);
            statusWindowHidden = true;
        }
    }
    public void removeNotify() {
        endCompositionNative(context, DISCARD_INPUT);
        awtFocussedComponent = null;
        awtFocussedComponentPeer = null;
    }
    static Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        int index;
        int state = highlight.getState();
        if (state == InputMethodHighlight.RAW_TEXT) {
            index = 0;
        } else if (state == InputMethodHighlight.CONVERTED_TEXT) {
            index = 2;
        } else {
            return null;
        }
        if (highlight.isSelected()) {
            index += 1;
        }
        return highlightStyles[index];
    }
    protected boolean supportsBelowTheSpot() {
        return true;
    }
    public void endComposition()
    {
        endCompositionNative(context,
            (haveActiveClient() ? COMMIT_INPUT : DISCARD_INPUT));
    }
    public void setCompositionEnabled(boolean enable) {
        setOpenStatus(context, enable);
    }
    public boolean isCompositionEnabled() {
        return getOpenStatus(context);
    }
    public void sendInputMethodEvent(int id, long when, String text,
                                     int[] clauseBoundary, String[] clauseReading,
                                     int[] attributeBoundary, byte[] attributeValue,
                                     int commitedTextLength, int caretPos, int visiblePos)
    {
        AttributedCharacterIterator iterator = null;
        if (text!=null) {
            AttributedString attrStr = new AttributedString(text);
            attrStr.addAttribute(Attribute.LANGUAGE,
                                            Locale.getDefault(), 0, text.length());
            if (clauseBoundary!=null && clauseReading!=null &&
                clauseReading.length!=0 && clauseBoundary.length==clauseReading.length+1 &&
                clauseBoundary[0]==0 && clauseBoundary[clauseReading.length]==text.length() )
            {
                for (int i=0; i<clauseBoundary.length-1; i++) {
                    attrStr.addAttribute(Attribute.INPUT_METHOD_SEGMENT,
                                            new Annotation(null), clauseBoundary[i], clauseBoundary[i+1]);
                    attrStr.addAttribute(Attribute.READING,
                                            new Annotation(clauseReading[i]), clauseBoundary[i], clauseBoundary[i+1]);
                }
            } else {
                attrStr.addAttribute(Attribute.INPUT_METHOD_SEGMENT,
                                        new Annotation(null), 0, text.length());
                attrStr.addAttribute(Attribute.READING,
                                     new Annotation(""), 0, text.length());
            }
            if (attributeBoundary!=null && attributeValue!=null &&
                attributeValue.length!=0 && attributeBoundary.length==attributeValue.length+1 &&
                attributeBoundary[0]==0 && attributeBoundary[attributeValue.length]==text.length() )
            {
                for (int i=0; i<attributeBoundary.length-1; i++) {
                    InputMethodHighlight highlight;
                    switch (attributeValue[i]) {
                        case ATTR_TARGET_CONVERTED:
                            highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
                            break;
                        case ATTR_CONVERTED:
                            highlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
                            break;
                        case ATTR_TARGET_NOTCONVERTED:
                            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
                            break;
                        case ATTR_INPUT:
                        case ATTR_INPUT_ERROR:
                        default:
                            highlight = InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT;
                            break;
                    }
                    attrStr.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                                         highlight,
                                         attributeBoundary[i], attributeBoundary[i+1]);
                }
            } else {
                attrStr.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                             InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT,
                             0, text.length());
            }
            iterator = attrStr.getIterator();
        }
        Component source = getClientComponent();
        if (source == null)
            return;
        InputMethodEvent event = new InputMethodEvent(source,
                                                      id,
                                                      when,
                                                      iterator,
                                                      commitedTextLength,
                                                      TextHitInfo.leading(caretPos),
                                                      TextHitInfo.leading(visiblePos));
        WToolkit.postEvent(WToolkit.targetToAppContext(source), event);
    }
    public void inquireCandidatePosition()
    {
        Component source = getClientComponent();
        if (source == null) {
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                int x = 0;
                int y = 0;
                Component client = getClientComponent();
                if (client != null) {
                    if (haveActiveClient()) {
                            Rectangle rc = inputContext.getTextLocation(TextHitInfo.leading(0));
                            x = rc.x;
                            y = rc.y + rc.height;
                    } else {
                            Point pt = client.getLocationOnScreen();
                            Dimension size = client.getSize();
                            x = pt.x;
                            y = pt.y + size.height;
                    }
                }
                openCandidateWindow(awtFocussedComponentPeer, x, y);
            }
        };
        WToolkit.postEvent(WToolkit.targetToAppContext(source),
                           new InvocationEvent(source, r));
    }
    private WComponentPeer getNearestNativePeer(Component comp)
    {
        if (comp==null)     return null;
        ComponentPeer peer = comp.getPeer();
        if (peer==null)     return null;
        while (peer instanceof java.awt.peer.LightweightPeer) {
            comp = comp.getParent();
            if (comp==null) return null;
            peer = comp.getPeer();
            if (peer==null) return null;
        }
        if (peer instanceof WComponentPeer)
            return (WComponentPeer)peer;
        else
            return null;
    }
    private native int createNativeContext();
    private native void destroyNativeContext(int context);
    private native void enableNativeIME(WComponentPeer peer, int context, boolean useNativeCompWindow);
    private native void disableNativeIME(WComponentPeer peer);
    private native void handleNativeIMEEvent(WComponentPeer peer, AWTEvent e);
    private native void endCompositionNative(int context, boolean flag);
    private native void setConversionStatus(int context, int cmode);
    private native int  getConversionStatus(int context);
    private native void setOpenStatus(int context, boolean flag);
    private native boolean getOpenStatus(int context);
    private native void setStatusWindowVisible(WComponentPeer peer, boolean visible);
    private native String getNativeIMMDescription();
    static native Locale getNativeLocale();
    static native boolean setNativeLocale(String localeName, boolean onActivate);
    private native void openCandidateWindow(WComponentPeer peer, int x, int y);
}
