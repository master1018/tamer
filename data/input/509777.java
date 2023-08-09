public class IccCardApplication {
    public enum AppType{
        APPTYPE_UNKNOWN,
        APPTYPE_SIM,
        APPTYPE_USIM,
        APPTYPE_RUIM,
        APPTYPE_CSIM
    };
    public enum AppState{
        APPSTATE_UNKNOWN,
        APPSTATE_DETECTED,
        APPSTATE_PIN,
        APPSTATE_PUK,
        APPSTATE_SUBSCRIPTION_PERSO,
        APPSTATE_READY;
        boolean isPinRequired() {
            return this == APPSTATE_PIN;
        }
        boolean isPukRequired() {
            return this == APPSTATE_PUK;
        }
        boolean isSubscriptionPersoEnabled() {
            return this == APPSTATE_SUBSCRIPTION_PERSO;
        }
        boolean isAppReady() {
            return this == APPSTATE_READY;
        }
        boolean isAppNotReady() {
            return this == APPSTATE_UNKNOWN  ||
                   this == APPSTATE_DETECTED;
        }
    };
    public enum PersoSubState{
        PERSOSUBSTATE_UNKNOWN,
        PERSOSUBSTATE_IN_PROGRESS,
        PERSOSUBSTATE_READY,
        PERSOSUBSTATE_SIM_NETWORK,
        PERSOSUBSTATE_SIM_NETWORK_SUBSET,
        PERSOSUBSTATE_SIM_CORPORATE,
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER,
        PERSOSUBSTATE_SIM_SIM,
        PERSOSUBSTATE_SIM_NETWORK_PUK,
        PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK,
        PERSOSUBSTATE_SIM_CORPORATE_PUK,
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK,
        PERSOSUBSTATE_SIM_SIM_PUK,
        PERSOSUBSTATE_RUIM_NETWORK1,
        PERSOSUBSTATE_RUIM_NETWORK2,
        PERSOSUBSTATE_RUIM_HRPD,
        PERSOSUBSTATE_RUIM_CORPORATE,
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER,
        PERSOSUBSTATE_RUIM_RUIM,
        PERSOSUBSTATE_RUIM_NETWORK1_PUK,
        PERSOSUBSTATE_RUIM_NETWORK2_PUK,
        PERSOSUBSTATE_RUIM_HRPD_PUK,
        PERSOSUBSTATE_RUIM_CORPORATE_PUK,
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK,
        PERSOSUBSTATE_RUIM_RUIM_PUK;
        boolean isPersoSubStateUnknown() {
            return this == PERSOSUBSTATE_UNKNOWN;
        }
    };
    public AppType        app_type;
    public AppState       app_state;
    public PersoSubState  perso_substate;
    public String         aid;
    public String         app_label;
    public int            pin1_replaced;
    public int            pin1;
    public int            pin2;
    AppType AppTypeFromRILInt(int type) {
        AppType newType;
        switch(type) {
            case 0: newType = AppType.APPTYPE_UNKNOWN; break;
            case 1: newType = AppType.APPTYPE_SIM;     break;
            case 2: newType = AppType.APPTYPE_USIM;    break;
            case 3: newType = AppType.APPTYPE_RUIM;    break;
            case 4: newType = AppType.APPTYPE_CSIM;    break;
            default:
                throw new RuntimeException(
                            "Unrecognized RIL_AppType: " +type);
        }
        return newType;
    }
    AppState AppStateFromRILInt(int state) {
        AppState newState;
        switch(state) {
            case 0: newState = AppState.APPSTATE_UNKNOWN;  break;
            case 1: newState = AppState.APPSTATE_DETECTED; break;
            case 2: newState = AppState.APPSTATE_PIN; break;
            case 3: newState = AppState.APPSTATE_PUK; break;
            case 4: newState = AppState.APPSTATE_SUBSCRIPTION_PERSO; break;
            case 5: newState = AppState.APPSTATE_READY; break;
            default:
                throw new RuntimeException(
                            "Unrecognized RIL_AppState: " +state);
        }
        return newState;
    }
    PersoSubState PersoSubstateFromRILInt(int substate) {
        PersoSubState newSubState;
        switch(substate) {
            case 0:  newSubState = PersoSubState.PERSOSUBSTATE_UNKNOWN;  break;
            case 1:  newSubState = PersoSubState.PERSOSUBSTATE_IN_PROGRESS; break;
            case 2:  newSubState = PersoSubState.PERSOSUBSTATE_READY; break;
            case 3:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_NETWORK; break;
            case 4:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET; break;
            case 5:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_CORPORATE; break;
            case 6:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER; break;
            case 7:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_SIM;  break;
            case 8:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK; break;
            case 9:  newSubState = PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK; break;
            case 10: newSubState = PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK; break;
            case 11: newSubState = PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK; break;
            case 12: newSubState = PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK; break;
            case 13: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1; break;
            case 14: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2; break;
            case 15: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_HRPD; break;
            case 16: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE; break;
            case 17: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER; break;
            case 18: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_RUIM; break;
            case 19: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK; break;
            case 20: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK; break;
            case 21: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK ; break;
            case 22: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK; break;
            case 23: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK; break;
            case 24: newSubState = PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK; break;
            default:
                throw new RuntimeException(
                            "Unrecognized RIL_PersoSubstate: " +substate);
        }
        return newSubState;
    }
}
