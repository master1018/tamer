public class SetWallpaperActivity extends Activity {
    final static private int[] mColors =
            {Color.BLUE, Color.GREEN, Color.RED, Color.LTGRAY, Color.MAGENTA, Color.CYAN,
                    Color.YELLOW, Color.WHITE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_2);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        final ImageView imageView = (ImageView) findViewById(R.id.imageview);
        imageView.setDrawingCacheEnabled(true);
        imageView.setImageDrawable(wallpaperDrawable);
        Button randomize = (Button) findViewById(R.id.randomize);
        randomize.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int mColor = (int) Math.floor(Math.random() * mColors.length);
                wallpaperDrawable.setColorFilter(mColors[mColor], PorterDuff.Mode.MULTIPLY);
                imageView.setImageDrawable(wallpaperDrawable);
                imageView.invalidate();
            }
        });
        Button setWallpaper = (Button) findViewById(R.id.setwallpaper);
        setWallpaper.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    wallpaperManager.setBitmap(imageView.getDrawingCache());
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
