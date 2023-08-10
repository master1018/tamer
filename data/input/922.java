public class SubmitAssociationCommandValidator extends MetadataUpdateCommandValidator {
    private enum RegistryObjectType {
        DOCUMENT, FOLDER
    }
    ;
    private static final String[] validDocumentToDocumentAssocTypes = { MetadataSupport.xdsB_ihe_assoc_type_rplc, MetadataSupport.xdsB_ihe_assoc_type_xfrm, MetadataSupport.xdsB_ihe_assoc_type_apnd, MetadataSupport.xdsB_ihe_assoc_type_xfrm_rplc, MetadataSupport.xdsB_ihe_assoc_type_signs };
    private RegistryObjectType sourceRegistryObjectType;
    private RegistryObjectType targetRegistryObjectType;
    private OMElement sourceRegistryObject;
    private OMElement targetRegistryObject;
    private Metadata sourceRegistryObjectMetadata;
    private Metadata targetRegistryObjectMetadata;
    public SubmitAssociationCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }
    public boolean validate() throws XdsException {
        SubmitAssociationCommand cmd = (SubmitAssociationCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(metadataUpdateContext.getRegistryResponse(), logMessage, metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(true);
        Metadata submittedMetadata = cmd.getMetadata();
        OMElement submittedAssoc = cmd.getTargetObject();
        String sourceObjectId = submittedMetadata.getAssocSource(submittedAssoc);
        String targetObjectId = submittedMetadata.getAssocTarget(submittedAssoc);
        this.validateRegistryObjectIds(sourceObjectId, targetObjectId);
        this.queryMetadata(muSQ, sourceObjectId, targetObjectId);
        this.validateRegistryObjectsApprovedStatus();
        this.validateRegistryObjectPatientIds();
        this.validateAssociationType(submittedAssoc);
        this.validateSubmittedAssocDoesNotExist(muSQ, submittedMetadata, submittedAssoc, sourceObjectId, targetObjectId);
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        rov.validateMetadataStructure(submittedMetadata, true, registryResponse.registryErrorList);
        if (registryResponse.has_errors()) {
            validationSuccess = false;
        } else {
            rov.validateSubmissionSetUniqueIds(submittedMetadata);
            rov.validatePatientId(submittedMetadata, configActor);
        }
        return validationSuccess;
    }
    private void queryMetadata(MetadataUpdateStoredQuerySupport muSQ, String sourceObjectId, String targetObjectId) throws XdsException {
        sourceRegistryObjectType = RegistryObjectType.DOCUMENT;
        sourceRegistryObjectMetadata = this.getDocumentMetadata(muSQ, sourceObjectId);
        if (sourceRegistryObjectMetadata == null) {
            sourceRegistryObjectType = RegistryObjectType.FOLDER;
            sourceRegistryObjectMetadata = this.getFolderMetadata(muSQ, sourceObjectId);
        }
        if (sourceRegistryObjectMetadata == null) {
            throw new XdsException("Can not find source registry object (document or folder) for UUID = " + sourceObjectId);
        }
        targetRegistryObjectType = RegistryObjectType.DOCUMENT;
        targetRegistryObjectMetadata = this.getDocumentMetadata(muSQ, targetObjectId);
        if (targetRegistryObjectMetadata == null) {
            targetRegistryObjectType = RegistryObjectType.FOLDER;
            targetRegistryObjectMetadata = this.getFolderMetadata(muSQ, targetObjectId);
        }
        if (targetRegistryObjectMetadata == null) {
            throw new XdsException("Can not find target registry object (document or folder) for UUID = " + targetObjectId);
        }
        if (targetRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            throw new XdsException("Target registry object is a folder and is not allowed.  Folder UUID = " + targetObjectId);
        }
        if (sourceRegistryObjectType.equals(RegistryObjectType.DOCUMENT)) {
            sourceRegistryObject = sourceRegistryObjectMetadata.getExtrinsicObject(0);
        } else {
            sourceRegistryObject = sourceRegistryObjectMetadata.getFolder(0);
        }
        if (targetRegistryObjectType.equals(RegistryObjectType.DOCUMENT)) {
            targetRegistryObject = targetRegistryObjectMetadata.getExtrinsicObject(0);
        } else {
            targetRegistryObject = targetRegistryObjectMetadata.getFolder(0);
        }
    }
    private void validateRegistryObjectIds(String sourceObjectId, String targetObjectId) throws XdsException {
        if (!MetadataUpdateHelper.isUUID(sourceObjectId)) {
            throw new XdsException("Source registry object is not in UUID format");
        }
        if (!MetadataUpdateHelper.isUUID(targetObjectId)) {
            throw new XdsException("Target registry object is not in UUID format");
        }
        if (sourceObjectId.equals(targetObjectId)) {
            throw new XdsException("Source and target registry objects can not be the same");
        }
    }
    private void validateRegistryObjectPatientIds() throws XdsException {
        String sourcePatientId = sourceRegistryObjectMetadata.getPatientId(sourceRegistryObject);
        String targetPatientId = targetRegistryObjectMetadata.getPatientId(targetRegistryObject);
        if (!sourcePatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Source registry object's patient identifier = " + sourcePatientId + " does not match target registry object's patient identifier = " + targetPatientId);
        }
    }
    private void validateRegistryObjectsApprovedStatus() throws XdsException {
        String sourceStatus = sourceRegistryObjectMetadata.getStatus(sourceRegistryObject);
        if (!sourceStatus.equals(MetadataSupport.status_type_approved)) {
            throw new XdsException("Source registry object found but status is not approved.  Status in registry = " + sourceStatus);
        }
        String targetStatus = targetRegistryObjectMetadata.getStatus(targetRegistryObject);
        if (!targetStatus.equals(MetadataSupport.status_type_approved)) {
            throw new XdsException("Target registry object found but status is not approved.  Status in registry = " + targetStatus);
        }
    }
    private void validateAssociationType(OMElement submittedAssoc) throws XdsException {
        Metadata m = this.getMetadataUpdateCommand().getMetadata();
        String assocType = m.getAssocType(submittedAssoc);
        if (sourceRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                throw new XdsException("Source registry object is a folder and only valid assocation type = " + MetadataSupport.xdsB_eb_assoc_type_has_member);
            }
        } else {
            boolean foundValidAssocType = false;
            for (String validDocumentToDocumentAssocType : validDocumentToDocumentAssocTypes) {
                if (assocType.equals(validDocumentToDocumentAssocType)) {
                    foundValidAssocType = true;
                    break;
                }
            }
            if (!foundValidAssocType) {
                throw new XdsException("Source registry object is a document and valid assocation type not provided.");
            }
        }
    }
    private void validateSubmittedAssocDoesNotExist(MetadataUpdateStoredQuerySupport muSQ, Metadata submittedMetadata, OMElement submittedAssoc, String sourceObjectId, String targetObjectId) throws XdsException {
        String assocType = submittedMetadata.getAssocType(submittedAssoc);
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(sourceObjectId);
        sourceOrTargetIds.add(targetObjectId);
        List<String> assocTypes = new ArrayList<String>();
        assocTypes.add(assocType);
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, null, assocTypes);
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        if (assocMetadata.getAssociationIds().size() > 0) {
            throw new XdsException("Registry already has an association between the source and target registry objects of type = " + assocType);
        }
    }
    private Metadata getDocumentMetadata(MetadataUpdateStoredQuerySupport muSQ, String registryObjectId) throws XdsException {
        OMElement queryResult = muSQ.getDocumentByUUID(registryObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getExtrinsicObjects().isEmpty()) {
            return m;
        }
        return null;
    }
    private Metadata getFolderMetadata(MetadataUpdateStoredQuerySupport muSQ, String registryObjectId) throws XdsException {
        OMElement queryResult = muSQ.getFolderByUUID(registryObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getFolders().isEmpty()) {
            return m;
        }
        return null;
    }
}
