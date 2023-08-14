class Vset implements Constants {
    long vset;                  
    long uset;                  
    long x[];                   
    static final long emptyX[] = new long[0]; 
    static final long fullX[]  = new long[0]; 
    static final int VBITS = 64; 
    static final Vset DEAD_END = new Vset(-1, -1, fullX);
    public Vset() {
        this.x = emptyX;
    }
    private Vset(long vset, long uset, long x[]) {
        this.vset = vset;
        this.uset = uset;
        this.x = x;
    }
    public Vset copy() {
        if (this == DEAD_END) {
            return this;
        }
        Vset vs = new Vset(vset, uset, x);
        if (x.length > 0) {
            vs.growX(x.length); 
        }
        return vs;
    }
    private void growX(int length) {
        long newX[] = new long[length];
        long oldX[] = x;
        for (int i = 0; i < oldX.length; i++) {
            newX[i] = oldX[i];
        }
        x = newX;
    }
    public boolean isDeadEnd() {
        return (this == DEAD_END);
    }
    public boolean isReallyDeadEnd() {
        return (x == fullX);
    }
    public Vset clearDeadEnd() {
        if (this == DEAD_END) {
            return new Vset(-1, -1, fullX);
        }
        return this;
    }
    public boolean testVar(int varNumber) {
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = (varNumber / VBITS - 1) * 2;
            if (i >= x.length) {
                return (x == fullX);
            }
            return (x[i] & bit) != 0;
        } else {
            return (vset & bit) != 0;
        }
    }
    public boolean testVarUnassigned(int varNumber) {
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = ((varNumber / VBITS - 1) * 2) + 1;
            if (i >= x.length) {
                return (x == fullX);
            }
            return (x[i] & bit) != 0;
        } else {
            return (uset & bit) != 0;
        }
    }
    public Vset addVar(int varNumber) {
        if (x == fullX) {
            return this;
        }
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = (varNumber / VBITS - 1) * 2;
            if (i >= x.length) {
                growX(i+1);
            }
            x[i] |= bit;
            if (i+1 < x.length) {
                x[i+1] &=~ bit;
            }
        } else {
            vset |= bit;
            uset &=~ bit;
        }
        return this;
    }
    public Vset addVarUnassigned(int varNumber) {
        if (x == fullX) {
            return this;
        }
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = ((varNumber / VBITS - 1) * 2) + 1;
            if (i >= x.length) {
                growX(i+1);
            }
            x[i] |= bit;
            x[i-1] &=~ bit;
        } else {
            uset |= bit;
            vset &=~ bit;
        }
        return this;
    }
    public Vset clearVar(int varNumber) {
        if (x == fullX) {
            return this;
        }
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = (varNumber / VBITS - 1) * 2;
            if (i >= x.length) {
                return this;
            }
            x[i] &=~ bit;
            if (i+1 < x.length) {
                x[i+1] &=~ bit;
            }
        } else {
            vset &=~ bit;
            uset &=~ bit;
        }
        return this;
    }
    public Vset join(Vset other) {
        if (this == DEAD_END) {
            return other.copy();
        }
        if (other == DEAD_END) {
            return this;
        }
        if (x == fullX) {
            return other.copy();
        }
        if (other.x == fullX) {
            return this;
        }
        vset &= other.vset;
        uset &= other.uset;
        if (other.x == emptyX) {
            x = emptyX;
        } else {
            long otherX[] = other.x;
            int selfLength = x.length;
            int limit = (otherX.length < selfLength) ? otherX.length : selfLength;
            for (int i = 0; i < limit; i++) {
                x[i] &= otherX[i];
            }
            for (int i = limit; i < selfLength; i++) {
                x[i] = 0;
            }
        }
        return this;
    }
    public Vset addDAandJoinDU(Vset other) {
        if (this == DEAD_END) {
            return this;
        }
        if (other == DEAD_END) {
            return other;
        }
        if (x == fullX) {
            return this;
        }
        if (other.x == fullX) {
            return other.copy();
        }
        vset = vset | other.vset;
        uset = (uset & other.uset) & ~other.vset;
        int selfLength = x.length;
        long otherX[] = other.x;
        int otherLength = otherX.length;
        if (otherX != emptyX) {
            if (otherLength > selfLength) {
                growX(otherLength);
            }
            int i = 0;
            while (i < otherLength) {
                x[i] |= otherX[i];
                i++;
                if (i == otherLength) break;
                x[i] = ((x[i] & otherX[i]) & ~otherX[i-1]);
                i++;
            }
        }
        for (int i = (otherLength | 1); i < selfLength; i += 2) {
            x[i] = 0;
        }
        return this;
    }
    public static Vset firstDAandSecondDU(Vset sourceDA, Vset sourceDU) {
        if (sourceDA.x == fullX) {
            return sourceDA.copy();
        }
        long sourceDAx[] = sourceDA.x;
        int lenDA = sourceDAx.length;
        long sourceDUx[] = sourceDU.x;
        int lenDU = sourceDUx.length;
        int limit = (lenDA > lenDU) ? lenDA : lenDU;
        long x[] = emptyX;
        if (limit > 0) {
            x = new long[limit];
            for (int i = 0; i < lenDA; i += 2) {
                x[i] = sourceDAx[i];
            }
            for (int i = 1; i < lenDU; i += 2) {
                x[i] = sourceDUx[i];
            }
        }
        return new Vset(sourceDA.vset, sourceDU.uset, x);
    }
    public Vset removeAdditionalVars(int varNumber) {
        if (x == fullX) {
            return this;
        }
        long bit = (1L << varNumber);
        if (varNumber >= VBITS) {
            int i = (varNumber / VBITS - 1) * 2;
            if (i < x.length) {
                x[i] &= (bit - 1);
                if (++i < x.length) {
                    x[i] &= (bit - 1); 
                }
                while (++i < x.length) {
                    x[i] = 0;
                }
            }
        } else {
            if (x.length > 0) {
                x = emptyX;
            }
            vset &= (bit - 1);
            uset &= (bit - 1);
        }
        return this;
    }
    public int varLimit() {
        long vset;
        int result;
    scan: {
            for (int i = (x.length / 2) * 2; i >= 0; i -= 2) {
                if (i == x.length)  continue; 
                vset = x[i];
                if (i+1 < x.length) {
                    vset |= x[i+1]; 
                }
                if (vset != 0) {
                    result = (i/2 + 1) * VBITS;
                    break scan;
                }
            }
            vset = this.vset;
            vset |= this.uset;  
            if (vset != 0) {
                result = 0;
                break scan;
            } else {
                return 0;
            }
        }
        while (vset != 0) {
            result += 1;
            vset >>>= 1;
        }
        return result;
    }
    public String toString() {
        if (this == DEAD_END)
            return "{DEAD_END}";
        StringBuffer sb = new StringBuffer("{");
        int maxVar = VBITS * (1 + (x.length+1)/2);
        for (int i = 0; i < maxVar; i++) {
            if (!testVarUnassigned(i)) {
                if (sb.length() > 1) {
                    sb.append(' ');
                }
                sb.append(i);
                if (!testVar(i)) {
                    sb.append('?'); 
                }
            }
        }
        if (x == fullX) {
            sb.append("...DEAD_END");
        }
        sb.append('}');
        return sb.toString();
    }
}
