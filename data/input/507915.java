public class ColorScaler {
    private static final float MAX_SHORT = 0xFFFF;
    private static final float MAX_SIGNED_SHORT = 0x7FFF;
    private static final float MAX_XYZ = 1f + (32767f/32768f);
    private float[] channelMinValues = null;
    private float[] channelMulipliers = null; 
    private float[] invChannelMulipliers = null; 
    int nColorChannels = 0;
    boolean isTTypeIntegral = false;
    public void loadScalingData(Raster r, ICC_Profile pf) {
        boolean isSrcTTypeIntegral =
            r.getTransferType() != DataBuffer.TYPE_FLOAT &&
            r.getTransferType() != DataBuffer.TYPE_DOUBLE;
        if (isSrcTTypeIntegral)
            loadScalingData(r.getSampleModel());
        else if (pf != null)
            loadScalingData(pf);
    }
    public void loadScalingData(SampleModel sm) {
        isTTypeIntegral = true;
        nColorChannels = sm.getNumBands();
        channelMinValues = new float[nColorChannels];
        channelMulipliers = new float[nColorChannels];
        invChannelMulipliers = new float[nColorChannels];
        boolean isSignedShort =
            (sm.getTransferType() == DataBuffer.TYPE_SHORT);
        float maxVal;
        for (int i=0; i<nColorChannels; i++) {
            channelMinValues[i] = 0;
            if (isSignedShort) {
                channelMulipliers[i] = MAX_SHORT / MAX_SIGNED_SHORT;
                invChannelMulipliers[i] = MAX_SIGNED_SHORT / MAX_SHORT;
            } else {
                maxVal = ((1 << sm.getSampleSize(i)) - 1);
                channelMulipliers[i] = MAX_SHORT / maxVal;
                invChannelMulipliers[i] = maxVal / MAX_SHORT;
            }
        }
    }
    public void loadScalingData(ICC_Profile pf) {
        isTTypeIntegral = false;
        nColorChannels = pf.getNumComponents();
        float maxValues[] = new float[nColorChannels];
        float minValues[] = new float[nColorChannels];
        switch (pf.getColorSpaceType()) {
            case ColorSpace.TYPE_XYZ:
                minValues[0] = 0;
                minValues[1] = 0;
                minValues[2] = 0;
                maxValues[0] = MAX_XYZ;
                maxValues[1] = MAX_XYZ;
                maxValues[2] = MAX_XYZ;
                break;
            case ColorSpace.TYPE_Lab:
                minValues[0] = 0;
                minValues[1] = -128;
                minValues[2] = -128;
                maxValues[0] = 100;
                maxValues[1] = 127;
                maxValues[2] = 127;
                break;
            default:
                for (int i=0; i<nColorChannels; i++) {
                    minValues[i] = 0;
                    maxValues[i] = 1;
                }
        }
        channelMinValues = minValues;
        channelMulipliers = new float[nColorChannels];
        invChannelMulipliers = new float[nColorChannels];
        for (int i = 0; i < nColorChannels; i++) {
            channelMulipliers[i] =
                MAX_SHORT / (maxValues[i] - channelMinValues[i]);
            invChannelMulipliers[i] =
                (maxValues[i] - channelMinValues[i]) / MAX_SHORT;
        }
    }
    public void loadScalingData(ColorSpace cs) {
        nColorChannels = cs.getNumComponents();
        channelMinValues = new float[nColorChannels];
        channelMulipliers = new float[nColorChannels];
        invChannelMulipliers = new float[nColorChannels];
        for (int i = 0; i < nColorChannels; i++) {
            channelMinValues[i] = cs.getMinValue(i);
            channelMulipliers[i] =
                MAX_SHORT / (cs.getMaxValue(i) - channelMinValues[i]);
            invChannelMulipliers[i] =
                (cs.getMaxValue(i) - channelMinValues[i]) / MAX_SHORT;
        }
    }
    public float[][] scaleNormalize(Raster r) {
        int width = r.getWidth();
        int height = r.getHeight();
        float result[][] = new float[width*height][nColorChannels];
        float normMultipliers[] = new float[nColorChannels];
        int pos = 0;
        if (isTTypeIntegral) {
            for (int i=0; i<nColorChannels; i++) {
                normMultipliers[i] = channelMulipliers[i] / MAX_SHORT;
            }
            int sample;
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        sample = r.getSample(row, col, chan);
                        result[pos][chan] = (sample * normMultipliers[chan]);
                    }
                    pos++;
                }
            }
        } else { 
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        result[pos][chan] = r.getSampleFloat(row, col, chan);
                    }
                    pos++;
                }
            }
        }
        return result;
    }
    public void unscaleNormalized(WritableRaster r, float data[][]) {
        int width = r.getWidth();
        int height = r.getHeight();
        float normMultipliers[] = new float[nColorChannels];
        int pos = 0;
        if (isTTypeIntegral) {
            for (int i=0; i<nColorChannels; i++) {
                normMultipliers[i] = invChannelMulipliers[i] * MAX_SHORT;
            }
            int sample;
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        sample = (int) (data[pos][chan] * normMultipliers[chan] + 0.5f);
                        r.setSample(row, col, chan, sample);
                    }
                    pos++;
                }
            }
        } else { 
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        r.setSample(row, col, chan, data[pos][chan]);
                    }
                    pos++;
                }
            }
        }
    }
    public short[] scale(Raster r) {
        int width = r.getWidth();
        int height = r.getHeight();
        short result[] = new short[width*height*nColorChannels];
        int pos = 0;
        if (isTTypeIntegral) {
            int sample;
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        sample = r.getSample(row, col, chan);
                        result[pos++] =
                            (short) (sample * channelMulipliers[chan] + 0.5f);
                    }
                }
            }
        } else {
            float sample;
            for (int row=r.getMinX(); row<width; row++) {
                for (int col=r.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                        sample = r.getSampleFloat(row, col, chan);
                        result[pos++] = (short) ((sample - channelMinValues[chan])
                            * channelMulipliers[chan] + 0.5f);
                    }
                }
            }
        }
        return result;
    }
    public void unscale(short[] data, WritableRaster wr) {
        int width = wr.getWidth();
        int height = wr.getHeight();
        int pos = 0;
        if (isTTypeIntegral) {
            int sample;
            for (int row=wr.getMinX(); row<width; row++) {
                for (int col=wr.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                         sample = (int) ((data[pos++] & 0xFFFF) *
                                invChannelMulipliers[chan] + 0.5f);
                         wr.setSample(row, col, chan, sample);
                    }
                }
            }
        } else {
            float sample;
            for (int row=wr.getMinX(); row<width; row++) {
                for (int col=wr.getMinY(); col<height; col++) {
                    for (int chan = 0; chan < nColorChannels; chan++) {
                         sample = (data[pos++] & 0xFFFF) *
                            invChannelMulipliers[chan] + channelMinValues[chan];
                         wr.setSample(row, col, chan, sample);
                    }
                }
            }
        }
    }
    public void scale(float[] pixelData, short[] chanData, int chanDataOffset) {
        for (int chan = 0; chan < nColorChannels; chan++) {
            chanData[chanDataOffset + chan] =
                    (short) ((pixelData[chan] - channelMinValues[chan]) *
                        channelMulipliers[chan] + 0.5f);
        }
    }
    public void unscale(float[] pixelData, short[] chanData, int chanDataOffset) {
        for (int chan = 0; chan < nColorChannels; chan++) {
            pixelData[chan] = (chanData[chanDataOffset + chan] & 0xFFFF)
                * invChannelMulipliers[chan] + channelMinValues[chan];
        }
    }
}