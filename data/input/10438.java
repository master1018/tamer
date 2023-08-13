class Mappings {
    private Map<String,String> aliases;
    private List<Integer> rawOffsetsIndex;
    private List<Set<String>> rawOffsetsIndexTable;
    private List<String> excludeList;
    Mappings() {
        aliases = new TreeMap<String,String>();
        rawOffsetsIndex = new LinkedList<Integer>();
        rawOffsetsIndexTable = new LinkedList<Set<String>>();
    }
    void add(Zoneinfo zi) {
        Map<String,Zone> zones = zi.getZones();
        for (String zoneName : zones.keySet()) {
            Zone zone = zones.get(zoneName);
            String zonename = zone.getName();
            int rawOffset = zone.get(zone.size()-1).getGmtOffset();
            boolean isExcluded = false;
            if (zone.size() > 1) {
                ZoneRec zrec = zone.get(zone.size()-2);
                if ((zrec.getGmtOffset() != rawOffset)
                    && (zrec.getUntilTime(0) > Time.getCurrentTime())) {
                    if (excludeList == null) {
                        excludeList = new ArrayList<String>();
                    }
                    excludeList.add(zone.getName());
                    isExcluded = true;
                }
            }
            if (!rawOffsetsIndex.contains(new Integer(rawOffset))) {
                int n = rawOffsetsIndex.size();
                int i;
                for (i = 0; i < n; i++) {
                    if (rawOffsetsIndex.get(i) > rawOffset) {
                        break;
                    }
                }
                rawOffsetsIndex.add(i, rawOffset);
                Set<String> perRawOffset = new TreeSet<String>();
                if (!isExcluded) {
                    perRawOffset.add(zonename);
                }
                rawOffsetsIndexTable.add(i, perRawOffset);
            } else if (!isExcluded) {
                int i = rawOffsetsIndex.indexOf(new Integer(rawOffset));
                Set<String> perRawOffset = rawOffsetsIndexTable.get(i);
                perRawOffset.add(zonename);
            }
        }
        Map<String,String> a = zi.getAliases();
        if (excludeList != null) {
            for (String zoneName : a.keySet()) {
                String realname = a.get(zoneName);
                if (excludeList.contains(realname)) {
                    excludeList.add(zoneName);
                }
            }
        }
        aliases.putAll(a);
    }
    void resolve() {
        int index = rawOffsetsIndexTable.size();
        List<String> toBeRemoved = new ArrayList<String>();
        for (String key : aliases.keySet()) {
            boolean validname = false;
            for (int j = 0; j < index; j++) {
                Set<String> perRO = rawOffsetsIndexTable.get(j);
                boolean isExcluded = (excludeList == null) ?
                                        false : excludeList.contains(key);
                if ((perRO.contains(aliases.get(key)) || isExcluded)
                    && Zone.isTargetZone(key)) {
                    validname = true;
                    if (!isExcluded) {
                        perRO.add(key);
                        Main.info("Alias <"+key+"> added to the list.");
                    }
                    break;
                }
            }
            if (!validname) {
                Main.info("Alias <"+key+"> removed from the list.");
                toBeRemoved.add(key);
            }
        }
        for (String key : toBeRemoved) {
            aliases.remove(key);
        }
    }
    Map<String,String> getAliases() {
        return(aliases);
    }
    List<Integer> getRawOffsetsIndex() {
        return(rawOffsetsIndex);
    }
    List<Set<String>> getRawOffsetsIndexTable() {
        return(rawOffsetsIndexTable);
    }
    List<String> getExcludeList() {
        return excludeList;
    }
}
