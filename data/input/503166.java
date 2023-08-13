public class ListGetCheckItemIdsTest extends ActivityInstrumentationTestCase2<ListSimple> {
    private ListView mListView;
    public ListGetCheckItemIdsTest() {
        super(ListSimple.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    private void assertChecked(String message, long... expectedIds) {
        long[] checkItemIds = mListView.getCheckItemIds();
        long[] sortedCheckItemsIds = new long[checkItemIds.length];
        System.arraycopy(checkItemIds, 0, sortedCheckItemsIds, 0, checkItemIds.length);
        Arrays.sort(sortedCheckItemsIds);
        long[] sortedExpectedIds = new long[expectedIds.length];
        System.arraycopy(expectedIds, 0, sortedExpectedIds, 0, expectedIds.length);
        Arrays.sort(sortedExpectedIds);
        assertTrue(message, Arrays.equals(sortedExpectedIds, sortedCheckItemsIds));
    }
    @MediumTest
    @UiThreadTest
    public void testNoneCheck() {
        mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mListView.setItemChecked(0, true);
        assertChecked("None check choice has item checked");
    }
    @MediumTest
    @UiThreadTest
    public void testSimpleCheck() {
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        assertChecked("Item checked when setting Single mode");
        int childCount = mListView.getChildCount();
        for (int i=0; i<childCount; i++) {
            mListView.setItemChecked(i, true);
            assertChecked("Only element " + i + " should be checked", i);
        }
        for (int i = 0; i < childCount; i++) {
            mListView.setItemChecked(i, true);
            mListView.setItemChecked((i - 3 + childCount) % childCount, false);
            mListView.setItemChecked((i + 1) % childCount, false);
            assertChecked("Only element " + i + " should be checked", i);
        }
    }
    @MediumTest
    @UiThreadTest
    public void testMultipleCheck() {
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        assertChecked("Item checked when setting Multiple mode");
        int childCount = mListView.getChildCount();
        assertTrue("Tests requires at least 4 items", childCount >= 4);
        mListView.setItemChecked(1, true);
        assertChecked("First element non checked", 1);
        mListView.setItemChecked(3, true);
        assertChecked("Second element not checked", 1, 3);
        mListView.setItemChecked(0, true);
        assertChecked("Third element not checked", 0, 1, 3);
        mListView.setItemChecked(2, false);
        assertChecked("Unchecked element appears checked", 0, 1, 3);
        mListView.setItemChecked(1, false);
        assertChecked("Unchecked element remains", 0, 3);
        mListView.setItemChecked(2, false);
        assertChecked("Already unchecked element appears", 0, 3);
        mListView.setItemChecked(3, false);
        assertChecked("Unchecked 3 remains", 0);
        mListView.setItemChecked(3, false);
        assertChecked("Twice unchecked 3 remains", 0);
        mListView.setItemChecked(0, false);
        assertChecked("Checked items after last element unchecked");
    }
    @MediumTest
    @UiThreadTest
    public void testClearChoices() {
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setItemChecked(0, true);
        mListView.clearChoices();
        assertChecked("Item checked after SINGLE clear choice");
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        int childCount = mListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            mListView.setItemChecked(0, i % 3 == 0);
        }
        mListView.clearChoices();
        assertChecked("Item checked after MULTIPLE clear choice");
    }
}
