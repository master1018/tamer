public class UiElementNodeTest extends TestCase {
    private ElementDescriptor e;
    private UiElementNode ui;
    @Override
    protected void setUp() throws Exception {
        e = new ElementDescriptor("manifest", new ElementDescriptor[] {
                new ElementDescriptor("application", new ElementDescriptor[] {
                    new ElementDescriptor("provider"), 
                    new ElementDescriptor("activity", new ElementDescriptor[] {
                        new ElementDescriptor("intent-filter")
                    }), 
                }), 
                new ElementDescriptor("permission")
            });
        ui = new UiElementNode(e);
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testInit() {
        assertSame(e, ui.getDescriptor());
        assertNull(ui.getUiParent());
        assertEquals(0, ui.getUiChildren().size());
        assertEquals(0, ui.getUiAttributes().size());
    }
    public void testLoadFrom_InvalidRoot() {
        assertEquals(0, ui.getUiChildren().size());
        MockXmlNode root = new MockXmlNode(null , "blah", Node.ELEMENT_NODE, null);
        ui.loadFromXmlNode(root);
        assertEquals(0, ui.getUiChildren().size());
    }
    public void testLoadFrom_NewTree_1_Node() {
        MockXmlNode root = new MockXmlNode(null , "manifest", Node.ELEMENT_NODE,
            new MockXmlNode[] {
                new MockXmlNode(null , "application", Node.ELEMENT_NODE, null)
            });
        ui.loadFromXmlNode(root);
        assertEquals("manifest", ui.getDescriptor().getXmlName());
        assertEquals(1, ui.getUiChildren().size());
        assertEquals(0, ui.getUiAttributes().size());
        Iterator<UiElementNode> ui_child_it = ui.getUiChildren().iterator();
        UiElementNode application = ui_child_it.next();
        assertEquals("application", application.getDescriptor().getXmlName());
        assertEquals(0, application.getUiChildren().size());
        assertEquals(0, application.getUiAttributes().size());
    }
    public void testLoadFrom_NewTree_2_Nodes() {
        MockXmlNode root = new MockXmlNode(null , "manifest", Node.ELEMENT_NODE,
            new MockXmlNode[] {
                new MockXmlNode(null , "application", Node.ELEMENT_NODE, null),
                new MockXmlNode(null , "permission", Node.ELEMENT_NODE, null),
            });
        ui.loadFromXmlNode(root);
        assertEquals("manifest", ui.getDescriptor().getXmlName());
        assertEquals(2, ui.getUiChildren().size());
        assertEquals(0, ui.getUiAttributes().size());
        Iterator<UiElementNode> ui_child_it = ui.getUiChildren().iterator();
        UiElementNode application = ui_child_it.next();
        assertEquals("application", application.getDescriptor().getXmlName());
        assertEquals(0, application.getUiChildren().size());
        assertEquals(0, application.getUiAttributes().size());
        UiElementNode first_permission = ui_child_it.next();
        assertEquals("permission", first_permission.getDescriptor().getXmlName());
        assertEquals(0, first_permission.getUiChildren().size());
        assertEquals(0, first_permission.getUiAttributes().size());
    }
    public void testLoadFrom_NewTree_N_Nodes() {
        MockXmlNode root = new MockXmlNode(null , "manifest", Node.ELEMENT_NODE,
            new MockXmlNode[] {
                new MockXmlNode(null , "application", Node.ELEMENT_NODE,
                    new MockXmlNode[] {
                        new MockXmlNode(null , "activity", Node.ELEMENT_NODE,
                            null),
                        new MockXmlNode(null , "activity", Node.ELEMENT_NODE,
                            new MockXmlNode[] {
                                new MockXmlNode(null , "intent-filter",
                                        Node.ELEMENT_NODE, null),
                            }),
                        new MockXmlNode(null , "provider", Node.ELEMENT_NODE,
                                null),
                        new MockXmlNode(null , "provider", Node.ELEMENT_NODE,
                                null),
                    }),
                new MockXmlNode(null , "permission", Node.ELEMENT_NODE,
                        null),
                new MockXmlNode(null , "permission", Node.ELEMENT_NODE,
                        null),
            });
        ui.loadFromXmlNode(root);
        assertEquals("manifest", ui.getDescriptor().getXmlName());
        assertEquals(3, ui.getUiChildren().size());
        assertEquals(0, ui.getUiAttributes().size());
        Iterator<UiElementNode> ui_child_it = ui.getUiChildren().iterator();
        UiElementNode application = ui_child_it.next();
        assertEquals("application", application.getDescriptor().getXmlName());
        assertEquals(4, application.getUiChildren().size());
        assertEquals(0, application.getUiAttributes().size());
        Iterator<UiElementNode> app_child_it = application.getUiChildren().iterator();
        UiElementNode first_activity = app_child_it.next();
        assertEquals("activity", first_activity.getDescriptor().getXmlName());
        assertEquals(0, first_activity.getUiChildren().size());
        assertEquals(0, first_activity.getUiAttributes().size());
        UiElementNode second_activity = app_child_it.next();
        assertEquals("activity", second_activity.getDescriptor().getXmlName());
        assertEquals(1, second_activity.getUiChildren().size());
        assertEquals(0, second_activity.getUiAttributes().size());
        Iterator<UiElementNode> activity_child_it = second_activity.getUiChildren().iterator();
        UiElementNode intent_filter = activity_child_it.next();
        assertEquals("intent-filter", intent_filter.getDescriptor().getXmlName());
        assertEquals(0, intent_filter.getUiChildren().size());
        assertEquals(0, intent_filter.getUiAttributes().size());
        UiElementNode first_provider = app_child_it.next();
        assertEquals("provider", first_provider.getDescriptor().getXmlName());
        assertEquals(0, first_provider.getUiChildren().size());
        assertEquals(0, first_provider.getUiAttributes().size());
        UiElementNode second_provider = app_child_it.next();
        assertEquals("provider", second_provider.getDescriptor().getXmlName());
        assertEquals(0, second_provider.getUiChildren().size());
        assertEquals(0, second_provider.getUiAttributes().size());
        UiElementNode first_permission = ui_child_it.next();
        assertEquals("permission", first_permission.getDescriptor().getXmlName());
        assertEquals(0, first_permission.getUiChildren().size());
        assertEquals(0, first_permission.getUiAttributes().size());
        UiElementNode second_permission = ui_child_it.next();
        assertEquals("permission", second_permission.getDescriptor().getXmlName());
        assertEquals(0, second_permission.getUiChildren().size());
        assertEquals(0, second_permission.getUiAttributes().size());
    }
}
