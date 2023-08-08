package weka.classifiers.lazy;
import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;
import junit.framework.Test;
import junit.framework.TestSuite;
public class IBkTest extends AbstractClassifierTest {
    public IBkTest(String name) {
        super(name);
    }
    public Classifier getClassifier() {
        return new IBk();
    }
    public static Test suite() {
        return new TestSuite(IBkTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
