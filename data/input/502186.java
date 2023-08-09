class GestureComparator {
    void assertGesturesEquals(Gesture expectedGesture, Gesture observedGesture) {
        Assert.assertEquals(expectedGesture.getID(), observedGesture.getID());
        Assert.assertEquals(expectedGesture.getStrokesCount(), observedGesture.getStrokesCount());
        for (int i=0; i < expectedGesture.getStrokesCount(); i++) {
            GestureStroke expectedStroke = expectedGesture.getStrokes().get(i);
            GestureStroke observedStroke = observedGesture.getStrokes().get(i);
            assertGestureStrokesEqual(expectedStroke, observedStroke);
        }
    }
    void assertGestureStrokesEqual(GestureStroke expectedStroke, GestureStroke observedStroke) {
        Assert.assertEquals(expectedStroke.length, observedStroke.length);
        Assert.assertTrue(Arrays.equals(expectedStroke.points, observedStroke.points));
    }
}
