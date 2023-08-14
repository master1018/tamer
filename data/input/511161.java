public class PixelInterleavedSampleModel extends ComponentSampleModel {
    public PixelInterleavedSampleModel(int dataType, int w, int h, int pixelStride,
            int scanlineStride, int bandOffsets[]) {
        super(dataType, w, h, pixelStride, scanlineStride, bandOffsets);
        int maxOffset = bandOffsets[0];
        int minOffset = bandOffsets[0];
        for (int i = 1; i < bandOffsets.length; i++) {
            if (bandOffsets[i] > maxOffset) {
                maxOffset = bandOffsets[i];
            }
            if (bandOffsets[i] < minOffset) {
                minOffset = bandOffsets[i];
            }
        }
        maxOffset -= minOffset;
        if (maxOffset > scanlineStride) {
            throw new IllegalArgumentException(Messages.getString("awt.241")); 
        }
        if (maxOffset > pixelStride) {
            throw new IllegalArgumentException(Messages.getString("awt.242")); 
        }
        if (pixelStride * w > scanlineStride) {
            throw new IllegalArgumentException(Messages.getString("awt.243")); 
        }
    }
    @Override
    public SampleModel createSubsetSampleModel(int bands[]) {
        int newOffsets[] = new int[bands.length];
        for (int i = 0; i < bands.length; i++) {
            newOffsets[i] = bandOffsets[bands[i]];
        }
        return new PixelInterleavedSampleModel(dataType, width, height, pixelStride,
                scanlineStride, newOffsets);
    }
    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int newOffsets[];
        int minOffset = bandOffsets[0];
        for (int i = 1; i < numBands; i++) {
            if (bandOffsets[i] < minOffset) {
                minOffset = bandOffsets[i];
            }
        }
        if (minOffset > 0) {
            newOffsets = new int[numBands];
            for (int i = 0; i < numBands; i++) {
                newOffsets[i] = bandOffsets[i] - minOffset;
            }
        } else {
            newOffsets = bandOffsets;
        }
        return new PixelInterleavedSampleModel(dataType, w, h, pixelStride, pixelStride * w,
                newOffsets);
    }
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        int tmp = hash >>> 8;
        hash <<= 8;
        hash |= tmp;
        return hash ^ 0x66;
    }
}
