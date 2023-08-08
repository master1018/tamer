public class StandardAllergyListController extends PatientFormRecordListController {
    @Override
    public boolean clientReceiveExtendedMessage(ISEvent event, Object a) {
        switch(event) {
            case EXECUTEACTION:
                BaseAction action = (BaseAction) a;
                if (action.hasContext(acm.getContextRefId())) {
                    switch(action.getActionReference()) {
                        case SYSTEMUPDATECALCULATION:
                            updateAllergyReviewList(action);
                    }
                }
        }
        return false;
    }
    public void updateAllergyReviewList(BaseAction action) {
        try {
            FormModel allergyForm = ClinicalServer.getActiveListForm(form.getPatientId(), 0L, StandardFormTypeReference.StandardActiveListPatientAllergies);
            if (allergyForm.isNotNew()) {
                RecordItemModel reviewBy = ClinicalServer.getRecordItemForRecordItemRefId(StandardRecordItemReference.StandardAllergyLastReviewedBy);
                FormRecordModel reviewRec = reviewBy.createFormRecord();
                reviewRec.setValueRef(State.getUser().getUserRef());
                allergyForm.giveFormRecord(reviewBy.getRecordItemRef().getId()).copyAllFrom(reviewRec);
                RecordItemModel reviewDate = ClinicalServer.getRecordItemForRecordItemRefId(StandardRecordItemReference.StandardAllergyLastReviewDate);
                FormRecordModel reviewDateRec = reviewDate.createFormRecord();
                reviewDateRec.setValueDate(DateTimeModel.getNow());
                allergyForm.giveFormRecord(reviewDate.getRecordItemRef().getId()).copyAllFrom(reviewDateRec);
                ClinicalServer.store(allergyForm);
                ISToolbarButton button = (ISToolbarButton) action.getSource();
                button.setText("Reviewed " + DateTimeModel.getNow().toString(DateTimeModel.getDefaultTimeAMPMFormat()));
                button.setEnabled(false);
            } else {
                PromptsController.warning("You must enter allergies or No Known Allergies", "Allergies not found");
            }
        } catch (Exception ex) {
            Log.exception(ex);
        }
    }
    public void removeActivelistRecord(Component frameOrDialog, final BaseAction action, final PatientModel patient) throws Exception {
        if (selectedRecords.size() > 0) {
            String reason = PromptsController.getInput(frameOrDialog, "Enter the reason for removing the allergy from this profile");
            if (Converter.isNotEmpty(reason)) {
                ApplicationControlModel acm = ServiceUtility.getApplicationControl(action);
                ServiceUtility.removeActivelistRecords(acm, frameOrDialog, selectedRecords, action, patient);
            }
        }
    }
}
