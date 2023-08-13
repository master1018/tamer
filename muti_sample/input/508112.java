public abstract class GradientShader extends Shader {
    protected final int[] mColors;
    protected final float[] mPositions;
    protected GradientShader(int colors[], float positions[]) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        }
        if (positions != null && colors.length != positions.length) {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
        if (positions == null) {
            float spacing = 1.f / (colors.length - 1);
            positions = new float[colors.length];
            positions[0] = 0.f;
            positions[colors.length-1] = 1.f;
            for (int i = 1; i < colors.length - 1 ; i++) {
                positions[i] = spacing * i;
            }
        }
        mColors = colors;
        mPositions = positions;
    }
    protected abstract static class GradientPaint implements java.awt.Paint {
        private final static int GRADIENT_SIZE = 100;
        private final int[] mColors;
        private final float[] mPositions;
        private final TileMode mTileMode;
        private int[] mGradient;
        protected GradientPaint(int[] colors, float[] positions, TileMode tileMode) {
            mColors = colors;
            mPositions = positions;
            mTileMode = tileMode;
        }
        public int getTransparency() {
            return java.awt.Paint.TRANSLUCENT;
        }
        protected synchronized void precomputeGradientColors() {
            if (mGradient == null) {
                mGradient = new int[GRADIENT_SIZE+1];
                int prevPos = 0;
                int nextPos = 1;
                for (int i  = 0 ; i <= GRADIENT_SIZE ; i++) {
                    float currentPos = (float)i/GRADIENT_SIZE;
                    while (currentPos > mPositions[nextPos]) {
                        prevPos = nextPos++;
                    }
                    float percent = (currentPos - mPositions[prevPos]) /
                            (mPositions[nextPos] - mPositions[prevPos]);
                    mGradient[i] = computeColor(mColors[prevPos], mColors[nextPos], percent);
                }
            }
        }
        protected int getGradientColor(float pos) {
            if (pos < 0.f) {
                if (mTileMode != null) {
                    switch (mTileMode) {
                        case CLAMP:
                            pos = 0.f;
                            break;
                        case REPEAT:
                            pos = pos - (float)Math.ceil(pos);
                            break;
                        case MIRROR:
                            int intPart = (int)Math.ceil(pos);
                            pos = pos - intPart;
                            if ((intPart % 2) == 0) {
                                pos = 1.f - pos;
                            }
                            break;
                    }
                } else {
                    pos = 0.0f;
                }
            } else if (pos > 1f) {
                if (mTileMode != null) {
                    switch (mTileMode) {
                        case CLAMP:
                            pos = 1.f;
                            break;
                        case REPEAT:
                            pos = pos - (float)Math.floor(pos);
                            break;
                        case MIRROR:
                            int intPart = (int)Math.floor(pos);
                            pos = pos - intPart;
                            if ((intPart % 2) == 1) {
                                pos = 1.f - pos;
                            }
                            break;
                    }
                } else {
                    pos = 1.0f;
                }
            }
            int index = (int)((pos * GRADIENT_SIZE) + .5);
            return mGradient[index];
        }
        private int computeColor(int c1, int c2, float percent) {
            int a = computeChannel((c1 >> 24) & 0xFF, (c2 >> 24) & 0xFF, percent);
            int r = computeChannel((c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF, percent);
            int g = computeChannel((c1 >>  8) & 0xFF, (c2 >>  8) & 0xFF, percent);
            int b = computeChannel((c1      ) & 0xFF, (c2      ) & 0xFF, percent);
            return a << 24 | r << 16 | g << 8 | b;
        }
        private int computeChannel(int c1, int c2, float percent) {
            return c1 + (int)((percent * (c2-c1)) + .5);
        }
    }
}
