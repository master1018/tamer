final class Alerts {
    static final byte           alert_warning = 1;
    static final byte           alert_fatal = 2;
    static final byte           alert_close_notify = 0;
    static final byte           alert_unexpected_message = 10;
    static final byte           alert_bad_record_mac = 20;
    static final byte           alert_decryption_failed = 21;
    static final byte           alert_record_overflow = 22;
    static final byte           alert_decompression_failure = 30;
    static final byte           alert_handshake_failure = 40;
    static final byte           alert_no_certificate = 41;
    static final byte           alert_bad_certificate = 42;
    static final byte           alert_unsupported_certificate = 43;
    static final byte           alert_certificate_revoked = 44;
    static final byte           alert_certificate_expired = 45;
    static final byte           alert_certificate_unknown = 46;
    static final byte           alert_illegal_parameter = 47;
    static final byte           alert_unknown_ca = 48;
    static final byte           alert_access_denied = 49;
    static final byte           alert_decode_error = 50;
    static final byte           alert_decrypt_error = 51;
    static final byte           alert_export_restriction = 60;
    static final byte           alert_protocol_version = 70;
    static final byte           alert_insufficient_security = 71;
    static final byte           alert_internal_error = 80;
    static final byte           alert_user_canceled = 90;
    static final byte           alert_no_renegotiation = 100;
    static final byte           alert_unsupported_extension = 110;
    static final byte           alert_certificate_unobtainable = 111;
    static final byte           alert_unrecognized_name = 112;
    static final byte           alert_bad_certificate_status_response = 113;
    static final byte           alert_bad_certificate_hash_value = 114;
    static String alertDescription(byte code) {
        switch (code) {
        case alert_close_notify:
            return "close_notify";
        case alert_unexpected_message:
            return "unexpected_message";
        case alert_bad_record_mac:
            return "bad_record_mac";
        case alert_decryption_failed:
            return "decryption_failed";
        case alert_record_overflow:
            return "record_overflow";
        case alert_decompression_failure:
            return "decompression_failure";
        case alert_handshake_failure:
            return "handshake_failure";
        case alert_no_certificate:
            return "no_certificate";
        case alert_bad_certificate:
            return "bad_certificate";
        case alert_unsupported_certificate:
            return "unsupported_certificate";
        case alert_certificate_revoked:
            return "certificate_revoked";
        case alert_certificate_expired:
            return "certificate_expired";
        case alert_certificate_unknown:
            return "certificate_unknown";
        case alert_illegal_parameter:
            return "illegal_parameter";
        case alert_unknown_ca:
            return "unknown_ca";
        case alert_access_denied:
            return "access_denied";
        case alert_decode_error:
            return "decode_error";
        case alert_decrypt_error:
            return "decrypt_error";
        case alert_export_restriction:
            return "export_restriction";
        case alert_protocol_version:
            return "protocol_version";
        case alert_insufficient_security:
            return "insufficient_security";
        case alert_internal_error:
            return "internal_error";
        case alert_user_canceled:
            return "user_canceled";
        case alert_no_renegotiation:
            return "no_renegotiation";
        case alert_unsupported_extension:
            return "unsupported_extension";
        case alert_certificate_unobtainable:
            return "certificate_unobtainable";
        case alert_unrecognized_name:
            return "unrecognized_name";
        case alert_bad_certificate_status_response:
            return "bad_certificate_status_response";
        case alert_bad_certificate_hash_value:
            return "bad_certificate_hash_value";
        default:
            return "<UNKNOWN ALERT: " + (code & 0x0ff) + ">";
        }
    }
    static SSLException getSSLException(byte description, String reason) {
        return getSSLException(description, null, reason);
    }
    static SSLException getSSLException(byte description, Throwable cause,
            String reason) {
        SSLException e;
        if (reason == null) {
            if (cause != null) {
                reason = cause.toString();
            } else {
                reason = "";
            }
        }
        switch (description) {
        case alert_handshake_failure:
        case alert_no_certificate:
        case alert_bad_certificate:
        case alert_unsupported_certificate:
        case alert_certificate_revoked:
        case alert_certificate_expired:
        case alert_certificate_unknown:
        case alert_unknown_ca:
        case alert_access_denied:
        case alert_decrypt_error:
        case alert_export_restriction:
        case alert_insufficient_security:
        case alert_unsupported_extension:
        case alert_certificate_unobtainable:
        case alert_unrecognized_name:
        case alert_bad_certificate_status_response:
        case alert_bad_certificate_hash_value:
            e = new SSLHandshakeException(reason);
            break;
        case alert_close_notify:
        case alert_unexpected_message:
        case alert_bad_record_mac:
        case alert_decryption_failed:
        case alert_record_overflow:
        case alert_decompression_failure:
        case alert_illegal_parameter:
        case alert_decode_error:
        case alert_protocol_version:
        case alert_internal_error:
        case alert_user_canceled:
        case alert_no_renegotiation:
        default:
            e = new SSLException(reason);
            break;
        }
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }
}
