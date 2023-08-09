public class LivenessAnalysis {
  private static final boolean DEBUG = false;
  private LivenessAnalysis() {}
  public static LivenessPathList computeAllLivenessPaths(Oop target) {
    LivenessPathList list = computeAllLivenessPaths(target, true);
    if ((list == null) || (list.size() == 0)) {
      return null;
    }
    return list;
  }
  private static LivenessPathList computeAllLivenessPaths(Oop target, boolean trimPathsThroughPopularObjects) {
    ReversePtrs rev = VM.getVM().getRevPtrs();
    if (rev == null) {
      throw new RuntimeException("LivenessAnalysis requires ReversePtrs to have been computed");
    }
    if (rev.get(target) == null) {
      return null;
    }
    Set visitedOops = new HashSet();
    Map visitedRoots =
      new IdentityHashMap();
    visitedOops.add(target);
    LivenessPathList list = new LivenessPathList();
    {
      LivenessPath path = new LivenessPath();
      path.push(new LivenessPathElement(target, null));
      list.add(path);
    }
    while (true) {
      LivenessPath path = null;
      for (int i = list.size() - 1; i >= 0; i--) {
        LivenessPath tmp = list.get(i);
        if (!tmp.isComplete()) {
          path = tmp;
          break;
        }
      }
      if (path == null) {
        return list;
      }
      list.remove(path);
      try {
        ArrayList nextPtrs =
          rev.get(path.peek().getObj());
        if (nextPtrs != null) {
          for (Iterator iter = nextPtrs.iterator(); iter.hasNext(); ) {
            LivenessPathElement nextElement = (LivenessPathElement) iter.next();
            if ((nextElement.isRoot() && (visitedRoots.get(nextElement) == null)) ||
                (!nextElement.isRoot() && !visitedOops.contains(nextElement.getObj()))) {
              if (nextElement.isRoot()) {
                visitedRoots.put(nextElement, nextElement);
              } else {
                visitedOops.add(nextElement.getObj());
              }
              LivenessPath nextPath = path.copy();
              nextPath.push(nextElement);
              list.add(path);
              list.add(nextPath);
              if (trimPathsThroughPopularObjects && nextElement.isRoot()) {
                for (int i = 1; i < nextPath.size() - 1; i++) {
                  LivenessPathElement el = nextPath.get(i);
                  int j = 0;
                  while (j < list.size()) {
                    LivenessPath curPath = list.get(j);
                    if (curPath.peek() == el) {
                      list.remove(curPath);
                    } else {
                      j++;
                    }
                  }
                }
              }
              break;
            }
          }
        }
      } catch (Exception e) {
        System.err.println("LivenessAnalysis: WARNING: " + e +
                           " during traversal");
      }
    }
  }
}
