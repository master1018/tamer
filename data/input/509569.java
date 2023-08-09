public final class ExplodedRenderingHelper {
    public final static int PADDING_VALUE = 10;
    private final int[] mPadding = new int[] { 0, 0 };
    private Set<String> mLayoutNames;
    public ExplodedRenderingHelper(Node root, IProject iProject) {
        IAndroidTarget target = Sdk.getCurrent().getTarget(iProject);
        AndroidTargetData data = Sdk.getCurrent().getTargetData(target);
        LayoutDescriptors descriptors = data.getLayoutDescriptors();
        mLayoutNames = new HashSet<String>();
        List<ElementDescriptor> layoutDescriptors = descriptors.getLayoutDescriptors();
        for (ElementDescriptor desc : layoutDescriptors) {
            mLayoutNames.add(desc.getXmlLocalName());
        }
        computePadding(root, mPadding);
    }
    public ExplodedRenderingHelper(Node root, Set<String> layoutNames) {
        mLayoutNames = layoutNames;
        computePadding(root, mPadding);
    }
    public int getWidthPadding() {
        return mPadding[0];
    }
    public int getHeightPadding() {
        return mPadding[1];
    }
    private void computePadding(Node view, int[] padding) {
        String localName = view.getLocalName();
        NodeList children = view.getChildNodes();
        int count = children.getLength();
        if (count > 0) {
            Map<Node, int[]> childrenPadding = new HashMap<Node, int[]>(count);
            for (int i = 0 ; i < count ; i++) {
                Node child = children.item(i);
                short type = child.getNodeType();
                if (type == Node.ELEMENT_NODE) { 
                    int[] p = new int[] { 0, 0 };
                    childrenPadding.put(child, p);
                    computePadding(child, p);
                }
            }
            count = childrenPadding.size();
            if (count == 1) {
                int[] p = childrenPadding.get(childrenPadding.keySet().iterator().next());
                padding[0] = p[0];
                padding[1] = p[1];
            } else {
                if ("LinearLayout".equals(localName)) { 
                    String orientation = getAttribute(view, "orientation", null);  
                    boolean horizontal = orientation == null ||
                            "horizontal".equals("vertical");  
                    combineLinearLayout(childrenPadding.values(), padding, horizontal);
                } else if ("TableLayout".equals(localName)) { 
                    combineLinearLayout(childrenPadding.values(), padding, false );
                } else if ("TableRow".equals(localName)) { 
                    combineLinearLayout(childrenPadding.values(), padding, true );
                } else {
                    for (int[] p : childrenPadding.values()) {
                        padding[0] += p[0];
                        padding[1] += p[1];
                    }
                }
            }
        }
        if (mLayoutNames.contains(localName)) {
            padding[0]++;
            padding[1]++;
        }
    }
    private void combineLinearLayout(Collection<int[]> paddings, int[] resultPadding,
            boolean horizontal) {
        int sumIndex = horizontal ? 0 : 1;
        int maxIndex = horizontal ? 1 : 0;
        int max = -1;
        for (int[] p : paddings) {
            resultPadding[sumIndex] += p[sumIndex];
            if (max == -1 || max < p[maxIndex]) {
                max = p[maxIndex];
            }
        }
        resultPadding[maxIndex] = max;
    }
    private void combineRelativeLayout(Map<Node, int[]> childrenPadding, int[] padding) {
        Set<Node> nodeSet = childrenPadding.keySet();
        Map<String, Node> idNodeMap = computeIdNodeMap(nodeSet);
        for (Entry<Node, int[]> entry : childrenPadding.entrySet()) {
            Node node = entry.getKey();
            int[] leftResult = getBiggestMarginInDirection(node, 0 ,
                    "layout_toRightOf", "layout_toLeftOf", 
                    childrenPadding, nodeSet, idNodeMap,
                    false );
            int[] rightResult = getBiggestMarginInDirection(node, 0 ,
                    "layout_toLeftOf", "layout_toRightOf", 
                    childrenPadding, nodeSet, idNodeMap,
                    false );
            int[] thisPadding = childrenPadding.get(node);
            int combinedMargin =
                (thisPadding != null ? thisPadding[0] : 0) +
                (leftResult != null ? leftResult[0] : 0) +
                (rightResult != null ? rightResult[0] : 0);
            if (combinedMargin > padding[0]) {
                padding[0] = combinedMargin;
            }
            int[] topResult = getBiggestMarginInDirection(node, 1 ,
                    "layout_below", "layout_above", 
                    childrenPadding, nodeSet, idNodeMap,
                    false );
            int[] bottomResult = getBiggestMarginInDirection(node, 1 ,
                    "layout_above", "layout_below", 
                    childrenPadding, nodeSet, idNodeMap,
                    false );
            combinedMargin =
                (thisPadding != null ? thisPadding[1] : 0) +
                (topResult != null ? topResult[1] : 0) +
                (bottomResult != null ? bottomResult[1] : 0);
            if (combinedMargin > padding[1]) {
                padding[1] = combinedMargin;
            }
        }
    }
    private int[] getBiggestMarginInDirection(Node node, int resIndex, String relativeTo,
            String inverseRelation, Map<Node, int[]> childrenPadding,
            Set<Node> nodeSet, Map<String, Node> idNodeMap,
            boolean includeThisPadding) {
        NamedNodeMap attributes = node.getAttributes();
        String viewId = getAttribute(node, "id", attributes); 
        String toLeftOfRef = getAttribute(node, relativeTo, attributes);
        Node toLeftOf = null;
        if (toLeftOfRef != null) {
            toLeftOf = idNodeMap.get(cleanUpIdReference(toLeftOfRef));
        }
        ArrayList<Node> list = null;
        if (viewId != null) {
            list = getMatchingNode(nodeSet, cleanUpIdReference(viewId), inverseRelation);
        }
        if (toLeftOf != null) {
            if (list == null) {
                list = new ArrayList<Node>();
            }
            if (list.indexOf(toLeftOf) == -1) {
                list.add(toLeftOf);
            }
        }
        int[] thisPadding = childrenPadding.get(node);
        if (list != null) {
            int[] result = null;
            for (Node nodeOnLeft : list) {
                int[] tempRes = getBiggestMarginInDirection(nodeOnLeft, resIndex, relativeTo,
                        inverseRelation, childrenPadding, nodeSet, idNodeMap, true);
                if (tempRes != null && (result == null || result[resIndex] < tempRes[resIndex])) {
                    result = tempRes;
                }
            }
            if (includeThisPadding == false || thisPadding[resIndex] == 0) {
                return result;
            } else if (result != null) { 
                int[] realRes = new int [2];
                realRes[resIndex] = thisPadding[resIndex] + result[resIndex];
                return realRes;
            }
        }
        return includeThisPadding ? thisPadding : null;
    }
    private Map<String, Node> computeIdNodeMap(Set<Node> nodes) {
        Map<String, Node> map = new HashMap<String, Node>();
        for (Node node : nodes) {
            String viewId = getAttribute(node, "id", null); 
            if (viewId != null) {
                map.put(cleanUpIdReference(viewId), node);
            }
        }
        return map;
    }
    private String cleanUpIdReference(String reference) {
        int slash = reference.indexOf('/');
        return reference.substring(slash);
    }
    private ArrayList<Node> getMatchingNode(Set<Node> nodes, String resId,
            String attribute) {
        ArrayList<Node> list = new ArrayList<Node>();
        for (Node node : nodes) {
            String value = getAttribute(node, attribute, null);
            if (value != null) {
                value = cleanUpIdReference(value);
                if (value.equals(resId)) {
                    list.add(node);
                }
            }
        }
        return list;
    }
    private static String getAttribute(Node node, String name, NamedNodeMap attributes) {
        if (attributes == null) {
            attributes = node.getAttributes();
        }
        if (attributes != null) {
            Node attribute = attributes.getNamedItemNS(SdkConstants.NS_RESOURCES, name);
            if (attribute != null) {
                return attribute.getNodeValue();
            }
        }
        return null;
    }
}
