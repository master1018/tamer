@XmlType(name = "", propOrder = { "stationSection", "classSection", "modellingGuiSection" })
@XmlRootElement(name = "BusinessCase")
public class BusinessCase {
    @XmlElement(name = "StationSection", required = true)
    protected StationSection stationSection;
    @XmlElement(name = "ClassSection", required = true)
    protected ClassSection classSection;
    @XmlElement(name = "ModellingGuiSection")
    protected ModellingGuiSection modellingGuiSection;
    public StationSection getStationSection() {
        return stationSection;
    }
    public void setStationSection(StationSection value) {
        this.stationSection = value;
    }
    public ClassSection getClassSection() {
        return classSection;
    }
    public void setClassSection(ClassSection value) {
        this.classSection = value;
    }
    public ModellingGuiSection getModellingGuiSection() {
        return modellingGuiSection;
    }
    public void setModellingGuiSection(ModellingGuiSection value) {
        this.modellingGuiSection = value;
    }
}
