public final class BridgeInflater extends LayoutInflater {
    private final IProjectCallback mProjectCallback;
    private static final String[] sClassPrefixList = {
        "android.widget.",
        "android.webkit."
    };
    protected BridgeInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        mProjectCallback = null;
    }
    public BridgeInflater(Context context, IProjectCallback projectCallback) {
        super(context);
        mProjectCallback = projectCallback;
        mConstructorArgs[0] = context;
    }
    @Override
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        View view = null;
        try {
            for (String prefix : sClassPrefixList) {
                try {
                    view = createView(name, prefix, attrs);
                    if (view != null) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                }
            }
            try {
                if (view == null) {
                    view = super.onCreateView(name, attrs);
                }
            } catch (ClassNotFoundException e) {
            }
            try {
                if (view == null) {
                    view = loadCustomView(name, attrs);
                }
            } catch (ClassNotFoundException e) {
                throw e;
            }
        } catch (Exception e) {
            ClassNotFoundException exception = new ClassNotFoundException("onCreateView", e);
            throw exception;
        }
        setupViewInContext(view, attrs);
        return view;
    }
    @Override
    public View createViewFromTag(String name, AttributeSet attrs) {
        View view = null;
        try {
            view = super.createViewFromTag(name, attrs);
        } catch (InflateException e) {
            try {
                view = loadCustomView(name, attrs);
            } catch (Exception e2) {
                InflateException exception = new InflateException();
                if (e2.getClass().equals(ClassNotFoundException.class) == false) { 
                    exception.initCause(e2);
                } else {
                    exception.initCause(e);
                }
                throw exception;
            }
        }
        setupViewInContext(view, attrs);
        return view;
    }
    @Override
    public View inflate(int resource, ViewGroup root) {
        Context context = getContext();
        if (context instanceof BridgeContext) {
            BridgeContext bridgeContext = (BridgeContext)context;
            IResourceValue value = null;
            String[] layoutInfo = Bridge.resolveResourceValue(resource);
            if (layoutInfo != null) {
                value = bridgeContext.getFrameworkResource(BridgeConstants.RES_LAYOUT,
                        layoutInfo[0]);
            } else {
                layoutInfo = mProjectCallback.resolveResourceValue(resource);
                if (layoutInfo != null) {
                    value = bridgeContext.getProjectResource(BridgeConstants.RES_LAYOUT,
                            layoutInfo[0]);
                }
            }
            if (value != null) {
                File f = new File(value.getValue());
                if (f.isFile()) {
                    try {
                        KXmlParser parser = new KXmlParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                        parser.setInput(new FileReader(f));
                        BridgeXmlBlockParser bridgeParser = new BridgeXmlBlockParser(
                                parser, bridgeContext, false);
                        return inflate(bridgeParser, root);
                    } catch (Exception e) {
                        bridgeContext.getLogger().error(e);
                    }
                }
            }
        }
        return null;
    }
    private View loadCustomView(String name, AttributeSet attrs) throws ClassNotFoundException,
            Exception{
        if (mProjectCallback != null) {
            if (name.equals("view")) {
                name = attrs.getAttributeValue(null, "class");
            }
            mConstructorArgs[1] = attrs;
            Object customView = mProjectCallback.loadView(name, mConstructorSignature,
                    mConstructorArgs);
            if (customView instanceof View) {
                return (View)customView;
            }
        }
        return null;
    }
    private void setupViewInContext(View view, AttributeSet attrs) {
        if (getContext() instanceof BridgeContext) {
            BridgeContext bc = (BridgeContext) getContext();
            if (attrs instanceof BridgeXmlBlockParser) {
                Object viewKey = ((BridgeXmlBlockParser) attrs).getViewKey();
                if (viewKey != null) {
                    bc.addViewKey(view, viewKey);
                }
            }
        }
    }
    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new BridgeInflater(this, newContext);
    }
}
