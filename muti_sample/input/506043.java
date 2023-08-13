@TestTargetClass(android.graphics.drawable.shapes.Shape.class)
public class ShapeTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resize",
            args = {float.class, float.class}
        )
    })
    public void testSize() {
        MockShape mockShape = new MockShape();
        assertFalse(mockShape.hasCalledOnResize());
        mockShape.resize(200f, 300f);
        assertEquals(200f, mockShape.getWidth());
        assertEquals(300f, mockShape.getHeight());
        assertTrue(mockShape.hasCalledOnResize());
        mockShape.resize(0f, 0f);
        assertEquals(0f, mockShape.getWidth());
        assertEquals(0f, mockShape.getHeight());
        mockShape.resize(Float.MAX_VALUE, Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, mockShape.getWidth());
        assertEquals(Float.MAX_VALUE, mockShape.getHeight());
        mockShape.resize(-1, -1);
        assertEquals(0f, mockShape.getWidth());
        assertEquals(0f, mockShape.getHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onResize",
        args = {float.class, float.class}
    )
    public void testOnResize() {
        MockShape mockShape = new MockShape();
        assertFalse(mockShape.hasCalledOnResize());
        mockShape.resize(200f, 300f);
        assertTrue(mockShape.hasCalledOnResize());
        mockShape.reset();
        mockShape.resize(200f, 300f);
        assertFalse(mockShape.hasCalledOnResize());
        mockShape.reset();
        mockShape.resize(100f, 200f);
        assertTrue(mockShape.hasCalledOnResize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        Shape shape = new MockShape();
        shape.resize(100f, 200f);
        Shape clonedShape = shape.clone();
        assertEquals(100f, shape.getWidth());
        assertEquals(200f, shape.getHeight());
        assertNotSame(shape, clonedShape);
        assertEquals(shape.getWidth(), clonedShape.getWidth());
        assertEquals(shape.getHeight(), clonedShape.getHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasAlpha",
        args = {}
    )
    public void testHasAlpha() {
        assertTrue(new MockShape().hasAlpha());
    }
    private static class MockShape extends Shape {
        private boolean mCalledOnResize = false;
        @Override
        public void draw(Canvas canvas, Paint paint) {
        }
        @Override
        protected void onResize(float width, float height) {
            super.onResize(width, height);
            mCalledOnResize = true;
        }
        public boolean hasCalledOnResize() {
            return mCalledOnResize;
        }
        public void reset() {
            mCalledOnResize = false;
        }
    }
}
