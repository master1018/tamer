public class TagsTests extends AndroidTestCase {
    public void disable_testNoDuplicates() {
        String[][] allTags = Tags.pages;
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        for (String[] page: allTags) {
            for (String tag: page) {
                assertTrue(tag, !map.containsKey(tag));
                map.put(tag, true);
            }
        }
    }
}
