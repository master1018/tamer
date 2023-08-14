public class TestProcessAudio {
    public static void main(String[] args) throws Exception {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        SoftAudioBuffer sbuffer = new SoftAudioBuffer(3600, format);
        SoftFilter filter = new SoftFilter(format.getSampleRate());
        Random random = new Random(42);
        for (int t = 0; t <= 6; t++)
        {
            if(t == 0) filter.setFilterType(SoftFilter.FILTERTYPE_BP12);
            if(t == 1) filter.setFilterType(SoftFilter.FILTERTYPE_HP12);
            if(t == 2) filter.setFilterType(SoftFilter.FILTERTYPE_HP24);
            if(t == 3) filter.setFilterType(SoftFilter.FILTERTYPE_LP12);
            if(t == 4) filter.setFilterType(SoftFilter.FILTERTYPE_LP24);
            if(t == 5) filter.setFilterType(SoftFilter.FILTERTYPE_LP6);
            if(t == 6) filter.setFilterType(SoftFilter.FILTERTYPE_NP12);
            for (int f = 1200; f < 3600; f+=100)
                for (int r = 0; r <= 30; r+=5) {
                    filter.reset();
                    filter.setResonance(r);
                    filter.setFrequency(f);
                    float[] data = sbuffer.array();
                    int len = sbuffer.getSize();
                    for (int i = 0; i < len; i++)
                        data[i] = random.nextFloat() - 0.5f;
                    filter.processAudio(sbuffer);
                }
            for (int f = 100; f < 12800; f+=1200)
            for (int r = 0; r <= 30; r+=5) {
                filter.setResonance(r);
                filter.setFrequency(f);
                float[] data = sbuffer.array();
                int len = sbuffer.getSize();
                for (int i = 0; i < len; i++)
                    data[i] = random.nextFloat() - 0.5f;
                filter.processAudio(sbuffer);
            }
            for (int f = 12800; f >= 100; f-=1200)
                for (int r = 30; r >= 0; r-=5) {
                    filter.setResonance(r);
                    filter.setFrequency(f);
                    float[] data = sbuffer.array();
                    int len = sbuffer.getSize();
                    for (int i = 0; i < len; i++)
                        data[i] = random.nextFloat() - 0.5f;
                    filter.processAudio(sbuffer);
                }
            filter.reset();
        }
    }
}
