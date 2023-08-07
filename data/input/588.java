public class RecommendationSummaryActivity extends ListActivity {
    private TextView selected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_summary);
        setListAdapter(new RecommnedArrayAdapter(this));
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }
}
