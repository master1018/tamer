public abstract class ResolutionSyntax implements Serializable, Cloneable {
    private static final long serialVersionUID = 2706743076526672017L;
    private int crossFeedResolution;
    private int feedResolution;
    public static final int DPI = 100;
    public static final int DPCM = 254;
    public ResolutionSyntax(int crossFeedResolution, int feedResolution,
                            int units) {
        if (crossFeedResolution < 1) {
            throw new IllegalArgumentException("crossFeedResolution is < 1");
        }
        if (feedResolution < 1) {
                throw new IllegalArgumentException("feedResolution is < 1");
        }
        if (units < 1) {
                throw new IllegalArgumentException("units is < 1");
        }
        this.crossFeedResolution = crossFeedResolution * units;
        this.feedResolution = feedResolution * units;
    }
    private static int convertFromDphi(int dphi, int units) {
        if (units < 1) {
            throw new IllegalArgumentException(": units is < 1");
        }
        int round = units / 2;
        return (dphi + round) / units;
    }
    public int[] getResolution(int units) {
        return new int[] { getCrossFeedResolution(units),
                               getFeedResolution(units)
                               };
    }
    public int getCrossFeedResolution(int units) {
        return convertFromDphi (crossFeedResolution, units);
    }
    public int getFeedResolution(int units) {
        return convertFromDphi (feedResolution, units);
    }
    public String toString(int units, String unitsName) {
        StringBuffer result = new StringBuffer();
        result.append(getCrossFeedResolution (units));
        result.append('x');
        result.append(getFeedResolution (units));
        if (unitsName != null) {
            result.append (' ');
            result.append (unitsName);
        }
        return result.toString();
    }
    public boolean lessThanOrEquals(ResolutionSyntax other) {
        return (this.crossFeedResolution <= other.crossFeedResolution &&
                this.feedResolution <= other.feedResolution);
    }
    public boolean equals(Object object) {
        return(object != null &&
               object instanceof ResolutionSyntax &&
               this.crossFeedResolution ==
               ((ResolutionSyntax) object).crossFeedResolution &&
               this.feedResolution ==
               ((ResolutionSyntax) object).feedResolution);
    }
    public int hashCode() {
        return(((crossFeedResolution & 0x0000FFFF)) |
               ((feedResolution      & 0x0000FFFF) << 16));
    }
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(crossFeedResolution);
        result.append('x');
        result.append(feedResolution);
        result.append(" dphi");
        return result.toString();
    }
    protected int getCrossFeedResolutionDphi() {
        return crossFeedResolution;
    }
    protected int getFeedResolutionDphi() {
        return feedResolution;
    }
}
