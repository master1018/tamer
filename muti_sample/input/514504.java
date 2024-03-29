public class RequestRectangleVisible extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.scroll_to_rectangle);
        final Rect rect = new Rect();
        final View childToMakeVisible = findViewById(R.id.childToMakeVisible);
        final TextView topBlob = (TextView) findViewById(R.id.topBlob);
        final TextView bottomBlob = (TextView) findViewById(R.id.bottomBlob);
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int numLinesForScreen = screenHeight / 18;
        for (int i = 0; i < numLinesForScreen; i++) {
            topBlob.append(i + " another line in the blob\n");
            bottomBlob.append(i + " another line in the blob\n");
        }
        findViewById(R.id.scrollToRectFromTop).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rect.set(0, 0, childToMakeVisible.getLeft(), childToMakeVisible.getHeight());
                childToMakeVisible.requestRectangleOnScreen(rect, true);
            }
        });
        findViewById(R.id.scrollToRectFromTop2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rect.set(0, 0, topBlob.getWidth(), topBlob.getHeight());
                topBlob.requestRectangleOnScreen(rect, true);
            }
        });
        findViewById(R.id.scrollToRectFromBottom).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rect.set(0, 0, childToMakeVisible.getLeft(), childToMakeVisible.getHeight());
                childToMakeVisible.requestRectangleOnScreen(rect, true);
            }
        });
        findViewById(R.id.scrollToRectFromBottom2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rect.set(0, 0, bottomBlob.getWidth(), bottomBlob.getHeight());
                bottomBlob.requestRectangleOnScreen(rect, true);
            }
        });
    }
}
