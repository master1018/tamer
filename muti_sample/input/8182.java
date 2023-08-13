public class ModelInstrumentComparator implements Comparator<Instrument> {
    public int compare(Instrument arg0, Instrument arg1) {
        Patch p0 = arg0.getPatch();
        Patch p1 = arg1.getPatch();
        int a = p0.getBank() * 128 + p0.getProgram();
        int b = p1.getBank() * 128 + p1.getProgram();
        if (p0 instanceof ModelPatch) {
            a += ((ModelPatch)p0).isPercussion() ? 2097152 : 0;
        }
        if (p1 instanceof ModelPatch) {
            b += ((ModelPatch)p1).isPercussion() ? 2097152 : 0;
        }
        return a - b;
    }
}
