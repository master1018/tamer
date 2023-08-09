public final class LoadingLayer extends Layer {
    private static final float FADE_INTERVAL = 0.5f;
    private static final float GRAY_VALUE = 0.1f;
    private static final int[] PRELOAD_RESOURCES_ASYNC_UNSCALED = { Res.drawable.stack_frame, Res.drawable.grid_frame,
            Res.drawable.stack_frame_focus, Res.drawable.stack_frame_gold, Res.drawable.btn_location_filter_unscaled,
            Res.drawable.videooverlay, Res.drawable.grid_check_on, Res.drawable.grid_check_off, Res.drawable.icon_camera_small_unscaled,
            Res.drawable.icon_picasa_small_unscaled };
    private static final int[] PRELOAD_RESOURCES_ASYNC_SCALED = {};
    private boolean mLoaded = false;
    private final FloatAnim mOpacity = new FloatAnim(1f);
    private IntBuffer mVertexBuffer;
    public LoadingLayer() {
        int dimension = 10000 * 0x10000;
        int[] vertices = { -dimension, -dimension, 0, dimension, -dimension, 0, -dimension, dimension, 0, dimension, dimension, 0 };
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = vertexByteBuffer.asIntBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }
    public boolean isLoaded() {
        return true;
    }
    @Override
    public void generate(RenderView view, RenderView.Lists lists) {
        lists.blendedList.add(this);
        int[] textures = PRELOAD_RESOURCES_ASYNC_UNSCALED;
        for (int i = 0; i != textures.length; ++i) {
            view.loadTexture(view.getResource(textures[i], false));
        }
        textures = PRELOAD_RESOURCES_ASYNC_SCALED;
        for (int i = 0; i != textures.length; ++i) {
            view.loadTexture(view.getResource(textures[i]));
        }
    }
    @Override
    public void renderBlended(RenderView view, GL11 gl) {
        if (!mLoaded) {
            view.processAllTextures();
            int[] textures = PRELOAD_RESOURCES_ASYNC_SCALED;
            boolean complete = true;
            for (int i = 0; i != textures.length; ++i) {
                if (view.getResource(textures[i]).mState != Texture.STATE_LOADED) {
                    complete = false;
                    break;
                }
            }
            textures = PRELOAD_RESOURCES_ASYNC_UNSCALED;
            for (int i = 0; i != textures.length; ++i) {
                if (view.getResource(textures[i], false).mState != Texture.STATE_LOADED) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                mLoaded = true;
                mOpacity.animateValue(0f, FADE_INTERVAL, SystemClock.uptimeMillis());
            }
        }
        float alpha = mOpacity.getValue(view.getFrameTime());
        if (alpha > 0.004f) {
            float gray = GRAY_VALUE * alpha;
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            gl.glColor4f(gray, gray, gray, alpha);
            gl.glVertexPointer(3, GL11.GL_FIXED, 0, mVertexBuffer);
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glDisable(GL11.GL_DEPTH_TEST);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            gl.glEnable(GL11.GL_DEPTH_TEST);
            gl.glEnable(GL11.GL_TEXTURE_2D);
            view.resetColor();
        } else {
            setHidden(true);
        }
    }
    void reset() {
        mLoaded = false;
        mOpacity.setValue(1.0f);
        setHidden(false);
    }
}
