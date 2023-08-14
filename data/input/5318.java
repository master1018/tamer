public class SoftPointResampler extends SoftAbstractResampler {
    public int getPadding() {
        return 100;
    }
    public void interpolate(float[] in, float[] in_offset, float in_end,
            float[] startpitch, float pitchstep, float[] out, int[] out_offset,
            int out_end) {
        float pitch = startpitch[0];
        float ix = in_offset[0];
        int ox = out_offset[0];
        float ix_end = in_end;
        float ox_end = out_end;
        if (pitchstep == 0) {
            while (ix < ix_end && ox < ox_end) {
                out[ox++] = in[(int) ix];
                ix += pitch;
            }
        } else {
            while (ix < ix_end && ox < ox_end) {
                out[ox++] = in[(int) ix];
                ix += pitch;
                pitch += pitchstep;
            }
        }
        in_offset[0] = ix;
        out_offset[0] = ox;
        startpitch[0] = pitch;
    }
}
