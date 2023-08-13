class Visualization3RS extends GenericWaveRS {
    private short [] mAnalyzer = new short[256];
    Visualization3RS(int width, int height) {
        super(width, height, R.drawable.ice);
    }
    @Override
    public void setOffset(float xOffset, float yOffset,
            float xStep, float yStep, int xPixels, int yPixels) {
        if (xStep <= 0.0f) {
            xStep = xOffset / 2; 
        }
        mWorldState.yRotation = xStep == 0.f ? 0.f : (xOffset / xStep) * 360;
        mState.data(mWorldState);
    }
    @Override
    public void update() {
        int len = MediaPlayer.snoop(mVizData, 1);
        if (len == 0) {
            if (mWorldState.idle == 0) {
                mWorldState.idle = 1;
                mState.data(mWorldState);
            }
            return;
        }   
        if (mWorldState.idle != 0) {
            mWorldState.idle = 0;
            mState.data(mWorldState);
        }
        for (int i=0; i < 256; i++) {
            short newval = (short)(mVizData[i] * (i/16+2));
            short oldval = mAnalyzer[i];
            if (newval >= oldval - 800) {
            } else {
                newval = (short)(oldval - 800);
            }
            mAnalyzer[i] = newval;
        }
        final int outlen = mPointData.length / 8;
        final int width = mWidth;
        final int skip = (outlen - mWidth) / 2;
        int srcidx = 0;
        int cnt = 0;
        for (int i = 0; i < width; i++) {
            float val = mAnalyzer[srcidx] / 50;
            if (val < 1f && val > -1f) val = 1;
            mPointData[(i + skip) * 8 + 1] = val;
            mPointData[(i + skip) * 8 + 5] = -val;
            cnt += 256;
            if (cnt > width) {
                srcidx++;
                cnt -= width;
            }
        }
        mPointAlloc.data(mPointData);
    }
}
