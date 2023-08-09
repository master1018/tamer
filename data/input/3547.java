abstract class BackEnd {
    abstract int processZoneinfo(Timezone tz);
    abstract int generateSrc(Mappings m);
    static BackEnd getBackEnd() {
        if (Zoneinfo.isYearForTimeZoneDataSpecified) {
            return new Simple();
        } else if (Main.outputDoc) {
            return new GenDoc();
        } else {
            return new Gen();
        }
    }
}
