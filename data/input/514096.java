public class LayoutAnalysisCategory {
    private static final String ANDROID_PADDING = "android:padding";
    private static final String ANDROID_PADDING_LEFT = "android:paddingLeft";
    private static final String ANDROID_PADDING_TOP = "android:paddingTop";
    private static final String ANDROID_PADDING_RIGHT = "android:paddingRight";
    private static final String ANDROID_PADDING_BOTTOM = "android:paddingBottom";
    private static final String ANDROID_LAYOUT_WIDTH = "android:layout_width";
    private static final String ANDROID_LAYOUT_HEIGHT = "android:layout_height";
    private static final String VALUE_FILL_PARENT = "fill_parent";
    private static final String VALUE_MATCH_PARENT = "match_parent";
    private static final String VALUE_WRAP_CONTENT = "wrap_content";
    private static final String[] sContainers = new String[] {
            "FrameLayout", "LinearLayout", "RelativeLayout", "SlidingDrawer",
            "AbsoluteLayout", "TableLayout", "Gallery", "GridView", "ListView",
            "RadioGroup", "ScrollView", "HorizontalScrollView", "Spinner",
            "ViewSwitcher", "ViewFlipper", "ViewAnimator", "ImageSwitcher",
            "TextSwitcher", "android.gesture.GestureOverlayView", "TabHost"
    };
    static {
        Arrays.sort(sContainers);
    }
    public static boolean isContainer(Element element) {
        return Arrays.binarySearch(sContainers, element.getNodeName()) >= 0;
    }
    public static List<Node> all(Element element) {
        NodeList list = DOMCategory.depthFirst(element);
        int count = list.getLength();
        List<Node> nodes = new ArrayList<Node>(count - 1);
        for (int i = 1; i < count; i++) {
            nodes.add(list.item(i));
        }
        return nodes;
    }
    public static int getStartLine(Node node) {
        final Object data = node == null ? null :
                node.getUserData(XmlDocumentBuilder.NODE_START_LINE);
        return data == null ? -1 : (Integer) data;
    }
    public static int getEndLine(Node node) {
        final Object data = node == null ? null :
                node.getUserData(XmlDocumentBuilder.NODE_END_LINE);
        return data == null ? -1 : (Integer) data;
    }
    public static boolean hasPadding(Element element) {
        return element.getAttribute(ANDROID_PADDING).length() > 0 ||
                element.getAttribute(ANDROID_PADDING_LEFT).length() > 0 ||
                element.getAttribute(ANDROID_PADDING_TOP).length() > 0 ||
                element.getAttribute(ANDROID_PADDING_BOTTOM).length() > 0 ||
                element.getAttribute(ANDROID_PADDING_RIGHT).length() > 0;
    }
    public static boolean isWidthFillParent(Element element) {
        final String attribute = element.getAttribute(ANDROID_LAYOUT_WIDTH);
        return attribute.equals(VALUE_FILL_PARENT) || attribute.equals(VALUE_MATCH_PARENT);
    }
    public static boolean isWidthWrapContent(Element element) {
        return element.getAttribute(ANDROID_LAYOUT_WIDTH).equals(VALUE_WRAP_CONTENT);
    }
    public static boolean isHeightFillParent(Element element) {
        final String attribute = element.getAttribute(ANDROID_LAYOUT_HEIGHT);
        return attribute.equals(VALUE_FILL_PARENT) || attribute.equals(VALUE_MATCH_PARENT);
    }
    public static boolean isHeightWrapContent(Element element) {
        return element.getAttribute(ANDROID_LAYOUT_HEIGHT).equals(VALUE_WRAP_CONTENT);
    }
    public static boolean isRoot(Node node) {
        return node.getOwnerDocument().getDocumentElement() == node;
    }
    public static boolean is(Node node, String name) {
        return node.getNodeName().equals(name);
    }
    public static int depth(Node node) {
        int maxDepth = 0;
        NodeList list = node.getChildNodes();
        int count = list.getLength();
        for (int i = 0; i < count; i++) {
            maxDepth = Math.max(maxDepth, depth(list.item(i)));
        }
        return maxDepth + 1;
    }
    public static LayoutAnalysis leftShift(LayoutAnalysis analysis, GString description) {
        analysis.addIssue(description.toString());
        return analysis;
    }
    public static LayoutAnalysis leftShift(LayoutAnalysis analysis, String description) {
        analysis.addIssue(description);
        return analysis;
    }
    public static LayoutAnalysis leftShift(LayoutAnalysis analysis, Map issue) {
        analysis.addIssue((Node) issue.get("node"), issue.get("description").toString());
        return analysis;
    }
}
