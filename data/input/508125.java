 class PropertyNodesVerifier extends VNodeBuilder {
    private final List<PropertyNodesVerifierElem> mPropertyNodesVerifierElemList;
    private final AndroidTestCase mAndroidTestCase;
    private int mIndex;
    public PropertyNodesVerifier(AndroidTestCase testCase) {
        mPropertyNodesVerifierElemList = new ArrayList<PropertyNodesVerifierElem>();
        mAndroidTestCase = testCase;
    }
    public PropertyNodesVerifierElem addPropertyNodesVerifierElem() {
        PropertyNodesVerifierElem elem = new PropertyNodesVerifierElem(mAndroidTestCase);
        mPropertyNodesVerifierElemList.add(elem);
        return elem;
    }
    public void verify(int resId, int vCardType)
            throws IOException, VCardException {
        verify(mAndroidTestCase.getContext().getResources().openRawResource(resId), vCardType);
    }
    public void verify(int resId, int vCardType, final VCardParser vCardParser)
            throws IOException, VCardException {
        verify(mAndroidTestCase.getContext().getResources().openRawResource(resId),
                vCardType, vCardParser);
    }
    public void verify(InputStream is, int vCardType) throws IOException, VCardException {
        final VCardParser vCardParser;
        if (VCardConfig.isV30(vCardType)) {
            vCardParser = new VCardParser_V30(true);  
        } else {
            vCardParser = new VCardParser_V21();
        }
        verify(is, vCardType, vCardParser);
    }
    public void verify(InputStream is, int vCardType, final VCardParser vCardParser)
            throws IOException, VCardException {
        try {
            vCardParser.parse(is, this);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
    @Override
    public void endEntry() {
        super.endEntry();
        mAndroidTestCase.assertTrue(mIndex < mPropertyNodesVerifierElemList.size());
        mAndroidTestCase.assertTrue(mIndex < vNodeList.size());
        mPropertyNodesVerifierElemList.get(mIndex).verify(vNodeList.get(mIndex));
        mIndex++;
    }
}
 class PropertyNodesVerifierElem {
    public static class TypeSet extends HashSet<String> {
        public TypeSet(String ... array) {
            super(Arrays.asList(array));
        }
    }
    public static class GroupSet extends HashSet<String> {
        public GroupSet(String ... array) {
            super(Arrays.asList(array));
        }
    }
    private final HashMap<String, List<PropertyNode>> mOrderedNodeMap;
    private final ArrayList<PropertyNode> mUnorderedNodeList;
    private final TestCase mTestCase;
    public PropertyNodesVerifierElem(TestCase testCase) {
        mOrderedNodeMap = new HashMap<String, List<PropertyNode>>();
        mUnorderedNodeList = new ArrayList<PropertyNode>();
        mTestCase = testCase;
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName, String propValue) {
        return addExpectedNodeWithOrder(propName, propValue, null, null, null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(
            String propName, String propValue, ContentValues contentValues) {
        return addExpectedNodeWithOrder(propName, propValue, null,
                null, contentValues, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(
            String propName, List<String> propValueList, ContentValues contentValues) {
        return addExpectedNodeWithOrder(propName, null, propValueList,
                null, contentValues, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(
            String propName, String propValue, List<String> propValueList) {
        return addExpectedNodeWithOrder(propName, propValue, propValueList, null,
                null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(
            String propName, List<String> propValueList) {
        final String propValue = concatinateListWithSemiColon(propValueList);
        return addExpectedNodeWithOrder(propName, propValue.toString(), propValueList,
                null, null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName, String propValue,
            TypeSet paramMap_TYPE) {
        return addExpectedNodeWithOrder(propName, propValue, null,
                null, null, paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName,
            List<String> propValueList, TypeSet paramMap_TYPE) {
        return addExpectedNodeWithOrder(propName, null, propValueList, null, null,
                paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName, String propValue,
            ContentValues paramMap, TypeSet paramMap_TYPE) {
        return addExpectedNodeWithOrder(propName, propValue, null, null,
                paramMap, paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName, String propValue,
            List<String> propValueList, TypeSet paramMap_TYPE) {
        return addExpectedNodeWithOrder(propName, propValue, propValueList, null, null,
                paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNodeWithOrder(String propName, String propValue,
            List<String> propValueList, byte[] propValue_bytes,
            ContentValues paramMap, TypeSet paramMap_TYPE, GroupSet propGroupSet) {
        if (propValue == null && propValueList != null) {
            propValue = concatinateListWithSemiColon(propValueList);
        }
        PropertyNode propertyNode = new PropertyNode(propName,
                propValue, propValueList, propValue_bytes,
                paramMap, paramMap_TYPE, propGroupSet);
        List<PropertyNode> expectedNodeList = mOrderedNodeMap.get(propName);
        if (expectedNodeList == null) {
            expectedNodeList = new ArrayList<PropertyNode>();
            mOrderedNodeMap.put(propName, expectedNodeList);
        }
        expectedNodeList.add(propertyNode);
        return this;
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue) {
        return addExpectedNode(propName, propValue, null, null, null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            ContentValues contentValues) {
        return addExpectedNode(propName, propValue, null, null, contentValues, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName,
            List<String> propValueList, ContentValues contentValues) {
        return addExpectedNode(propName, null,
                propValueList, null, contentValues, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            List<String> propValueList) {
        return addExpectedNode(propName, propValue, propValueList, null, null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName,
            List<String> propValueList) {
        return addExpectedNode(propName, null, propValueList,
                null, null, null, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            TypeSet paramMap_TYPE) {
        return addExpectedNode(propName, propValue, null, null, null, paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName,
            List<String> propValueList, TypeSet paramMap_TYPE) {
        final String propValue = concatinateListWithSemiColon(propValueList);
        return addExpectedNode(propName, propValue, propValueList, null, null,
                paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            List<String> propValueList, TypeSet paramMap_TYPE) {
        return addExpectedNode(propName, propValue, propValueList, null, null,
                paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            ContentValues paramMap, TypeSet paramMap_TYPE) {
        return addExpectedNode(propName, propValue, null, null,
                paramMap, paramMap_TYPE, null);
    }
    public PropertyNodesVerifierElem addExpectedNode(String propName, String propValue,
            List<String> propValueList, byte[] propValue_bytes,
            ContentValues paramMap, TypeSet paramMap_TYPE, GroupSet propGroupSet) {
        if (propValue == null && propValueList != null) {
            propValue = concatinateListWithSemiColon(propValueList);
        }
        mUnorderedNodeList.add(new PropertyNode(propName, propValue,
                propValueList, propValue_bytes, paramMap, paramMap_TYPE, propGroupSet));
        return this;
    }
    public void verify(VNode vnode) {
        for (PropertyNode actualNode : vnode.propList) {
            verifyNode(actualNode.propName, actualNode);
        }
        if (!mOrderedNodeMap.isEmpty() || !mUnorderedNodeList.isEmpty()) {
            List<String> expectedProps = new ArrayList<String>();
            for (List<PropertyNode> nodes : mOrderedNodeMap.values()) {
                for (PropertyNode node : nodes) {
                    if (!expectedProps.contains(node.propName)) {
                        expectedProps.add(node.propName);
                    }
                }
            }
            for (PropertyNode node : mUnorderedNodeList) {
                if (!expectedProps.contains(node.propName)) {
                    expectedProps.add(node.propName);
                }
            }
            mTestCase.fail("Expected property " + Arrays.toString(expectedProps.toArray())
                    + " was not found.");
        }
    }
    private void verifyNode(final String propName, final PropertyNode actualNode) {
        List<PropertyNode> expectedNodeList = mOrderedNodeMap.get(propName);
        final int size = (expectedNodeList != null ? expectedNodeList.size() : 0);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                PropertyNode expectedNode = expectedNodeList.get(i);
                List<PropertyNode> expectedButDifferentValueList = new ArrayList<PropertyNode>();
                if (expectedNode.propName.equals(propName)) {
                    if (expectedNode.equals(actualNode)) {
                        expectedNodeList.remove(i);
                        if (expectedNodeList.size() == 0) {
                            mOrderedNodeMap.remove(propName);
                        }
                        return;
                    } else {
                        expectedButDifferentValueList.add(expectedNode);
                    }
                }
                if (tryFoundExpectedNodeFromUnorderedList(actualNode,
                        expectedButDifferentValueList)) {
                    return;
                }
                if (!expectedButDifferentValueList.isEmpty()) {
                    failWithExpectedNodeList(propName, actualNode,
                            expectedButDifferentValueList);
                } else {
                    mTestCase.fail("Unexpected property \"" + propName + "\" exists.");
                }
            }
        } else {
            List<PropertyNode> expectedButDifferentValueList =
                new ArrayList<PropertyNode>();
            if (tryFoundExpectedNodeFromUnorderedList(actualNode, expectedButDifferentValueList)) {
                return;
            } else {
                if (!expectedButDifferentValueList.isEmpty()) {
                    failWithExpectedNodeList(propName, actualNode,
                            expectedButDifferentValueList);
                } else {
                    mTestCase.fail("Unexpected property \"" + propName + "\" exists.");
                }
            }
        }
    }
    private String concatinateListWithSemiColon(List<String> array) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (String propValueElem : array) {
            if (first) {
                first = false;
            } else {
                buffer.append(';');
            }
            buffer.append(propValueElem);
        }
        return buffer.toString();
    }
    private boolean tryFoundExpectedNodeFromUnorderedList(PropertyNode actualNode,
            List<PropertyNode> expectedButDifferentValueList) {
        final String propName = actualNode.propName;
        int unorderedListSize = mUnorderedNodeList.size();
        for (int i = 0; i < unorderedListSize; i++) {
            PropertyNode unorderedExpectedNode = mUnorderedNodeList.get(i);
            if (unorderedExpectedNode.propName.equals(propName)) {
                if (unorderedExpectedNode.equals(actualNode)) {
                    mUnorderedNodeList.remove(i);
                    return true;
                }
                expectedButDifferentValueList.add(unorderedExpectedNode);
            }
        }
        return false;
    }
    private void failWithExpectedNodeList(String propName, PropertyNode actualNode,
            List<PropertyNode> expectedNodeList) {
        StringBuilder builder = new StringBuilder();
        for (PropertyNode expectedNode : expectedNodeList) {
            builder.append("expected: ");
            builder.append(expectedNode.toString());
            builder.append("\n");
        }
        mTestCase.fail("Property \"" + propName + "\" has wrong value.\n"
                + builder.toString()
                + "  actual: " + actualNode.toString());
    }
}
