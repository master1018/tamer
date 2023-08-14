public class ObserverNodeTest extends AndroidTestCase {
    static class TestObserver  extends ContentObserver {
        public TestObserver() {
            super(new Handler());
        }
    }
    public void testUri() {
        ObserverNode root = new ObserverNode("");
        Uri[] uris = new Uri[] {
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
        };
        int[] nums = new int[] {4, 7, 1, 4, 2, 2, 3, 3};
        root.addObserverLocked(uris[0], new TestObserver().getContentObserver(), false, root);
        for(int i = 1; i < uris.length; i++) {
            root.addObserverLocked(uris[i], new TestObserver().getContentObserver(), true, root);
        }
        ArrayList<ObserverCall> calls = new ArrayList<ObserverCall>();
        for (int i = nums.length - 1; i >=0; --i) {
            root.collectObserversLocked(uris[i], 0, null, false, calls);
            assertEquals(nums[i], calls.size());
            calls.clear();
        }
    }
    public void testUriNotNotify() {
        ObserverNode root = new ObserverNode("");
        Uri[] uris = new Uri[] {
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
            Uri.parse("content:
        };
        int[] nums = new int[] {7, 1, 3, 3, 1, 1, 1, 1};
        for(int i = 0; i < uris.length; i++) {
            root.addObserverLocked(uris[i], new TestObserver().getContentObserver(), false, root);
        }
        ArrayList<ObserverCall> calls = new ArrayList<ObserverCall>();
        for (int i = uris.length - 1; i >=0; --i) {
            root.collectObserversLocked(uris[i], 0, null, false, calls);
            assertEquals(nums[i], calls.size());
            calls.clear();
        }
    }
}
