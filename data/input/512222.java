public class StateSetTest extends TestCase {
    @SmallTest
    public void testStateSetPositiveMatches() throws Exception {
         int[] stateSpec = new int[2];
         int[] stateSet = new int[3];
         stateSpec[0] = 1;
         stateSet[0] = 1;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[0] = 2;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSpec[1] = 2;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[1] = 1;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[2] = 12345;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
     }
     @SmallTest
     public void testStatesSetMatchMixEmUp() throws Exception {
         int[] stateSpec = new int[2];
         int[] stateSet = new int[2];
         stateSpec[0] = 1;
         stateSpec[1] = -2;
         stateSet[0] = 1;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[0] = 2;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[1] = 1;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[0] = 12345;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
     }
     @SmallTest
     public void testStateSetNegativeMatches() throws Exception {
         int[] stateSpec = new int[2];
         int[] stateSet = new int[3];
         stateSpec[0] = -1;
         stateSet[0] = 2;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[1] = 12345;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[0] = 1;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSpec[1] = -2;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSet[2] = 12345;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
     }
     @SmallTest
     public void testEmptySetMatchesNegtives() throws Exception {
         int[] stateSpec = {-12345, -6789};
         int[] stateSet = new int[0];
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         int[] stateSet2 = {0};
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet2));
     }
     @SmallTest
     public void testEmptySetFailsPositives() throws Exception {
         int[] stateSpec = {12345};
         int[] stateSet = new int[0];
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         int[] stateSet2 = {0};
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet2));
     }
     @SmallTest
     public void testEmptySetMatchesWildcard() throws Exception {
         int[] stateSpec = StateSet.WILD_CARD;
         int[] stateSet = new int[0];
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
         int[] stateSet2 = {0};
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet2));
     }
     @SmallTest
     public void testSingleStatePositiveMatches() throws Exception {
         int[] stateSpec = new int[2];
         int state;
         stateSpec[0] = 1;
         state = 1;
         assertTrue(StateSet.stateSetMatches(stateSpec, state));
         state = 2;
         assertFalse(StateSet.stateSetMatches(stateSpec, state));
         stateSpec[1] = -12345;
         assertFalse(StateSet.stateSetMatches(stateSpec, state));
     }
     @SmallTest
     public void testSingleStateNegativeMatches() throws Exception {
         int[] stateSpec = new int[2];
         int state;
         stateSpec[0] = -1;
         state = 1;
         assertFalse(StateSet.stateSetMatches(stateSpec, state));
         state = 2;
         assertTrue(StateSet.stateSetMatches(stateSpec, state));
         stateSpec[1] = -12345;
         assertTrue(StateSet.stateSetMatches(stateSpec, state));
     }
     @SmallTest
     public void testZeroStateOnlyMatchesDefault() throws Exception {
         int[] stateSpec = new int[3];
         int state = 0;
         stateSpec[0] = 1;
         assertFalse(StateSet.stateSetMatches(stateSpec, state));
         stateSpec[1] = -1;
         assertFalse(StateSet.stateSetMatches(stateSpec, state));
         stateSpec = StateSet.WILD_CARD;
         assertTrue(StateSet.stateSetMatches(stateSpec, state));
     }
     @SmallTest
     public void testNullStateOnlyMatchesDefault() throws Exception {
         int[] stateSpec = new int[3];
         int[] stateSet = null;
         stateSpec[0] = 1;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSpec[1] = -1;
         assertFalse(StateSet.stateSetMatches(stateSpec, stateSet));
         stateSpec = StateSet.WILD_CARD;
         assertTrue(StateSet.stateSetMatches(stateSpec, stateSet));
     }
}
