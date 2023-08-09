public class CompressedTextureActivity extends Activity {
    private final static String TAG = "CompressedTextureActivity";
    private final static boolean TEST_CREATE_TEXTURE = false;
    private final static boolean USE_STREAM_IO = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mGLView.setEGLConfigChooser(false);
        StaticTriangleRenderer.TextureLoader loader;
        if (TEST_CREATE_TEXTURE) {
            loader = new SyntheticCompressedTextureLoader();
        } else {
            loader = new CompressedTextureLoader();
        }
        mGLView.setRenderer(new StaticTriangleRenderer(this, loader));
        setContentView(mGLView);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
    private class CompressedTextureLoader implements StaticTriangleRenderer.TextureLoader {
        public void load(GL10 gl) {
            Log.w(TAG, "ETC1 texture support: " + ETC1Util.isETC1Supported());
            InputStream input = getResources().openRawResource(R.raw.androids);
            try {
                ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                        GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, input);
            } catch (IOException e) {
                Log.w(TAG, "Could not load texture: " + e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }
    private class SyntheticCompressedTextureLoader implements StaticTriangleRenderer.TextureLoader {
        public void load(GL10 gl) {
            int width = 128;
            int height = 128;
            Buffer image = createImage(width, height);
            ETC1Util.ETC1Texture etc1Texture = ETC1Util.compressTexture(image, width, height, 3, 3 * width);
            if (USE_STREAM_IO) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETC1Util.writeTexture(etc1Texture, bos);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                            GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, bis);
                } catch (IOException e) {
                    Log.w(TAG, "Could not load texture: " + e);
                }
            } else {
                ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
                        GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, etc1Texture);
            }
        }
        private Buffer createImage(int width, int height) {
            int stride = 3 * width;
            ByteBuffer image = ByteBuffer.allocateDirect(height * stride)
                .order(ByteOrder.nativeOrder());
            for (int t = 0; t < height; t++) {
                byte red = (byte)(255-2*t);
                byte green = (byte)(2*t);
                byte blue = 0;
                for (int x = 0; x < width; x++) {
                    int y = x ^ t;
                    image.position(stride*y+x*3);
                    image.put(red);
                    image.put(green);
                    image.put(blue);
                }
            }
            image.position(0);
            return image;
        }
    }
    private GLSurfaceView mGLView;
}
