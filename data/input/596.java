public abstract class CoverageCriterionImpl extends EObjectImpl implements CoverageCriterion {
    protected EList<TestGoal> testgoals;
    protected CoverageCriterionImpl() {
        super();
    }
    @Override
    protected EClass eStaticClass() {
        return CoveragePackage.Literals.COVERAGE_CRITERION;
    }
    public EList<TestGoal> getTestgoals() {
        if (testgoals == null) {
            testgoals = new EObjectContainmentWithInverseEList<TestGoal>(TestGoal.class, this, CoveragePackage.COVERAGE_CRITERION__TESTGOALS, CoveragePackage.TEST_GOAL__CRITERION);
        }
        return testgoals;
    }
    public boolean subsumes(CoverageCriterion cc) {
        CoverageCriterion cc1 = cc;
        CoverageCriterion cc2 = this;
        boolean result = false;
        if (cc1 == cc2) {
            result = true;
        } else {
            for (int i = 0; i < cc1.getTestgoals().size(); i++) {
                boolean first = false;
                TestGoal tg1 = cc1.getTestgoals().get(i);
                for (int j = 0; j < cc2.getTestgoals().size(); j++) {
                    TestGoal tg2 = cc2.getTestgoals().get(j);
                    first = tg1.equals(tg2);
                    if (first) {
                        result = first;
                        break;
                    }
                }
                if (!first) {
                    return false;
                }
            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch(featureID) {
            case CoveragePackage.COVERAGE_CRITERION__TESTGOALS:
                return ((InternalEList<InternalEObject>) (InternalEList<?>) getTestgoals()).basicAdd(otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch(featureID) {
            case CoveragePackage.COVERAGE_CRITERION__TESTGOALS:
                return ((InternalEList<?>) getTestgoals()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch(featureID) {
            case CoveragePackage.COVERAGE_CRITERION__TESTGOALS:
                return getTestgoals();
        }
        return super.eGet(featureID, resolve, coreType);
    }
    @Override
    public boolean eIsSet(int featureID) {
        switch(featureID) {
            case CoveragePackage.COVERAGE_CRITERION__TESTGOALS:
                return testgoals != null && !testgoals.isEmpty();
        }
        return super.eIsSet(featureID);
    }
}
