public class CSVforLogExportPlugin implements ExportPlugin {
    public CSVforLogExportPlugin() {
    }
    protected LogReader log;
    protected ArrayList<String> instanceDataSet = new ArrayList<String>();
    protected ArrayList<String> eventDataSet = new ArrayList<String>();
    public boolean accepts(ProvidedObject object) {
        Object[] o = object.getObjects();
        boolean logr = false;
        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof LogReader) {
                logr = true;
            }
        }
        return logr;
    }
    public String getFileExtension() {
        return "csv";
    }
    public void export(ProvidedObject object, OutputStream output) throws IOException {
        Object[] o = object.getObjects();
        log = null;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof LogReader) {
                log = (LogReader) o[i];
            }
        }
        for (int c = 0; c < log.numberOfInstances(); c++) {
            AuditTrailEntryList ateList = log.getInstance(c).getAuditTrailEntryList();
            Map<String, String> attributes = log.getInstance(c).getAttributes();
            for (String key : attributes.keySet()) {
                if (!instanceDataSet.contains(key)) instanceDataSet.add(key);
            }
            for (int i = 0; i < ateList.size(); i++) {
                for (String key : ateList.get(i).getAttributes().keySet()) {
                    if (!eventDataSet.contains(key)) eventDataSet.add(key);
                }
            }
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
        bw.write("caseID;taskID;originator;eventtype;timestamp;");
        for (String str : instanceDataSet) {
            bw.write(str + ";");
        }
        for (String str : eventDataSet) {
            bw.write(str + ";");
        }
        bw.write("\n");
        for (int c = 0; c < log.numberOfInstances(); c++) {
            AuditTrailEntryList ateList = log.getInstance(c).getAuditTrailEntryList();
            Map<String, String> attributes = log.getInstance(c).getAttributes();
            String temp = "";
            for (String str : instanceDataSet) {
                if (attributes.containsKey(str)) {
                    temp += attributes.get(str) + ";";
                } else {
                    temp += ";";
                }
            }
            for (int i = 0; i < ateList.size(); i++) {
                AuditTrailEntry ate = ateList.get(i);
                bw.write(log.getInstance(c).getName() + ";");
                bw.write(ate.getName() + ";");
                if (ate.getOriginator() == null) {
                    bw.write(";");
                } else {
                    bw.write(ate.getOriginator() + ";");
                }
                if (ate.getType() == null) {
                    bw.write(";");
                } else {
                    bw.write(ate.getType() + ";");
                }
                if (ate.getTimestamp() == null) {
                    bw.write(";");
                } else {
                    bw.write(format.format(ate.getTimestamp()) + ";");
                }
                bw.write(temp);
                for (String str : eventDataSet) {
                    if (ate.getAttributes().containsKey(str)) {
                        bw.write(ate.getAttributes().get(str) + ";");
                    } else {
                        bw.write(";");
                    }
                }
                bw.write("\n");
            }
        }
        bw.close();
    }
    public String getName() {
        return "CSV for log Exporter";
    }
    public String getHtmlDescription() {
        String s = "<html>";
        s += "<head><title>ProM Framework: CSV for log Export Plug-in</title></head>";
        s += "<body><h1>CSV for Log Export Plug</h1>";
        s += "<p>The CSV for log Export Plug-in writes a log to a CSV file.</p>";
        s += "</body></html>";
        return s;
    }
}
