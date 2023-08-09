public class CompositeFontDescriptor {
    private String faceName;
    private int coreComponentCount;
    private String[] componentFaceNames;
    private String[] componentFileNames;
    private int[] exclusionRanges;
    private int[] exclusionRangeLimits;
    public CompositeFontDescriptor(String faceName,
            int coreComponentCount,
            String[] componentFaceNames,
            String[] componentFileNames,
            int[] exclusionRanges,
            int[] exclusionRangeLimits) {
        this.faceName = faceName;
        this.coreComponentCount = coreComponentCount;
        this.componentFaceNames = componentFaceNames;
        this.componentFileNames = componentFileNames;
        this.exclusionRanges = exclusionRanges;
        this.exclusionRangeLimits = exclusionRangeLimits;
    }
    public String getFaceName() {
        return faceName;
    }
    public int getCoreComponentCount() {
        return coreComponentCount;
    }
    public String[] getComponentFaceNames() {
        return componentFaceNames;
    }
    public String[] getComponentFileNames() {
        return componentFileNames;
    }
    public int[] getExclusionRanges() {
        return exclusionRanges;
    }
    public int[] getExclusionRangeLimits() {
        return exclusionRangeLimits;
    }
}
