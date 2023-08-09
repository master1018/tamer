class Rule {
    private List<RuleRec> list;
    private String name;
    Rule(String name) {
        this.name = name;
        list = new ArrayList<RuleRec>();
    }
    void add(RuleRec rec) {
        list.add(rec);
    }
    String getName() {
        return name;
    }
    List<RuleRec> getRules(int year) {
        List<RuleRec> rules = new ArrayList<RuleRec>(3);
        for (RuleRec rec : list) {
            if (year >= rec.getFromYear() && year <= rec.getToYear()) {
                if ((rec.isOdd() && year % 2 == 0) || (rec.isEven() && year % 2 == 1))
                    continue;
                rules.add(rec);
            }
        }
        int n = rules.size();
        if (n <= 1) {
            return rules;
        }
        if (n == 2) {
            RuleRec rec1 = rules.get(0);
            RuleRec rec2 = rules.get(1);
            if (rec1.getMonthNum() > rec2.getMonthNum()) {
                rules.set(0, rec2);
                rules.set(1, rec1);
            } else if (rec1.getMonthNum() == rec2.getMonthNum()) {
                long t1 = Time.getLocalTime(year, rec1.getMonth(),
                                            rec1.getDay(), rec1.getTime().getTime());
                long t2 = Time.getLocalTime(year, rec2.getMonth(),
                                            rec2.getDay(), rec2.getTime().getTime());
                if (t1 > t2) {
                    rules.set(0, rec2);
                    rules.set(1, rec1);
                }
            }
            return rules;
        }
        final int y = year;
        RuleRec[] recs = new RuleRec[rules.size()];
        rules.toArray(recs);
        Arrays.sort(recs, new Comparator<RuleRec>() {
                public int compare(RuleRec r1, RuleRec r2) {
                    int n = r1.getMonthNum() - r2.getMonthNum();
                    if (n != 0) {
                        return n;
                    }
                    long t1 = Time.getLocalTime(y, r1.getMonth(),
                                                r1.getDay(), r1.getTime().getTime());
                    long t2 = Time.getLocalTime(y, r2.getMonth(),
                                                r2.getDay(), r2.getTime().getTime());
                    return (int)(t1 - t2);
                }
                public boolean equals(Object o) {
                    return this == o;
                }
            });
        rules.clear();
        for (int i = 0; i < n; i++) {
            rules.add(recs[i]);
        }
        return rules;
    }
    List<RuleRec> getLastRules() {
        RuleRec start = null;
        RuleRec end = null;
        for (int i = 0; i < list.size(); i++) {
            RuleRec rec = list.get(i);
            if (rec.isLastRule()) {
                if (rec.getSave() > 0) {
                    start = rec;
                } else {
                    end = rec;
                }
            }
        }
        if (start == null || end == null) {
            int endYear = Zoneinfo.getEndYear();
            for (int i  = 0; i < list.size(); i++) {
                RuleRec rec = list.get(i);
                if (endYear >= rec.getFromYear() && endYear <= rec.getToYear()) {
                    if (start == null && rec.getSave() > 0) {
                        start = rec;
                    } else {
                        if (end == null && rec.getSave() == 0) {
                            end = rec;
                        }
                    }
                }
            }
        }
        List<RuleRec> r = new ArrayList<RuleRec>(2);
        if (start == null || end == null) {
            if (start != null || end != null) {
                Main.warning("found last rules for "+name+" inconsistent.");
            }
            return r;
        }
        r.add(start);
        r.add(end);
        return r;
    }
}
