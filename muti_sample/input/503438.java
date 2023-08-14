public class SearchablesTest extends AndroidTestCase {
    public void testNonSearchable() {
        Searchables searchables = new Searchables(mContext);
        searchables.buildSearchableList();
        ComponentName nonActivity = new ComponentName(
                            "com.android.frameworks.coretests",
                            "com.android.frameworks.coretests.activity.NO_SEARCH_ACTIVITY");
        SearchableInfo si = searchables.getSearchableInfo(nonActivity);
        assertNull(si);
    }
    public void testSearchablesListReal() {
        MyMockPackageManager mockPM = new MyMockPackageManager(mContext.getPackageManager());
        MyMockContext mockContext = new MyMockContext(mContext, mockPM);
        mockPM.setSearchablesMode(MyMockPackageManager.SEARCHABLES_PASSTHROUGH);
        Searchables searchables = new Searchables(mockContext);
        searchables.buildSearchableList();
        ArrayList<SearchableInfo> searchablesList = searchables.getSearchablesList();
        int count = searchablesList.size();
        assertTrue(count >= 1);         
        checkSearchables(searchablesList);
        ArrayList<SearchableInfo> global = searchables.getSearchablesInGlobalSearchList();
        checkSearchables(global);
    }
    public void testSearchablesListEmpty() {
        MyMockPackageManager mockPM = new MyMockPackageManager(mContext.getPackageManager());
        MyMockContext mockContext = new MyMockContext(mContext, mockPM);
        mockPM.setSearchablesMode(MyMockPackageManager.SEARCHABLES_MOCK_ZERO);
        Searchables searchables = new Searchables(mockContext);
        searchables.buildSearchableList();
        ArrayList<SearchableInfo> searchablesList = searchables.getSearchablesList();
        assertNotNull(searchablesList);
        MoreAsserts.assertEmpty(searchablesList);
        ArrayList<SearchableInfo> global = searchables.getSearchablesInGlobalSearchList();
        MoreAsserts.assertEmpty(global);
    }
    private void checkSearchables(ArrayList<SearchableInfo> searchablesList) {
        assertNotNull(searchablesList);
        int count = searchablesList.size();
        for (int ii = 0; ii < count; ii++) {
            SearchableInfo si = searchablesList.get(ii);
            checkSearchable(si);
        }
    }
    private void checkSearchable(SearchableInfo si) {
        assertNotNull(si);
        assertTrue(si.getLabelId() != 0);        
        assertNotEmpty(si.getSearchActivity().getClassName());
        assertNotEmpty(si.getSearchActivity().getPackageName());
        if (si.getSuggestAuthority() != null) {
            assertNotEmpty(si.getSuggestAuthority());
            assertNullOrNotEmpty(si.getSuggestPath());
            assertNullOrNotEmpty(si.getSuggestSelection());
            assertNullOrNotEmpty(si.getSuggestIntentAction());
            assertNullOrNotEmpty(si.getSuggestIntentData());
        }
        ActionKeyInfo ai = si.findActionKey(KeyEvent.KEYCODE_CALL);
        if (ai != null) {
            assertEquals(ai.getKeyCode(), KeyEvent.KEYCODE_CALL);
            boolean m1 = (ai.getQueryActionMsg() != null) && (ai.getQueryActionMsg().length() > 0);
            boolean m2 = (ai.getSuggestActionMsg() != null) && (ai.getSuggestActionMsg().length() > 0);
            boolean m3 = (ai.getSuggestActionMsgColumn() != null) && 
                            (ai.getSuggestActionMsgColumn().length() > 0);
            assertTrue(m1 || m2 || m3);
        }
    }
    private void assertNotEmpty(final String s) {
        assertNotNull(s);
        MoreAsserts.assertNotEqual(s, "");
    }
    private void assertNullOrNotEmpty(final String s) {
        if (s != null) {
            MoreAsserts.assertNotEqual(s, "");
        }
    }    
    private class MyMockContext extends MockContext {
        protected Context mRealContext;
        protected PackageManager mPackageManager;
        MyMockContext(Context realContext, PackageManager packageManager) {
            mRealContext = realContext;
            mPackageManager = packageManager;
        }
        @Override
        public Resources getResources() {
            return mRealContext.getResources();
        }
        @Override
        public PackageManager getPackageManager() {
            return mPackageManager;
        }
        @Override
        public Context createPackageContext(String packageName, int flags)
                throws PackageManager.NameNotFoundException {
            return mRealContext.createPackageContext(packageName, flags);
        }
        @Override
        public void sendBroadcast(Intent intent) {
            mRealContext.sendBroadcast(intent);
        }
    }
    private class MyMockPackageManager extends MockPackageManager {
        public final static int SEARCHABLES_PASSTHROUGH = 0;
        public final static int SEARCHABLES_MOCK_ZERO = 1;
        public final static int SEARCHABLES_MOCK_ONEGOOD = 2;
        public final static int SEARCHABLES_MOCK_ONEGOOD_ONEBAD = 3;
        protected PackageManager mRealPackageManager;
        protected int mSearchablesMode;
        public MyMockPackageManager(PackageManager realPM) {
            mRealPackageManager = realPM;
            mSearchablesMode = SEARCHABLES_PASSTHROUGH;
        }
        public void setSearchablesMode(int newMode) {
            switch (newMode) {
            case SEARCHABLES_PASSTHROUGH:
            case SEARCHABLES_MOCK_ZERO:
                mSearchablesMode = newMode;
                break;
            default:
                throw new UnsupportedOperationException();       
            }
        }
        @Override 
        public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
            assertNotNull(intent);
            assertTrue(intent.getAction().equals(Intent.ACTION_SEARCH)
                    || intent.getAction().equals(Intent.ACTION_WEB_SEARCH)
                    || intent.getAction().equals(SearchManager.INTENT_ACTION_GLOBAL_SEARCH));
            switch (mSearchablesMode) {
            case SEARCHABLES_PASSTHROUGH:
                return mRealPackageManager.queryIntentActivities(intent, flags);
            case SEARCHABLES_MOCK_ZERO:
                return null;
            default:
                throw new UnsupportedOperationException();
            }
        }
        @Override
        public ResolveInfo resolveActivity(Intent intent, int flags) {
            assertNotNull(intent);
            assertTrue(intent.getAction().equals(Intent.ACTION_WEB_SEARCH)
                    || intent.getAction().equals(SearchManager.INTENT_ACTION_GLOBAL_SEARCH));
            switch (mSearchablesMode) {
            case SEARCHABLES_PASSTHROUGH:
                return mRealPackageManager.resolveActivity(intent, flags);
            case SEARCHABLES_MOCK_ZERO:
                return null;
            default:
                throw new UnsupportedOperationException();
            }
        }
        @Override 
        public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
            assertNotNull(packageName);
            MoreAsserts.assertNotEqual(packageName, "");
            MoreAsserts.assertNotEqual(resid, 0);
            switch (mSearchablesMode) {
            case SEARCHABLES_PASSTHROUGH:
                return mRealPackageManager.getXml(packageName, resid, appInfo);
            case SEARCHABLES_MOCK_ZERO:
            default:
                throw new UnsupportedOperationException();
            }
        }
        @Override 
        public ProviderInfo resolveContentProvider(String name, int flags) {
            assertNotNull(name);
            MoreAsserts.assertNotEqual(name, "");
            assertEquals(flags, 0);
            switch (mSearchablesMode) {
            case SEARCHABLES_PASSTHROUGH:
                return mRealPackageManager.resolveContentProvider(name, flags);
            case SEARCHABLES_MOCK_ZERO:
            default:
                throw new UnsupportedOperationException();
            }
        }
        @Override
        public ActivityInfo getActivityInfo(ComponentName name, int flags)
                throws NameNotFoundException {
            assertNotNull(name);
            MoreAsserts.assertNotEqual(name, "");
            switch (mSearchablesMode) {
            case SEARCHABLES_PASSTHROUGH:
                return mRealPackageManager.getActivityInfo(name, flags);
            case SEARCHABLES_MOCK_ZERO:
                throw new NameNotFoundException();
            default:
                throw new UnsupportedOperationException();
            }
        }
        @Override
        public int checkPermission(String permName, String pkgName) {
            assertNotNull(permName);
            assertNotNull(pkgName);
            switch (mSearchablesMode) {
                case SEARCHABLES_PASSTHROUGH:
                    return mRealPackageManager.checkPermission(permName, pkgName);
                case SEARCHABLES_MOCK_ZERO:
                    return PackageManager.PERMISSION_DENIED;
                default:
                    throw new UnsupportedOperationException();
                }
        }
    }
}
