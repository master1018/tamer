class GraphicsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void setContentView(View view) {
        if (false) { 
            ViewGroup vg = new PictureLayout(this);
            vg.addView(view);
            view = vg;
        }
        super.setContentView(view);
    }
}
