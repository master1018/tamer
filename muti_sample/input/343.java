public class SoftLinearResampler2 extends SoftAbstractResampler {
    public int getPadding() {
        return 2;
    }
    public void interpolate(float[] in, float[] in_offset, float in_end,
            float[] startpitch, float pitchstep, float[] out, int[] out_offset,
            int out_end) {
        float pitch = startpitch[0];
        float ix = in_offset[0];
        int ox = out_offset[0];
        float ix_end = in_end;
        int ox_end = out_end;
        if (!(ix < ix_end && ox < ox_end))
            return;
        int p_ix = (int) (ix * (1 << 15));
        int p_ix_end = (int) (ix_end * (1 << 15));
        int p_pitch = (int) (pitch * (1 << 15));
        pitch = p_pitch * (1f / (1 << 15));
        if (pitchstep == 0f) {
            int p_ix_len = p_ix_end - p_ix;
            int p_mod = p_ix_len % p_pitch;
            if (p_mod != 0)
                p_ix_len += p_pitch - p_mod;
            int ox_end2 = ox + p_ix_len / p_pitch;
            if (ox_end2 < ox_end)
                ox_end = ox_end2;
            while (ox < ox_end) {
                int iix = p_ix >> 15;
                float fix = ix - iix;
                float i = in[iix];
                out[ox++] = i + (in[iix + 1] - i) * fix;
                p_ix += p_pitch;
                ix += pitch;
            }
        } else {
            int p_pitchstep = (int) (pitchstep * (1 << 15));
            pitchstep = p_pitchstep * (1f / (1 << 15));
            while (p_ix < p_ix_end && ox < ox_end) {
                int iix = p_ix >> 15;
                float fix = ix - iix;
                float i = in[iix];
                out[ox++] = i + (in[iix + 1] - i) * fix;
                ix += pitch;
                p_ix += p_pitch;
                pitch += pitchstep;
                p_pitch += p_pitchstep;
            }
        }
        in_offset[0] = ix;
        out_offset[0] = ox;
        startpitch[0] = pitch;
    }
}
