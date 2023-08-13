public class DescriptionGenerator extends Doclet {
    static final String HOST_CONTROLLER = "dalvik.annotation.HostController";
    static final String KNOWN_FAILURE = "dalvik.annotation.KnownFailure";
    static final String BROKEN_TEST = "dalvik.annotation.BrokenTest";
    static final String JUNIT_TEST_CASE_CLASS_NAME = "junit.framework.testcase";
    static final String TAG_PACKAGE = "TestPackage";
    static final String TAG_SUITE = "TestSuite";
    static final String TAG_CASE = "TestCase";
    static final String TAG_TEST = "Test";
    static final String TAG_DESCRIPTION = "Description";
    static final String ATTRIBUTE_NAME_VERSION = "version";
    static final String ATTRIBUTE_VALUE_VERSION = "1.0";
    static final String ATTRIBUTE_NAME_FRAMEWORK = "AndroidFramework";
    static final String ATTRIBUTE_VALUE_FRAMEWORK = "Android 1.0";
    static final String ATTRIBUTE_NAME = "name";
    static final String ATTRIBUTE_HOST_CONTROLLER = "HostController";
    static final String ATTRIBUTE_KNOWN_FAILURE = "KnownFailure";
    static final String XML_OUTPUT_PATH = "./description.xml";
    static final String OUTPUT_PATH_OPTION = "-o";
    public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();
        if (classes == null) {
            Log.e("No class found!", null);
            return true;
        }
        String outputPath = XML_OUTPUT_PATH;
        String[][] options = root.options();
        for (String[] option : options) {
            if (option.length == 2 && option[0].equals(OUTPUT_PATH_OPTION)) {
                outputPath = option[1];
            }
        }
        XMLGenerator xmlGenerator = null;
        try {
            xmlGenerator = new XMLGenerator(outputPath);
        } catch (ParserConfigurationException e) {
            Log.e("Cant initialize XML Generator!", e);
            return true;
        }
        for (ClassDoc clazz : classes) {
            if ((!clazz.isAbstract()) && (isValidJUnitTestCase(clazz))) {
                xmlGenerator.addTestClass(new TestClass(clazz));
            }
        }
        try {
            xmlGenerator.dump();
        } catch (Exception e) {
            Log.e("Can't dump to XML file!", e);
        }
        return true;
    }
    public static int optionLength(String option) {
        if (option.equals(OUTPUT_PATH_OPTION)) {
            return 2;
        }
        return 0;
    }
    static boolean isValidJUnitTestCase(ClassDoc clazz) {
        while((clazz = clazz.superclass()) != null) {
            if (JUNIT_TEST_CASE_CLASS_NAME.equals(clazz.qualifiedName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    static class Log {
        private static boolean TRACE = true;
        private static BufferedWriter mTraceOutput = null;
        static void e(String msg, Exception e) {
            System.out.println(msg);
            if (e != null) {
                e.printStackTrace();
            }
        }
        public static void t(String msg) {
            if (TRACE) {
                try {
                    if ((mTraceOutput != null) && (msg != null)) {
                        mTraceOutput.write(msg + "\n");
                        mTraceOutput.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public static void initTrace(String name) {
            if (TRACE) {
                try {
                    if (mTraceOutput == null) {
                        String fileName = "cts_debug_dg_" + name + ".txt";
                        mTraceOutput = new BufferedWriter(new FileWriter(fileName));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public static void closeTrace() {
            if (mTraceOutput != null) {
                try {
                    mTraceOutput.close();
                    mTraceOutput = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static class XMLGenerator {
        String mOutputPath;
        Document mDoc;
        XMLGenerator(String outputPath) throws ParserConfigurationException {
            mOutputPath = outputPath;
            mDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Node testPackageElem = mDoc.appendChild(mDoc.createElement(TAG_PACKAGE));
            setAttribute(testPackageElem, ATTRIBUTE_NAME_VERSION, ATTRIBUTE_VALUE_VERSION);
            setAttribute(testPackageElem, ATTRIBUTE_NAME_FRAMEWORK, ATTRIBUTE_VALUE_FRAMEWORK);
        }
        void addTestClass(TestClass tc) {
            appendSuiteToElement(mDoc.getDocumentElement(), tc);
        }
        void dump() throws TransformerFactoryConfigurationError,
                FileNotFoundException, TransformerException {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty("indent", "yes");
            t.setOutputProperty("{http:
            File file = new File(mOutputPath);
            file.getParentFile().mkdirs();
            t.transform(new DOMSource(mDoc),
                    new StreamResult(new FileOutputStream(file)));
        }
        void rebuildDocument() {
            Collection<Node> suiteElems = getUnmutableChildNodes(mDoc.getDocumentElement());
            Iterator<Node> suiteIterator = suiteElems.iterator();
            while (suiteIterator.hasNext()) {
                Node suiteElem = suiteIterator.next();
                mergeEmptySuites(suiteElem);
            }
        }
        void mergeEmptySuites(Node suiteElem) {
            Collection<Node> suiteChildren = getSuiteChildren(suiteElem);
            if (suiteChildren.size() > 1) {
                for (Node suiteChild : suiteChildren) {
                    mergeEmptySuites(suiteChild);
                }
            } else if (suiteChildren.size() == 1) {
                Node child = suiteChildren.iterator().next();
                String newName = getAttribute(suiteElem, ATTRIBUTE_NAME) + "."
                        + getAttribute(child, ATTRIBUTE_NAME);
                setAttribute(child, ATTRIBUTE_NAME, newName);
                Node parentNode = suiteElem.getParentNode();
                parentNode.removeChild(suiteElem);
                parentNode.appendChild(child);
                mergeEmptySuites(child);
            }
        }
        private Collection<Node> getUnmutableChildNodes(Node node) {
            ArrayList<Node> nodes = new ArrayList<Node>();
            NodeList nodelist = node.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                nodes.add(nodelist.item(i));
            }
            return nodes;
        }
        void appendSuiteToElement(Node elem, TestClass testSuite) {
            String suiteName = testSuite.mName;
            Collection<Node> children = getSuiteChildren(elem);
            int dotIndex = suiteName.indexOf('.');
            String name = dotIndex == -1 ? suiteName : suiteName.substring(0, dotIndex);
            boolean foundMatch = false;
            for (Node child : children) {
                String childName = child.getAttributes().getNamedItem(ATTRIBUTE_NAME)
                        .getNodeValue();
                if (childName.equals(name)) {
                    foundMatch = true;
                    if (dotIndex == -1) {
                        appendTestCases(child, testSuite.mCases);
                    } else {
                        testSuite.mName = suiteName.substring(dotIndex + 1, suiteName.length());
                        appendSuiteToElement(child, testSuite);
                    }
                }
            }
            if (!foundMatch) {
                appendSuiteToElementImpl(elem, testSuite);
            }
        }
        Collection<Node> getSuiteChildren(Node elem) {
            ArrayList<Node> suites = new ArrayList<Node>();
            NodeList children = elem.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals(DescriptionGenerator.TAG_SUITE)) {
                    suites.add(child);
                }
            }
            return suites;
        }
        void appendTestCases(Node elem, Collection<TestMethod> cases) {
            if (cases.isEmpty()) {
                elem.getParentNode().removeChild(elem);
            } else {
                for (TestMethod caze : cases) {
                    if (caze.mIsBroken) {
                        continue;
                    }
                    Node caseNode = elem.appendChild(mDoc.createElement(TAG_TEST));
                    setAttribute(caseNode, ATTRIBUTE_NAME, caze.mName);
                    if ((caze.mController != null) && (caze.mController.length() != 0)) {
                        setAttribute(caseNode, ATTRIBUTE_HOST_CONTROLLER, caze.mController);
                    }
                    if (caze.mKnownFailure != null) {
                        setAttribute(caseNode, ATTRIBUTE_KNOWN_FAILURE, caze.mKnownFailure);
                    }
                    if (caze.mDescription != null && !caze.mDescription.equals("")) {
                        caseNode.appendChild(mDoc.createElement(TAG_DESCRIPTION))
                                .setTextContent(caze.mDescription);
                    }
                }
            }
        }
        protected void setAttribute(Node elem, String name, String value) {
            Attr attr = mDoc.createAttribute(name);
            attr.setNodeValue(value);
            elem.getAttributes().setNamedItem(attr);
        }
        private String getAttribute(Node elem, String name) {
            return elem.getAttributes().getNamedItem(name).getNodeValue();
        }
        void appendSuiteToElementImpl(Node elem, TestClass testSuite) {
            Node parent = elem;
            String suiteName = testSuite.mName;
            int dotIndex;
            while ((dotIndex = suiteName.indexOf('.')) != -1) {
                String name = suiteName.substring(0, dotIndex);
                Node suiteElem = parent.appendChild(mDoc.createElement(TAG_SUITE));
                setAttribute(suiteElem, ATTRIBUTE_NAME, name);
                parent = suiteElem;
                suiteName = suiteName.substring(dotIndex + 1, suiteName.length());
            }
            Node leafSuiteElem = parent.appendChild(mDoc.createElement(TAG_CASE));
            setAttribute(leafSuiteElem, ATTRIBUTE_NAME, suiteName);
            appendTestCases(leafSuiteElem, testSuite.mCases);
        }
    }
    static class TestClass {
        String mName;
        Collection<TestMethod> mCases;
        TestClass(String name, Collection<TestMethod> cases) {
            mName = name;
            mCases = cases;
        }
        TestClass(ClassDoc clazz) {
            mName = clazz.toString();
            mCases = getTestMethods(clazz);
        }
        Collection<TestMethod> getTestMethods(ClassDoc clazz) {
            Collection<MethodDoc> methods = getAllMethods(clazz);
            ArrayList<TestMethod> cases = new ArrayList<TestMethod>();
            Iterator<MethodDoc> iterator = methods.iterator();
            while (iterator.hasNext()) {
                MethodDoc method = iterator.next();
                String name = method.name();
                AnnotationDesc[] annotations = method.annotations();
                String controller = "";
                String knownFailure = null;
                boolean isBroken = false;
                for (AnnotationDesc cAnnot : annotations) {
                    AnnotationTypeDoc atype = cAnnot.annotationType();
                    if (atype.toString().equals(HOST_CONTROLLER)) {
                        controller = getAnnotationDescription(cAnnot);
                    } else if (atype.toString().equals(KNOWN_FAILURE)) {
                        knownFailure = getAnnotationDescription(cAnnot);
                    } else if (atype.toString().equals(BROKEN_TEST)) {
                        isBroken = true;
                    }
                }
                if (name.startsWith("test")) {
                    cases.add(new TestMethod(name, method.commentText(), controller, knownFailure,
                            isBroken));
                }
            }
            return cases;
        }
        String getAnnotationDescription(AnnotationDesc cAnnot) {
            ElementValuePair[] cpairs = cAnnot.elementValues();
            ElementValuePair evp = cpairs[0];
            AnnotationValue av = evp.value();
            String description = av.toString();
            description = description.substring(1, description.length() -1);
            return description;
        }
        Collection<MethodDoc> getAllMethods(ClassDoc clazz) {
            ArrayList<MethodDoc> methods = new ArrayList<MethodDoc>();
            for (MethodDoc method : clazz.methods()) {
                methods.add(method);
            }
            ClassDoc superClass = clazz.superclass();
            while (superClass != null) {
                for (MethodDoc method : superClass.methods()) {
                    methods.add(method);
                }
                superClass = superClass.superclass();
            }
            return methods;
        }
    }
    static class TestMethod {
        String mName;
        String mDescription;
        String mController;
        String mKnownFailure;
        boolean mIsBroken;
        TestMethod(String name, String description, String controller, String knownFailure,
                boolean isBroken) {
            mName = name;
            mDescription = description;
            mController = controller;
            mKnownFailure = knownFailure;
            mIsBroken = isBroken;
        }
    }
}
