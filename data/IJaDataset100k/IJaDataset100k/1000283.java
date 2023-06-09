package mulan.classifier.meta;

import java.util.ArrayList;
import java.util.HashSet;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.MultiLabelOutput;
import mulan.data.MultiLabelInstances;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;

/**
 * Class that implements a node for the {@link HMC}
 *
 * @author George Saridis
 * @version 0.2
 */
public class HMCNode extends MultiLabelMetaLearner {

    private String nodeName;

    private HashSet<HMCNode> children = null;

    private Instances header;

    public int[] getLabelIndices() {
        return labelIndices;
    }

    public HMCNode(String name, MultiLabelLearner mlc) throws Exception {
        super(mlc);
        nodeName = name;
    }

    public Instances getHeader() {
        return header;
    }

    @Override
    protected void buildInternal(MultiLabelInstances trainingSet) throws Exception {
        header = new Instances(trainingSet.getDataSet(), 0);
        baseLearner.build(trainingSet);
    }

    protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception {
        return baseLearner.makePrediction(instance);
    }

    public boolean hasChildren() {
        if (children != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getNumChildren() {
        return children.size();
    }

    public HashSet<HMCNode> getChildren() {
        return children;
    }

    public ArrayList<String> getChildrenLabels() {
        ArrayList<String> childrenLabels = new ArrayList<String>();
        for (HMCNode child : getChildren()) {
            childrenLabels.add(child.getName());
        }
        return childrenLabels;
    }

    public ArrayList<String> getDescendantLabels() {
        ArrayList<String> descendantLabels = new ArrayList<String>();
        if (getChildren() != null) {
            for (HMCNode child : getChildren()) {
                descendantLabels.add(child.getName());
                descendantLabels.addAll(child.getDescendantLabels());
            }
        }
        return descendantLabels;
    }

    /**
     * Adds a child to the node.
     *
     * @param child - the child that will be added
     */
    public void addChild(HMCNode child) {
        if (children == null) {
            children = new HashSet<HMCNode>();
        }
        children.add(child);
    }

    public String getName() {
        return nodeName;
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
