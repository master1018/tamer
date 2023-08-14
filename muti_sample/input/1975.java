class Simple extends BackEnd {
    private static Map<String,ZoneRec> lastZoneRecs
        = new HashMap<String,ZoneRec>();
    private static Map<String,List<RuleRec>> lastRules
        = new TreeMap<String,List<RuleRec>>();
    private SortedMap<Integer, Set<String>> zonesByOffset
        = new TreeMap<Integer,  Set<String>>();
    int processZoneinfo(Timezone tz) {
        String zonename = tz.getName();
        lastRules.put(zonename, tz.getLastRules());
        lastZoneRecs.put(zonename, tz.getLastZoneRec());
        int lastKnownOffset = tz.getRawOffset();
        Set<String> set = zonesByOffset.get(lastKnownOffset);
        if (set == null) {
            set = new TreeSet<String>();
            zonesByOffset.put(lastKnownOffset, set);
        }
        set.add(zonename);
        return 0;
    }
    int generateSrc(Mappings map) {
        try {
            String outputDir = Main.getOutputDir();
            File outD = new File(outputDir);
            if (!outputDir.endsWith(File.separator)) {
                outputDir += outD.separator;
            }
            outD.mkdirs();
            FileWriter fw =
                new FileWriter(outputDir + "TimeZoneData.java", false);
            BufferedWriter out = new BufferedWriter(fw);
            out.write("import java.util.SimpleTimeZone;\n\n");
            out.write("    static SimpleTimeZone zones[] = {\n");
            Map<String,String> a = map.getAliases();
            List<Integer> roi = map.getRawOffsetsIndex();
            List<Set<String>> roit = map.getRawOffsetsIndexTable();
            int index = 0;
            for (int offset : zonesByOffset.keySet()) {
                int o = roi.get(index);
                Set<String> set = zonesByOffset.get(offset);
                if (offset == o) {
                    set.addAll(roit.get(index));
                }
                index++;
                for (String key : set) {
                    ZoneRec zrec;
                    String realname;
                    List<RuleRec> stz;
                    if ((realname = a.get(key)) != null) {
                        if (!Zone.isTargetZone(key)) {
                            continue;
                        }
                        stz = lastRules.get(realname);
                        zrec = lastZoneRecs.get(realname);
                    } else {
                        stz = lastRules.get(key);
                        zrec = lastZoneRecs.get(key);
                    }
                    out.write("\t
                    String s = Time.toFormedString(offset);
                    out.write("\tnew SimpleTimeZone(" +
                        Time.toFormedString(offset) + ", \"" + key + "\"");
                    if (realname != null) {
                        out.write(" ");
                    }
                    if (stz == null) {
                        out.write("),\n");
                    } else {
                        RuleRec rr0 = stz.get(0);
                        RuleRec rr1 = stz.get(1);
                        out.write(",\n\t  " + Month.toString(rr0.getMonthNum()) +
                                  ", " + rr0.getDay().getDayForSimpleTimeZone() + ", " +
                                  rr0.getDay().getDayOfWeekForSimpleTimeZone() + ", " +
                                  Time.toFormedString((int)rr0.getTime().getTime()) + ", " +
                                  rr0.getTime().getTypeForSimpleTimeZone() + ",\n" +
                                  "\t  " + Month.toString(rr1.getMonthNum()) + ", " +
                                  rr1.getDay().getDayForSimpleTimeZone() + ", " +
                                  rr1.getDay().getDayOfWeekForSimpleTimeZone() + ", " +
                                  Time.toFormedString((int)rr1.getTime().getTime())+ ", " +
                                  rr1.getTime().getTypeForSimpleTimeZone() + ",\n" +
                                  "\t  " + Time.toFormedString(rr0.getSave()) + "),\n");
                        out.write("\t
                        out.write("\t
                    }
                    String zline = zrec.getLine();
                    if (zline.indexOf("Zone") == -1) {
                        zline = "Zone " + key + "\t" + zline.trim();
                    }
                    out.write("\t
                }
            }
            out.write("    };\n");
            out.close();
            fw.close();
        } catch(IOException e) {
            Main.panic("IO error: "+e.getMessage());
            return 1;
        }
        return 0;
    }
}
