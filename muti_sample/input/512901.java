public class XmlReportPrinter {
    private static final String TESTSUITE = "testsuite";
    private static final String TESTCASE = "testcase";
    private static final String ERROR = "error";
    private static final String FAILURE = "failure";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TIME = "time";
    private static final String ATTR_ERRORS = "errors";
    private static final String ATTR_FAILURES = "failures";
    private static final String ATTR_TESTS = "tests";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_MESSAGE = "message";
    private static final String PROPERTIES = "properties";
    private static final String ATTR_CLASSNAME = "classname";
    private static final String TIMESTAMP = "timestamp";
    private static final String HOSTNAME = "hostname";
    private static final String ns = null;
    private final Map<String, Suite> suites = new LinkedHashMap<String, Suite>();
    public XmlReportPrinter(CoreTestSuite allTests) {
        for (Enumeration<Test> e = allTests.tests(); e.hasMoreElements(); ) {
            TestId test = new TestId(e.nextElement());
            Suite suite = suites.get(test.className);
            if (suite == null) {
                suite = new Suite(test.className);
                suites.put(test.className, suite);
            }
            suite.tests.add(test);
        }
    }
    public void setResults(TestResult result) {
        populateFailures(true, result.errors());
        populateFailures(false, result.failures());
    }
    private void populateFailures(boolean errors, Enumeration<TestFailure> failures) {
        while (failures.hasMoreElements()) {
            TestFailure failure = failures.nextElement();
            TestId test = new TestId(failure.failedTest());
            Suite suite = suites.get(test.className);
            if (suite == null) {
                throw new IllegalStateException( "received a failure for a "
                        + "test that wasn't in the original test suite!");
            }
            if (errors) {
                suite.errors.put(test, failure);
            } else {
                suite.failures.put(test, failure);
            }
        }
    }
    public int generateReports(String directory) {
        File parent = new File(directory);
        parent.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmt);
        dateFormat.setLenient(true);
        String timestamp = dateFormat.format(new Date());
        for (Suite suite : suites.values()) {
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(new File(parent, "TEST-" + suite.name + ".xml"));
                KXmlSerializer serializer = new KXmlSerializer();
                serializer.setOutput(stream, "UTF-8");
                serializer.startDocument("UTF-8", null);
                serializer.setFeature(
                        "http:
                suite.print(serializer, timestamp);
                serializer.endDocument();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
        return suites.size();
    }
    static class Suite {
        private final String name;
        private final List<TestId> tests = new ArrayList<TestId>();
        private final Map<TestId, TestFailure> failures = new HashMap<TestId, TestFailure>();
        private final Map<TestId, TestFailure> errors = new HashMap<TestId, TestFailure>();
        Suite(String name) {
            this.name = name;
        }
        void print(KXmlSerializer serializer, String timestamp) throws IOException {
            serializer.startTag(ns, TESTSUITE);
            serializer.attribute(ns, ATTR_NAME, name);
            serializer.attribute(ns, ATTR_TESTS, Integer.toString(tests.size()));
            serializer.attribute(ns, ATTR_FAILURES, Integer.toString(failures.size()));
            serializer.attribute(ns, ATTR_ERRORS, Integer.toString(errors.size()));
            serializer.attribute(ns, ATTR_TIME, "0");
            serializer.attribute(ns, TIMESTAMP, timestamp);
            serializer.attribute(ns, HOSTNAME, "localhost");
            serializer.startTag(ns, PROPERTIES);
            serializer.endTag(ns, PROPERTIES);
            for (TestId testId : tests) {
                TestFailure error = errors.get(testId);
                TestFailure failure = failures.get(testId);
                if (error != null) {
                    testId.printFailure(serializer, ERROR, error.thrownException());
                } else if (failure != null) {
                    testId.printFailure(serializer, FAILURE, failure.thrownException());
                } else {
                    testId.printSuccess(serializer);
                }
            }
            serializer.endTag(ns, TESTSUITE);
        }
    }
    private static class TestId {
        private final String name;
        private final String className;
        TestId(Test test) {
            this.name = test instanceof TestCase
                    ? ((TestCase) test).getName()
                    : test.toString();
            this.className = test.getClass().getName();
        }
        void printSuccess(KXmlSerializer serializer) throws IOException {
            serializer.startTag(ns, TESTCASE);
            printAttributes(serializer);
            serializer.endTag(ns, TESTCASE);
        }
        void printFailure(KXmlSerializer serializer, String type, Throwable t)
                throws IOException {
            serializer.startTag(ns, TESTCASE);
            printAttributes(serializer);
            serializer.startTag(ns, type);
            String message = t.getMessage();
            if (message != null && message.length() > 0) {
                serializer.attribute(ns, ATTR_MESSAGE, t.getMessage());
            }
            serializer.attribute(ns, ATTR_TYPE, t.getClass().getName());
            serializer.text(BaseTestRunner.getFilteredTrace(t));
            serializer.endTag(ns, type);
            serializer.endTag(ns, TESTCASE);
        }
        void printAttributes(KXmlSerializer serializer) throws IOException {
            serializer.attribute(ns, ATTR_NAME, name);
            serializer.attribute(ns, ATTR_CLASSNAME, className);
            serializer.attribute(ns, ATTR_TIME, "0");
        }
        @Override public boolean equals(Object o) {
            return o instanceof TestId
                    && ((TestId) o).name.equals(name)
                    && ((TestId) o).className.equals(className);
        }
        @Override public int hashCode() {
            return name.hashCode() ^ className.hashCode();
        }
    }
}
