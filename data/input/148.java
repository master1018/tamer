public class Type1DiabetesRisks extends RiskChecker {
    public Type1DiabetesRisks(DAOFactory factory, long patientID) throws DBException, NoHealthRecordsException {
        super(factory, patientID);
    }
    @Override
    public boolean qualifiesForDisease() {
        return patient.getAge() < 12;
    }
    @Override
    protected List<PatientRiskFactor> getDiseaseRiskFactors() {
        List<PatientRiskFactor> factors = new ArrayList<PatientRiskFactor>();
        factors.add(new EthnicityFactor(patient, Ethnicity.Caucasian));
        factors.add(new FamilyHistoryFactor(factory, patient.getMID(), 250.0, 251.0));
        factors.add(new ChildhoodInfectionFactor(factory, patient.getMID(), 79.30));
        return factors;
    }
    @Override
    public String getName() {
        return "Type 1 Diabetes";
    }
}
