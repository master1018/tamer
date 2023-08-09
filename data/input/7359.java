public class SoftAudioBuffer {
    private int size;
    private float[] buffer;
    private boolean empty = true;
    private AudioFormat format;
    private AudioFloatConverter converter;
    private byte[] converter_buffer;
    public SoftAudioBuffer(int size, AudioFormat format) {
        this.size = size;
        this.format = format;
        converter = AudioFloatConverter.getConverter(format);
    }
    public void swap(SoftAudioBuffer swap)
    {
        int bak_size = size;
        float[] bak_buffer = buffer;
        boolean bak_empty = empty;
        AudioFormat bak_format = format;
        AudioFloatConverter bak_converter = converter;
        byte[] bak_converter_buffer = converter_buffer;
        size = swap.size;
        buffer = swap.buffer;
        empty = swap.empty;
        format = swap.format;
        converter = swap.converter;
        converter_buffer = swap.converter_buffer;
        swap.size = bak_size;
        swap.buffer = bak_buffer;
        swap.empty = bak_empty;
        swap.format = bak_format;
        swap.converter = bak_converter;
        swap.converter_buffer = bak_converter_buffer;
    }
    public AudioFormat getFormat() {
        return format;
    }
    public int getSize() {
        return size;
    }
    public void clear() {
        if (!empty) {
            Arrays.fill(buffer, 0);
            empty = true;
        }
    }
    public boolean isSilent() {
        return empty;
    }
    public float[] array() {
        empty = false;
        if (buffer == null)
            buffer = new float[size];
        return buffer;
    }
    public void get(byte[] buffer, int channel) {
        int framesize_pc = (format.getFrameSize() / format.getChannels());
        int c_len = size * framesize_pc;
        if (converter_buffer == null || converter_buffer.length < c_len)
            converter_buffer = new byte[c_len];
        if (format.getChannels() == 1) {
            converter.toByteArray(array(), size, buffer);
        } else {
            converter.toByteArray(array(), size, converter_buffer);
            if (channel >= format.getChannels())
                return;
            int z_stepover = format.getChannels() * framesize_pc;
            int k_stepover = framesize_pc;
            for (int j = 0; j < framesize_pc; j++) {
                int k = j;
                int z = channel * framesize_pc + j;
                for (int i = 0; i < size; i++) {
                    buffer[z] = converter_buffer[k];
                    z += z_stepover;
                    k += k_stepover;
                }
            }
        }
    }
}
