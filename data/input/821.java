package weka.classifiers.trees;
import weka.classifiers.Classifier;
import weka.classifiers.IterativeClassifier;
import weka.classifiers.trees.adtree.PredictionNode;
import weka.classifiers.trees.adtree.ReferenceInstances;
import weka.classifiers.trees.adtree.Splitter;
import weka.classifiers.trees.adtree.TwoWayNominalSplit;
import weka.classifiers.trees.adtree.TwoWayNumericSplit;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SerializedObject;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
public class ADTree extends Classifier implements OptionHandler, Drawable, AdditionalMeasureProducer, WeightedInstancesHandler, IterativeClassifier, TechnicalInformationHandler {
    static final long serialVersionUID = -1532264837167690683L;
    public String globalInfo() {
        return "Class for generating an alternating decision tree. The basic " + "algorithm is based on:\n\n" + getTechnicalInformation().toString() + "\n\n" + "This version currently only supports two-class problems. The number of boosting " + "iterations needs to be manually tuned to suit the dataset and the desired " + "complexity/accuracy tradeoff. Induction of the trees has been optimized, and heuristic " + "search methods have been introduced to speed learning.";
    }
    public static final int SEARCHPATH_ALL = 0;
    public static final int SEARCHPATH_HEAVIEST = 1;
    public static final int SEARCHPATH_ZPURE = 2;
    public static final int SEARCHPATH_RANDOM = 3;
    public static final Tag[] TAGS_SEARCHPATH = { new Tag(SEARCHPATH_ALL, "Expand all paths"), new Tag(SEARCHPATH_HEAVIEST, "Expand the heaviest path"), new Tag(SEARCHPATH_ZPURE, "Expand the best z-pure path"), new Tag(SEARCHPATH_RANDOM, "Expand a random path") };
    protected Instances m_trainInstances;
    protected PredictionNode m_root = null;
    protected Random m_random = null;
    protected int m_lastAddedSplitNum = 0;
    protected int[] m_numericAttIndices;
    protected int[] m_nominalAttIndices;
    protected double m_trainTotalWeight;
    protected ReferenceInstances m_posTrainInstances;
    protected ReferenceInstances m_negTrainInstances;
    protected PredictionNode m_search_bestInsertionNode;
    protected Splitter m_search_bestSplitter;
    protected double m_search_smallestZ;
    protected Instances m_search_bestPathPosInstances;
    protected Instances m_search_bestPathNegInstances;
    protected int m_nodesExpanded = 0;
    protected int m_examplesCounted = 0;
    protected int m_boostingIterations = 10;
    protected int m_searchPath = 0;
    protected int m_randomSeed = 0;
    protected boolean m_saveInstanceData = false;
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation result;
        result = new TechnicalInformation(Type.INPROCEEDINGS);
        result.setValue(Field.AUTHOR, "Freund, Y. and Mason, L.");
        result.setValue(Field.YEAR, "1999");
        result.setValue(Field.TITLE, "The alternating decision tree learning algorithm");
        result.setValue(Field.BOOKTITLE, "Proceeding of the Sixteenth International Conference on Machine Learning");
        result.setValue(Field.ADDRESS, "Bled, Slovenia");
        result.setValue(Field.PAGES, "124-133");
        return result;
    }
    public void initClassifier(Instances instances) throws Exception {
        m_nodesExpanded = 0;
        m_examplesCounted = 0;
        m_lastAddedSplitNum = 0;
        m_random = new Random(m_randomSeed);
        m_trainInstances = new Instances(instances);
        m_posTrainInstances = new ReferenceInstances(m_trainInstances, m_trainInstances.numInstances());
        m_negTrainInstances = new ReferenceInstances(m_trainInstances, m_trainInstances.numInstances());
        for (Enumeration e = m_trainInstances.enumerateInstances(); e.hasMoreElements(); ) {
            Instance inst = (Instance) e.nextElement();
            if ((int) inst.classValue() == 0) m_negTrainInstances.addReference(inst); else m_posTrainInstances.addReference(inst);
        }
        m_posTrainInstances.compactify();
        m_negTrainInstances.compactify();
        double rootPredictionValue = calcPredictionValue(m_posTrainInstances, m_negTrainInstances);
        m_root = new PredictionNode(rootPredictionValue);
        updateWeights(m_posTrainInstances, m_negTrainInstances, rootPredictionValue);
        generateAttributeIndicesSingle();
    }
    public void next(int iteration) throws Exception {
        boost();
    }
    public void boost() throws Exception {
        if (m_trainInstances == null || m_trainInstances.numInstances() == 0) throw new Exception("Trying to boost with no training data");
        searchForBestTestSingle();
        if (m_search_bestSplitter == null) return;
        for (int i = 0; i < 2; i++) {
            Instances posInstances = m_search_bestSplitter.instancesDownBranch(i, m_search_bestPathPosInstances);
            Instances negInstances = m_search_bestSplitter.instancesDownBranch(i, m_search_bestPathNegInstances);
            double predictionValue = calcPredictionValue(posInstances, negInstances);
            PredictionNode newPredictor = new PredictionNode(predictionValue);
            updateWeights(posInstances, negInstances, predictionValue);
            m_search_bestSplitter.setChildForBranch(i, newPredictor);
        }
        m_search_bestInsertionNode.addChild((Splitter) m_search_bestSplitter, this);
        m_search_bestPathPosInstances = null;
        m_search_bestPathNegInstances = null;
        m_search_bestSplitter = null;
    }
    private void generateAttributeIndicesSingle() {
        FastVector nominalIndices = new FastVector();
        FastVector numericIndices = new FastVector();
        for (int i = 0; i < m_trainInstances.numAttributes(); i++) {
            if (i != m_trainInstances.classIndex()) {
                if (m_trainInstances.attribute(i).isNumeric()) numericIndices.addElement(new Integer(i)); else nominalIndices.addElement(new Integer(i));
            }
        }
        m_nominalAttIndices = new int[nominalIndices.size()];
        for (int i = 0; i < nominalIndices.size(); i++) m_nominalAttIndices[i] = ((Integer) nominalIndices.elementAt(i)).intValue();
        m_numericAttIndices = new int[numericIndices.size()];
        for (int i = 0; i < numericIndices.size(); i++) m_numericAttIndices[i] = ((Integer) numericIndices.elementAt(i)).intValue();
    }
    private void searchForBestTestSingle() throws Exception {
        m_trainTotalWeight = m_trainInstances.sumOfWeights();
        m_search_smallestZ = Double.POSITIVE_INFINITY;
        searchForBestTestSingle(m_root, m_posTrainInstances, m_negTrainInstances);
    }
    private void searchForBestTestSingle(PredictionNode currentNode, Instances posInstances, Instances negInstances) throws Exception {
        if (posInstances.numInstances() == 0 || negInstances.numInstances() == 0) return;
        if (calcZpure(posInstances, negInstances) >= m_search_smallestZ) return;
        m_nodesExpanded++;
        m_examplesCounted += posInstances.numInstances() + negInstances.numInstances();
        for (int i = 0; i < m_nominalAttIndices.length; i++) evaluateNominalSplitSingle(m_nominalAttIndices[i], currentNode, posInstances, negInstances);
        if (m_numericAttIndices.length > 0) {
            Instances allInstances = new Instances(posInstances);
            for (Enumeration e = negInstances.enumerateInstances(); e.hasMoreElements(); ) allInstances.add((Instance) e.nextElement());
            for (int i = 0; i < m_numericAttIndices.length; i++) evaluateNumericSplitSingle(m_numericAttIndices[i], currentNode, posInstances, negInstances, allInstances);
        }
        if (currentNode.getChildren().size() == 0) return;
        switch(m_searchPath) {
            case SEARCHPATH_ALL:
                goDownAllPathsSingle(currentNode, posInstances, negInstances);
                break;
            case SEARCHPATH_HEAVIEST:
                goDownHeaviestPathSingle(currentNode, posInstances, negInstances);
                break;
            case SEARCHPATH_ZPURE:
                goDownZpurePathSingle(currentNode, posInstances, negInstances);
                break;
            case SEARCHPATH_RANDOM:
                goDownRandomPathSingle(currentNode, posInstances, negInstances);
                break;
        }
    }
    private void goDownAllPathsSingle(PredictionNode currentNode, Instances posInstances, Instances negInstances) throws Exception {
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            for (int i = 0; i < split.getNumOfBranches(); i++) searchForBestTestSingle(split.getChildForBranch(i), split.instancesDownBranch(i, posInstances), split.instancesDownBranch(i, negInstances));
        }
    }
    private void goDownHeaviestPathSingle(PredictionNode currentNode, Instances posInstances, Instances negInstances) throws Exception {
        Splitter heaviestSplit = null;
        int heaviestBranch = 0;
        double largestWeight = 0.0;
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            for (int i = 0; i < split.getNumOfBranches(); i++) {
                double weight = split.instancesDownBranch(i, posInstances).sumOfWeights() + split.instancesDownBranch(i, negInstances).sumOfWeights();
                if (weight > largestWeight) {
                    heaviestSplit = split;
                    heaviestBranch = i;
                    largestWeight = weight;
                }
            }
        }
        if (heaviestSplit != null) searchForBestTestSingle(heaviestSplit.getChildForBranch(heaviestBranch), heaviestSplit.instancesDownBranch(heaviestBranch, posInstances), heaviestSplit.instancesDownBranch(heaviestBranch, negInstances));
    }
    private void goDownZpurePathSingle(PredictionNode currentNode, Instances posInstances, Instances negInstances) throws Exception {
        double lowestZpure = m_search_smallestZ;
        PredictionNode bestPath = null;
        Instances bestPosSplit = null, bestNegSplit = null;
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            for (int i = 0; i < split.getNumOfBranches(); i++) {
                Instances posSplit = split.instancesDownBranch(i, posInstances);
                Instances negSplit = split.instancesDownBranch(i, negInstances);
                double newZpure = calcZpure(posSplit, negSplit);
                if (newZpure < lowestZpure) {
                    lowestZpure = newZpure;
                    bestPath = split.getChildForBranch(i);
                    bestPosSplit = posSplit;
                    bestNegSplit = negSplit;
                }
            }
        }
        if (bestPath != null) searchForBestTestSingle(bestPath, bestPosSplit, bestNegSplit);
    }
    private void goDownRandomPathSingle(PredictionNode currentNode, Instances posInstances, Instances negInstances) throws Exception {
        FastVector children = currentNode.getChildren();
        Splitter split = (Splitter) children.elementAt(getRandom(children.size()));
        int branch = getRandom(split.getNumOfBranches());
        searchForBestTestSingle(split.getChildForBranch(branch), split.instancesDownBranch(branch, posInstances), split.instancesDownBranch(branch, negInstances));
    }
    private void evaluateNominalSplitSingle(int attIndex, PredictionNode currentNode, Instances posInstances, Instances negInstances) {
        double[] indexAndZ = findLowestZNominalSplit(posInstances, negInstances, attIndex);
        if (indexAndZ[1] < m_search_smallestZ) {
            m_search_smallestZ = indexAndZ[1];
            m_search_bestInsertionNode = currentNode;
            m_search_bestSplitter = new TwoWayNominalSplit(attIndex, (int) indexAndZ[0]);
            m_search_bestPathPosInstances = posInstances;
            m_search_bestPathNegInstances = negInstances;
        }
    }
    private void evaluateNumericSplitSingle(int attIndex, PredictionNode currentNode, Instances posInstances, Instances negInstances, Instances allInstances) throws Exception {
        double[] splitAndZ = findLowestZNumericSplit(allInstances, attIndex);
        if (splitAndZ[1] < m_search_smallestZ) {
            m_search_smallestZ = splitAndZ[1];
            m_search_bestInsertionNode = currentNode;
            m_search_bestSplitter = new TwoWayNumericSplit(attIndex, splitAndZ[0]);
            m_search_bestPathPosInstances = posInstances;
            m_search_bestPathNegInstances = negInstances;
        }
    }
    private double calcPredictionValue(Instances posInstances, Instances negInstances) {
        return 0.5 * Math.log((posInstances.sumOfWeights() + 1.0) / (negInstances.sumOfWeights() + 1.0));
    }
    private double calcZpure(Instances posInstances, Instances negInstances) {
        double posWeight = posInstances.sumOfWeights();
        double negWeight = negInstances.sumOfWeights();
        return (2.0 * (Math.sqrt(posWeight + 1.0) + Math.sqrt(negWeight + 1.0))) + (m_trainTotalWeight - (posWeight + negWeight));
    }
    private void updateWeights(Instances posInstances, Instances negInstances, double predictionValue) {
        double weightMultiplier = Math.pow(Math.E, -predictionValue);
        for (Enumeration e = posInstances.enumerateInstances(); e.hasMoreElements(); ) {
            Instance inst = (Instance) e.nextElement();
            inst.setWeight(inst.weight() * weightMultiplier);
        }
        weightMultiplier = Math.pow(Math.E, predictionValue);
        for (Enumeration e = negInstances.enumerateInstances(); e.hasMoreElements(); ) {
            Instance inst = (Instance) e.nextElement();
            inst.setWeight(inst.weight() * weightMultiplier);
        }
    }
    private double[] findLowestZNominalSplit(Instances posInstances, Instances negInstances, int attIndex) {
        double lowestZ = Double.MAX_VALUE;
        int bestIndex = 0;
        double[] posWeights = attributeValueWeights(posInstances, attIndex);
        double[] negWeights = attributeValueWeights(negInstances, attIndex);
        double posWeight = Utils.sum(posWeights);
        double negWeight = Utils.sum(negWeights);
        int maxIndex = posWeights.length;
        if (maxIndex == 2) maxIndex = 1;
        for (int i = 0; i < maxIndex; i++) {
            double w1 = posWeights[i] + 1.0;
            double w2 = negWeights[i] + 1.0;
            double w3 = posWeight - w1 + 2.0;
            double w4 = negWeight - w2 + 2.0;
            double wRemainder = m_trainTotalWeight + 4.0 - (w1 + w2 + w3 + w4);
            double newZ = (2.0 * (Math.sqrt(w1 * w2) + Math.sqrt(w3 * w4))) + wRemainder;
            if (newZ < lowestZ) {
                lowestZ = newZ;
                bestIndex = i;
            }
        }
        double[] indexAndZ = new double[2];
        indexAndZ[0] = (double) bestIndex;
        indexAndZ[1] = lowestZ;
        return indexAndZ;
    }
    private double[] attributeValueWeights(Instances instances, int attIndex) {
        double[] weights = new double[instances.attribute(attIndex).numValues()];
        for (int i = 0; i < weights.length; i++) weights[i] = 0.0;
        for (Enumeration e = instances.enumerateInstances(); e.hasMoreElements(); ) {
            Instance inst = (Instance) e.nextElement();
            if (!inst.isMissing(attIndex)) weights[(int) inst.value(attIndex)] += inst.weight();
        }
        return weights;
    }
    private double[] findLowestZNumericSplit(Instances instances, int attIndex) throws Exception {
        double splitPoint = 0.0;
        double bestVal = Double.MAX_VALUE, currVal, currCutPoint;
        int numMissing = 0;
        double[][] distribution = new double[3][instances.numClasses()];
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance inst = instances.instance(i);
            if (!inst.isMissing(attIndex)) {
                distribution[1][(int) inst.classValue()] += inst.weight();
            } else {
                distribution[2][(int) inst.classValue()] += inst.weight();
                numMissing++;
            }
        }
        instances.sort(attIndex);
        for (int i = 0; i < instances.numInstances() - (numMissing + 1); i++) {
            Instance inst = instances.instance(i);
            Instance instPlusOne = instances.instance(i + 1);
            distribution[0][(int) inst.classValue()] += inst.weight();
            distribution[1][(int) inst.classValue()] -= inst.weight();
            if (Utils.sm(inst.value(attIndex), instPlusOne.value(attIndex))) {
                currCutPoint = (inst.value(attIndex) + instPlusOne.value(attIndex)) / 2.0;
                currVal = conditionedZOnRows(distribution);
                if (currVal < bestVal) {
                    splitPoint = currCutPoint;
                    bestVal = currVal;
                }
            }
        }
        double[] splitAndZ = new double[2];
        splitAndZ[0] = splitPoint;
        splitAndZ[1] = bestVal;
        return splitAndZ;
    }
    private double conditionedZOnRows(double[][] distribution) {
        double w1 = distribution[0][0] + 1.0;
        double w2 = distribution[0][1] + 1.0;
        double w3 = distribution[1][0] + 1.0;
        double w4 = distribution[1][1] + 1.0;
        double wRemainder = m_trainTotalWeight + 4.0 - (w1 + w2 + w3 + w4);
        return (2.0 * (Math.sqrt(w1 * w2) + Math.sqrt(w3 * w4))) + wRemainder;
    }
    public double[] distributionForInstance(Instance instance) {
        double predVal = predictionValueForInstance(instance, m_root, 0.0);
        double[] distribution = new double[2];
        distribution[0] = 1.0 / (1.0 + Math.pow(Math.E, predVal));
        distribution[1] = 1.0 / (1.0 + Math.pow(Math.E, -predVal));
        return distribution;
    }
    protected double predictionValueForInstance(Instance inst, PredictionNode currentNode, double currentValue) {
        currentValue += currentNode.getValue();
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            int branch = split.branchInstanceGoesDown(inst);
            if (branch >= 0) currentValue = predictionValueForInstance(inst, split.getChildForBranch(branch), currentValue);
        }
        return currentValue;
    }
    public String toString() {
        if (m_root == null) return ("ADTree not built yet"); else {
            return ("Alternating decision tree:\n\n" + toString(m_root, 1) + "\nLegend: " + legend() + "\nTree size (total number of nodes): " + numOfAllNodes(m_root) + "\nLeaves (number of predictor nodes): " + numOfPredictionNodes(m_root));
        }
    }
    protected String toString(PredictionNode currentNode, int level) {
        StringBuffer text = new StringBuffer();
        text.append(": " + Utils.doubleToString(currentNode.getValue(), 3));
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            for (int j = 0; j < split.getNumOfBranches(); j++) {
                PredictionNode child = split.getChildForBranch(j);
                if (child != null) {
                    text.append("\n");
                    for (int k = 0; k < level; k++) {
                        text.append("|  ");
                    }
                    text.append("(" + split.orderAdded + ")");
                    text.append(split.attributeString(m_trainInstances) + " " + split.comparisonString(j, m_trainInstances));
                    text.append(toString(child, level + 1));
                }
            }
        }
        return text.toString();
    }
    public int graphType() {
        return Drawable.TREE;
    }
    public String graph() throws Exception {
        StringBuffer text = new StringBuffer();
        text.append("digraph ADTree {\n");
        graphTraverse(m_root, text, 0, 0, m_trainInstances);
        return text.toString() + "}\n";
    }
    protected void graphTraverse(PredictionNode currentNode, StringBuffer text, int splitOrder, int predOrder, Instances instances) throws Exception {
        text.append("S" + splitOrder + "P" + predOrder + " [label=\"");
        text.append(Utils.doubleToString(currentNode.getValue(), 3));
        if (splitOrder == 0) text.append(" (" + legend() + ")");
        text.append("\" shape=box style=filled");
        if (instances.numInstances() > 0) text.append(" data=\n" + instances + "\n,\n");
        text.append("]\n");
        for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
            Splitter split = (Splitter) e.nextElement();
            text.append("S" + splitOrder + "P" + predOrder + "->" + "S" + split.orderAdded + " [style=dotted]\n");
            text.append("S" + split.orderAdded + " [label=\"" + split.orderAdded + ": " + split.attributeString(m_trainInstances) + "\"]\n");
            for (int i = 0; i < split.getNumOfBranches(); i++) {
                PredictionNode child = split.getChildForBranch(i);
                if (child != null) {
                    text.append("S" + split.orderAdded + "->" + "S" + split.orderAdded + "P" + i + " [label=\"" + split.comparisonString(i, m_trainInstances) + "\"]\n");
                    graphTraverse(child, text, split.orderAdded, i, split.instancesDownBranch(i, instances));
                }
            }
        }
    }
    public String legend() {
        Attribute classAttribute = null;
        if (m_trainInstances == null) return "";
        try {
            classAttribute = m_trainInstances.classAttribute();
        } catch (Exception x) {
        }
        ;
        return ("-ve = " + classAttribute.value(0) + ", +ve = " + classAttribute.value(1));
    }
    public String numOfBoostingIterationsTipText() {
        return "Sets the number of boosting iterations to perform. You will need to manually " + "tune this parameter to suit the dataset and the desired complexity/accuracy " + "tradeoff. More boosting iterations will result in larger (potentially more " + " accurate) trees, but will make learning slower. Each iteration will add 3 nodes " + "(1 split + 2 prediction) to the tree unless merging occurs.";
    }
    public int getNumOfBoostingIterations() {
        return m_boostingIterations;
    }
    public void setNumOfBoostingIterations(int b) {
        m_boostingIterations = b;
    }
    public String searchPathTipText() {
        return "Sets the type of search to perform when building the tree. The default option" + " (Expand all paths) will do an exhaustive search. The other search methods are" + " heuristic, so they are not guaranteed to find an optimal solution but they are" + " much faster. Expand the heaviest path: searches the path with the most heavily" + " weighted instances. Expand the best z-pure path: searches the path determined" + " by the best z-pure estimate. Expand a random path: the fastest method, simply" + " searches down a single random path on each iteration.";
    }
    public SelectedTag getSearchPath() {
        return new SelectedTag(m_searchPath, TAGS_SEARCHPATH);
    }
    public void setSearchPath(SelectedTag newMethod) {
        if (newMethod.getTags() == TAGS_SEARCHPATH) {
            m_searchPath = newMethod.getSelectedTag().getID();
        }
    }
    public String randomSeedTipText() {
        return "Sets the random seed to use for a random search.";
    }
    public int getRandomSeed() {
        return m_randomSeed;
    }
    public void setRandomSeed(int seed) {
        m_randomSeed = seed;
    }
    public String saveInstanceDataTipText() {
        return "Sets whether the tree is to save instance data - the model will take up more" + " memory if it does. If enabled you will be able to visualize the instances at" + " the prediction nodes when visualizing the tree.";
    }
    public boolean getSaveInstanceData() {
        return m_saveInstanceData;
    }
    public void setSaveInstanceData(boolean v) {
        m_saveInstanceData = v;
    }
    public Enumeration listOptions() {
        Vector newVector = new Vector(3);
        newVector.addElement(new Option("\tNumber of boosting iterations.\n" + "\t(Default = 10)", "B", 1, "-B <number of boosting iterations>"));
        newVector.addElement(new Option("\tExpand nodes: -3(all), -2(weight), -1(z_pure), " + ">=0 seed for random walk\n" + "\t(Default = -3)", "E", 1, "-E <-3|-2|-1|>=0>"));
        newVector.addElement(new Option("\tSave the instance data with the model", "D", 0, "-D"));
        return newVector.elements();
    }
    public void setOptions(String[] options) throws Exception {
        String bString = Utils.getOption('B', options);
        if (bString.length() != 0) setNumOfBoostingIterations(Integer.parseInt(bString));
        String eString = Utils.getOption('E', options);
        if (eString.length() != 0) {
            int value = Integer.parseInt(eString);
            if (value >= 0) {
                setSearchPath(new SelectedTag(SEARCHPATH_RANDOM, TAGS_SEARCHPATH));
                setRandomSeed(value);
            } else setSearchPath(new SelectedTag(value + 3, TAGS_SEARCHPATH));
        }
        setSaveInstanceData(Utils.getFlag('D', options));
        Utils.checkForRemainingOptions(options);
    }
    public String[] getOptions() {
        String[] options = new String[6];
        int current = 0;
        options[current++] = "-B";
        options[current++] = "" + getNumOfBoostingIterations();
        options[current++] = "-E";
        options[current++] = "" + (m_searchPath == SEARCHPATH_RANDOM ? m_randomSeed : m_searchPath - 3);
        if (getSaveInstanceData()) options[current++] = "-D";
        while (current < options.length) options[current++] = "";
        return options;
    }
    public double measureTreeSize() {
        return numOfAllNodes(m_root);
    }
    public double measureNumLeaves() {
        return numOfPredictionNodes(m_root);
    }
    public double measureNumPredictionLeaves() {
        return numOfPredictionLeafNodes(m_root);
    }
    public double measureNodesExpanded() {
        return m_nodesExpanded;
    }
    public double measureExamplesProcessed() {
        return m_examplesCounted;
    }
    public Enumeration enumerateMeasures() {
        Vector newVector = new Vector(4);
        newVector.addElement("measureTreeSize");
        newVector.addElement("measureNumLeaves");
        newVector.addElement("measureNumPredictionLeaves");
        newVector.addElement("measureNodesExpanded");
        newVector.addElement("measureExamplesProcessed");
        return newVector.elements();
    }
    public double getMeasure(String additionalMeasureName) {
        if (additionalMeasureName.equalsIgnoreCase("measureTreeSize")) {
            return measureTreeSize();
        } else if (additionalMeasureName.equalsIgnoreCase("measureNumLeaves")) {
            return measureNumLeaves();
        } else if (additionalMeasureName.equalsIgnoreCase("measureNumPredictionLeaves")) {
            return measureNumPredictionLeaves();
        } else if (additionalMeasureName.equalsIgnoreCase("measureNodesExpanded")) {
            return measureNodesExpanded();
        } else if (additionalMeasureName.equalsIgnoreCase("measureExamplesProcessed")) {
            return measureExamplesProcessed();
        } else {
            throw new IllegalArgumentException(additionalMeasureName + " not supported (ADTree)");
        }
    }
    protected int numOfAllNodes(PredictionNode root) {
        int numSoFar = 0;
        if (root != null) {
            numSoFar++;
            for (Enumeration e = root.children(); e.hasMoreElements(); ) {
                numSoFar++;
                Splitter split = (Splitter) e.nextElement();
                for (int i = 0; i < split.getNumOfBranches(); i++) numSoFar += numOfAllNodes(split.getChildForBranch(i));
            }
        }
        return numSoFar;
    }
    protected int numOfPredictionNodes(PredictionNode root) {
        int numSoFar = 0;
        if (root != null) {
            numSoFar++;
            for (Enumeration e = root.children(); e.hasMoreElements(); ) {
                Splitter split = (Splitter) e.nextElement();
                for (int i = 0; i < split.getNumOfBranches(); i++) numSoFar += numOfPredictionNodes(split.getChildForBranch(i));
            }
        }
        return numSoFar;
    }
    protected int numOfPredictionLeafNodes(PredictionNode root) {
        int numSoFar = 0;
        if (root.getChildren().size() > 0) {
            for (Enumeration e = root.children(); e.hasMoreElements(); ) {
                Splitter split = (Splitter) e.nextElement();
                for (int i = 0; i < split.getNumOfBranches(); i++) numSoFar += numOfPredictionLeafNodes(split.getChildForBranch(i));
            }
        } else numSoFar = 1;
        return numSoFar;
    }
    protected int getRandom(int max) {
        return m_random.nextInt(max);
    }
    public int nextSplitAddedOrder() {
        return ++m_lastAddedSplitNum;
    }
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.DATE_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        result.enable(Capability.BINARY_CLASS);
        result.enable(Capability.MISSING_CLASS_VALUES);
        return result;
    }
    public void buildClassifier(Instances instances) throws Exception {
        getCapabilities().testWithFail(instances);
        instances = new Instances(instances);
        instances.deleteWithMissingClass();
        initClassifier(instances);
        for (int T = 0; T < m_boostingIterations; T++) boost();
        if (!m_saveInstanceData) done();
    }
    public void done() {
        m_trainInstances = new Instances(m_trainInstances, 0);
        m_random = null;
        m_numericAttIndices = null;
        m_nominalAttIndices = null;
        m_posTrainInstances = null;
        m_negTrainInstances = null;
    }
    public Object clone() {
        ADTree clone = new ADTree();
        if (m_root != null) {
            clone.m_root = (PredictionNode) m_root.clone();
            clone.m_trainInstances = new Instances(m_trainInstances);
            if (m_random != null) {
                SerializedObject randomSerial = null;
                try {
                    randomSerial = new SerializedObject(m_random);
                } catch (Exception ignored) {
                }
                clone.m_random = (Random) randomSerial.getObject();
            }
            clone.m_lastAddedSplitNum = m_lastAddedSplitNum;
            clone.m_numericAttIndices = m_numericAttIndices;
            clone.m_nominalAttIndices = m_nominalAttIndices;
            clone.m_trainTotalWeight = m_trainTotalWeight;
            if (m_posTrainInstances != null) {
                clone.m_posTrainInstances = new ReferenceInstances(m_trainInstances, m_posTrainInstances.numInstances());
                clone.m_negTrainInstances = new ReferenceInstances(m_trainInstances, m_negTrainInstances.numInstances());
                for (Enumeration e = clone.m_trainInstances.enumerateInstances(); e.hasMoreElements(); ) {
                    Instance inst = (Instance) e.nextElement();
                    try {
                        if ((int) inst.classValue() == 0) clone.m_negTrainInstances.addReference(inst); else clone.m_posTrainInstances.addReference(inst);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        clone.m_nodesExpanded = m_nodesExpanded;
        clone.m_examplesCounted = m_examplesCounted;
        clone.m_boostingIterations = m_boostingIterations;
        clone.m_searchPath = m_searchPath;
        clone.m_randomSeed = m_randomSeed;
        return clone;
    }
    public void merge(ADTree mergeWith) throws Exception {
        if (m_root == null || mergeWith.m_root == null) throw new Exception("Trying to merge an uninitialized tree");
        m_root.merge(mergeWith.m_root, this);
    }
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 1.8 $");
    }
    public static void main(String[] argv) {
        runClassifier(new ADTree(), argv);
    }
}
