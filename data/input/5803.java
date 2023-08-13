public class AdjacencyList {
    private ArrayList<BuildStep> mStepList;
    private List<List<Vertex>> mOrigList;
    public AdjacencyList(List<List<Vertex>> list) {
        mStepList = new ArrayList<BuildStep>();
        mOrigList = list;
        buildList(list, 0, null);
    }
    public Iterator<BuildStep> iterator() {
        return Collections.unmodifiableList(mStepList).iterator();
    }
    private boolean buildList(List<List<Vertex>> theList, int index,
        BuildStep follow) {
        List<Vertex> l = theList.get(index);
        try {
            boolean allNegOne = true;
            boolean allXcps = true;
            for (Vertex v : l) {
                if (v.getIndex() != -1) {
                    if (theList.get(v.getIndex()).size() != 0)
                        allNegOne = false;
                }
                else
                    if (v.getThrowable() == null)
                        allXcps = false;
                mStepList.add(new BuildStep(v, BuildStep.POSSIBLE));
            }
            if (allNegOne) {
                if (allXcps) {
                    if (follow == null)
                        mStepList.add(new BuildStep(null, BuildStep.FAIL));
                    else
                        mStepList.add(new BuildStep(follow.getVertex(),
                                                    BuildStep.BACK));
                    return false;
                } else {
                    List<Vertex> possibles = new ArrayList<Vertex>();
                    for (Vertex v : l) {
                        if (v.getThrowable() == null)
                            possibles.add(v);
                    }
                    if (possibles.size() == 1) {
                        mStepList.add(new BuildStep(possibles.get(0),
                                                    BuildStep.SUCCEED));
                    } else {
                        mStepList.add(new BuildStep(possibles.get(0),
                                                    BuildStep.SUCCEED));
                    }
                    return true;
                }
            } else {
                boolean success = false;
                for (Vertex v : l) {
                    if (v.getIndex() != -1) {
                        if (theList.get(v.getIndex()).size() != 0) {
                            BuildStep bs = new BuildStep(v, BuildStep.FOLLOW);
                            mStepList.add(bs);
                            success = buildList(theList, v.getIndex(), bs);
                        }
                    }
                }
                if (success) {
                    return true;
                } else {
                    if (follow == null)
                        mStepList.add(new BuildStep(null, BuildStep.FAIL));
                    else
                        mStepList.add(new BuildStep(follow.getVertex(),
                                                    BuildStep.BACK));
                    return false;
                }
            }
        }
        catch (Exception e) {}
        return false;
    }
    public String toString() {
        String out = "[\n";
        int i = 0;
        for (List<Vertex> l : mOrigList) {
            out = out + "LinkedList[" + i++ + "]:\n";
            for (Vertex step : l) {
                try {
                    out = out + step.toString();
                    out = out + "\n";
                }
                catch (Exception e) { out = out + "No Such Element\n"; }
            }
        }
        out = out + "]\n";
        return out;
    }
}
