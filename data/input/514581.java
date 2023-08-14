public class BackgroundSurface extends TextView {
    public BackgroundSurface(Context context) {
        super(context);
        this.setBackgroundColor(Color.BLACK);
        this.setTextColor(Color.WHITE);
        this.setText("This is a java background plugin");
        this.setWillNotDraw(false);
    }
}
