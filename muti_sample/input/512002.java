public class DescendantFocusability extends Activity {
    public ViewGroup beforeDescendants;
    public Button beforeDescendantsChild;
    public ViewGroup afterDescendants;
    public Button afterDescendantsChild;
    public ViewGroup blocksDescendants;
    public Button blocksDescendantsChild;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.descendant_focusability);
        beforeDescendants = (ViewGroup) findViewById(R.id.beforeDescendants);
        beforeDescendantsChild = (Button) beforeDescendants.getChildAt(0);
        afterDescendants = (ViewGroup) findViewById(R.id.afterDescendants);
        afterDescendantsChild = (Button) afterDescendants.getChildAt(0);
        blocksDescendants = (ViewGroup) findViewById(R.id.blocksDescendants);
        blocksDescendantsChild = (Button) blocksDescendants.getChildAt(0);
    }
}
