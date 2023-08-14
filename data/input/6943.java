public class SolveCubicTest {
    public static void main(String[] args) throws Exception {
        double[] eqn = {0, 0, 1, 1};
        int numRoots = solveCubic(eqn, eqn);
        if (numRoots < 2) {
            throw new Exception("There are 2 roots. Only " + numRoots + " were found.");
        }
    }
}
