public class SoftTuning {
    private String name = null;
    private double[] tuning = new double[128];
    private Patch patch = null;
    public SoftTuning() {
        name = "12-TET";
        for (int i = 0; i < tuning.length; i++)
            tuning[i] = i * 100;
    }
    public SoftTuning(byte[] data) {
        for (int i = 0; i < tuning.length; i++)
            tuning[i] = i * 100;
        load(data);
    }
    public SoftTuning(Patch patch) {
        this.patch = patch;
        name = "12-TET";
        for (int i = 0; i < tuning.length; i++)
            tuning[i] = i * 100;
    }
    public SoftTuning(Patch patch, byte[] data) {
        this.patch = patch;
        for (int i = 0; i < tuning.length; i++)
            tuning[i] = i * 100;
        load(data);
    }
    private boolean checksumOK(byte[] data) {
        int x = data[1] & 0xFF;
        for (int i = 2; i < data.length - 2; i++)
            x = x ^ (data[i] & 0xFF);
        return (data[data.length - 2] & 0xFF) == (x & 127);
    }
    public void load(byte[] data) {
        if ((data[1] & 0xFF) == 0x7E || (data[1] & 0xFF) == 0x7F) {
            int subid1 = data[3] & 0xFF;
            switch (subid1) {
            case 0x08: 
                int subid2 = data[4] & 0xFF;
                switch (subid2) {
                case 0x01: 
                {
                    try {
                        name = new String(data, 6, 16, "ascii");
                    } catch (UnsupportedEncodingException e) {
                        name = null;
                    }
                    int r = 22;
                    for (int i = 0; i < 128; i++) {
                        int xx = data[r++] & 0xFF;
                        int yy = data[r++] & 0xFF;
                        int zz = data[r++] & 0xFF;
                        if (!(xx == 127 && yy == 127 && zz == 127))
                            tuning[i] = 100.0 *
                                    (((xx * 16384) + (yy * 128) + zz) / 16384.0);
                    }
                    break;
                }
                case 0x02: 
                {
                    int ll = data[6] & 0xFF;
                    int r = 7;
                    for (int i = 0; i < ll; i++) {
                        int kk = data[r++] & 0xFF;
                        int xx = data[r++] & 0xFF;
                        int yy = data[r++] & 0xFF;
                        int zz = data[r++] & 0xFF;
                        if (!(xx == 127 && yy == 127 && zz == 127))
                            tuning[kk] = 100.0*(((xx*16384) + (yy*128) + zz)/16384.0);
                    }
                    break;
                }
                case 0x04: 
                {
                    if (!checksumOK(data))
                        break;
                    try {
                        name = new String(data, 7, 16, "ascii");
                    } catch (UnsupportedEncodingException e) {
                        name = null;
                    }
                    int r = 23;
                    for (int i = 0; i < 128; i++) {
                        int xx = data[r++] & 0xFF;
                        int yy = data[r++] & 0xFF;
                        int zz = data[r++] & 0xFF;
                        if (!(xx == 127 && yy == 127 && zz == 127))
                            tuning[i] = 100.0*(((xx*16384) + (yy*128) + zz)/16384.0);
                    }
                    break;
                }
                case 0x05: 
                {
                    if (!checksumOK(data))
                        break;
                    try {
                        name = new String(data, 7, 16, "ascii");
                    } catch (UnsupportedEncodingException e) {
                        name = null;
                    }
                    int[] octave_tuning = new int[12];
                    for (int i = 0; i < 12; i++)
                        octave_tuning[i] = (data[i + 23] & 0xFF) - 64;
                    for (int i = 0; i < tuning.length; i++)
                        tuning[i] = i * 100 + octave_tuning[i % 12];
                    break;
                }
                case 0x06: 
                {
                    if (!checksumOK(data))
                        break;
                    try {
                        name = new String(data, 7, 16, "ascii");
                    } catch (UnsupportedEncodingException e) {
                        name = null;
                    }
                    double[] octave_tuning = new double[12];
                    for (int i = 0; i < 12; i++) {
                        int v = (data[i * 2 + 23] & 0xFF) * 128
                                + (data[i * 2 + 24] & 0xFF);
                        octave_tuning[i] = (v / 8192.0 - 1) * 100.0;
                    }
                    for (int i = 0; i < tuning.length; i++)
                        tuning[i] = i * 100 + octave_tuning[i % 12];
                    break;
                }
                case 0x07: 
                    int ll = data[7] & 0xFF;
                    int r = 8;
                    for (int i = 0; i < ll; i++) {
                        int kk = data[r++] & 0xFF;
                        int xx = data[r++] & 0xFF;
                        int yy = data[r++] & 0xFF;
                        int zz = data[r++] & 0xFF;
                        if (!(xx == 127 && yy == 127 && zz == 127))
                            tuning[kk] = 100.0
                                    * (((xx*16384) + (yy*128) + zz) / 16384.0);
                    }
                    break;
                case 0x08: 
                {
                    int[] octave_tuning = new int[12];
                    for (int i = 0; i < 12; i++)
                        octave_tuning[i] = (data[i + 8] & 0xFF) - 64;
                    for (int i = 0; i < tuning.length; i++)
                        tuning[i] = i * 100 + octave_tuning[i % 12];
                    break;
                }
                case 0x09: 
                {
                    double[] octave_tuning = new double[12];
                    for (int i = 0; i < 12; i++) {
                        int v = (data[i * 2 + 8] & 0xFF) * 128
                                + (data[i * 2 + 9] & 0xFF);
                        octave_tuning[i] = (v / 8192.0 - 1) * 100.0;
                    }
                    for (int i = 0; i < tuning.length; i++)
                        tuning[i] = i * 100 + octave_tuning[i % 12];
                    break;
                }
                default:
                    break;
                }
            }
        }
    }
    public double[] getTuning() {
        return Arrays.copyOf(tuning, tuning.length);
    }
    public double getTuning(int noteNumber) {
        return tuning[noteNumber];
    }
    public Patch getPatch() {
        return patch;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
