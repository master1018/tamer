public class MockTextAppearanceFactory extends TextAppearanceFactory {
    public static final int ID_QUERY_TEXT = 0;
    public static final int ID_SUGGESTED_TEXT = 1;
    public MockTextAppearanceFactory() {
        super(null);
    }
    @Override
    public Object[] createSuggestionQueryTextAppearance() {
        return new Object[]{
                new MockStyleSpan(ID_QUERY_TEXT)
        };
    }
    @Override
    public Object[] createSuggestionSuggestedTextAppearance() {
        return new Object[]{
                new MockStyleSpan(ID_SUGGESTED_TEXT),
        };
    }
    public static class MockStyleSpan {
        private final int mId;
        public MockStyleSpan(int id) {
            mId = id;
        }
        public int getId() {
            return mId;
        }
    }
}
