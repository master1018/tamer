public class CycleTimes {
    private CycleTimeEntry[] entries;
    public CycleTimes(int numberOfCycles) {
        entries = new CycleTimeEntry[numberOfCycles];
    }
    public CycleTimeEntry getEntry(int i) {
        return entries[i];
    }
    public void setEntry(int i, CycleTimeEntry entry) {
        entries[i] = entry;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CycleTimeEntry ct : entries) {
            if (ct == null) {
                sb.append("ON --:-- OFF --:-- | ");
            } else {
                sb.append(String.format("ON %02d:%02d OFF %02d:%02d | ", ct.getStartHour(), ct.getStartMin(), ct.getEndHour(), ct.getEndMin()));
            }
        }
        sb.delete(sb.lastIndexOf(" | "), sb.length());
        return sb.toString();
    }
}
