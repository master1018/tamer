abstract public class Selector {
    public static final String PACKAGE_SELECTOR = "packageSelector";
    public static final String SUITE_SELECTOR   = "suiteSelector";
    public static final String CASE_SELECTOR    = "caseSelector";
    public static final String TEST_SELECTOR    = "testSelector";
    public static final String PACKAGE_HDR  = "[Choose package] ";
    public static final String PACKAGE_TAIL =
        ": select[Y], reject[n], or choose suite in it[m]?  [Y/n/m] ";
    public static final String SUITE_HDR    = "[Choose suite]   ";
    public static final String SUITE_TAIL   =
        ": select[Y], reject[n], or choose case in it[m]?  [Y/n/m] ";
    public static final String CASE_HDR  = "[Choose case]    ";
    public static final String CASE_TAIL =
        ": select[Y], reject[n], or choose method in it[m]?  [Y/n/m] ";
    public static final String TEST_HDR     = "[Choose test]    ";
    public static final String TEST_TAIL    = "?: [Y/n] ";
    public enum Selection {
        ACCEPT, REJECT, MORE
    }
    public List<String> mRecords;
    public String mHdr;
    public String mTail;
    public String mType;
    public BufferedReader mBufferedReader;
    public Selector(String type, List<String> records) {
        mRecords = records;
        Collections.sort(mRecords);
        mBufferedReader = null;
        mType = type;
        if (type.equals(PACKAGE_SELECTOR)) {
            mHdr  = PACKAGE_HDR;
            mTail = PACKAGE_TAIL;
        } else if (type.equals(SUITE_SELECTOR)) {
            mHdr  = SUITE_HDR;
            mTail = SUITE_TAIL;
        } else if (type.equals(CASE_SELECTOR)) {
            mHdr  = CASE_HDR;
            mTail = CASE_TAIL;
        } else if (type.equals(TEST_SELECTOR)) {
            mHdr  = TEST_HDR;
            mTail = TEST_TAIL;
        }
    }
    public void setInputStream(BufferedReader in) {
        mBufferedReader = in;
    }
    public String readLine(String prompt) throws IOException {
        String str = null;
        if (mBufferedReader != null) {
            CUIOutputStream.print(prompt);
            str = mBufferedReader.readLine().trim();
        }
        return str;
    }
    public Selection doAccept(final String name) throws IOException {
        Selection selection = Selection.REJECT;
        if (mType.equals(TEST_SELECTOR)) {
            String prompt = mHdr + "Include " + name + mTail;
            String answer = readLine(prompt);
            while (!answer.matches("[yn]?")) {
                CUIOutputStream.println(
                           "Invalid input. Please chose 'y' or 'n' (default is 'y')");
                answer = readLine(prompt);
            }
            if (ConsoleUi.isConfirmation(answer, true)) {
                selection = Selection.ACCEPT;
            }
        } else {
            String prompt = mHdr + name + mTail;
            String answer = readLine(prompt);
            while (!answer.matches("[ynm]?")) {
                CUIOutputStream.println(
                           "Invalid input. Please chose 'y', 'n', or 'm' (default is 'y')");
                answer = readLine(prompt);
            }
            if (ConsoleUi.isConfirmation(answer, true)) {
                selection = Selection.ACCEPT;
            } else if (0 == "m".compareToIgnoreCase(answer)) {
                selection = Selection.MORE;
            }
        }
        return selection;
    }
    public boolean isSelected(ArrayList<String> selectedList, String name) {
        for (String str : selectedList) {
            if (name.equals(str) || isSubName(str, name)) {
                return true;
            }
        }
        return false;
    }
     private boolean isSubName(String parent, String name) {
        return name.startsWith(parent + ".");
    }
}
class PlanBuilder extends Selector{
    private SuiteSelector mSuiteSelector;
    public PlanBuilder(List<String> records) {
        super(PACKAGE_SELECTOR, records);
    }
    public HashMap<String, ArrayList<String>> doSelect() throws IOException {
        HashMap<String, ArrayList<String>> packages = new HashMap<String, ArrayList<String>>();
        for (String javaPkgName : mRecords) {
            Selection select = doAccept(javaPkgName);
            TestPackage testPackage =
                HostConfig.getInstance().getTestPackage(javaPkgName);
            if (select == Selection.ACCEPT) {
                packages.put(javaPkgName, null);
            } else if (select == Selection.MORE) {
                List<String> suiteNames = testPackage.getAllTestSuiteNames();
                mSuiteSelector = new SuiteSelector(suiteNames, testPackage);
                if (mBufferedReader != null) {
                    mSuiteSelector.setInputStream(mBufferedReader);
                }
                ArrayList<String> excludedSuites = new ArrayList<String>();
                ArrayList<String> excludedCases = new ArrayList<String>();
                mSuiteSelector.doSelect(excludedSuites, excludedCases);
                if (suiteNames.size() == excludedSuites.size()) {
                    Log.i("package=" + javaPkgName + " has been removed all.");
                } else {
                    excludedSuites.addAll(excludedCases);
                    packages.put(javaPkgName, excludedSuites);
                }
            }
        }
        if (packages.size() == 0) {
            return null;
        } else {
            return packages;
        }
    }
    class SuiteSelector extends Selector {
        private TestCaseSelector mCaseSelector;
        private TestPackage mTestPackage;
        public SuiteSelector(List<String> suites, TestPackage testPackage) {
            super(SUITE_SELECTOR, suites);
            mTestPackage = testPackage;
        }
        public void doSelect(ArrayList<String> excludedTestSuites,
                ArrayList<String> excludedTestCases) throws IOException {
            ArrayList<String> selectedList = new ArrayList<String>();
            for (String suiteName : mRecords) {
                if (!isSelected(selectedList, suiteName)) {
                    Selection select = doAccept(suiteName);
                    if (select == Selection.REJECT) {
                        excludedTestSuites.add(suiteName);
                    } else if (select == Selection.MORE) {
                        List<String> testCaseNames =
                            mTestPackage.getAllTestCaseNames(suiteName);
                        mCaseSelector = new TestCaseSelector(testCaseNames, mTestPackage);
                        if (mBufferedReader != null) {
                            mCaseSelector.setInputStream(mBufferedReader);
                        }
                        ArrayList<String> notIncludedTestCases = new ArrayList<String>();
                        ArrayList<String> notIncludedTests = new ArrayList<String>();
                        mCaseSelector.doSelect(notIncludedTestCases, notIncludedTests);
                        if (testCaseNames.size() == notIncludedTestCases.size()) {
                            Log.i("suite=" + suiteName + " has been removed all");
                            excludedTestSuites.add(suiteName);
                        } else {
                            excludedTestCases.addAll(notIncludedTestCases);
                            excludedTestCases.addAll(notIncludedTests);
                        }
                    }
                    selectedList.add(suiteName);
                }
            }
        }
    }
    class TestCaseSelector extends Selector {
        private TestSelector mTestSelector;
        private TestPackage mTestPackage;
        public TestCaseSelector(List<String> testCases, TestPackage testPackage) {
            super(CASE_SELECTOR, testCases);
            mTestPackage = testPackage;
        }
        public void doSelect(ArrayList<String> excludedTestCases,
                ArrayList<String> excludedTests) throws IOException {
            ArrayList<String> selectedList = new ArrayList<String>();
            for (String testCaseName : mRecords) {
                if (!isSelected(selectedList, testCaseName)) {
                    Selection select = doAccept(testCaseName);
                    if (select == Selection.REJECT) {
                        excludedTestCases.add(testCaseName);
                    } else if (select == Selection.MORE) {
                        List<String> testNames = mTestPackage.getAllTestNames(testCaseName);
                        mTestSelector = new TestSelector(testNames);
                        if (mBufferedReader != null) {
                            mTestSelector.setInputStream(mBufferedReader);
                        }
                        ArrayList<String> notIncludedTests = mTestSelector.doSelect();
                        if (notIncludedTests.size() == testNames.size()) {
                            Log.i("testCase=" + testCaseName + " has been removed all");
                            excludedTestCases.add(testCaseName);
                        } else {
                            excludedTests.addAll(notIncludedTests);
                        }
                    }
                    selectedList.add(testCaseName);
                }
            }
        }
    }
    class TestSelector extends Selector {
        public TestSelector(List<String> records) {
            super(TEST_SELECTOR, records);
        }
        public ArrayList<String> doSelect() throws IOException {
            ArrayList<String> records = new ArrayList<String>();
            for (String test : mRecords) {
                Selection select = doAccept(test);
                if (select == Selection.REJECT) {
                    records.add(test);
                }
            }
            return records;
        }
    }
}
