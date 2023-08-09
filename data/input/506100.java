public final class BridgeTypedArray extends TypedArray {
    @SuppressWarnings("hiding")
    private BridgeResources mResources;
    private BridgeContext mContext;
    @SuppressWarnings("hiding")
    private IResourceValue[] mData;
    private String[] mNames;
    private final boolean mPlatformFile;
    public BridgeTypedArray(BridgeResources resources, BridgeContext context, int len,
            boolean platformFile) {
        super(null, null, null, 0);
        mResources = resources;
        mContext = context;
        mPlatformFile = platformFile;
        mData = new IResourceValue[len];
        mNames = new String[len];
    }
    public void bridgeSetValue(int index, String name, IResourceValue value) {
        mData[index] = value;
        mNames[index] = name;
    }
    public void sealArray() {
        int count = 0;
        for (IResourceValue data : mData) {
            if (data != null) {
                count++;
            }
        }
        mIndices = new int[count+1];
        mIndices[0] = count;
        int index = 1;
        for (int i = 0 ; i < mData.length ; i++) {
            if (mData[i] != null) {
                mIndices[index++] = i;
            }
        }
    }
    @Override
    public int length() {
        return mData.length;
    }
    @Override
    public Resources getResources() {
        return mResources;
    }
    @Override
    public CharSequence getText(int index) {
        if (mData[index] != null) {
            return mData[index].getValue();
        }
        return null;
    }
    @Override
    public String getString(int index) {
        if (mData[index] != null) {
            return mData[index].getValue();
        }
        return null;
    }
    @Override
    public boolean getBoolean(int index, boolean defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        if (s != null) {
            return XmlUtils.convertValueToBoolean(s, defValue);
        }
        return defValue;
    }
    @Override
    public int getInt(int index, int defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        try {
            return (s == null) ? defValue : XmlUtils.convertValueToInt(s, defValue);
        } catch (NumberFormatException e) {
        }
        Map<String, Integer> map = Bridge.getEnumValues(mNames[index]);
        if (map != null) {
            int result = 0;
            String[] keywords = s.split("\\|");
            for (String keyword : keywords) {
                Integer i = map.get(keyword.trim());
                if (i != null) {
                    result |= i.intValue();
                } else {
                    mContext.getLogger().warning(String.format(
                            "Unknown constant \"%s\" in attribute \"%2$s\"",
                            keyword, mNames[index]));
                }
            }
            return result;
        }
        return defValue;
    }
    @Override
    public float getFloat(int index, float defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        if (s != null) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                mContext.getLogger().warning(String.format(
                        "Unable to convert \"%s\" into a float in attribute \"%2$s\"",
                        s, mNames[index]));
            }
        }
        return defValue;
    }
    @Override
    public int getColor(int index, int defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        try {
            return ResourceHelper.getColor(s);
        } catch (NumberFormatException e) {
            mContext.getLogger().warning(String.format(
                    "Unable to convert \"%s\" into a color in attribute \"%2$s\"",
                    s, mNames[index]));
        }
        return defValue;
    }
    @Override
    public ColorStateList getColorStateList(int index) {
        if (mData[index] == null) {
            return null;
        }
        String value = mData[index].getValue();
        if (value == null) {
            return null;
        }
        try {
            int color = ResourceHelper.getColor(value);
            return ColorStateList.valueOf(color);
        } catch (NumberFormatException e) {
        }
        try {
            File f = new File(value);
            if (f.isFile()) {
                KXmlParser parser = new KXmlParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(new FileReader(f));
                ColorStateList colorStateList = ColorStateList.createFromXml(
                        mContext.getResources(),
                        new BridgeXmlBlockParser(parser, mContext, false));
                return colorStateList;
            }
        } catch (Exception e) {
            mContext.getLogger().error(e);
        }
        mContext.getLogger().warning(String.format(
                "Unable to resolve color value \"%1$s\" in attribute \"%2$s\"",
                value, mNames[index]));
        return null;
    }
    @Override
    public int getInteger(int index, int defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                mContext.getLogger().warning(String.format(
                        "Unable to convert \"%s\" into a integer in attribute \"%2$s\"",
                        s, mNames[index]));
            }
        }
        return defValue;
    }
    @Override
    public float getDimension(int index, float defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        if (s == null) {
            return defValue;
        } else if (s.equals(BridgeConstants.MATCH_PARENT) ||
                s.equals(BridgeConstants.FILL_PARENT)) {
            return LayoutParams.MATCH_PARENT;
        } else if (s.equals(BridgeConstants.WRAP_CONTENT)) {
            return LayoutParams.WRAP_CONTENT;
        }
        if (ResourceHelper.stringToFloat(s, mValue)) {
            return mValue.getDimension(mResources.mMetrics);
        }
        mContext.getLogger().warning(String.format(
                "Unable to resolve dimension value \"%1$s\" in attribute \"%2$s\"",
                s, mNames[index]));
        return defValue;
    }
    @Override
    public int getDimensionPixelOffset(int index, int defValue) {
        return (int) getDimension(index, defValue);
    }
    @Override
    public int getDimensionPixelSize(int index, int defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String s = mData[index].getValue();
        if (s == null) {
            return defValue;
        } else if (s.equals(BridgeConstants.MATCH_PARENT) ||
                s.equals(BridgeConstants.FILL_PARENT)) {
            return LayoutParams.MATCH_PARENT;
        } else if (s.equals(BridgeConstants.WRAP_CONTENT)) {
            return LayoutParams.WRAP_CONTENT;
        }
        float f = getDimension(index, defValue);
        final int res = (int)(f+0.5f);
        if (res != 0) return res;
        if (f == 0) return 0;
        if (f > 0) return 1;
        throw new UnsupportedOperationException("Can't convert to dimension: " +
                Integer.toString(index));
    }
    @Override
    public int getLayoutDimension(int index, String name) {
        return getDimensionPixelSize(index, 0);
    }
    @Override
    public int getLayoutDimension(int index, int defValue) {
        return getDimensionPixelSize(index, defValue);
    }
    @Override
    public float getFraction(int index, int base, int pbase, float defValue) {
        if (mData[index] == null) {
            return defValue;
        }
        String value = mData[index].getValue();
        if (value == null) {
            return defValue;
        }
        if (ResourceHelper.stringToFloat(value, mValue)) {
            return mValue.getFraction(base, pbase);
        }
        mContext.getLogger().warning(String.format(
                "Unable to resolve fraction value \"%1$s\" in attribute \"%2$s\"",
                value, mNames[index]));
        return defValue;
    }
    @Override
    public int getResourceId(int index, int defValue) {
        IResourceValue resValue = mData[index];
        if (resValue == null) {
            return defValue;
        }
        if (resValue instanceof IStyleResourceValue) {
            return mContext.getDynamicIdByStyle((IStyleResourceValue)resValue);
        }
        if (resValue.getType() != null && resValue.getType().equals(BridgeConstants.RES_ID)) {
            if (mPlatformFile || resValue.isFramework()) {
                return mContext.getFrameworkIdValue(resValue.getName(), defValue);
            }
            return mContext.getProjectIdValue(resValue.getName(), defValue);
        }
        String value = resValue.getValue();
        if (value == null) {
            return defValue;
        }
        try {
            int i = Integer.parseInt(value);
            if (Integer.toString(i).equals(value)) {
                return i;
            }
        } catch (NumberFormatException e) {
        }
        if (value.startsWith("@id/") || value.startsWith("@+") ||
                value.startsWith("@android:id/")) {
            int pos = value.indexOf('/');
            String idName = value.substring(pos + 1);
            if (mPlatformFile || value.startsWith("@android") || value.startsWith("@+android")) {
                return mContext.getFrameworkIdValue(idName, defValue);
            }
            return mContext.getProjectIdValue(idName, defValue);
        }
        Integer idValue = null;
        if (resValue.isFramework()) {
            idValue = Bridge.getResourceValue(resValue.getType(), resValue.getName());
        } else {
            idValue = mContext.getProjectCallback().getResourceValue(
                    resValue.getType(), resValue.getName());
        }
        if (idValue != null) {
            return idValue.intValue();
        }
        mContext.getLogger().warning(String.format(
                "Unable to resolve id \"%1$s\" for attribute \"%2$s\"", value, mNames[index]));
        return defValue;
    }
    @Override
    public Drawable getDrawable(int index) {
        if (mData[index] == null) {
            return null;
        }
        IResourceValue value = mData[index];
        String stringValue = value.getValue();
        if (stringValue == null || BridgeConstants.REFERENCE_NULL.equals(stringValue)) {
            return null;
        }
        Drawable d = ResourceHelper.getDrawable(value, mContext, mData[index].isFramework());
        if (d != null) {
            return d;
        }
        mContext.getLogger().warning(String.format(
                "Unable to resolve drawable \"%1$s\" in attribute \"%2$s\"", stringValue,
                mNames[index]));
        return null;
    }
    @Override
    public CharSequence[] getTextArray(int index) {
        if (mData[index] == null) {
            return null;
        }
        String value = mData[index].getValue();
        if (value != null) {
            return new CharSequence[] { value };
        }
        mContext.getLogger().warning(String.format(
                String.format("Unknown value for getTextArray(%d) => %s", 
                index, mData[index].getName())));
        return null;
    }
    @Override
    public boolean getValue(int index, TypedValue outValue) {
        if (mData[index] == null) {
            return false;
        }
        String s = mData[index].getValue();
        return ResourceHelper.stringToFloat(s, outValue);
    }
    @Override
    public boolean hasValue(int index) {
        return mData[index] != null;
    }
    @Override
    public TypedValue peekValue(int index) {
        if (getValue(index, mValue)) {
            return mValue;
        }
        return null;
    }
    @Override
    public String getPositionDescription() {
        return "<internal -- stub if needed>";
    }
    @Override
    public void recycle() {
    }
    @Override
    public boolean getValueAt(int index, TypedValue outValue) {
        return false;
    }
    @Override
    public String toString() {
        return mData.toString();
    }
 }
