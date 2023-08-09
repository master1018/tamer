public class Test7047069 {
    static boolean verbose;
    static final int GROW_SIZE = 24;    
    float squareflat;           
    int limit;              
    float hold[] = new float[14];   
    int holdEnd;            
    int holdIndex;          
    int levels[];           
    int levelIndex;         
    public static void subdivide(float src[], int srcoff,
                                 float left[], int leftoff,
                                 float right[], int rightoff)
    {
        float x1 = src[srcoff + 0];
        float y1 = src[srcoff + 1];
        float ctrlx = src[srcoff + 2];
        float ctrly = src[srcoff + 3];
        float x2 = src[srcoff + 4];
        float y2 = src[srcoff + 5];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
        x1 = (x1 + ctrlx) / 2f;
        y1 = (y1 + ctrly) / 2f;
        x2 = (x2 + ctrlx) / 2f;
        y2 = (y2 + ctrly) / 2f;
        ctrlx = (x1 + x2) / 2f;
        ctrly = (y1 + y2) / 2f;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx;
            left[leftoff + 5] = ctrly;
        }
        if (right != null) {
            right[rightoff + 0] = ctrlx;
            right[rightoff + 1] = ctrly;
            right[rightoff + 2] = x2;
            right[rightoff + 3] = y2;
        }
    }
    public static double getFlatnessSq(float coords[], int offset) {
        return Line2D.ptSegDistSq(coords[offset + 0], coords[offset + 1],
                                  coords[offset + 4], coords[offset + 5],
                                  coords[offset + 2], coords[offset + 3]);
    }
    public Test7047069() {
        this.squareflat = .0001f * .0001f;
        holdIndex = hold.length - 6;
        holdEnd = hold.length - 2;
        hold[holdIndex + 0] = (float) (Math.random() * 100);
        hold[holdIndex + 1] = (float) (Math.random() * 100);
        hold[holdIndex + 2] = (float) (Math.random() * 100);
        hold[holdIndex + 3] = (float) (Math.random() * 100);
        hold[holdIndex + 4] = (float) (Math.random() * 100);
        hold[holdIndex + 5] = (float) (Math.random() * 100);
        levelIndex = 0;
        this.limit = 10;
        this.levels = new int[limit + 1];
    }
    void ensureHoldCapacity(int want) {
        if (holdIndex - want < 0) {
            int have = hold.length - holdIndex;
            int newsize = hold.length + GROW_SIZE;
            float newhold[] = new float[newsize];
            System.arraycopy(hold, holdIndex,
                     newhold, holdIndex + GROW_SIZE,
                     have);
            if (verbose) System.err.println("old hold = "+hold+"["+hold.length+"]");
            if (verbose) System.err.println("replacement hold = "+newhold+"["+newhold.length+"]");
            hold = newhold;
            if (verbose) System.err.println("new hold = "+hold+"["+hold.length+"]");
            if (verbose) System.err.println("replacement hold still = "+newhold+"["+newhold.length+"]");
            holdIndex += GROW_SIZE;
            holdEnd += GROW_SIZE;
        }
    }
    private boolean next() {
        if (holdIndex >= holdEnd) {
            return false;
        }
        int level = levels[levelIndex];
        while (level < limit) {
            if (getFlatnessSq(hold, holdIndex) < squareflat) {
                break;
            }
            ensureHoldCapacity(4);
            subdivide(hold, holdIndex,
                      hold, holdIndex - 4,
                      hold, holdIndex);
            holdIndex -= 4;
            level++;
            levels[levelIndex] = level;
            levelIndex++;
            levels[levelIndex] = level;
        }
        holdIndex += 4;
        levelIndex--;
        return true;
    }
    public static void main(String argv[]) {
        verbose = (argv.length > 0);
        for (int i = 0; i < 100000; i++) {
            Test7047069 st = new Test7047069();
            while (st.next()) {}
        }
    }
}
