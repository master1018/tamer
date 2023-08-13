public class ColorStateList implements Parcelable {
    private int[][] mStateSpecs; 
    private int[] mColors;      
    private int mDefaultColor = 0xffff0000;
    private static final int[][] EMPTY = new int[][] { new int[0] };
    private static final SparseArray<WeakReference<ColorStateList>> sCache =
                            new SparseArray<WeakReference<ColorStateList>>();
    private ColorStateList() { }
    public ColorStateList(int[][] states, int[] colors) {
        mStateSpecs = states;
        mColors = colors;
        if (states.length > 0) {
            mDefaultColor = colors[0];
            for (int i = 0; i < states.length; i++) {
                if (states[i].length == 0) {
                    mDefaultColor = colors[i];
                }
            }
        }
    }
    public static ColorStateList valueOf(int color) {
        synchronized (sCache) {
            WeakReference<ColorStateList> ref = sCache.get(color);
            ColorStateList csl = ref != null ? ref.get() : null;
            if (csl != null) {
                return csl;
            }
            csl = new ColorStateList(EMPTY, new int[] { color });
            sCache.put(color, new WeakReference<ColorStateList>(csl));
            return csl;
        }
    }
    public static ColorStateList createFromXml(Resources r, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG
                   && type != XmlPullParser.END_DOCUMENT) {
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        return createFromXmlInner(r, parser, attrs);
    }
    private static ColorStateList createFromXmlInner(Resources r, XmlPullParser parser,
            AttributeSet attrs) throws XmlPullParserException, IOException {
        ColorStateList colorStateList;
        final String name = parser.getName();
        if (name.equals("selector")) {
            colorStateList = new ColorStateList();
        } else {
            throw new XmlPullParserException(
                parser.getPositionDescription() + ": invalid drawable tag " + name);
        }
        colorStateList.inflate(r, parser, attrs);
        return colorStateList;
    }
    public ColorStateList withAlpha(int alpha) {
        int[] colors = new int[mColors.length];
        int len = colors.length;
        for (int i = 0; i < len; i++) {
            colors[i] = (mColors[i] & 0xFFFFFF) | (alpha << 24);
        }
        return new ColorStateList(mStateSpecs, colors);
    }
    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
        throws XmlPullParserException, IOException {
        int type;
        final int innerDepth = parser.getDepth()+1;
        int depth;
        int listAllocated = 20;
        int listSize = 0;
        int[] colorList = new int[listAllocated];
        int[][] stateSpecList = new int[listAllocated][];
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && ((depth=parser.getDepth()) >= innerDepth
                   || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }
            int colorRes = 0;
            int color = 0xffff0000;
            boolean haveColor = false;
            int i;
            int j = 0;
            final int numAttrs = attrs.getAttributeCount();
            int[] stateSpec = new int[numAttrs];
            for (i = 0; i < numAttrs; i++) {
                final int stateResId = attrs.getAttributeNameResource(i);
                if (stateResId == 0) break;
                if (stateResId == com.android.internal.R.attr.color) {
                    colorRes = attrs.getAttributeResourceValue(i, 0);
                    if (colorRes == 0) {
                        color = attrs.getAttributeIntValue(i, color);
                        haveColor = true;
                    }
                } else {
                    stateSpec[j++] = attrs.getAttributeBooleanValue(i, false)
                                  ? stateResId
                                  : -stateResId;
                }
            }
            stateSpec = StateSet.trimStateSet(stateSpec, j);
            if (colorRes != 0) {
                color = r.getColor(colorRes);
            } else if (!haveColor) {
                throw new XmlPullParserException(
                        parser.getPositionDescription()
                        + ": <item> tag requires a 'android:color' attribute.");
            }
            if (listSize == 0 || stateSpec.length == 0) {
                mDefaultColor = color;
            }
            if (listSize + 1 >= listAllocated) {
                listAllocated = ArrayUtils.idealIntArraySize(listSize + 1);
                int[] ncolor = new int[listAllocated];
                System.arraycopy(colorList, 0, ncolor, 0, listSize);
                int[][] nstate = new int[listAllocated][];
                System.arraycopy(stateSpecList, 0, nstate, 0, listSize);
                colorList = ncolor;
                stateSpecList = nstate;
            }
            colorList[listSize] = color;
            stateSpecList[listSize] = stateSpec;
            listSize++;
        }
        mColors = new int[listSize];
        mStateSpecs = new int[listSize][];
        System.arraycopy(colorList, 0, mColors, 0, listSize);
        System.arraycopy(stateSpecList, 0, mStateSpecs, 0, listSize);
    }
    public boolean isStateful() {
        return mStateSpecs.length > 1;
    }
    public int getColorForState(int[] stateSet, int defaultColor) {
        final int setLength = mStateSpecs.length;
        for (int i = 0; i < setLength; i++) {
            int[] stateSpec = mStateSpecs[i];
            if (StateSet.stateSetMatches(stateSpec, stateSet)) {
                return mColors[i];
            }
        }
        return defaultColor;
    }
    public int getDefaultColor() {
        return mDefaultColor;
    }
    public String toString() {
        return "ColorStateList{" +
               "mStateSpecs=" + Arrays.deepToString(mStateSpecs) +
               "mColors=" + Arrays.toString(mColors) +
               "mDefaultColor=" + mDefaultColor + '}';
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        final int N = mStateSpecs.length;
        dest.writeInt(N);
        for (int i=0; i<N; i++) {
            dest.writeIntArray(mStateSpecs[i]);
        }
        dest.writeIntArray(mColors);
    }
    public static final Parcelable.Creator<ColorStateList> CREATOR =
            new Parcelable.Creator<ColorStateList>() {
        public ColorStateList[] newArray(int size) {
            return new ColorStateList[size];
        }
        public ColorStateList createFromParcel(Parcel source) {
            final int N = source.readInt();
            int[][] stateSpecs = new int[N][];
            for (int i=0; i<N; i++) {
                stateSpecs[i] = source.createIntArray();
            }
            int[] colors = source.createIntArray();
            return new ColorStateList(stateSpecs, colors);
        }
    };
}
