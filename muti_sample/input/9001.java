class ZoneRec {
    private int gmtOffset;
    private String ruleName;
    private int directSave;
    private Rule ruleRef;
    private String format;
    private boolean hasUntil;
    private int untilYear;
    private Month untilMonth;
    private RuleDay untilDay;
    private Time untilTime;
    private long untilInMillis;
    private String line;
    Time getUntilTime() {
        return untilTime;
    }
    int getGmtOffset() {
        return gmtOffset;
    }
    String getRuleName() {
        return ruleName;
    }
    int getDirectSave() {
        return directSave;
    }
    boolean hasRuleReference() {
        return ruleRef != null;
    }
    String getFormat() {
        return format;
    }
    int getUntilYear() {
        return untilYear;
    }
    long getUntilTime(int currentSave) {
        if (untilTime.isWall()) {
            return untilInMillis - currentSave;
        }
        return untilInMillis;
    }
    long getLocalUntilTime() {
        return Time.getLocalTime(untilYear,
                                 untilMonth,
                                 untilDay,
                                 untilTime.getTime());
    }
    long getLocalUntilTime(int save, int gmtOffset) {
        return Time.getLocalTime(untilYear,
                                 untilMonth,
                                 untilDay,
                                 save,
                                 gmtOffset,
                                 untilTime);
    }
    String getLine() {
        return line;
    }
    void setLine(String line) {
        this.line = line;
    }
    boolean hasUntil() {
        return this.hasUntil;
    }
    void adjustTime() {
        if (!hasUntil()) {
            return;
        }
        if (untilTime.isSTD() || untilTime.isWall()) {
            untilInMillis -= gmtOffset;
        }
    }
    Rule getRuleRef() {
        return ruleRef;
    }
    void resolve(Zoneinfo zi) {
        if (ruleName != null && (!"-".equals(ruleName))) {
                ruleRef = zi.getRule(ruleName);
        }
        adjustTime();
    }
    static ZoneRec parse(StringTokenizer tokens) {
        ZoneRec rec = new ZoneRec();
        try {
            rec.gmtOffset = (int) Time.parse(tokens.nextToken()).getTime();
            String token = tokens.nextToken();
            char c = token.charAt(0);
            if (c >= '0' && c <= '9') {
                rec.directSave = (int) Time.parse(token).getTime();
            } else {
                rec.ruleName = token;
            }
            rec.format = tokens.nextToken();
            if (tokens.hasMoreTokens()) {
                rec.hasUntil = true;
                rec.untilYear = Integer.parseInt(tokens.nextToken());
                if (tokens.hasMoreTokens()) {
                    rec.untilMonth = Month.parse(tokens.nextToken());
                } else {
                    rec.untilMonth = Month.JANUARY;
                }
                if (tokens.hasMoreTokens()) {
                    rec.untilDay = RuleDay.parse(tokens.nextToken());
                } else {
                    rec.untilDay = new RuleDay(1);
                }
                if (tokens.hasMoreTokens()) {
                    rec.untilTime = Time.parse(tokens.nextToken());
                } else {
                    rec.untilTime = Time.parse("0:00");
                }
                rec.untilInMillis = rec.getLocalUntilTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rec;
    }
    private static void panic(String msg) {
        Main.panic(msg);
    }
}
