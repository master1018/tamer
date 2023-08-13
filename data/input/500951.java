public class ActiveWallpaper extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slideshow slideshow = new Slideshow(this);
        slideshow.setDataSource(new RandomDataSource());
        setContentView(slideshow);
    }
}
