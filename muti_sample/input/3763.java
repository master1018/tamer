public class ZoneInfo extends TimeZone {
    private static final int UTC_TIME = 0;
    private static final int STANDARD_TIME = 1;
    private static final int WALL_TIME = 2;
    private static final long OFFSET_MASK = 0x0fL;
    private static final long DST_MASK = 0xf0L;
    private static final int DST_NSHIFT = 4;
    private static final long ABBR_MASK = 0xf00L;
    private static final int TRANSITION_NSHIFT = 12;
    private static final boolean USE_OLDMAPPING;
    static {
      String oldmapping = AccessController.doPrivileged(
          new sun.security.action.GetPropertyAction("sun.timezone.ids.oldmapping", "false")).toLowerCase(Locale.ROOT);
      USE_OLDMAPPING = (oldmapping.equals("yes") || oldmapping.equals("true"));
    }
    private static final CalendarSystem gcal = CalendarSystem.getGregorianCalendar();
    private int rawOffset;
    private int rawOffsetDiff = 0;
    private int checksum;
    private int dstSavings;
    private long[] transitions;
    private int[] offsets;
    private int[] simpleTimeZoneParams;
    private boolean willGMTOffsetChange = false;
    transient private boolean dirty = false;
    private static final long serialVersionUID = 2653134537216586139L;
    public ZoneInfo() {
    }
    public ZoneInfo(String ID, int rawOffset) {
        this(ID, rawOffset, 0, 0, null, null, null, false);
    }
    ZoneInfo(String ID,
             int rawOffset,
             int dstSavings,
             int checksum,
             long[] transitions,
             int[] offsets,
             int[] simpleTimeZoneParams,
             boolean willGMTOffsetChange) {
        setID(ID);
        this.rawOffset = rawOffset;
        this.dstSavings = dstSavings;
        this.checksum = checksum;
        this.transitions = transitions;
        this.offsets = offsets;
        this.simpleTimeZoneParams = simpleTimeZoneParams;
        this.willGMTOffsetChange = willGMTOffsetChange;
    }
    public int getOffset(long date) {
        return getOffsets(date, null, UTC_TIME);
    }
    public int getOffsets(long utc, int[] offsets) {
        return getOffsets(utc, offsets, UTC_TIME);
    }
    public int getOffsetsByStandard(long standard, int[] offsets) {
        return getOffsets(standard, offsets, STANDARD_TIME);
    }
    public int getOffsetsByWall(long wall, int[] offsets) {
        return getOffsets(wall, offsets, WALL_TIME);
    }
    private int getOffsets(long date, int[] offsets, int type) {
        if (transitions == null) {
            int offset = getLastRawOffset();
            if (offsets != null) {
                offsets[0] = offset;
                offsets[1] = 0;
            }
            return offset;
        }
        date -= rawOffsetDiff;
        int index = getTransitionIndex(date, type);
        if (index < 0) {
            int offset = getLastRawOffset();
            if (offsets != null) {
                offsets[0] = offset;
                offsets[1] = 0;
            }
            return offset;
        }
        if (index < transitions.length) {
            long val = transitions[index];
            int offset = this.offsets[(int)(val & OFFSET_MASK)] + rawOffsetDiff;
            if (offsets != null) {
                int dst = (int)((val >>> DST_NSHIFT) & 0xfL);
                int save = (dst == 0) ? 0 : this.offsets[dst];
                offsets[0] = offset - save;
                offsets[1] = save;
            }
            return offset;
        }
        SimpleTimeZone tz = getLastRule();
        if (tz != null) {
            int rawoffset = tz.getRawOffset();
            long msec = date;
            if (type != UTC_TIME) {
                msec -= rawOffset;
            }
            int dstoffset = tz.getOffset(msec) - rawOffset;
            if (dstoffset > 0 && tz.getOffset(msec - dstoffset) == rawoffset) {
                dstoffset = 0;
            }
            if (offsets != null) {
                offsets[0] = rawoffset;
                offsets[1] = dstoffset;
            }
            return rawoffset + dstoffset;
        }
        int offset = getLastRawOffset();
        if (offsets != null) {
            offsets[0] = offset;
            offsets[1] = 0;
        }
        return offset;
    }
    private final int getTransitionIndex(long date, int type) {
        int low = 0;
        int high = transitions.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            long val = transitions[mid];
            long midVal = val >> TRANSITION_NSHIFT; 
            if (type != UTC_TIME) {
                midVal += offsets[(int)(val & OFFSET_MASK)]; 
            }
            if (type == STANDARD_TIME) {
                int dstIndex = (int)((val >>> DST_NSHIFT) & 0xfL);
                if (dstIndex != 0) {
                    midVal -= offsets[dstIndex]; 
                }
            }
            if (midVal < date) {
                low = mid + 1;
            } else if (midVal > date) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        if (low >= transitions.length) {
            return low;
        }
        return low - 1;
    }
    public int getOffset(int era, int year, int month, int day,
                         int dayOfWeek, int milliseconds) {
        if (milliseconds < 0 || milliseconds >= AbstractCalendar.DAY_IN_MILLIS) {
            throw new IllegalArgumentException();
        }
        if (era == java.util.GregorianCalendar.BC) { 
            year = 1 - year;
        } else if (era != java.util.GregorianCalendar.AD) {
            throw new IllegalArgumentException();
        }
        CalendarDate date = gcal.newCalendarDate(null);
        date.setDate(year, month + 1, day);
        if (gcal.validate(date) == false) {
            throw new IllegalArgumentException();
        }
        if (dayOfWeek < java.util.GregorianCalendar.SUNDAY
            || dayOfWeek > java.util.GregorianCalendar.SATURDAY) {
            throw new IllegalArgumentException();
        }
        if (transitions == null) {
            return getLastRawOffset();
        }
        long dateInMillis = gcal.getTime(date) + milliseconds;
        dateInMillis -= (long) rawOffset; 
        return getOffsets(dateInMillis, null, UTC_TIME);
    }
    public synchronized void setRawOffset(int offsetMillis) {
        if (offsetMillis == rawOffset + rawOffsetDiff) {
            return;
        }
        rawOffsetDiff = offsetMillis - rawOffset;
        if (lastRule != null) {
            lastRule.setRawOffset(offsetMillis);
        }
        dirty = true;
    }
    public int getRawOffset() {
        if (!willGMTOffsetChange) {
            return rawOffset + rawOffsetDiff;
        }
        int[] offsets = new int[2];
        getOffsets(System.currentTimeMillis(), offsets, UTC_TIME);
        return offsets[0];
    }
    public boolean isDirty() {
        return dirty;
    }
    private int getLastRawOffset() {
        return rawOffset + rawOffsetDiff;
    }
    public boolean useDaylightTime() {
        return (simpleTimeZoneParams != null);
    }
    @Override
    public boolean observesDaylightTime() {
        if (simpleTimeZoneParams != null) {
            return true;
        }
        if (transitions == null) {
            return false;
        }
        long utc = System.currentTimeMillis() - rawOffsetDiff;
        int index = getTransitionIndex(utc, UTC_TIME);
        if (index < 0) {
            return false;
        }
        for (int i = index; i < transitions.length; i++) {
            if ((transitions[i] & DST_MASK) != 0) {
                return true;
            }
        }
        return false;
    }
    public boolean inDaylightTime(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (transitions == null) {
            return false;
        }
        long utc = date.getTime() - rawOffsetDiff;
        int index = getTransitionIndex(utc, UTC_TIME);
        if (index < 0) {
            return false;
        }
        if (index < transitions.length) {
            return (transitions[index] & DST_MASK) != 0;
        }
        SimpleTimeZone tz = getLastRule();
        if (tz != null) {
            return tz.inDaylightTime(date);
        }
        return false;
    }
    public int getDSTSavings() {
        return dstSavings;
    }
    public String toString() {
        return getClass().getName() +
            "[id=\"" + getID() + "\"" +
            ",offset=" + getLastRawOffset() +
            ",dstSavings=" + dstSavings +
            ",useDaylight=" + useDaylightTime() +
            ",transitions=" + ((transitions != null) ? transitions.length : 0) +
            ",lastRule=" + (lastRule == null ? getLastRuleInstance() : lastRule) +
            "]";
    }
    public static String[] getAvailableIDs() {
        List<String> idList = ZoneInfoFile.getZoneIDs();
        List<String> excluded = ZoneInfoFile.getExcludedZones();
        if (excluded != null) {
            List<String> list = new ArrayList<String>(idList.size() + excluded.size());
            list.addAll(idList);
            list.addAll(excluded);
            idList = list;
        }
        String[] ids = new String[idList.size()];
        return idList.toArray(ids);
    }
    public static String[] getAvailableIDs(int rawOffset) {
        String[] result;
        List<String> matched = new ArrayList<String>();
        List<String> IDs = ZoneInfoFile.getZoneIDs();
        int[] rawOffsets = ZoneInfoFile.getRawOffsets();
    loop:
        for (int index = 0; index < rawOffsets.length; index++) {
            if (rawOffsets[index] == rawOffset) {
                byte[] indices = ZoneInfoFile.getRawOffsetIndices();
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] == index) {
                        matched.add(IDs.get(i++));
                        while (i < indices.length && indices[i] == index) {
                            matched.add(IDs.get(i++));
                        }
                        break loop;
                    }
                }
            }
        }
        List<String> excluded = ZoneInfoFile.getExcludedZones();
        if (excluded != null) {
            for (String id : excluded) {
                TimeZone zi = getTimeZone(id);
                if (zi != null && zi.getRawOffset() == rawOffset) {
                    matched.add(id);
                }
            }
        }
        result = new String[matched.size()];
        matched.toArray(result);
        return result;
    }
    public static TimeZone getTimeZone(String ID) {
        String givenID = null;
        if (USE_OLDMAPPING) {
            String compatibleID = TzIDOldMapping.MAP.get(ID);
            if (compatibleID != null) {
                givenID = ID;
                ID = compatibleID;
            }
        }
        ZoneInfo zi = ZoneInfoFile.getZoneInfo(ID);
        if (zi == null) {
            try {
                Map<String, String> map = getAliasTable();
                String alias = ID;
                while ((alias = map.get(alias)) != null) {
                    zi = ZoneInfoFile.getZoneInfo(alias);
                    if (zi != null) {
                        zi.setID(ID);
                        zi = ZoneInfoFile.addToCache(ID, zi);
                        zi = (ZoneInfo) zi.clone();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        if (givenID != null && zi != null) {
            zi.setID(givenID);
        }
        return zi;
    }
    private transient SimpleTimeZone lastRule;
    private synchronized SimpleTimeZone getLastRule() {
        if (lastRule == null) {
            lastRule = getLastRuleInstance();
        }
        return lastRule;
    }
    public SimpleTimeZone getLastRuleInstance() {
        if (simpleTimeZoneParams == null) {
            return null;
        }
        if (simpleTimeZoneParams.length == 10) {
            return new SimpleTimeZone(getLastRawOffset(), getID(),
                                      simpleTimeZoneParams[0],
                                      simpleTimeZoneParams[1],
                                      simpleTimeZoneParams[2],
                                      simpleTimeZoneParams[3],
                                      simpleTimeZoneParams[4],
                                      simpleTimeZoneParams[5],
                                      simpleTimeZoneParams[6],
                                      simpleTimeZoneParams[7],
                                      simpleTimeZoneParams[8],
                                      simpleTimeZoneParams[9],
                                      dstSavings);
        }
        return new SimpleTimeZone(getLastRawOffset(), getID(),
                                  simpleTimeZoneParams[0],
                                  simpleTimeZoneParams[1],
                                  simpleTimeZoneParams[2],
                                  simpleTimeZoneParams[3],
                                  simpleTimeZoneParams[4],
                                  simpleTimeZoneParams[5],
                                  simpleTimeZoneParams[6],
                                  simpleTimeZoneParams[7],
                                  dstSavings);
    }
    public Object clone() {
        ZoneInfo zi = (ZoneInfo) super.clone();
        zi.lastRule = null;
        return zi;
    }
    public int hashCode() {
        return getLastRawOffset() ^ checksum;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo that = (ZoneInfo) obj;
        return (getID().equals(that.getID())
                && (getLastRawOffset() == that.getLastRawOffset())
                && (checksum == that.checksum));
    }
    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ZoneInfo)) {
            if (getRawOffset() != other.getRawOffset()) {
                return false;
            }
            if ((transitions == null)
                && (useDaylightTime() == false)
                && (other.useDaylightTime() == false)) {
                return true;
            }
            return false;
        }
        if (getLastRawOffset() != ((ZoneInfo)other).getLastRawOffset()) {
            return false;
        }
        return (checksum == ((ZoneInfo)other).checksum);
    }
    private static SoftReference<Map> aliasTable;
    public synchronized static Map<String, String> getAliasTable() {
        Map<String, String> aliases = null;
        SoftReference<Map> cache = aliasTable;
        if (cache != null) {
            aliases = cache.get();
            if (aliases != null) {
                return aliases;
            }
        }
        aliases = ZoneInfoFile.getZoneAliases();
        if (aliases != null) {
            aliasTable = new SoftReference<Map>(aliases);
        }
        return aliases;
    }
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        dirty = true;
    }
}
