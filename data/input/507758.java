public class WaveformImage {
    private static final int SAMPLING_RATE = 8000;
    private WaveformImage() {}
    public static Bitmap drawWaveform(
        ByteArrayOutputStream waveBuffer, int w, int h, int start, int end) {
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(b);
        final Paint paint = new Paint();
        paint.setColor(0xFFFFFFFF); 
        paint.setAntiAlias(true);
        paint.setStrokeWidth(0);
        final ShortBuffer buf = ByteBuffer
            .wrap(waveBuffer.toByteArray())
            .order(ByteOrder.nativeOrder())
            .asShortBuffer();
        buf.position(0);
        final int numSamples = waveBuffer.size() / 2;
        final int delay = (SAMPLING_RATE * 100 / 1000);
        int endIndex = end / 2 + delay;
        if (end == 0 || endIndex >= numSamples) {
            endIndex = numSamples;
        }
        int index = start / 2 - delay;
        if (index < 0) {
            index = 0;
        }
        final int size = endIndex - index;
        int numSamplePerPixel = 32;
        int delta = size / (numSamplePerPixel * w);
        if (delta == 0) {
            numSamplePerPixel = size / w;
            delta = 1;
        }
        final float scale = 3.5f / 65536.0f;
        try {
            for (int i = 0; i < w - 1 ; i++) {
                final float x = i;
                for (int j = 0; j < numSamplePerPixel; j++) {
                    final short s = buf.get(index);
                    final float y = (h / 2) - (s * h * scale);
                    c.drawPoint(x, y, paint);
                    index += delta;
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return b;
    }
}
