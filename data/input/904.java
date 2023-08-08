public class TestLinearSolverCholBlock64 {
    @Test
    public void standardTests() {
        LinearSolverCholBlock64 solver = new LinearSolverCholBlock64();
        BaseCholeskySolveTests tests = new BaseCholeskySolveTests();
        tests.standardTests(solver);
    }
}
