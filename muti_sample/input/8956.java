class RuleRec {
    private int fromYear;
    private int toYear;
    private String type;
    private Month inMonth;
    private RuleDay onDay;
    private Time atTime;
    private int save;
    private String letters;
    private String line;
    private boolean isLastRule;
    int getFromYear() {
        return fromYear;
    }
    int getToYear() {
        return toYear;
    }
    Month getMonth() {
        return inMonth;
    }
    int getMonthNum() {
        return inMonth.value();
    }
    RuleDay getDay() {
        return onDay;
    }
    Time getTime() {
        return atTime;
    }
    int getSave() {
        return save;
    }
    String getLine() {
        return line;
    }
    void setLine(String line) {
        this.line = line;
    }
    boolean isOdd() {
        return "odd".equals(type);
    }
    boolean isEven() {
        return "even".equals(type);
    }
    boolean isLastRule() {
        return isLastRule;
    }
    boolean isSameTransition(ZoneRec zrec, int save, int gmtOffset) {
        long    until, transition;
        if (zrec.getUntilTime().getType() != atTime.getType()) {
            until = zrec.getLocalUntilTime(save, gmtOffset);
            transition = Time.getLocalTime(zrec.getUntilYear(),
                                           getMonth(),
                                           getDay(),
                                           save,
                                           gmtOffset,
                                           atTime);
        } else {
            until = zrec.getLocalUntilTime();
            transition = Time.getLocalTime(zrec.getUntilYear(),
                                           getMonth(),
                                           getDay(),
                                           atTime.getTime());
        }
        return until == transition;
    }
    static RuleRec parse(StringTokenizer tokens) {
        RuleRec rec = new RuleRec();
        try {
            String token = tokens.nextToken();
            try {
                rec.fromYear = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                if ("min".equals(token) || "minimum".equals(token)) {
                    rec.fromYear = Zoneinfo.getMinYear();
                } else if ("max".equals(token) || "maximum".equals(token)) {
                    rec.fromYear = Zoneinfo.getMaxYear();
                } else {
                    Main.panic("invalid year value: "+token);
                }
            }
            token = tokens.nextToken();
            rec.isLastRule = false;
            try {
                rec.toYear = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                if ("min".equals(token) || "minimum".equals(token)) {
                    rec.fromYear = Zoneinfo.getMinYear();
                } else if ("max".equals(token) || "maximum".equals(token)) {
                    rec.toYear = Integer.MAX_VALUE;
                    rec.isLastRule = true;
                } else if ("only".equals(token)) {
                    rec.toYear = rec.fromYear;
                } else {
                    Main.panic("invalid year value: "+token);
                }
            }
            rec.type = tokens.nextToken();
            rec.inMonth = Month.parse(tokens.nextToken());
            rec.onDay = RuleDay.parse(tokens.nextToken());
            rec.atTime = Time.parse(tokens.nextToken());
            rec.save = (int) Time.parse(tokens.nextToken()).getTime();
            rec.letters = tokens.nextToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rec;
    }
    long getTransitionTime(int year, int gmtOffset, int save) {
        long time = Time.getLocalTime(year, getMonth(),
                                      getDay(), atTime.getTime());
        if (atTime.isSTD()) {
            time -= gmtOffset;
        } else if (atTime.isWall()) {
            time -= gmtOffset + save;
        }
        return time;
    }
    private static int getInt(StringTokenizer tokens) {
        String token = tokens.nextToken();
        return Integer.parseInt(token);
    }
}
