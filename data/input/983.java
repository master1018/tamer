public abstract class AbstractDynamicAtomProperty implements Feature {
    private static Category logger = Category.getInstance(AbstractDynamicAtomProperty.class.getName());
    public BasicFeatureInfo descInfo;
    public abstract Object getAtomPropertiesArray(Molecule mol);
    public BasicProperty[] acceptedProperties() {
        return null;
    }
    public FeatureResult calculate(Molecule mol) throws FeatureException {
        FeatureResult result = ResultFactory.instance().getFeatureResult(descInfo.getName());
        return calculate(mol, result, null);
    }
    public FeatureResult calculate(Molecule mol, Map properties) throws FeatureException {
        FeatureResult result = ResultFactory.instance().getFeatureResult(descInfo.getName());
        return calculate(mol, result, properties);
    }
    public FeatureResult calculate(Molecule mol, FeatureResult descResult) throws FeatureException {
        return calculate(mol, descResult, null);
    }
    public FeatureResult calculate(Molecule mol, FeatureResult descResult, Map properties) throws FeatureException {
        AtomDynamicResult result = null;
        if (!(descResult instanceof AtomDynamicResult)) {
            logger.error(descInfo.getName() + " result should be of type " + AtomDynamicResult.class.getName() + " but it's of type " + descResult.getClass().toString());
        } else {
            if (initialize(properties)) {
                result = (AtomDynamicResult) descResult;
                Object array = getAtomPropertiesArray(mol);
                if (array == null) {
                    return null;
                }
                result.setArray(array);
                result.addCMLProperty(IdentifierExpertSystem.instance().getKernelID());
                result.setDataDescription(FeatureHelper.VERSION_IDENTIFIER + " " + String.valueOf(this.hashedDependencyTreeVersion()));
            }
        }
        return result;
    }
    public void clear() {
    }
    public BasicFeatureInfo getDescInfo() {
        return descInfo;
    }
    public FeatureDescription getDescription() {
        return new BasicFeatureDescription(descInfo.getDescriptionFile());
    }
    public boolean initialize(Map properties) {
        return true;
    }
    public boolean testDescriptor() {
        return true;
    }
}
