public final class GlyphJustificationInfo {
    public static final int PRIORITY_KASHIDA = 0;
    public static final int PRIORITY_WHITESPACE = 1;
    public static final int PRIORITY_INTERCHAR = 2;
    public static final int PRIORITY_NONE = 3;
    public final boolean growAbsorb;
    public final float growLeftLimit;
    public final float growRightLimit;
    public final int growPriority;
    public final boolean shrinkAbsorb;
    public final float shrinkLeftLimit;
    public final float shrinkRightLimit;
    public final int shrinkPriority;
    public final float weight;
    public GlyphJustificationInfo(float weight, boolean growAbsorb, int growPriority,
            float growLeftLimit, float growRightLimit, boolean shrinkAbsorb, int shrinkPriority,
            float shrinkLeftLimit, float shrinkRightLimit) {
        if (weight < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.19C")); 
        }
        this.weight = weight;
        if (growLeftLimit < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.19D")); 
        }
        this.growLeftLimit = growLeftLimit;
        if (growRightLimit < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.19E")); 
        }
        this.growRightLimit = growRightLimit;
        if ((shrinkPriority < 0) || (shrinkPriority > PRIORITY_NONE)) {
            throw new IllegalArgumentException(Messages.getString("awt.19F")); 
        }
        this.shrinkPriority = shrinkPriority;
        if ((growPriority < 0) || (growPriority > PRIORITY_NONE)) {
            throw new IllegalArgumentException(Messages.getString("awt.200")); 
        }
        this.growPriority = growPriority;
        if (shrinkLeftLimit < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.201")); 
        }
        this.shrinkLeftLimit = shrinkLeftLimit;
        if (shrinkRightLimit < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.202")); 
        }
        this.shrinkRightLimit = shrinkRightLimit;
        this.shrinkAbsorb = shrinkAbsorb;
        this.growAbsorb = growAbsorb;
    }
}
