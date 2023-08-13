class Zone {
    private String name;
    private List<ZoneRec> list;
    private static Set<String> targetZones;
    Zone(String name) {
        this.name = name;
        list = new ArrayList<ZoneRec>();
    }
    static void readZoneNames(String fileName) {
        if (fileName == null) {
            return;
        }
        BufferedReader in = null;
        try {
            FileReader fr = new FileReader(fileName);
            in = new BufferedReader(fr);
        } catch (FileNotFoundException e) {
            Main.panic("can't open file: " + fileName);
        }
        targetZones = new HashSet<String>();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                if (!targetZones.add(line)) {
                    Main.warning("duplicated target zone name: " + line);
                }
            }
            in.close();
        } catch (IOException e) {
            Main.panic("IO error: "+e.getMessage());
        }
    }
    static boolean isTargetZone(String zoneName) {
        if (targetZones == null) {
            return true;
        }
        return targetZones.contains(zoneName);
    }
    static void addMET() {
        if (targetZones != null) {
            targetZones.add("MET");
        }
    }
    String getName() {
        return name;
    }
    void add(ZoneRec rec) {
        list.add(rec);
    }
    ZoneRec get(int index) {
        return list.get(index);
    }
    int size() {
        return list.size();
    }
    void resolve(Zoneinfo zi) {
        for (int i = 0; i < list.size(); i++) {
            ZoneRec rec = list.get(i);
            rec.resolve(zi);
        }
    }
}
