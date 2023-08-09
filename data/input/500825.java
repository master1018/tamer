public class DisplayFilteredLog extends DisplayLog {
    public DisplayFilteredLog(String name) {
        super(name);
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        ArrayList<ValueDisplayDescriptor> valueDescriptors =
                new ArrayList<ValueDisplayDescriptor>();
        ArrayList<OccurrenceDisplayDescriptor> occurrenceDescriptors =
                new ArrayList<OccurrenceDisplayDescriptor>();
        if (filterEvent(event, valueDescriptors, occurrenceDescriptors)) {
            addToLog(event, logParser, valueDescriptors, occurrenceDescriptors);
        }
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_FILTERED_LOG;
    }
}
