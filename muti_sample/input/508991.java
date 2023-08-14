public class IntentFilter implements Parcelable {
    private static final String SGLOB_STR = "sglob";
    private static final String PREFIX_STR = "prefix";
    private static final String LITERAL_STR = "literal";
    private static final String PATH_STR = "path";
    private static final String PORT_STR = "port";
    private static final String HOST_STR = "host";
    private static final String AUTH_STR = "auth";
    private static final String SCHEME_STR = "scheme";
    private static final String TYPE_STR = "type";
    private static final String CAT_STR = "cat";
    private static final String NAME_STR = "name";
    private static final String ACTION_STR = "action";
    public static final int SYSTEM_HIGH_PRIORITY = 1000;
    public static final int SYSTEM_LOW_PRIORITY = -1000;
    public static final int MATCH_CATEGORY_MASK = 0xfff0000;
    public static final int MATCH_ADJUSTMENT_MASK = 0x000ffff;
    public static final int MATCH_ADJUSTMENT_NORMAL = 0x8000;
    public static final int MATCH_CATEGORY_EMPTY = 0x0100000;
    public static final int MATCH_CATEGORY_SCHEME = 0x0200000;
    public static final int MATCH_CATEGORY_HOST = 0x0300000;
    public static final int MATCH_CATEGORY_PORT = 0x0400000;
    public static final int MATCH_CATEGORY_PATH = 0x0500000;
    public static final int MATCH_CATEGORY_TYPE = 0x0600000;
    public static final int NO_MATCH_TYPE = -1;
    public static final int NO_MATCH_DATA = -2;
    public static final int NO_MATCH_ACTION = -3;
    public static final int NO_MATCH_CATEGORY = -4;
    private int mPriority;
    private final ArrayList<String> mActions;
    private ArrayList<String> mCategories = null;
    private ArrayList<String> mDataSchemes = null;
    private ArrayList<AuthorityEntry> mDataAuthorities = null;
    private ArrayList<PatternMatcher> mDataPaths = null;
    private ArrayList<String> mDataTypes = null;
    private boolean mHasPartialTypes = false;
    private static int findStringInSet(String[] set, String string,
            int[] lengths, int lenPos) {
        if (set == null) return -1;
        final int N = lengths[lenPos];
        for (int i=0; i<N; i++) {
            if (set[i].equals(string)) return i;
        }
        return -1;
    }
    private static String[] addStringToSet(String[] set, String string,
            int[] lengths, int lenPos) {
        if (findStringInSet(set, string, lengths, lenPos) >= 0) return set;
        if (set == null) {
            set = new String[2];
            set[0] = string;
            lengths[lenPos] = 1;
            return set;
        }
        final int N = lengths[lenPos];
        if (N < set.length) {
            set[N] = string;
            lengths[lenPos] = N+1;
            return set;
        }
        String[] newSet = new String[(N*3)/2 + 2];
        System.arraycopy(set, 0, newSet, 0, N);
        set = newSet;
        set[N] = string;
        lengths[lenPos] = N+1;
        return set;
    }
    private static String[] removeStringFromSet(String[] set, String string,
            int[] lengths, int lenPos) {
        int pos = findStringInSet(set, string, lengths, lenPos);
        if (pos < 0) return set;
        final int N = lengths[lenPos];
        if (N > (set.length/4)) {
            int copyLen = N-(pos+1);
            if (copyLen > 0) {
                System.arraycopy(set, pos+1, set, pos, copyLen);
            }
            set[N-1] = null;
            lengths[lenPos] = N-1;
            return set;
        }
        String[] newSet = new String[set.length/3];
        if (pos > 0) System.arraycopy(set, 0, newSet, 0, pos);
        if ((pos+1) < N) System.arraycopy(set, pos+1, newSet, pos, N-(pos+1));
        return newSet;
    }
    public static class MalformedMimeTypeException extends AndroidException {
        public MalformedMimeTypeException() {
        }
        public MalformedMimeTypeException(String name) {
            super(name);
        }
    };
    public static IntentFilter create(String action, String dataType) {
        try {
            return new IntentFilter(action, dataType);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Bad MIME type", e);
        }
    }
    public IntentFilter() {
        mPriority = 0;
        mActions = new ArrayList<String>();
    }
    public IntentFilter(String action) {
        mPriority = 0;
        mActions = new ArrayList<String>();
        addAction(action);
    }
    public IntentFilter(String action, String dataType)
        throws MalformedMimeTypeException {
        mPriority = 0;
        mActions = new ArrayList<String>();
        addAction(action);
        addDataType(dataType);
    }
    public IntentFilter(IntentFilter o) {
        mPriority = o.mPriority;
        mActions = new ArrayList<String>(o.mActions);
        if (o.mCategories != null) {
            mCategories = new ArrayList<String>(o.mCategories);
        }
        if (o.mDataTypes != null) {
            mDataTypes = new ArrayList<String>(o.mDataTypes);
        }
        if (o.mDataSchemes != null) {
            mDataSchemes = new ArrayList<String>(o.mDataSchemes);
        }
        if (o.mDataAuthorities != null) {
            mDataAuthorities = new ArrayList<AuthorityEntry>(o.mDataAuthorities);
        }
        if (o.mDataPaths != null) {
            mDataPaths = new ArrayList<PatternMatcher>(o.mDataPaths);
        }
        mHasPartialTypes = o.mHasPartialTypes;
    }
    public final void setPriority(int priority) {
        mPriority = priority;
    }
    public final int getPriority() {
        return mPriority;
    }
    public final void addAction(String action) {
        if (!mActions.contains(action)) {
            mActions.add(action.intern());
        }
    }
    public final int countActions() {
        return mActions.size();
    }
    public final String getAction(int index) {
        return mActions.get(index);
    }
    public final boolean hasAction(String action) {
        return mActions.contains(action);
    }
    public final boolean matchAction(String action) {
        if (action == null || mActions == null || mActions.size() == 0) {
            return false;
        }
        return mActions.contains(action);
    }
    public final Iterator<String> actionsIterator() {
        return mActions != null ? mActions.iterator() : null;
    }
    public final void addDataType(String type)
        throws MalformedMimeTypeException {
        final int slashpos = type.indexOf('/');
        final int typelen = type.length();
        if (slashpos > 0 && typelen >= slashpos+2) {
            if (mDataTypes == null) mDataTypes = new ArrayList<String>();
            if (typelen == slashpos+2 && type.charAt(slashpos+1) == '*') {
                String str = type.substring(0, slashpos);
                if (!mDataTypes.contains(str)) {
                    mDataTypes.add(str.intern());
                }
                mHasPartialTypes = true;
            } else {
                if (!mDataTypes.contains(type)) {
                    mDataTypes.add(type.intern());
                }
            }
            return;
        }
        throw new MalformedMimeTypeException(type);
    }
    public final boolean hasDataType(String type) {
        return mDataTypes != null && findMimeType(type);
    }
    public final int countDataTypes() {
        return mDataTypes != null ? mDataTypes.size() : 0;
    }
    public final String getDataType(int index) {
        return mDataTypes.get(index);
    }
    public final Iterator<String> typesIterator() {
        return mDataTypes != null ? mDataTypes.iterator() : null;
    }
    public final void addDataScheme(String scheme) {
        if (mDataSchemes == null) mDataSchemes = new ArrayList<String>();
        if (!mDataSchemes.contains(scheme)) {
            mDataSchemes.add(scheme.intern());
        }
    }
    public final int countDataSchemes() {
        return mDataSchemes != null ? mDataSchemes.size() : 0;
    }
    public final String getDataScheme(int index) {
        return mDataSchemes.get(index);
    }
    public final boolean hasDataScheme(String scheme) {
        return mDataSchemes != null && mDataSchemes.contains(scheme);
    }
    public final Iterator<String> schemesIterator() {
        return mDataSchemes != null ? mDataSchemes.iterator() : null;
    }
    public final static class AuthorityEntry {
        private final String mOrigHost;
        private final String mHost;
        private final boolean mWild;
        private final int mPort;
        public AuthorityEntry(String host, String port) {
            mOrigHost = host;
            mWild = host.length() > 0 && host.charAt(0) == '*';
            mHost = mWild ? host.substring(1).intern() : host;
            mPort = port != null ? Integer.parseInt(port) : -1;
        }
        AuthorityEntry(Parcel src) {
            mOrigHost = src.readString();
            mHost = src.readString();
            mWild = src.readInt() != 0;
            mPort = src.readInt();
        }
        void writeToParcel(Parcel dest) {
            dest.writeString(mOrigHost);
            dest.writeString(mHost);
            dest.writeInt(mWild ? 1 : 0);
            dest.writeInt(mPort);
        }
        public String getHost() {
            return mOrigHost;
        }
        public int getPort() {
            return mPort;
        }
        public int match(Uri data) {
            String host = data.getHost();
            if (host == null) {
                return NO_MATCH_DATA;
            }
            if (Config.LOGV) Log.v("IntentFilter",
                    "Match host " + host + ": " + mHost);
            if (mWild) {
                if (host.length() < mHost.length()) {
                    return NO_MATCH_DATA;
                }
                host = host.substring(host.length()-mHost.length());
            }
            if (host.compareToIgnoreCase(mHost) != 0) {
                return NO_MATCH_DATA;
            }
            if (mPort >= 0) {
                if (mPort != data.getPort()) {
                    return NO_MATCH_DATA;
                }
                return MATCH_CATEGORY_PORT;
            }
            return MATCH_CATEGORY_HOST;
        }
    };
    public final void addDataAuthority(String host, String port) {
        if (mDataAuthorities == null) mDataAuthorities =
                new ArrayList<AuthorityEntry>();
        if (port != null) port = port.intern();
        mDataAuthorities.add(new AuthorityEntry(host.intern(), port));
    }
    public final int countDataAuthorities() {
        return mDataAuthorities != null ? mDataAuthorities.size() : 0;
    }
    public final AuthorityEntry getDataAuthority(int index) {
        return mDataAuthorities.get(index);
    }
    public final boolean hasDataAuthority(Uri data) {
        return matchDataAuthority(data) >= 0;
    }
    public final Iterator<AuthorityEntry> authoritiesIterator() {
        return mDataAuthorities != null ? mDataAuthorities.iterator() : null;
    }
    public final void addDataPath(String path, int type) {
        if (mDataPaths == null) mDataPaths = new ArrayList<PatternMatcher>();
        mDataPaths.add(new PatternMatcher(path.intern(), type));
    }
    public final int countDataPaths() {
        return mDataPaths != null ? mDataPaths.size() : 0;
    }
    public final PatternMatcher getDataPath(int index) {
        return mDataPaths.get(index);
    }
    public final boolean hasDataPath(String data) {
        if (mDataPaths == null) {
            return false;
        }
        Iterator<PatternMatcher> i = mDataPaths.iterator();
        while (i.hasNext()) {
            final PatternMatcher pe = i.next();
            if (pe.match(data)) {
                return true;
            }
        }
        return false;
    }
    public final Iterator<PatternMatcher> pathsIterator() {
        return mDataPaths != null ? mDataPaths.iterator() : null;
    }
    public final int matchDataAuthority(Uri data) {
        if (mDataAuthorities == null) {
            return NO_MATCH_DATA;
        }
        Iterator<AuthorityEntry> i = mDataAuthorities.iterator();
        while (i.hasNext()) {
            final AuthorityEntry ae = i.next();
            int match = ae.match(data);
            if (match >= 0) {
                return match;
            }
        }
        return NO_MATCH_DATA;
    }
    public final int matchData(String type, String scheme, Uri data) {
        final ArrayList<String> types = mDataTypes;
        final ArrayList<String> schemes = mDataSchemes;
        final ArrayList<AuthorityEntry> authorities = mDataAuthorities;
        final ArrayList<PatternMatcher> paths = mDataPaths;
        int match = MATCH_CATEGORY_EMPTY;
        if (types == null && schemes == null) {
            return ((type == null && data == null)
                ? (MATCH_CATEGORY_EMPTY+MATCH_ADJUSTMENT_NORMAL) : NO_MATCH_DATA);
        }
        if (schemes != null) {
            if (schemes.contains(scheme != null ? scheme : "")) {
                match = MATCH_CATEGORY_SCHEME;
            } else {
                return NO_MATCH_DATA;
            }
            if (authorities != null) {
                int authMatch = matchDataAuthority(data);
                if (authMatch >= 0) {
                    if (paths == null) {
                        match = authMatch;
                    } else if (hasDataPath(data.getPath())) {
                        match = MATCH_CATEGORY_PATH;
                    } else {
                        return NO_MATCH_DATA;
                    }
                } else {
                    return NO_MATCH_DATA;
                }
            }
        } else {
            if (scheme != null && !"".equals(scheme)
                    && !"content".equals(scheme)
                    && !"file".equals(scheme)) {
                return NO_MATCH_DATA;
            }
        }
        if (types != null) {
            if (findMimeType(type)) {
                match = MATCH_CATEGORY_TYPE;
            } else {
                return NO_MATCH_TYPE;
            }
        } else {
            if (type != null) {
                return NO_MATCH_TYPE;
            }
        }
        return match + MATCH_ADJUSTMENT_NORMAL;
    }
    public final void addCategory(String category) {
        if (mCategories == null) mCategories = new ArrayList<String>();
        if (!mCategories.contains(category)) {
            mCategories.add(category.intern());
        }
    }
    public final int countCategories() {
        return mCategories != null ? mCategories.size() : 0;
    }
    public final String getCategory(int index) {
        return mCategories.get(index);
    }
    public final boolean hasCategory(String category) {
        return mCategories != null && mCategories.contains(category);
    }
    public final Iterator<String> categoriesIterator() {
        return mCategories != null ? mCategories.iterator() : null;
    }
    public final String matchCategories(Set<String> categories) {
        if (categories == null) {
            return null;
        }
        Iterator<String> it = categories.iterator();
        if (mCategories == null) {
            return it.hasNext() ? it.next() : null;
        }
        while (it.hasNext()) {
            final String category = it.next();
            if (!mCategories.contains(category)) {
                return category;
            }
        }
        return null;
    }
    public final int match(ContentResolver resolver, Intent intent,
            boolean resolve, String logTag) {
        String type = resolve ? intent.resolveType(resolver) : intent.getType();
        return match(intent.getAction(), type, intent.getScheme(),
                     intent.getData(), intent.getCategories(), logTag);
    }
    public final int match(String action, String type, String scheme,
            Uri data, Set<String> categories, String logTag) {
        if (action != null && !matchAction(action)) {
            if (Config.LOGV) Log.v(
                logTag, "No matching action " + action + " for " + this);
            return NO_MATCH_ACTION;
        }
        int dataMatch = matchData(type, scheme, data);
        if (dataMatch < 0) {
            if (Config.LOGV) {
                if (dataMatch == NO_MATCH_TYPE) {
                    Log.v(logTag, "No matching type " + type
                          + " for " + this);
                }
                if (dataMatch == NO_MATCH_DATA) {
                    Log.v(logTag, "No matching scheme/path " + data
                          + " for " + this);
                }
            }
            return dataMatch;
        }
        String categoryMatch = matchCategories(categories);
        if (categoryMatch != null) {
            if (Config.LOGV) Log.v(
                logTag, "No matching category "
                + categoryMatch + " for " + this);
            return NO_MATCH_CATEGORY;
        }
        if (false) {
            if (categories != null) {
                dataMatch -= mCategories.size() - categories.size();
            }
        }
        return dataMatch;
    }
    public void writeToXml(XmlSerializer serializer) throws IOException {
        int N = countActions();
        for (int i=0; i<N; i++) {
            serializer.startTag(null, ACTION_STR);
            serializer.attribute(null, NAME_STR, mActions.get(i));
            serializer.endTag(null, ACTION_STR);
        }
        N = countCategories();
        for (int i=0; i<N; i++) {
            serializer.startTag(null, CAT_STR);
            serializer.attribute(null, NAME_STR, mCategories.get(i));
            serializer.endTag(null, CAT_STR);
        }
        N = countDataTypes();
        for (int i=0; i<N; i++) {
            serializer.startTag(null, TYPE_STR);
            String type = mDataTypes.get(i);
            if (type.indexOf('/') < 0) type = type + "
    public boolean debugCheck() {
        return true;
    }
    private IntentFilter(Parcel source) {
        mActions = new ArrayList<String>();
        source.readStringList(mActions);
        if (source.readInt() != 0) {
            mCategories = new ArrayList<String>();
            source.readStringList(mCategories);
        }
        if (source.readInt() != 0) {
            mDataSchemes = new ArrayList<String>();
            source.readStringList(mDataSchemes);
        }
        if (source.readInt() != 0) {
            mDataTypes = new ArrayList<String>();
            source.readStringList(mDataTypes);
        }
        int N = source.readInt();
        if (N > 0) {
            mDataAuthorities = new ArrayList<AuthorityEntry>();
            for (int i=0; i<N; i++) {
                mDataAuthorities.add(new AuthorityEntry(source));
            }
        }
        N = source.readInt();
        if (N > 0) {
            mDataPaths = new ArrayList<PatternMatcher>();
            for (int i=0; i<N; i++) {
                mDataPaths.add(new PatternMatcher(source));
            }
        }
        mPriority = source.readInt();
        mHasPartialTypes = source.readInt() > 0;
    }
    private final boolean findMimeType(String type) {
        final ArrayList<String> t = mDataTypes;
        if (type == null) {
            return false;
        }
        if (t.contains(type)) {
            return true;
        }
        final int typeLength = type.length();
        if (typeLength == 3 && type.equals("*/*")) {
            return !t.isEmpty();
        }
        if (mHasPartialTypes && t.contains("*")) {
            return true;
        }
        final int slashpos = type.indexOf('/');
        if (slashpos > 0) {
            if (mHasPartialTypes && t.contains(type.substring(0, slashpos))) {
                return true;
            }
            if (typeLength == slashpos+2 && type.charAt(slashpos+1) == '*') {
                final Iterator<String> it = t.iterator();
                while (it.hasNext()) {
                    String v = it.next();
                    if (type.regionMatches(0, v, 0, slashpos+1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
