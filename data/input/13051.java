public class SoftCubicResampler extends SoftAbstractResampler {
    public int getPadding() {
        return 3;
    }
    public void interpolate(float[] in, float[] in_offset, float in_end,
            float[] startpitch, float pitchstep, float[] out, int[] out_offset,
            int out_end) {
        float pitch = startpitch[0];
        float ix = in_offset[0];
        int ox = out_offset[0];
        float ix_end = in_end;
        int ox_end = out_end;
        if (pitchstep == 0) {
            while (ix < ix_end && ox < ox_end) {
                int iix = (int) ix;
                float fix = ix - iix;
                float y0 = in[iix - 1];
                float y1 = in[iix];
                float y2 = in[iix + 1];
                float y3 = in[iix + 2];
                float a0 = y3 - y2 + y1 - y0;
                float a1 = y0 - y1 - a0;
                float a2 = y2 - y0;
                float a3 = y1;
                out[ox++] = ((a0 * fix + a1) * fix + a2) * fix + a3;
                ix += pitch;
            }
        } else {
            while (ix < ix_end && ox < ox_end) {
                int iix = (int) ix;
                float fix = ix - iix;
                float y0 = in[iix - 1];
                float y1 = in[iix];
                float y2 = in[iix + 1];
                float y3 = in[iix + 2];
                float a0 = y3 - y2 + y1 - y0;
                float a1 = y0 - y1 - a0;
                float a2 = y2 - y0;
                float a3 = y1;
                out[ox++] = ((a0 * fix + a1) * fix + a2) * fix + a3;
                ix += pitch;
                pitch += pitchstep;
            }
        }
        in_offset[0] = ix;
        out_offset[0] = ox;
        startpitch[0] = pitch;
    }
}
