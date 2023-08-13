public class IntentResolver<F extends IntentFilter, R extends Object> {
    final private static String TAG = "IntentResolver";
    final private static boolean DEBUG = false;
    final private static boolean localLOGV = DEBUG || Config.LOGV;
    public void addFilter(F f) {
        if (localLOGV) {
            Slog.v(TAG, "Adding filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Building Lookup Maps:");
        }
        mFilters.add(f);
        int numS = register_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = register_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    }
    public void removeFilter(F f) {
        removeFilterInternal(f);
        mFilters.remove(f);
    }
    void removeFilterInternal(F f) {
        if (localLOGV) {
            Slog.v(TAG, "Removing filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Cleaning Lookup Maps:");
        }
        int numS = unregister_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = unregister_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    }
    boolean dumpMap(PrintWriter out, String titlePrefix, String title,
            String prefix, Map<String, ArrayList<F>> map, String packageName) {
        String eprefix = prefix + "  ";
        String fprefix = prefix + "    ";
        boolean printedSomething = false;
        for (Map.Entry<String, ArrayList<F>> e : map.entrySet()) {
            ArrayList<F> a = e.getValue();
            final int N = a.size();
            boolean printedHeader = false;
            for (int i=0; i<N; i++) {
                F filter = a.get(i);
                if (packageName != null && !packageName.equals(packageForFilter(filter))) {
                    continue;
                }
                if (title != null) {
                    out.print(titlePrefix); out.println(title);
                    title = null;
                }
                if (!printedHeader) {
                    out.print(eprefix); out.print(e.getKey()); out.println(":");
                    printedHeader = true;
                }
                printedSomething = true;
                dumpFilter(out, fprefix, filter);
            }
        }
        return printedSomething;
    }
    public boolean dump(PrintWriter out, String title, String prefix, String packageName) {
        String innerPrefix = prefix + "  ";
        String sepPrefix = "\n" + prefix;
        String curPrefix = title + "\n" + prefix;
        if (dumpMap(out, curPrefix, "Full MIME Types:", innerPrefix,
                mTypeToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Base MIME Types:", innerPrefix,
                mBaseTypeToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Wild MIME Types:", innerPrefix,
                mWildTypeToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Schemes:", innerPrefix,
                mSchemeToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Non-Data Actions:", innerPrefix,
                mActionToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "MIME Typed Actions:", innerPrefix,
                mTypedActionToFilter, packageName)) {
            curPrefix = sepPrefix;
        }
        return curPrefix == sepPrefix;
    }
    private class IteratorWrapper implements Iterator<F> {
        private final Iterator<F> mI;
        private F mCur;
        IteratorWrapper(Iterator<F> it) {
            mI = it;
        }
        public boolean hasNext() {
            return mI.hasNext();
        }
        public F next() {
            return (mCur = mI.next());
        }
        public void remove() {
            if (mCur != null) {
                removeFilterInternal(mCur);
            }
            mI.remove();
        }
    }
    public Iterator<F> filterIterator() {
        return new IteratorWrapper(mFilters.iterator());
    }
    public Set<F> filterSet() {
        return Collections.unmodifiableSet(mFilters);
    }
    public List<R> queryIntentFromList(Intent intent, String resolvedType, 
            boolean defaultOnly, ArrayList<ArrayList<F>> listCut) {
        ArrayList<R> resultList = new ArrayList<R>();
        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
        final String scheme = intent.getScheme();
        int N = listCut.size();
        for (int i = 0; i < N; ++i) {
            buildResolveList(intent, debug, defaultOnly,
                             resolvedType, scheme, listCut.get(i), resultList);
        }
        sortResults(resultList);
        return resultList;
    }
    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly) {
        String scheme = intent.getScheme();
        ArrayList<R> finalList = new ArrayList<R>();
        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
        if (debug) Slog.v(
            TAG, "Resolving type " + resolvedType + " scheme " + scheme
            + " of intent " + intent);
        ArrayList<F> firstTypeCut = null;
        ArrayList<F> secondTypeCut = null;
        ArrayList<F> thirdTypeCut = null;
        ArrayList<F> schemeCut = null;
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf('/');
            if (slashpos > 0) {
                final String baseType = resolvedType.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    if (resolvedType.length() != slashpos+2
                            || resolvedType.charAt(slashpos+1) != '*') {
                        firstTypeCut = mTypeToFilter.get(resolvedType);
                        if (debug) Slog.v(TAG, "First type cut: " + firstTypeCut);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: " + secondTypeCut);
                    } else {
                        firstTypeCut = mBaseTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "First type cut: " + firstTypeCut);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: " + secondTypeCut);
                    }
                    thirdTypeCut = mWildTypeToFilter.get("*");
                    if (debug) Slog.v(TAG, "Third type cut: " + thirdTypeCut);
                } else if (intent.getAction() != null) {
                    firstTypeCut = mTypedActionToFilter.get(intent.getAction());
                    if (debug) Slog.v(TAG, "Typed Action list: " + firstTypeCut);
                }
            }
        }
        if (scheme != null) {
            schemeCut = mSchemeToFilter.get(scheme);
            if (debug) Slog.v(TAG, "Scheme list: " + schemeCut);
        }
        if (resolvedType == null && scheme == null && intent.getAction() != null) {
            firstTypeCut = mActionToFilter.get(intent.getAction());
            if (debug) Slog.v(TAG, "Action list: " + firstTypeCut);
        }
        if (firstTypeCut != null) {
            buildResolveList(intent, debug, defaultOnly,
                    resolvedType, scheme, firstTypeCut, finalList);
        }
        if (secondTypeCut != null) {
            buildResolveList(intent, debug, defaultOnly,
                    resolvedType, scheme, secondTypeCut, finalList);
        }
        if (thirdTypeCut != null) {
            buildResolveList(intent, debug, defaultOnly,
                    resolvedType, scheme, thirdTypeCut, finalList);
        }
        if (schemeCut != null) {
            buildResolveList(intent, debug, defaultOnly,
                    resolvedType, scheme, schemeCut, finalList);
        }
        sortResults(finalList);
        if (debug) {
            Slog.v(TAG, "Final result list:");
            for (R r : finalList) {
                Slog.v(TAG, "  " + r);
            }
        }
        return finalList;
    }
    protected boolean allowFilterResult(F filter, List<R> dest) {
        return true;
    }
    protected String packageForFilter(F filter) {
        return null;
    }
    protected R newResult(F filter, int match) {
        return (R)filter;
    }
    protected void sortResults(List<R> results) {
        Collections.sort(results, mResolvePrioritySorter);
    }
    protected void dumpFilter(PrintWriter out, String prefix, F filter) {
        out.print(prefix); out.println(filter);
    }
    private final int register_mime_types(F filter, String prefix) {
        final Iterator<String> i = filter.typesIterator();
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            String name = (String)i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            String baseName = name;
            final int slashpos = name.indexOf('/');
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + "
    private final HashSet<F> mFilters = new HashSet<F>();
    private final HashMap<String, ArrayList<F>> mTypeToFilter
            = new HashMap<String, ArrayList<F>>();
    private final HashMap<String, ArrayList<F>> mBaseTypeToFilter
            = new HashMap<String, ArrayList<F>>();
    private final HashMap<String, ArrayList<F>> mWildTypeToFilter
            = new HashMap<String, ArrayList<F>>();
    private final HashMap<String, ArrayList<F>> mSchemeToFilter
            = new HashMap<String, ArrayList<F>>();
    private final HashMap<String, ArrayList<F>> mActionToFilter
            = new HashMap<String, ArrayList<F>>();
    private final HashMap<String, ArrayList<F>> mTypedActionToFilter
            = new HashMap<String, ArrayList<F>>();
}
