class Visualization2RS extends GenericWaveRS {
    Visualization2RS(int width, int height) {
        super(width, height, R.drawable.fire);
    }
    @Override
    public void update() {
        int len = MediaPlayer.snoop(mVizData, 0);
        int outlen = mPointData.length / 8;
        if (len > outlen) len = outlen;
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
        for(int i = 0; i < len; i++) {
            int amp = mVizData[i] / 128;
            mPointData[i*8+1] = amp;
            mPointData[i*8+5] = -amp;
        }
        mPointAlloc.data(mPointData);
    }
}
