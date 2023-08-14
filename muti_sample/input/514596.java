public class ICC_Transform {
    private long transformHandle;
    private int numInputChannels;
    private int numOutputChannels;
    private ICC_Profile src;
    private ICC_Profile dst;
    public int getNumInputChannels() {
        return numInputChannels;
    }
    public int getNumOutputChannels() {
        return numOutputChannels;
    }
    public ICC_Profile getDst() {
        return dst;
    }
    public ICC_Profile getSrc() {
        return src;
    }
    public ICC_Transform(ICC_Profile[] profiles, int[] renderIntents) {
        int numProfiles = profiles.length;
        long[] profileHandles = new long[numProfiles];
        for (int i=0; i<numProfiles; i++) {
            profileHandles[i] = NativeCMM.getHandle(profiles[i]);
        }
        transformHandle = NativeCMM.cmmCreateMultiprofileTransform(
                profileHandles,
                renderIntents);
        src = profiles[0];
        dst = profiles[numProfiles-1];
        numInputChannels = src.getNumComponents();
        numOutputChannels = dst.getNumComponents();
    }
    public ICC_Transform(ICC_Profile[] profiles) {
        int numProfiles = profiles.length;
        int[] renderingIntents = new int[numProfiles];
        int currRenderingIntent = ICC_Profile.icPerceptual;
        if (profiles[0].getProfileClass() == ICC_Profile.CLASS_OUTPUT) {
            currRenderingIntent = ICC_Profile.icRelativeColorimetric;
        }
        for (int i = 0; i < numProfiles; i++) {
            if (i != 0 &&
               i != numProfiles - 1 &&
               profiles[i].getProfileClass() == ICC_Profile.CLASS_ABSTRACT
            ) {
                currRenderingIntent = ICC_Profile.icPerceptual;
            }
            renderingIntents[i] = currRenderingIntent;
            currRenderingIntent =
                ICC_ProfileHelper.getRenderingIntent(profiles[i]);
        }
        long[] profileHandles = new long[numProfiles];
        for (int i=0; i<numProfiles; i++) {
            profileHandles[i] = NativeCMM.getHandle(profiles[i]);
        }
        transformHandle = NativeCMM.cmmCreateMultiprofileTransform(
                profileHandles,
                renderingIntents);
        src = profiles[0];
        dst = profiles[numProfiles-1];
        numInputChannels = src.getNumComponents();
        numOutputChannels = dst.getNumComponents();
    }
    @Override
    protected void finalize() {
        if (transformHandle != 0) {
            NativeCMM.cmmDeleteTransform(transformHandle);
        }
    }
    public void translateColors(NativeImageFormat src, NativeImageFormat dst) {
        NativeCMM.cmmTranslateColors(transformHandle, src, dst);
    }
}