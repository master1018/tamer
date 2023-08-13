public class FocusFinderTest extends AndroidTestCase {
    private FocusFinderHelper mFocusFinder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFocusFinder = new FocusFinderHelper(FocusFinder.getInstance());
    }
    @SmallTest
    public void testPreconditions() {
        assertNotNull("focus finder instance", mFocusFinder);
    }
    @SmallTest
    public void testBelowNotCandidateForDirectionUp() {
        assertIsNotCandidate(View.FOCUS_UP,
                new Rect(0, 30, 10, 40),  
                new Rect(0, 50, 10, 60));  
    }
    @SmallTest
    public void testAboveShareEdgeEdgeOkForDirectionUp() {
        final Rect src = new Rect(0, 30, 10, 40);
        final Rect dest = new Rect(src);
        dest.offset(0, -src.height());
        assertEquals(src.top, dest.bottom);
        assertDirectionIsCandidate(View.FOCUS_UP, src, dest);
    }
    @SmallTest
    public void testCompletelyContainedNotCandidate() {
        assertIsNotCandidate(
                View.FOCUS_DOWN,
                new Rect(0, 0,  50, 50),
                new Rect(0, 1,  50, 49));
    }
    @SmallTest
    public void testContinaedWithCommonBottomNotCandidate() {
        assertIsNotCandidate(
                View.FOCUS_DOWN,
                new Rect(0, 0,  50, 50),
                new Rect(0, 1,  50, 50));
    }
    @SmallTest
    public void testOverlappingIsCandidateWhenBothEdgesAreInDirection() {
        assertDirectionIsCandidate(
                View.FOCUS_DOWN,
                new Rect(0, 0,  50, 50),
                new Rect(0, 1,  50, 51));
    }
    @SmallTest
    public void testTopEdgeOfDestAtOrAboveTopOfSrcNotCandidateForDown() {
        assertIsNotCandidate(
                View.FOCUS_DOWN,
                new Rect(0, 0,  50, 50),
                new Rect(0, 0,  50, 51));
        assertIsNotCandidate(
                View.FOCUS_DOWN,
                new Rect(0, 0,  50, 50),
                new Rect(0, -1, 50, 51));
    }
    @SmallTest
    public void testSameRectBeamsOverlap() {
        final Rect rect = new Rect(0, 0, 20, 20);
        assertBeamsOverlap(View.FOCUS_LEFT, rect, rect);
        assertBeamsOverlap(View.FOCUS_RIGHT, rect, rect);
        assertBeamsOverlap(View.FOCUS_UP, rect, rect);
        assertBeamsOverlap(View.FOCUS_DOWN, rect, rect);
    }
    @SmallTest
    public void testOverlapBeamsRightLeftUpToEdge() {
        final Rect rect1 = new Rect(0, 0, 20, 20);
        final Rect rect2 = new Rect(rect1);
        rect2.offset(0, rect1.height() - 1);
        assertBeamsOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_RIGHT, rect1, rect2);
        rect2.offset(0, 1);
        assertBeamsOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_RIGHT, rect1, rect2);
        rect2.offset(0, 1);
        assertBeamsDontOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsDontOverlap(View.FOCUS_RIGHT, rect1, rect2);
        rect2.set(rect1);
        rect2.offset(0, -(rect1.height() - 1));
        assertBeamsOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_RIGHT, rect1, rect2);
        rect2.offset(0, -1);
        assertBeamsOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_RIGHT, rect1, rect2);
        rect2.offset(0, -1);
        assertBeamsDontOverlap(View.FOCUS_LEFT, rect1, rect2);
        assertBeamsDontOverlap(View.FOCUS_RIGHT, rect1, rect2);
    }
    @SmallTest
    public void testOverlapBeamsUpDownUpToEdge() {
        final Rect rect1 = new Rect(0, 0, 20, 20);
        final Rect rect2 = new Rect(rect1);
        rect2.offset(rect1.width() - 1, 0);
        assertBeamsOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_DOWN, rect1, rect2);
        rect2.offset(1, 0);
        assertBeamsOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_DOWN, rect1, rect2);
        rect2.offset(1, 0);
        assertBeamsDontOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsDontOverlap(View.FOCUS_DOWN, rect1, rect2);
        rect2.set(rect1);
        rect2.offset(-(rect1.width() - 1), 0);
        assertBeamsOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_DOWN, rect1, rect2);
        rect2.offset(-1, 0);
        assertBeamsOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsOverlap(View.FOCUS_DOWN, rect1, rect2);
        rect2.offset(-1, 0);
        assertBeamsDontOverlap(View.FOCUS_UP, rect1, rect2);
        assertBeamsDontOverlap(View.FOCUS_DOWN, rect1, rect2);
    }
    @SmallTest
    public void testDirectlyAboveTrumpsAboveLeft() {
        Rect src = new Rect(0, 50, 20, 70);  
        Rect directlyAbove = new Rect(src);
        directlyAbove.offset(0, -(1 + src.height()));
        Rect aboveLeft = new Rect(src);
        aboveLeft.offset(-(1 + src.width()), -(1 + src.height()));
        assertBetterCandidate(View.FOCUS_UP, src, directlyAbove, aboveLeft);
    }
    @SmallTest
    public void testAboveInBeamTrumpsSlightlyCloserOutOfBeam() {
        Rect src = new Rect(0, 50, 20, 70);  
        Rect directlyAbove = new Rect(src);
        directlyAbove.offset(0, -(1 + src.height()));
        Rect aboveLeft = new Rect(src);
        aboveLeft.offset(-(1 + src.width()), -(1 + src.height()));
        directlyAbove.offset(0, -5);
        assertBetterCandidate(View.FOCUS_UP, src, directlyAbove, aboveLeft);
    }
    @SmallTest
    public void testOutOfBeamBeatsInBeamUp() {
        Rect src = new Rect(0, 0, 50, 50); 
        Rect aboveLeftOfBeam = new Rect(src);
        aboveLeftOfBeam.offset(-(src.width() + 1), -src.height());
        assertBeamsDontOverlap(View.FOCUS_UP, src, aboveLeftOfBeam);
        Rect aboveInBeam = new Rect(src);
        aboveInBeam.offset(0, -src.height());
        assertBeamsOverlap(View.FOCUS_UP, src, aboveInBeam);
        assertBetterCandidate(View.FOCUS_UP, src, aboveInBeam, aboveLeftOfBeam);
        aboveInBeam.offset(0, -(aboveLeftOfBeam.height() - 1));
        assertTrue("aboveInBeam.bottom > aboveLeftOfBeam.top", aboveInBeam.bottom > aboveLeftOfBeam.top);
        assertBetterCandidate(View.FOCUS_UP, src, aboveInBeam, aboveLeftOfBeam);
        aboveInBeam.offset(0, -1);
        assertEquals(aboveInBeam.bottom, aboveLeftOfBeam.top);
        assertBetterCandidate(View.FOCUS_UP, src, aboveLeftOfBeam, aboveInBeam);
    }
    @MediumTest
    public void testSomeCandidateBetterThanNonCandidate() {
        Rect src = new Rect(0, 0, 50, 50); 
        Rect nonCandidate = new Rect(src);
        nonCandidate.offset(src.width() + 1, 0);
        assertIsNotCandidate(View.FOCUS_LEFT, src, nonCandidate);
        Rect candidate = new Rect(src);
        candidate.offset(-(4 * src.width()), 0);
        assertDirectionIsCandidate(View.FOCUS_LEFT, src, candidate);
        assertBetterCandidate(View.FOCUS_LEFT, src, candidate, nonCandidate);
    }
    @SmallTest
    public void testVerticalFocusSearchScenario() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   109, 153, 169),   
                new Rect(166, 169, 319, 229),  
                new Rect(0,   229, 320, 289)); 
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   91, 153, 133),   
                new Rect(166, 133, 319, 175),  
                new Rect(0,   175, 320, 217)); 
    }
    @SmallTest
    public void testBeamsOverlapMajorAxisCloserMinorAxisFurther() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,  0,   100,  100),  
                new Rect(0,  100, 480,  200),  
                new Rect(0,  200, 100,  300)); 
    }
    @SmallTest
    public void testMusicPlaybackScenario() {
        assertBetterCandidate(View.FOCUS_LEFT,
                new Rect(227, 185, 312, 231),   
                new Rect(195, 386, 266, 438),   
                new Rect(124, 386, 195, 438));  
    }
    @SmallTest
    public void testOutOfBeamOverlapBeatsOutOfBeamFurtherOnMajorAxis() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   0,   50,  50),   
                new Rect(60,  40,  110, 90),   
                new Rect(60,  70,  110, 120));  
    }
    @SmallTest
    public void testInBeamTrumpsOutOfBeamOverlapping() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   0,   50,  50),   
                new Rect(0,   60,  50,  110),  
                new Rect(51,  1,   101, 51)); 
    }
    @SmallTest
    public void testOverlappingBeatsNonOverlapping() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   0,   50,  50),   
                new Rect(0,   40,  50,  90),   
                new Rect(0,   75,  50,  125)); 
    }
    @SmallTest
    public void testEditContactScenarioLeftFromDiscardChangesGoesToSaveContactInLandscape() {
        assertBetterCandidate(View.FOCUS_LEFT,
                new Rect(357, 258, 478, 318),  
                new Rect(2,   258, 100, 318),  
                new Rect(106, 120, 424, 184)); 
    }
    @SmallTest
    public void testGridWithTouchingEdges() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(106, 49,  212, 192),  
                new Rect(106, 192, 212, 335),  
                new Rect(0,   192, 106, 335)); 
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(106, 49,  212, 192),  
                new Rect(106, 192, 212, 335),  
                new Rect(212, 192, 318, 335)); 
    }
    @SmallTest
    public void testSearchFromEmptyRect() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,  0,   0,   0),    
                new Rect(0,  0,   320, 45),   
                new Rect(0,  45,  320, 545)); 
    }
    @SmallTest
    public void testGmailReplyButtonsScenario() {
        assertBetterCandidate(View.FOCUS_LEFT,
                new Rect(223, 380, 312, 417),  
                new Rect(102, 380, 210, 417),  
                new Rect(111, 443, 206, 480)); 
        assertBeamBeats(View.FOCUS_LEFT,
            new Rect(223, 380, 312, 417),  
            new Rect(102, 380, 210, 417),  
            new Rect(111, 443, 206, 480)); 
        assertBeamsOverlap(View.FOCUS_LEFT,
                new Rect(223, 380, 312, 417),
                new Rect(102, 380, 210, 417));
        assertBeamsDontOverlap(View.FOCUS_LEFT,
                new Rect(223, 380, 312, 417),
                new Rect(111, 443, 206, 480));
        assertTrue(
                "major axis distance less than major axis distance to "
                        + "far edge",
                FocusFinderHelper.majorAxisDistance(View.FOCUS_LEFT,
                        new Rect(223, 380, 312, 417),
                        new Rect(102, 380, 210, 417)) <
                FocusFinderHelper.majorAxisDistanceToFarEdge(View.FOCUS_LEFT,
                        new Rect(223, 380, 312, 417),
                        new Rect(111, 443, 206, 480)));
    }
    @SmallTest
    public void testGmailScenarioBug1203288() {
        assertBetterCandidate(View.FOCUS_DOWN,
                new Rect(0,   2,   480, 82),   
                new Rect(344, 87,  475, 124),  
                new Rect(0,   130, 480, 203)); 
    }
    @SmallTest
    public void testHomeShortcutScenarioBug1295354() {
        assertBetterCandidate(View.FOCUS_RIGHT,
                new Rect(3, 338, 77, 413),   
                new Rect(163, 338, 237, 413),  
                new Rect(83, 38, 157, 113)); 
    }
    @SmallTest
    public void testBeamAlwaysBeatsHoriz() {
        assertBetterCandidate(View.FOCUS_RIGHT,
                new Rect(0,   0,   50,  50),   
                new Rect(150, 0,   200, 50),   
                new Rect(60,  51,  110, 101)); 
        assertBetterCandidate(View.FOCUS_LEFT,
                new Rect(150, 0,   200,  50),   
                new Rect(0,   50,  50,   50),   
                new Rect(49,  99,  149,  101)); 
    }
    @SmallTest
    public void testIsCandidateOverlappingEdgeFromEmptyRect() {
        assertDirectionIsCandidate(View.FOCUS_DOWN,
                new Rect(0,  0,   0,   0),   
                new Rect(0,  0,   20,  1));  
        assertDirectionIsCandidate(View.FOCUS_UP,
                new Rect(0,  0,   0,   0),   
                new Rect(0,  -1,  20,  0));  
        assertDirectionIsCandidate(View.FOCUS_LEFT,
                new Rect(0,  0,   0,   0),    
                new Rect(-1,  0,  0,   20));  
        assertDirectionIsCandidate(View.FOCUS_RIGHT,
                new Rect(0,  0,   0,   0),    
                new Rect(0,  0,   1,   20));  
    }
    private void assertBeamsOverlap(int direction, Rect rect1, Rect rect2) {
        String directionStr = validateAndGetStringFor(direction);
        String assertMsg = String.format("Expected beams to overlap in direction %s "
                + "for rectangles %s and %s", directionStr, rect1, rect2);
        assertTrue(assertMsg, mFocusFinder.beamsOverlap(direction, rect1, rect2));
    }
    private void assertBeamsDontOverlap(int direction, Rect rect1, Rect rect2) {
        String directionStr = validateAndGetStringFor(direction);
        String assertMsg = String.format("Expected beams not to overlap in direction %s "
                + "for rectangles %s and %s", directionStr, rect1, rect2);
        assertFalse(assertMsg, mFocusFinder.beamsOverlap(direction, rect1, rect2));
    }
    private void assertBetterCandidate(int direction, Rect srcRect,
            Rect expectedBetter, Rect expectedWorse) {
        String directionStr = validateAndGetStringFor(direction);
        String assertMsg = String.format(
                "expected %s to be a better focus search candidate than "
                        + "%s when searching "
                        + "from %s in direction %s",
                expectedBetter, expectedWorse, srcRect, directionStr);
        assertTrue(assertMsg,
                mFocusFinder.isBetterCandidate(direction, srcRect,
                        expectedBetter, expectedWorse));
        assertMsg = String.format(
                "expected %s to not be a better focus search candidate than "
                        + "%s when searching "
                        + "from %s in direction %s",
                expectedWorse, expectedBetter, srcRect, directionStr);
        assertFalse(assertMsg,
                mFocusFinder.isBetterCandidate(direction, srcRect,
                        expectedWorse, expectedBetter));
    }
    private void assertIsNotCandidate(int direction, Rect src, Rect dest) {
        String directionStr = validateAndGetStringFor(direction);
        final String assertMsg = String.format(
                "expected going from %s to %s in direction %s to be an invalid "
                        + "focus search candidate",
                src, dest, directionStr);
        assertFalse(assertMsg, mFocusFinder.isCandidate(src, dest, direction));
    }
    private void assertBeamBeats(int direction, Rect srcRect,
            Rect rect1, Rect rect2) {
        String directionStr = validateAndGetStringFor(direction);
        String assertMsg = String.format(
                "expecting %s to beam beat %s w.r.t %s in direction %s",
                rect1, rect2, srcRect, directionStr);
        assertTrue(assertMsg, mFocusFinder.beamBeats(direction, srcRect, rect1, rect2));
    }
    private void assertDirectionIsCandidate(int direction, Rect src, Rect dest) {
        String directionStr = validateAndGetStringFor(direction);
        final String assertMsg = String.format(
                "expected going from %s to %s in direction %s to be a valid "
                        + "focus search candidate",
                src, dest, directionStr);
        assertTrue(assertMsg, mFocusFinder.isCandidate(src, dest, direction));
    }
    private String validateAndGetStringFor(int direction) {
        String directionStr = "??";
        switch(direction) {
            case View.FOCUS_UP:
                directionStr = "FOCUS_UP";
                break;
            case View.FOCUS_DOWN:
                directionStr = "FOCUS_DOWN";
                break;
            case View.FOCUS_LEFT:
                directionStr = "FOCUS_LEFT";
                break;
            case View.FOCUS_RIGHT:
                directionStr = "FOCUS_RIGHT";
                break;
            default:
                fail("passed in unknown direction, ya blewit!");
        }
        return directionStr;
    }
}
