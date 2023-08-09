public class StkResponseMessage {
        CommandDetails cmdDet = null;
        ResultCode resCode  = ResultCode.OK;
        int usersMenuSelection = 0;
        String usersInput  = null;
        boolean usersYesNoSelection = false;
        boolean usersConfirm = false;
        public StkResponseMessage(StkCmdMessage cmdMsg) {
            this.cmdDet = cmdMsg.mCmdDet;
        }
        public void setResultCode(ResultCode resCode) {
            this.resCode = resCode;
        }
        public void setMenuSelection(int selection) {
            this.usersMenuSelection = selection;
        }
        public void setInput(String input) {
            this.usersInput = input;
        }
        public void setYesNo(boolean yesNo) {
            usersYesNoSelection = yesNo;
        }
        public void setConfirmation(boolean confirm) {
            usersConfirm = confirm;
        }
        CommandDetails getCmdDetails() {
            return cmdDet;
        }
    }