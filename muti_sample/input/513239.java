public class LayoutAnimation2 extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mStrings));
        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);
        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        ListView listView = getListView();        
        listView.setLayoutAnimation(controller);
    }
    private String[] mStrings = {
        "Bordeaux",
        "Lyon",
        "Marseille",
        "Nancy",
        "Paris",
        "Toulouse",
        "Strasbourg"
    };
}
