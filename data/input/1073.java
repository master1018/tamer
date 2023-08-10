public class AeWSBPELExtensionActivityValidator extends AeExtensionActivityValidator {
    public AeWSBPELExtensionActivityValidator(AeExtensionActivityDef aDef) {
        super(aDef);
    }
    public void validate() {
        AeActivityDef activityDef = getDef().getActivityDef();
        if (activityDef != null) {
            IAeExtensionActivityDef extActivity = (IAeExtensionActivityDef) activityDef;
            AeExtensionValidator extensionValidator = findExtensionValidator(extActivity.getNamespace());
            processExtensionValidator(extensionValidator, extActivity.isUnderstood(), extActivity.getNamespace());
        }
        super.validate();
    }
}
