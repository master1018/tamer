public class SimpleSoundbank implements Soundbank {
    String name = "";
    String version = "";
    String vendor = "";
    String description = "";
    List<SoundbankResource> resources = new ArrayList<SoundbankResource>();
    List<Instrument> instruments = new ArrayList<Instrument>();
    public String getName() {
        return name;
    }
    public String getVersion() {
        return version;
    }
    public String getVendor() {
        return vendor;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public SoundbankResource[] getResources() {
        return resources.toArray(new SoundbankResource[resources.size()]);
    }
    public Instrument[] getInstruments() {
        Instrument[] inslist_array
                = instruments.toArray(new Instrument[resources.size()]);
        Arrays.sort(inslist_array, new ModelInstrumentComparator());
        return inslist_array;
    }
    public Instrument getInstrument(Patch patch) {
        int program = patch.getProgram();
        int bank = patch.getBank();
        boolean percussion = false;
        if (patch instanceof ModelPatch)
            percussion = ((ModelPatch)patch).isPercussion();
        for (Instrument instrument : instruments) {
            Patch patch2 = instrument.getPatch();
            int program2 = patch2.getProgram();
            int bank2 = patch2.getBank();
            if (program == program2 && bank == bank2) {
                boolean percussion2 = false;
                if (patch2 instanceof ModelPatch)
                    percussion2 = ((ModelPatch)patch2).isPercussion();
                if (percussion == percussion2)
                    return instrument;
            }
        }
        return null;
    }
    public void addResource(SoundbankResource resource) {
        if (resource instanceof Instrument)
            instruments.add((Instrument) resource);
        else
            resources.add(resource);
    }
    public void removeResource(SoundbankResource resource) {
        if (resource instanceof Instrument)
            instruments.remove((Instrument) resource);
        else
            resources.remove(resource);
    }
    public void addInstrument(Instrument resource) {
        instruments.add(resource);
    }
    public void removeInstrument(Instrument resource) {
        instruments.remove(resource);
    }
    public void addAllInstruments(Soundbank soundbank) {
        for (Instrument ins : soundbank.getInstruments())
            addInstrument(ins);
    }
    public void removeAllInstruments(Soundbank soundbank) {
        for (Instrument ins : soundbank.getInstruments())
            removeInstrument(ins);
    }
}
