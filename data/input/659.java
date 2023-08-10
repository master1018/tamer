@RefClass(BDoc.class)
public class TestBDocBehaviour {
    @Test
    public void shouldAddScenariosSpecifiedInATestMethodBlock() {
        givenAnEmptyBdoc();
        whenABehaviourTestClassIsAddedWithAScenarioDescribedInATestMethodBlock();
        thenEnsureThatTheScenarioIsExtracted();
    }
    private BDoc doc;
    private ClassBehaviour behaviour;
    void givenAnEmptyBdoc() {
        doc = new BDoc();
    }
    void whenABehaviourTestClassIsAddedWithAScenarioDescribedInATestMethodBlock() {
        doc.addBehaviourFrom(new TestClass(TestBDocBehaviour.class), BConst.SRC_TEST_JAVA);
        behaviour = doc.classBehaviourInModuleBehaviour(TestBDocBehaviour.class);
    }
    void thenEnsureThatTheScenarioIsExtracted() {
        List<Scenario.Part> expectedScenarioParts = new ArrayList<Scenario.Part>();
        expectedScenarioParts.add(new Scenario.Part("givenAnEmptyBdoc"));
        expectedScenarioParts.add(new Scenario.Part("whenABehaviourTestClassIsAddedWithAScenarioDescribedInATestMethodBlock"));
        expectedScenarioParts.add(new Scenario.Part("thenEnsureThatTheScenarioIsExtracted"));
        Specification specification = from(behaviour.getSpecifications()).equalTo(new Specification("shouldAddScenariosSpecifiedInATestMethodBlock"));
        assertFalse(specification.getScenarios().isEmpty());
        assertTrue(specification.getScenarios().contains(new Scenario(expectedScenarioParts)));
    }
}
