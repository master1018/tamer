public class TestCreateSoundbank {
    public static void main(String[] args) throws Exception {
        Soundbank soundbank = EmergencySoundbank.createSoundbank();
        for (int i = 0; i < 128; i++) {
            Patch patch = new ModelPatch(0, i, false);
            ModelInstrument ins = (ModelInstrument)soundbank.getInstrument(patch);
            if(ins == null)
                throw new Exception("Instrument " + i + " is missing!");
            if(ins.getPerformers().length == 0)
                throw new Exception("Instrument " + i + " doesn't have any performers!");
        }
        Patch patch = new ModelPatch(0, 0, true);
        ModelInstrument ins = (ModelInstrument)soundbank.getInstrument(patch);
        if(ins == null)
            throw new Exception("Drumkit instrument is missing!");
        if(ins.getPerformers().length == 0)
            throw new Exception("Drumkit instrument doesn't have any performers!");
    }
}
