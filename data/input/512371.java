public class QueryTask<C extends SuggestionCursor> implements NamedTask {
    private final String mQuery;
    private final int mQueryLimit;
    private final SuggestionCursorProvider<C> mProvider;
    private final Handler mHandler;
    private final Consumer<C> mConsumer;
    private final boolean mTheOnlyOne;
    public QueryTask(String query, int queryLimit, SuggestionCursorProvider<C> provider,
            Handler handler, Consumer<C> consumer, boolean onlyTask) {
        mQuery = query;
        mQueryLimit = queryLimit;
        mProvider = provider;
        mHandler = handler;
        mConsumer = consumer;
        mTheOnlyOne = onlyTask;
    }
    public String getName() {
        return mProvider.getName();
    }
    public void run() {
        final C cursor = mProvider.getSuggestions(mQuery, mQueryLimit, mTheOnlyOne);
        if (mHandler == null) {
            mConsumer.consume(cursor);
        } else {
            mHandler.post(new Runnable() {
                public void run() {
                    boolean accepted = mConsumer.consume(cursor);
                    if (!accepted) {
                        cursor.close();
                    }
                }
            });
        }
    }
    @Override
    public String toString() {
        return mProvider + "[" + mQuery + "]";
    }
    public static <C extends SuggestionCursor> void startQueries(String query,
            int maxResultsPerProvider,
            Iterable<? extends SuggestionCursorProvider<C>> providers,
            NamedTaskExecutor executor, Handler handler,
            Consumer<C> consumer, boolean onlyOneProvider) {
        for (SuggestionCursorProvider<C> provider : providers) {
            QueryTask<C> task = new QueryTask<C>(query, maxResultsPerProvider, provider, handler,
                    consumer, onlyOneProvider);
            executor.execute(task);
        }
    }
}
