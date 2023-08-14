public class ExplodeRenderingHelperTest extends TestCase {
    private final HashSet<String> mLayoutNames = new HashSet<String>();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLayoutNames.add("LinearLayout");
        mLayoutNames.add("RelativeLayout");
    }
    public void testSingleHorizontalLinearLayout() {
        MockXmlNode layout = createLinearLayout(true ,
                new MockXmlNode[] { createButton(), createButton()} );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(1, helper.getHeightPadding());
        assertEquals(1, helper.getWidthPadding());
    }
    public void testSingleVerticalLinearLayout() {
        MockXmlNode layout = createLinearLayout(false ,
                new MockXmlNode[] { createButton(), createButton()} );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(1, helper.getWidthPadding());
        assertEquals(1, helper.getHeightPadding());
    }
    public void testEmbeddedLinearLayouts() {
        MockXmlNode layout = createLinearLayout(false ,
                new MockXmlNode[] {
                    createLinearLayout(true ,
                            new MockXmlNode[] { createButton(), createButton()}),
                    createLinearLayout(true ,
                            new MockXmlNode[] { createButton(), createButton(), createButton()}),
                } );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(2, helper.getWidthPadding());
        assertEquals(3, helper.getHeightPadding());
    }
    public void testSimpleRelativeLayoutWithOneLinearLayouts() {
        MockXmlNode layout = createRelativeLayout(
                new MockXmlNode[] {
                    createLinearLayout(true ,
                            new MockXmlNode[] { createButton(), createButton()}),
                } );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(2, helper.getWidthPadding());
        assertEquals(2, helper.getHeightPadding());
    }
    public void RelativeLayoutWithVerticalLinearLayouts() {
        MockXmlNode linear1 = createLinearLayout(true ,
                new MockXmlNode[] { createButton(), createButton()});
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear1");
        MockXmlNode linear2 = createLinearLayout(true ,
                        new MockXmlNode[] { createButton(), createButton()});
        linear2.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear2");
        linear2.addAttributes(SdkConstants.NS_RESOURCES, "layout_below", "@+id/linear1");
        MockXmlNode layout = createRelativeLayout(new MockXmlNode[] { linear1, linear2 } );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(2, helper.getWidthPadding());
        assertEquals(3, helper.getHeightPadding());
    }
    public void RelativeLayoutWithVerticalLinearLayouts2() {
        MockXmlNode linear1 = createLinearLayout(true ,
                new MockXmlNode[] { createButton(), createButton() } );
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear1");
        MockXmlNode linear2 = createLinearLayout(true ,
                        new MockXmlNode[] { createButton(), createButton() } );
        linear2.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear2");
        linear2.addAttributes(SdkConstants.NS_RESOURCES, "layout_above", "@+id/linear1");
        MockXmlNode layout = createRelativeLayout(new MockXmlNode[] { linear1, linear2 } );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(2, helper.getWidthPadding());
        assertEquals(3, helper.getHeightPadding());
    }
    public void ComplexRelativeLayout() {
        MockXmlNode button1 = createButton();
        button1.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/button1");
        MockXmlNode button2 = createButton();
        button2.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/button2");
        MockXmlNode linear1 = createLinearLayout(true ,
                new MockXmlNode[] { createButton() } );
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear1");
        MockXmlNode linear2 = createLinearLayout(true ,
                new MockXmlNode[] { createButton() } );
        linear2.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear2");
        MockXmlNode linear3 = createLinearLayout(true ,
                new MockXmlNode[] { createButton() } );
        linear3.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear3");
        MockXmlNode linear4 = createLinearLayout(true ,
                new MockXmlNode[] { createButton() } );
        linear4.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear4");
        MockXmlNode linear5 = createLinearLayout(true ,
                new MockXmlNode[] { createButton() } );
        linear5.addAttributes(SdkConstants.NS_RESOURCES, "id", "@+id/linear5");
        button1.addAttributes(SdkConstants.NS_RESOURCES, "layout_toLeftOf",  "@+id/linear3");
        button2.addAttributes(SdkConstants.NS_RESOURCES, "layout_toRightOf", "@+id/linear3");
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "layout_toRightOf", "@+id/linear3");
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "layout_toLeftOf",  "@+id/linear2");
        linear1.addAttributes(SdkConstants.NS_RESOURCES, "layout_above",     "@+id/button2");
        linear3.addAttributes(SdkConstants.NS_RESOURCES, "layout_above",     "@+id/linear4");
        linear4.addAttributes(SdkConstants.NS_RESOURCES, "layout_toRightOf", "@+id/button1");
        linear5.addAttributes(SdkConstants.NS_RESOURCES, "layout_toRightOf", "@+id/linear4");
        linear5.addAttributes(SdkConstants.NS_RESOURCES, "layout_below",     "@+id/linear4");
        MockXmlNode layout = createRelativeLayout(
                new MockXmlNode[] {
                        button1, button2, linear1, linear2, linear3, linear4, linear5 } );
        ExplodedRenderingHelper helper = new ExplodedRenderingHelper(layout, mLayoutNames);
        assertEquals(4, helper.getWidthPadding());
        assertEquals(5, helper.getHeightPadding());
    }
    private MockXmlNode createButton() {
        return new MockXmlNode(null, "Button", Node.ELEMENT_NODE, null);
    }
    private MockXmlNode createLinearLayout(boolean horizontal, MockXmlNode[] children) {
        MockXmlNode layout = new MockXmlNode(null, "LinearLayout", Node.ELEMENT_NODE, children);
        layout.addAttributes(SdkConstants.NS_RESOURCES, "orientation",
                horizontal ? "horizontal" : "vertical");
        return layout;
    }
    private MockXmlNode createRelativeLayout(MockXmlNode[] children) {
        MockXmlNode layout = new MockXmlNode(null, "RelativeLayout", Node.ELEMENT_NODE, children);
        return layout;
    }
}
