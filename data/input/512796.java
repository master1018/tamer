@TestTargetClass(DefaultHandler2.class)
public class DefaultHandler2Test extends TestCase {
    private DefaultHandler2 h = new DefaultHandler2();
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "DefaultHandler2",
        args = { }
    )
    public void testDefaultHandler2() {
        new DefaultHandler2();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startCDATA",
        args = { }
    )
    public void testStartCDATA() {
        try {
            h.startCDATA();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endCDATA",
        args = { }
    )
    public void testEndCDATA() {
        try {
            h.endCDATA();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startDTD",
        args = { String.class, String.class, String.class }
    )
    public void testStartDTD() {
        try {
            h.startDTD("name", "publicId", "systemId");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endDTD",
        args = { }
    )
    public void testEndDTD() {
        try {
            h.endDTD();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startEntity",
        args = { String.class }
    )
    public void testStartEntity() {
        try {
            h.startEntity("name");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endEntity",
        args = { String.class }
    )
    public void testEndEntity() {
        try {
            h.endEntity("name");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "comment",
        args = { char[].class, int.class, int.class }
    )
    public void testComment() {
        try {
            h.comment("<!-- Comment -->".toCharArray(), 0, 15);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "attributeDecl",
        args = { String.class, String.class, String.class, String.class,
                 String.class }
    )
    public void testAttributeDecl() {
        try {
            h.attributeDecl("eName", "aName", "type", "mode", "value");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "elementDecl",
        args = { String.class, String.class }
    )
    public void testElementDecl() {
        try {
            h.elementDecl("name", "model");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "externalEntityDecl",
        args = { String.class, String.class, String.class }
    )
    public void testExternalEntityDecl() {
        try {
            h.externalEntityDecl("name", "publicId", "systemId");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "internalEntityDecl",
        args = { String.class, String.class }
    )
    public void testInternalEntityDecl() {
        try {
            h.internalEntityDecl("name", "value");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getExternalSubset",
        args = { String.class, String.class }
    )
    public void testGetExternalSubset() {
        try {
            assertNull(h.getExternalSubset("name", "http:
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveEntity",
        args = { String.class, String.class }
    )
    public void testResolveEntityStringString() {
        try {
            assertNull(h.resolveEntity("publicId", "systemId"));
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveEntity",
        args = { String.class, String.class, String.class, String.class }
    )
    public void testResolveEntityStringStringStringString() {
        try {
            assertNull(h.resolveEntity("name", "publicId", "http:
                    "systemId"));
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
