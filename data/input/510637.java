public class FixedGridLayoutTest extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        FixedGridLayout grid = (FixedGridLayout)findViewById(R.id.grid);
        grid.setCellWidth(80);
        grid.setCellHeight(80);
    }
}
