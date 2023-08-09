public class WebView1 extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.webview_1);
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        WebView wv;
        wv = (WebView) findViewById(R.id.wv1);
        wv.loadData("<a href='x'>Hello World! - 1</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv2);
        wv.loadData("<a href='x'>Hello World! - 2</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv3);
        wv.loadData("<a href='x'>Hello World! - 3</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv4);
        wv.loadData("<a href='x'>Hello World! - 4</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv5);
        wv.loadData("<a href='x'>Hello World! - 5</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv6);
        wv.loadData("<a href='x'>Hello World! - 6</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv7);
        wv.loadData("<a href='x'>Hello World! - 7</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv8);
        wv.loadData("<a href='x'>Hello World! - 8</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv9);
        wv.loadData("<a href='x'>Hello World! - 9</a>", mimeType, encoding);
        wv = (WebView) findViewById(R.id.wv10);
        wv.loadData("<a href='x'>Hello World! - 10</a>", mimeType, encoding);
    }
}
