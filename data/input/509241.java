class CSVFormatter implements IFormatter {
    private static final String DELIMITER = ", ";
    public String getHeader() {
        StringBuilder csvBuilder = new StringBuilder();
        for (String col : TrackerEntry.ATTRIBUTES) {
            if (!TrackerEntry.ENTRY_TYPE.equals(col) &&
                !TrackerEntry.ID_COL.equals(col)) {
                csvBuilder.append(col);
                csvBuilder.append(DELIMITER);
            }
        }
        csvBuilder.append("\n");
        return csvBuilder.toString();
    }
    public String getOutput(TrackerEntry entry) {
        StringBuilder rowOutput = new StringBuilder();
        rowOutput.append(entry.getTimestamp());
        rowOutput.append(DELIMITER);
        rowOutput.append(entry.getTag());
        rowOutput.append(DELIMITER);
        if (entry.getType() == EntryType.LOCATION_TYPE) {
            if (entry.getLocation().hasAccuracy()) {
                rowOutput.append(entry.getLocation().getAccuracy());
            }
            rowOutput.append(DELIMITER);
            rowOutput.append(entry.getLocation().getLatitude());
            rowOutput.append(DELIMITER);
            rowOutput.append(entry.getLocation().getLongitude());
            rowOutput.append(DELIMITER);
            if (entry.getLocation().hasAltitude()) {
                rowOutput.append(entry.getLocation().getAltitude());
            }
            rowOutput.append(DELIMITER);
            if (entry.getLocation().hasSpeed()) {
                rowOutput.append(entry.getLocation().getSpeed());
            }
            rowOutput.append(DELIMITER);
            if (entry.getLocation().hasBearing()) {
                rowOutput.append(entry.getLocation().getBearing());
            }
            rowOutput.append(DELIMITER);
            rowOutput.append(entry.getDistFromNetLocation());
            rowOutput.append(DELIMITER);
            rowOutput.append(DateUtils.getKMLTimestamp(entry.getLocation()
                    .getTime()));
            rowOutput.append(DELIMITER);
        }
        rowOutput.append(entry.getLogMsg());
        rowOutput.append("\n");
        return rowOutput.toString();
    }
    public String getFooter() {
        return "";
    }
}
