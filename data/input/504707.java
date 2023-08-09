public class ReverbType {
    protected ReverbType(String name, int earlyReflectionDelay,
            float earlyReflectionIntensity, int lateReflectionDelay,
            float lateReflectionIntensity, int decayTime) {
        this.name = name;
        this.earlyReflectionDelay = earlyReflectionDelay;
        this.earlyReflectionIntensity = earlyReflectionIntensity;
        this.lateReflectionDelay = lateReflectionDelay;
        this.lateReflectionIntensity = lateReflectionIntensity;
        this.decayTime = decayTime;
    }
    private String name;
    private int earlyReflectionDelay;
    private float earlyReflectionIntensity;
    private int lateReflectionDelay;
    private float lateReflectionIntensity;
    private int decayTime;
    public String getName() {
        return this.name;
    }
    public final int getEarlyReflectionDelay() {
        return this.earlyReflectionDelay;
    }
    public final float getEarlyReflectionIntensity() {
        return this.earlyReflectionIntensity;
    }
    public final int getLateReflectionDelay() {
        return this.lateReflectionDelay;
    }
    public final float getLateReflectionIntensity() {
        return this.lateReflectionIntensity;
    }
    public final int getDecayTime() {
        return this.decayTime;
    }
    public final boolean equals(Object obj) {
        return this == obj;
    }
    public final int hashCode() {
        return toString().hashCode();
    }
    public final String toString() {
        return name + ", early reflection delay " + earlyReflectionDelay 
                + " ns, early reflection intensity " + earlyReflectionIntensity 
                + " dB, late deflection delay " + lateReflectionDelay 
                + " ns, late reflection intensity " + lateReflectionIntensity 
                + " dB, decay time " + decayTime; 
    }
}
