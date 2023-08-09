public class Test6890943 {
  public static final boolean AIR = true, ROCK = false;
  public static void main(String[] args) {
    new Test6890943().go();
  }
  int r, c, f, t;
  boolean[][] grid;
  public void go() {
    Scanner s = new Scanner(System.in);
    s.useDelimiter("\\s+");
    int T = s.nextInt();
    for (t = 0 ; t < T ; t++) {
      r = s.nextInt(); c = s.nextInt(); f = s.nextInt();
      grid = new boolean[r][c];
      for (int x = 0 ; x < r ; x++) {
        String line = s.next();
        for (int y = 0 ; y < c ; y++) grid[x][y] = line.charAt(y) == '.';
      }
      int digs = solve();
      String res = digs == -1 ? "No" : "Yes " + digs;
      System.out.printf("Case #%d: %s\n", t+1, res);
    }
  }
  Map<Integer, Integer> M = new HashMap<Integer, Integer>();
  private int solve() {
    M = new HashMap<Integer, Integer>();
    M.put(calcWalkingRange(0, 0), 0);
    for (int digDown = 0 ; digDown < r ; digDown++) {
      Map<Integer, Integer> tries = new HashMap<Integer, Integer>();
      for (Map.Entry<Integer, Integer> m : M.entrySet()) {
        int q = m.getKey();
        if (depth(q) != (digDown)) continue;
        if (stuck(q)) continue;
        tries.put(q, m.getValue());
      }
      for (Map.Entry<Integer, Integer> m : tries.entrySet()) {
        int q = m.getKey();
        int fallLeftDelta = 0, fallRightDelta = 0;
        int fallLeft = fall(digDown, start(q));
        if (fallLeft > 0) {
          fallLeftDelta = 1;
          if (fallLeft <= f) addToM(calcWalkingRange(digDown+fallLeft, start(q)), m.getValue());
        }
        int fallRight = fall(digDown, end(q));
        if (fallRight > 0) {
          fallRightDelta = 1;
          if (fallRight <= f) addToM(calcWalkingRange(digDown+fallRight, end(q)), m.getValue());
        }
        for (int p = start(q) + fallLeftDelta ; p <= end(q) - fallRightDelta ; p++) {
          for (int digSpot = p ; digSpot > start(q) +fallLeftDelta ; digSpot--) {
            int fallDown = 1+fall(digDown+1, digSpot);
            if (fallDown <= f) {
              if (fallDown == 1) {
                addToM(calcWalkingRange(digDown + 1, digSpot, digSpot, p), m.getValue() + Math.abs(digSpot-p)+1);
              } else {
                addToM(calcWalkingRange(digDown + fallDown, digSpot), m.getValue() + Math.abs(digSpot-p)+1);
              }
            }
          }
          for (int digSpot = p ; digSpot < end(q)-fallRightDelta ;digSpot++) {
            int fallDown = 1+fall(digDown+1, digSpot);
            if (fallDown <= f) {
              if (fallDown == 1) {
                addToM(calcWalkingRange(digDown + 1, digSpot, p, digSpot), m.getValue() + Math.abs(digSpot-p)+1);
              } else {
                addToM(calcWalkingRange(digDown + fallDown, digSpot), m.getValue() + Math.abs(digSpot-p)+1);
              }
            }
          }
        }
      }
    }
    int result = Integer.MAX_VALUE;
    for (Map.Entry<Integer, Integer> m : M.entrySet()) {
      if (depth(m.getKey()) == r-1) result = Math.min(m.getValue(), result);
    }
    if (result == Integer.MAX_VALUE) return -1;
    return result;
  }
  private void addToM(int q, int i) {
    Integer original = M.get(q);
    if ( original == null ) M.put(q, i);
    else M.put(q, Math.min(original, i));
  }
  private int fall(int row, int column) {
    int res = 0;
    for ( int p = row+1 ; p < r ; p++) {
      if (grid[p][column] == AIR) res++;
      else break;
    }
    return res;
  }
  private boolean stuck(int q) {
    return start(q) == end(q);
  }
  private int depth(int q) {
    return q % 50;
  }
  private int start(int q) {
    return q / (50*50);
  }
  private int end(int q) {
    return (q / 50) % 50;
  }
  private int calcWalkingRange(int depth, int pos) {
    return calcWalkingRange(depth, pos, Integer.MAX_VALUE, Integer.MIN_VALUE);
  }
  private int calcWalkingRange(int depth, int pos, int airOverrideStart, int airOverrideEnd) {
    int left = pos, right = pos;
    if (depth >= r) return (c-1)*50 + depth;
    while (left > 0) {
      if (grid[depth][left-1] == ROCK && (left-1 < airOverrideStart || left-1 > airOverrideEnd)) break;
      if (depth < r-1 && grid[depth+1][left-1] == AIR) {
        left--;
        break;
      }
      left--;
    }
    while (right < c-1) {
      if (grid[depth][right+1] == ROCK && (right+1 < airOverrideStart || right+1 > airOverrideEnd)) break;
      if (depth < r-1 && grid[depth+1][right+1] == AIR) {
        right++;
        break;
      }
      right++;
    }
    return left *50*50 + right*50 + depth;
  }
}
