public abstract class SetOfIntegerSyntax implements Serializable, Cloneable {
    private static final long serialVersionUID = 3666874174847632203L;
    private int[][] members;
    protected SetOfIntegerSyntax(String members) {
        this.members = parse (members);
    }
    private static int[][] parse(String members) {
        Vector theRanges = new Vector();
        int n = (members == null ? 0 : members.length());
        int i = 0;
        int state = 0;
        int lb = 0;
        int ub = 0;
        char c;
        int digit;
        while (i < n) {
            c = members.charAt(i ++);
            switch (state) {
            case 0: 
                if (Character.isWhitespace(c)) {
                    state = 0;
                }
                else if ((digit = Character.digit(c, 10)) != -1) {
                    lb = digit;
                    state = 1;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 1: 
                if (Character.isWhitespace(c)){
                        state = 2;
                } else if ((digit = Character.digit(c, 10)) != -1) {
                    lb = 10 * lb + digit;
                    state = 1;
                } else if (c == '-' || c == ':') {
                    state = 3;
                } else if (c == ',') {
                    accumulate (theRanges, lb, lb);
                    state = 6;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 2: 
                if (Character.isWhitespace(c)) {
                    state = 2;
                }
                else if (c == '-' || c == ':') {
                    state = 3;
                }
                else if (c == ',') {
                    accumulate(theRanges, lb, lb);
                    state = 6;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 3: 
                if (Character.isWhitespace(c)) {
                    state = 3;
                } else if ((digit = Character.digit(c, 10)) != -1) {
                    ub = digit;
                    state = 4;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 4: 
                if (Character.isWhitespace(c)) {
                    state = 5;
                } else if ((digit = Character.digit(c, 10)) != -1) {
                    ub = 10 * ub + digit;
                    state = 4;
                } else if (c == ',') {
                    accumulate(theRanges, lb, ub);
                    state = 6;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 5: 
                if (Character.isWhitespace(c)) {
                    state = 5;
                } else if (c == ',') {
                    accumulate(theRanges, lb, ub);
                    state = 6;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case 6: 
                if (Character.isWhitespace(c)) {
                    state = 6;
                } else if ((digit = Character.digit(c, 10)) != -1) {
                    lb = digit;
                    state = 1;
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            }
        }
        switch (state) {
        case 0: 
            break;
        case 1: 
        case 2: 
            accumulate(theRanges, lb, lb);
            break;
        case 4: 
        case 5: 
            accumulate(theRanges, lb, ub);
            break;
        case 3: 
        case 6: 
            throw new IllegalArgumentException();
        }
        return canonicalArrayForm (theRanges);
    }
    private static void accumulate(Vector ranges, int lb,int ub) {
        if (lb <= ub) {
            ranges.add(new int[] {lb, ub});
            for (int j = ranges.size()-2; j >= 0; -- j) {
                int[] rangea = (int[]) ranges.elementAt (j);
                int lba = rangea[0];
                int uba = rangea[1];
                int[] rangeb = (int[]) ranges.elementAt (j+1);
                int lbb = rangeb[0];
                int ubb = rangeb[1];
                if (Math.max(lba, lbb) - Math.min(uba, ubb) <= 1) {
                    ranges.setElementAt(new int[]
                                           {Math.min(lba, lbb),
                                                Math.max(uba, ubb)}, j);
                    ranges.remove (j+1);
                } else if (lba > lbb) {
                    ranges.setElementAt (rangeb, j);
                    ranges.setElementAt (rangea, j+1);
                } else {
                    break;
                }
            }
        }
    }
    private static int[][] canonicalArrayForm(Vector ranges) {
        return (int[][]) ranges.toArray (new int[ranges.size()][]);
    }
    protected SetOfIntegerSyntax(int[][] members) {
        this.members = parse (members);
    }
    private static int[][] parse(int[][] members) {
        Vector ranges = new Vector();
        int n = (members == null ? 0 : members.length);
        for (int i = 0; i < n; ++ i) {
            int lb, ub;
            if (members[i].length == 1) {
                lb = ub = members[i][0];
            } else if (members[i].length == 2) {
                lb = members[i][0];
                ub = members[i][1];
            } else {
                throw new IllegalArgumentException();
            }
            if (lb <= ub && lb < 0) {
                throw new IllegalArgumentException();
            }
            accumulate(ranges, lb, ub);
        }
                return canonicalArrayForm (ranges);
                }
    protected SetOfIntegerSyntax(int member) {
        if (member < 0) {
            throw new IllegalArgumentException();
        }
        members = new int[][] {{member, member}};
    }
    protected SetOfIntegerSyntax(int lowerBound, int upperBound) {
        if (lowerBound <= upperBound && lowerBound < 0) {
            throw new IllegalArgumentException();
        }
        members = lowerBound <=upperBound ?
            new int[][] {{lowerBound, upperBound}} :
            new int[0][];
    }
    public int[][] getMembers() {
        int n = members.length;
        int[][] result = new int[n][];
        for (int i = 0; i < n; ++ i) {
            result[i] = new int[] {members[i][0], members[i][1]};
        }
        return result;
    }
    public boolean contains(int x) {
        int n = members.length;
        for (int i = 0; i < n; ++ i) {
            if (x < members[i][0]) {
                return false;
            } else if (x <= members[i][1]) {
                return true;
            }
        }
        return false;
    }
    public boolean contains(IntegerSyntax attribute) {
        return contains (attribute.getValue());
    }
    public int next(int x) {
        int n = members.length;
        for (int i = 0; i < n; ++ i) {
            if (x < members[i][0]) {
                return members[i][0];
            } else if (x < members[i][1]) {
                return x + 1;
            }
        }
        return -1;
    }
    public boolean equals(Object object) {
        if (object != null && object instanceof SetOfIntegerSyntax) {
            int[][] myMembers = this.members;
            int[][] otherMembers = ((SetOfIntegerSyntax) object).members;
            int m = myMembers.length;
            int n = otherMembers.length;
            if (m == n) {
                for (int i = 0; i < m; ++ i) {
                    if (myMembers[i][0] != otherMembers[i][0] ||
                        myMembers[i][1] != otherMembers[i][1]) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public int hashCode() {
        int result = 0;
        int n = members.length;
        for (int i = 0; i < n; ++ i) {
            result += members[i][0] + members[i][1];
        }
        return result;
    }
    public String toString() {
        StringBuffer result = new StringBuffer();
        int n = members.length;
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                result.append (',');
            }
            result.append (members[i][0]);
            if (members[i][0] != members[i][1]) {
                result.append ('-');
                result.append (members[i][1]);
            }
        }
        return result.toString();
    }
}
