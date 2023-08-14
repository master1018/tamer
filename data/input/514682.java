class Instance {
    private static final int SEQUENCE_SAMPLE_SIZE = 16;
    private static final int PATCH_SAMPLE_SIZE = 16;
    private final static float[] ORIENTATIONS = {
            0, (float) (Math.PI / 4), (float) (Math.PI / 2), (float) (Math.PI * 3 / 4),
            (float) Math.PI, -0, (float) (-Math.PI / 4), (float) (-Math.PI / 2),
            (float) (-Math.PI * 3 / 4), (float) -Math.PI
    };
    final float[] vector;
    final String label;
    final long id;
    private Instance(long id, float[] sample, String sampleName) {
        this.id = id;
        vector = sample;
        label = sampleName;
    }
    private void normalize() {
        float[] sample = vector;
        float sum = 0;
        int size = sample.length;
        for (int i = 0; i < size; i++) {
            sum += sample[i] * sample[i];
        }
        float magnitude = (float)Math.sqrt(sum);
        for (int i = 0; i < size; i++) {
            sample[i] /= magnitude;
        }
    }
    static Instance createInstance(int sequenceType, int orientationType, Gesture gesture, String label) {
        float[] pts;
        Instance instance;
        if (sequenceType == GestureStore.SEQUENCE_SENSITIVE) {
            pts = temporalSampler(orientationType, gesture);
            instance = new Instance(gesture.getID(), pts, label);
            instance.normalize();
        } else {
            pts = spatialSampler(gesture);
            instance = new Instance(gesture.getID(), pts, label);
        }
        return instance;
    }
    private static float[] spatialSampler(Gesture gesture) {
        return GestureUtils.spatialSampling(gesture, PATCH_SAMPLE_SIZE, false);
    }
    private static float[] temporalSampler(int orientationType, Gesture gesture) {
        float[] pts = GestureUtils.temporalSampling(gesture.getStrokes().get(0),
                SEQUENCE_SAMPLE_SIZE);
        float[] center = GestureUtils.computeCentroid(pts);
        float orientation = (float)Math.atan2(pts[1] - center[1], pts[0] - center[0]);
        float adjustment = -orientation;
        if (orientationType != GestureStore.ORIENTATION_INVARIANT) {
            int count = ORIENTATIONS.length;
            for (int i = 0; i < count; i++) {
                float delta = ORIENTATIONS[i] - orientation;
                if (Math.abs(delta) < Math.abs(adjustment)) {
                    adjustment = delta;
                }
            }
        }
        GestureUtils.translate(pts, -center[0], -center[1]);
        GestureUtils.rotate(pts, adjustment);
        return pts;
    }
}
