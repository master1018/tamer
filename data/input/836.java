public class RequestExecutionStatusXMLConvertor {
    private static final String COPYRIGHT_NOTICE = "Copyright (c) The University of Edinburgh, 2007.";
    public static RequestExecutionStatus convert(Text text) {
        RequestExecutionStatus result;
        String status = text.getNodeValue();
        result = getExecutionStatus(status);
        return result;
    }
    public static RequestExecutionStatus getExecutionStatus(String status) {
        if (RequestExecutionStatus.COMPLETED.getLocalPart().equals(status)) {
            return RequestExecutionStatus.COMPLETED;
        }
        if (RequestExecutionStatus.COMPLETED_WITH_ERROR.getLocalPart().equals(status)) {
            return RequestExecutionStatus.COMPLETED_WITH_ERROR;
        }
        if (RequestExecutionStatus.ERROR.getLocalPart().equals(status)) {
            return RequestExecutionStatus.ERROR;
        }
        if (RequestExecutionStatus.PROCESSING.getLocalPart().equals(status)) {
            return RequestExecutionStatus.PROCESSING;
        }
        if (RequestExecutionStatus.PROCESSING_WITH_ERROR.getLocalPart().equals(status)) {
            return RequestExecutionStatus.PROCESSING_WITH_ERROR;
        }
        if (RequestExecutionStatus.TERMINATED.getLocalPart().equals(status)) {
            return RequestExecutionStatus.TERMINATED;
        }
        if (RequestExecutionStatus.UNSTARTED.getLocalPart().equals(status)) {
            return RequestExecutionStatus.UNSTARTED;
        }
        throw new IllegalArgumentException("Unsupported request execution status!");
    }
}
