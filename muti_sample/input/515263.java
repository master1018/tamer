public final class BridgeResources extends Resources {
    private BridgeContext mContext;
    private IProjectCallback mProjectCallback;
    private boolean[] mPlatformResourceFlag = new boolean[1];
     static Resources initSystem(BridgeContext context,
            AssetManager assets,
            DisplayMetrics metrics,
            Configuration config,
            IProjectCallback projectCallback) {
        if (!(Resources.mSystem instanceof BridgeResources)) {
            Resources.mSystem = new BridgeResources(context,
                    assets,
                    metrics,
                    config,
                    projectCallback);
        }
        return Resources.mSystem;
    }
     static void clearSystem() {
        if (Resources.mSystem instanceof BridgeResources) {
            ((BridgeResources)(Resources.mSystem)).mContext = null;
            ((BridgeResources)(Resources.mSystem)).mProjectCallback = null;
        }
        Resources.mSystem = null;
    }
    private BridgeResources(BridgeContext context, AssetManager assets, DisplayMetrics metrics,
            Configuration config, IProjectCallback projectCallback) {
        super(assets, metrics, config);
        mContext = context;
        mProjectCallback = projectCallback;
    }
    public BridgeTypedArray newTypeArray(int numEntries, boolean platformFile) {
        return new BridgeTypedArray(this, mContext, numEntries, platformFile);
    }
    private IResourceValue getResourceValue(int id, boolean[] platformResFlag_out) {
        String[] resourceInfo = Bridge.resolveResourceValue(id);
        if (resourceInfo != null) {
            platformResFlag_out[0] = true;
            return mContext.getFrameworkResource(resourceInfo[1], resourceInfo[0]);
        }
        if (mProjectCallback != null) {
            resourceInfo = mProjectCallback.resolveResourceValue(id);
            if (resourceInfo != null) {
                platformResFlag_out[0] = false;
                return mContext.getProjectResource(resourceInfo[1], resourceInfo[0]);
            }
        }
        return null;
    }
    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            return ResourceHelper.getDrawable(value, mContext, value.isFramework());
        }
        throwException(id);
        return null;
    }
    @Override
    public int getColor(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            try {
                return ResourceHelper.getColor(value.getValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        throwException(id);
        return 0;
    }
    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            try {
                int color = ResourceHelper.getColor(value.getValue());
                return ColorStateList.valueOf(color);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        throwException(id);
        return null;
    }
    @Override
    public CharSequence getText(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            return value.getValue();
        }
        throwException(id);
        return null;
    }
    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            File xml = new File(value.getValue());
            if (xml.isFile()) {
                try {
                    KXmlParser parser = new KXmlParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(new FileReader(xml));
                    return new BridgeXmlBlockParser(parser, mContext, mPlatformResourceFlag[0]);
                } catch (XmlPullParserException e) {
                    mContext.getLogger().error(e);
                } catch (FileNotFoundException e) {
                }
            }
        }
        throwException(id);
        return null;
    }
    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return mContext.obtainStyledAttributes(set, attrs);
    }
    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public float getDimension(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                if (v.equals(BridgeConstants.MATCH_PARENT) ||
                        v.equals(BridgeConstants.FILL_PARENT)) {
                    return LayoutParams.MATCH_PARENT;
                } else if (v.equals(BridgeConstants.WRAP_CONTENT)) {
                    return LayoutParams.WRAP_CONTENT;
                }
                if (ResourceHelper.stringToFloat(v, mTmpValue) &&
                        mTmpValue.type == TypedValue.TYPE_DIMENSION) {
                    return mTmpValue.getDimension(mMetrics);
                }
            }
        }
        throwException(id);
        return 0;
    }
    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                if (ResourceHelper.stringToFloat(v, mTmpValue) &&
                        mTmpValue.type == TypedValue.TYPE_DIMENSION) {
                    return TypedValue.complexToDimensionPixelOffset(mTmpValue.data, mMetrics);
                }
            }
        }
        throwException(id);
        return 0;
    }
    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                if (ResourceHelper.stringToFloat(v, mTmpValue) &&
                        mTmpValue.type == TypedValue.TYPE_DIMENSION) {
                    return TypedValue.complexToDimensionPixelSize(mTmpValue.data, mMetrics);
                }
            }
        }
        throwException(id);
        return 0;
    }
    @Override
    public int getInteger(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null && value.getValue() != null) {
            String v = value.getValue();
            int radix = 10;
            if (v.startsWith("0x")) {
                v = v.substring(2);
                radix = 16;
            }
            try {
                return Integer.parseInt(v, radix);
            } catch (NumberFormatException e) {
            }
        }
        throwException(id);
        return 0;
    }
    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getResourceName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        String s = getString(id);
        if (s != null) {
            return String.format(s, formatArgs);
        }
        throwException(id);
        return null;
    }
    @Override
    public String getString(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null && value.getValue() != null) {
            return value.getValue();
        }
        throwException(id);
        return null;
    }
    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                if (ResourceHelper.stringToFloat(v, outValue)) {
                    return;
                }
                outValue.type = TypedValue.TYPE_STRING;
                outValue.string = v;
                return;
            }
        }
        throwException(id);
    }
    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                File f = new File(value.getValue());
                if (f.isFile()) {
                    try {
                        KXmlParser parser = new KXmlParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                        parser.setInput(new FileReader(f));
                        return new BridgeXmlBlockParser(parser, mContext, mPlatformResourceFlag[0]);
                    } catch (XmlPullParserException e) {
                        NotFoundException newE = new NotFoundException();
                        newE.initCause(e);
                        throw newE;
                    } catch (FileNotFoundException e) {
                        NotFoundException newE = new NotFoundException();
                        newE.initCause(e);
                        throw newE;
                    }
                }
            }
        }
        throwException(id);
        return null;
    }
    @Override
    public XmlResourceParser loadXmlResourceParser(String file, int id,
            int assetCookie, String type) throws NotFoundException {
        getResourceValue(id, mPlatformResourceFlag);
        File f = new File(file);
        try {
            KXmlParser parser = new KXmlParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new FileReader(f));
            return new BridgeXmlBlockParser(parser, mContext, mPlatformResourceFlag[0]);
        } catch (XmlPullParserException e) {
            NotFoundException newE = new NotFoundException();
            newE.initCause(e);
            throw newE;
        } catch (FileNotFoundException e) {
            NotFoundException newE = new NotFoundException();
            newE.initCause(e);
            throw newE;
        }
    }
    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        IResourceValue value = getResourceValue(id, mPlatformResourceFlag);
        if (value != null) {
            String v = value.getValue();
            if (v != null) {
                File f = new File(value.getValue());
                if (f.isFile()) {
                    try {
                        return new FileInputStream(f);
                    } catch (FileNotFoundException e) {
                        NotFoundException newE = new NotFoundException();
                        newE.initCause(e);
                        throw newE;
                    }
                }
            }
        }
        throwException(id);
        return null;
    }
    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        getValue(id, value, true);
        File f = new File(value.string.toString());
        if (f.isFile()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                NotFoundException exception = new NotFoundException();
                exception.initCause(e);
                throw exception;
            }
        }
        throw new NotFoundException();
    }
    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    private void throwException(int id) throws NotFoundException {
        String[] resourceInfo = Bridge.resolveResourceValue(id);
        if (resourceInfo == null && mProjectCallback != null) {
            resourceInfo = mProjectCallback.resolveResourceValue(id);
        }
        String message = null;
        if (resourceInfo != null) {
            message = String.format(
                    "Could not find %1$s resource matching value 0x%2$X (resolved name: %3$s) in current configuration.",
                    resourceInfo[1], id, resourceInfo[0]);
        } else {
            message = String.format(
                    "Could not resolve resource value: 0x%1$X.", id);
        }
        throw new NotFoundException(message);
    }
}
