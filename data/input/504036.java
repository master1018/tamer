@TestTargetClass(X509KeyManager.class) 
public class X509KeyManagerTest extends TestCase {
    private X509KeyManager manager;
    private KeyManagerFactory factory;
    private String keyType;
    private String client = "CLIENT";
    private String server = "SERVER";
    private String type = "RSA";
    private KeyStore keyTest;
    private X509Certificate[] cert = null;
    private PrivateKey[] keys = null;
    private String password = "1234";
    String certificate = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDPzCCAqigAwIBAgIBADANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJBTjEQ\n"
            + "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k\n"
            + "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBh\n"
            + "bmRyb2lkLmNvbTAeFw0wOTAzMjAxNzAwMDZaFw0xMjAzMTkxNzAwMDZaMHkxCzAJ\n"
            + "BgNVBAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQKEwdBbmRyb2lkMRAw\n"
            + "DgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkB\n"
            + "FhNhbmRyb2lkQGFuZHJvaWQuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n"
            + "gQCqQkDtkiEXmV8O5EK4y2Y9YyoWNDx70z4fqD+9muuzJGuM5NovMbxhBycuKHF3\n"
            + "WK60iXzrsAYkB1c8VHHbcUEFqz2fBdLKyxy/nYohlo8TYSVpEjt3vfc0sgmp4FKU\n"
            + "RDHO2z3rZPHWysV9L9ZvjeQpiwaYipU9epdBmvFmxQmCDQIDAQABo4HWMIHTMB0G\n"
            + "A1UdDgQWBBTnm32QKeqQC38IQXZOQSPoQyypAzCBowYDVR0jBIGbMIGYgBTnm32Q\n"
            + "KeqQC38IQXZOQSPoQyypA6F9pHsweTELMAkGA1UEBhMCQU4xEDAOBgNVBAgTB0Fu\n"
            + "ZHJvaWQxEDAOBgNVBAoTB0FuZHJvaWQxEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNV\n"
            + "BAMTB0FuZHJvaWQxIjAgBgkqhkiG9w0BCQEWE2FuZHJvaWRAYW5kcm9pZC5jb22C\n"
            + "AQAwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOBgQAUmDApQu+r5rglS1WF\n"
            + "BKXE3R2LasFvbBwdw2E0MAc0TWqLVW91VW4VWMX4r+C+c7rZpYXXtRqFRCuI/czL\n"
            + "0e1GaUP/Wa6bXBcm2u7Iv2dVAaAOELmFSVTZeR57Lm9lT9kQLp24kmNndIsiDW3T\n"
            + "XZ4pY/k2kxungOKx8b8pGYE9Bw==\n"
            + "-----END CERTIFICATE-----";
    ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
            .getBytes());
    byte[] keyBytes = new byte[] {
            (byte)0x30, (byte)0x82, (byte)0x02, (byte)0x77, (byte)0x02, (byte)0x01, (byte)0x00,
            (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48,
            (byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x05,
            (byte)0x00, (byte)0x04, (byte)0x82, (byte)0x02, (byte)0x61, (byte)0x30, (byte)0x82,
            (byte)0x02, (byte)0x5d, (byte)0x02, (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x81,
            (byte)0x81, (byte)0x00, (byte)0xaa, (byte)0x42, (byte)0x40, (byte)0xed, (byte)0x92,
            (byte)0x21, (byte)0x17, (byte)0x99, (byte)0x5f, (byte)0x0e, (byte)0xe4, (byte)0x42,
            (byte)0xb8, (byte)0xcb, (byte)0x66, (byte)0x3d, (byte)0x63, (byte)0x2a, (byte)0x16,
            (byte)0x34, (byte)0x3c, (byte)0x7b, (byte)0xd3, (byte)0x3e, (byte)0x1f, (byte)0xa8,
            (byte)0x3f, (byte)0xbd, (byte)0x9a, (byte)0xeb, (byte)0xb3, (byte)0x24, (byte)0x6b,
            (byte)0x8c, (byte)0xe4, (byte)0xda, (byte)0x2f, (byte)0x31, (byte)0xbc, (byte)0x61,
            (byte)0x07, (byte)0x27, (byte)0x2e, (byte)0x28, (byte)0x71, (byte)0x77, (byte)0x58,
            (byte)0xae, (byte)0xb4, (byte)0x89, (byte)0x7c, (byte)0xeb, (byte)0xb0, (byte)0x06,
            (byte)0x24, (byte)0x07, (byte)0x57, (byte)0x3c, (byte)0x54, (byte)0x71, (byte)0xdb,
            (byte)0x71, (byte)0x41, (byte)0x05, (byte)0xab, (byte)0x3d, (byte)0x9f, (byte)0x05,
            (byte)0xd2, (byte)0xca, (byte)0xcb, (byte)0x1c, (byte)0xbf, (byte)0x9d, (byte)0x8a,
            (byte)0x21, (byte)0x96, (byte)0x8f, (byte)0x13, (byte)0x61, (byte)0x25, (byte)0x69,
            (byte)0x12, (byte)0x3b, (byte)0x77, (byte)0xbd, (byte)0xf7, (byte)0x34, (byte)0xb2,
            (byte)0x09, (byte)0xa9, (byte)0xe0, (byte)0x52, (byte)0x94, (byte)0x44, (byte)0x31,
            (byte)0xce, (byte)0xdb, (byte)0x3d, (byte)0xeb, (byte)0x64, (byte)0xf1, (byte)0xd6,
            (byte)0xca, (byte)0xc5, (byte)0x7d, (byte)0x2f, (byte)0xd6, (byte)0x6f, (byte)0x8d,
            (byte)0xe4, (byte)0x29, (byte)0x8b, (byte)0x06, (byte)0x98, (byte)0x8a, (byte)0x95,
            (byte)0x3d, (byte)0x7a, (byte)0x97, (byte)0x41, (byte)0x9a, (byte)0xf1, (byte)0x66,
            (byte)0xc5, (byte)0x09, (byte)0x82, (byte)0x0d, (byte)0x02, (byte)0x03, (byte)0x01,
            (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x81, (byte)0x80, (byte)0x34, (byte)0x91,
            (byte)0x8e, (byte)0x50, (byte)0x8b, (byte)0xfc, (byte)0xf1, (byte)0xb7, (byte)0x66,
            (byte)0x35, (byte)0x47, (byte)0xdf, (byte)0x1e, (byte)0x05, (byte)0x97, (byte)0x44,
            (byte)0xbe, (byte)0xf8, (byte)0x80, (byte)0xb0, (byte)0x92, (byte)0x38, (byte)0x3d,
            (byte)0x4a, (byte)0x02, (byte)0x26, (byte)0x45, (byte)0xbf, (byte)0xfa, (byte)0x34,
            (byte)0x6a, (byte)0x34, (byte)0x85, (byte)0x8c, (byte)0x94, (byte)0x20, (byte)0x95,
            (byte)0xcf, (byte)0xca, (byte)0x75, (byte)0x3e, (byte)0xeb, (byte)0x27, (byte)0x02,
            (byte)0x4f, (byte)0xbe, (byte)0x64, (byte)0xc0, (byte)0x54, (byte)0x77, (byte)0xda,
            (byte)0xfd, (byte)0x3e, (byte)0x75, (byte)0x36, (byte)0xec, (byte)0x99, (byte)0x4f,
            (byte)0xc4, (byte)0x56, (byte)0xff, (byte)0x45, (byte)0x61, (byte)0xa8, (byte)0xa8,
            (byte)0x41, (byte)0xe4, (byte)0x42, (byte)0x71, (byte)0x7a, (byte)0x8c, (byte)0x84,
            (byte)0xc2, (byte)0x02, (byte)0x40, (byte)0x0b, (byte)0x3d, (byte)0x42, (byte)0xe0,
            (byte)0x8b, (byte)0x22, (byte)0xf7, (byte)0x4c, (byte)0xa3, (byte)0xbb, (byte)0xd8,
            (byte)0x8f, (byte)0x45, (byte)0xa2, (byte)0x55, (byte)0xc7, (byte)0xd0, (byte)0x6a,
            (byte)0x25, (byte)0xbf, (byte)0xda, (byte)0x54, (byte)0x57, (byte)0x14, (byte)0x91,
            (byte)0x0c, (byte)0x09, (byte)0x0b, (byte)0x9a, (byte)0x50, (byte)0xca, (byte)0xe6,
            (byte)0x9e, (byte)0x28, (byte)0xc3, (byte)0x78, (byte)0x39, (byte)0x10, (byte)0x06,
            (byte)0x02, (byte)0x96, (byte)0x10, (byte)0x1a, (byte)0xd2, (byte)0x4b, (byte)0x7b,
            (byte)0x6c, (byte)0x72, (byte)0x9e, (byte)0x1e, (byte)0xac, (byte)0xd2, (byte)0xc1,
            (byte)0x02, (byte)0x41, (byte)0x00, (byte)0xde, (byte)0x27, (byte)0xbd, (byte)0x43,
            (byte)0xa4, (byte)0xbd, (byte)0x95, (byte)0x14, (byte)0x2e, (byte)0x1c, (byte)0xa0,
            (byte)0x74, (byte)0xa5, (byte)0x3e, (byte)0xfa, (byte)0xf9, (byte)0x15, (byte)0xb2,
            (byte)0x29, (byte)0x6a, (byte)0x2a, (byte)0x42, (byte)0x94, (byte)0x5a, (byte)0xf2,
            (byte)0x81, (byte)0xf3, (byte)0xe1, (byte)0x76, (byte)0x49, (byte)0x11, (byte)0x9d,
            (byte)0x18, (byte)0xc5, (byte)0xeb, (byte)0xb6, (byte)0xbc, (byte)0x81, (byte)0x3a,
            (byte)0x14, (byte)0x9c, (byte)0x41, (byte)0x01, (byte)0x58, (byte)0x56, (byte)0xa9,
            (byte)0x9b, (byte)0x73, (byte)0x2f, (byte)0xd9, (byte)0xa8, (byte)0x8e, (byte)0xc4,
            (byte)0x48, (byte)0x69, (byte)0x35, (byte)0xe6, (byte)0xf4, (byte)0x73, (byte)0x2f,
            (byte)0xf9, (byte)0x12, (byte)0x12, (byte)0x71, (byte)0x02, (byte)0x41, (byte)0x00,
            (byte)0xc4, (byte)0x32, (byte)0x81, (byte)0x5d, (byte)0x19, (byte)0x54, (byte)0x2c,
            (byte)0x29, (byte)0x5a, (byte)0x9f, (byte)0x36, (byte)0x4c, (byte)0x6f, (byte)0x2d,
            (byte)0xfd, (byte)0x62, (byte)0x0e, (byte)0xe6, (byte)0x37, (byte)0xc2, (byte)0xf6,
            (byte)0x69, (byte)0x64, (byte)0xf9, (byte)0x3a, (byte)0xcc, (byte)0xb2, (byte)0x63,
            (byte)0x2f, (byte)0xa9, (byte)0xfe, (byte)0x7e, (byte)0x8b, (byte)0x2d, (byte)0x69,
            (byte)0x13, (byte)0xe5, (byte)0x61, (byte)0x58, (byte)0xb7, (byte)0xfa, (byte)0x55,
            (byte)0x74, (byte)0x2c, (byte)0xe8, (byte)0xa1, (byte)0xac, (byte)0xc3, (byte)0xdd,
            (byte)0x5b, (byte)0x62, (byte)0xae, (byte)0x0a, (byte)0x27, (byte)0xce, (byte)0xb0,
            (byte)0xf2, (byte)0x81, (byte)0x5f, (byte)0x9a, (byte)0x6f, (byte)0x5f, (byte)0x3f,
            (byte)0x5d, (byte)0x02, (byte)0x41, (byte)0x00, (byte)0x92, (byte)0x42, (byte)0xff,
            (byte)0xac, (byte)0xe5, (byte)0x6d, (byte)0x9c, (byte)0x15, (byte)0x29, (byte)0x36,
            (byte)0xd7, (byte)0xbd, (byte)0x74, (byte)0x7e, (byte)0x3e, (byte)0xa6, (byte)0x77,
            (byte)0xce, (byte)0x50, (byte)0xce, (byte)0x00, (byte)0xfc, (byte)0xcc, (byte)0xc8,
            (byte)0x04, (byte)0x19, (byte)0xe3, (byte)0x03, (byte)0x71, (byte)0xe9, (byte)0x31,
            (byte)0x9b, (byte)0x88, (byte)0x8f, (byte)0xe6, (byte)0x5c, (byte)0xed, (byte)0x46,
            (byte)0xf7, (byte)0x82, (byte)0x52, (byte)0x4d, (byte)0xca, (byte)0x20, (byte)0xeb,
            (byte)0x0d, (byte)0xc7, (byte)0xb6, (byte)0xd2, (byte)0xae, (byte)0x2e, (byte)0xf7,
            (byte)0xaf, (byte)0xeb, (byte)0x2c, (byte)0xb9, (byte)0xbc, (byte)0x50, (byte)0xfc,
            (byte)0xf5, (byte)0x7c, (byte)0xba, (byte)0x95, (byte)0x41, (byte)0x02, (byte)0x40,
            (byte)0x54, (byte)0xf8, (byte)0x46, (byte)0x9c, (byte)0x6a, (byte)0x5e, (byte)0xd0,
            (byte)0xed, (byte)0x6c, (byte)0x08, (byte)0xed, (byte)0xfc, (byte)0x36, (byte)0x5e,
            (byte)0x65, (byte)0x91, (byte)0x75, (byte)0x40, (byte)0x71, (byte)0x3f, (byte)0xe7,
            (byte)0x76, (byte)0x07, (byte)0xbc, (byte)0x04, (byte)0xa2, (byte)0x28, (byte)0x53,
            (byte)0xda, (byte)0x8d, (byte)0xb5, (byte)0xe1, (byte)0x5a, (byte)0x27, (byte)0x65,
            (byte)0x8d, (byte)0xaf, (byte)0x56, (byte)0xf4, (byte)0x94, (byte)0x61, (byte)0x3f,
            (byte)0x67, (byte)0x1c, (byte)0x17, (byte)0xf8, (byte)0x05, (byte)0x19, (byte)0xa2,
            (byte)0xa1, (byte)0x74, (byte)0x60, (byte)0x49, (byte)0x97, (byte)0xa9, (byte)0xe5,
            (byte)0x6a, (byte)0x71, (byte)0x6b, (byte)0x55, (byte)0x38, (byte)0x0c, (byte)0xb9,
            (byte)0x25, (byte)0x02, (byte)0x41, (byte)0x00, (byte)0xae, (byte)0xf2, (byte)0xa8,
            (byte)0x6d, (byte)0x1d, (byte)0x35, (byte)0x38, (byte)0x73, (byte)0x98, (byte)0x15,
            (byte)0xc7, (byte)0x15, (byte)0x02, (byte)0x2f, (byte)0x29, (byte)0x5d, (byte)0x18,
            (byte)0x4b, (byte)0x7d, (byte)0xb2, (byte)0x59, (byte)0xbe, (byte)0x5a, (byte)0xc7,
            (byte)0x72, (byte)0xd0, (byte)0x80, (byte)0xd8, (byte)0x77, (byte)0xa1, (byte)0x7f,
            (byte)0xb2, (byte)0x35, (byte)0x0d, (byte)0x78, (byte)0x92, (byte)0x91, (byte)0x35,
            (byte)0x47, (byte)0xeb, (byte)0x4b, (byte)0x00, (byte)0x59, (byte)0xb4, (byte)0xc4,
            (byte)0x2c, (byte)0x29, (byte)0xe7, (byte)0x39, (byte)0x9d, (byte)0x48, (byte)0x8b,
            (byte)0x4f, (byte)0x46, (byte)0xe6, (byte)0xce, (byte)0xd3, (byte)0x6c, (byte)0x84,
            (byte)0x9b, (byte)0xd2, (byte)0x10, (byte)0xb0, (byte)0xe1
    };
    String certificate2 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIC9jCCAl+gAwIBAgIBATANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJBTjEQ\n"
            + "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k\n"
            + "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBh\n"
            + "bmRyb2lkLmNvbTAeFw0wOTAzMjAxNzAwNDBaFw0xMDAzMjAxNzAwNDBaMIGLMQsw\n"
            + "CQYDVQQGEwJBTjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEBxMHQW5kcm9pZDEQ\n"
            + "MA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5k\n"
            + "cm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCBnzANBgkq\n"
            + "hkiG9w0BAQEFAAOBjQAwgYkCgYEA0ERaxHbvrv+ZW8M3wQkzwZflZHqpfphLOqMz\n"
            + "0FzHVqzYQuhKrJzZj4mEyEaVziL3agnekUecOCOlSvwIr1q0bjmO6fUORgBp4eXM\n"
            + "TIG2gntW+/TcBP9h4n5f4vmXU5PUaZu6eSDNHj7VmkSVfM/BUfIi/OzMZhh0YCqi\n"
            + "vgbCno0CAwEAAaN7MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNT\n"
            + "TCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFJU+w0ZpUngIBUa5AGnl\n"
            + "56eZ48RnMB8GA1UdIwQYMBaAFOebfZAp6pALfwhBdk5BI+hDLKkDMA0GCSqGSIb3\n"
            + "DQEBBQUAA4GBAKNbMPUoP4f2GzZqIm1mSPrL7kwEzxEU4h+1aAznYQ6802kZAovV\n"
            + "0wVKyCno49DpMq1sfZzERmz5ZuZkYEdr747IHGdaWs9zo36dbokMZ5kXPbK4jkGV\n"
            + "nISVv1eVJCKPGRLB/SNFdX9PYQbjnwXc5ymaaxfh4TfVi7q00Io83T9q\n\n"
            + "-----END CERTIFICATE-----";
    ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
            .getBytes());
       byte[] key2Bytes = new byte[] {
            (byte)0x30, (byte)0x82, (byte)0x02, (byte)0x75, (byte)0x02, (byte)0x01, (byte)0x00,
            (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48,
            (byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x05,
            (byte)0x00, (byte)0x04, (byte)0x82, (byte)0x02, (byte)0x5f, (byte)0x30, (byte)0x82,
            (byte)0x02, (byte)0x5b, (byte)0x02, (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x81,
            (byte)0x81, (byte)0x00, (byte)0xd0, (byte)0x44, (byte)0x5a, (byte)0xc4, (byte)0x76,
            (byte)0xef, (byte)0xae, (byte)0xff, (byte)0x99, (byte)0x5b, (byte)0xc3, (byte)0x37,
            (byte)0xc1, (byte)0x09, (byte)0x33, (byte)0xc1, (byte)0x97, (byte)0xe5, (byte)0x64,
            (byte)0x7a, (byte)0xa9, (byte)0x7e, (byte)0x98, (byte)0x4b, (byte)0x3a, (byte)0xa3,
            (byte)0x33, (byte)0xd0, (byte)0x5c, (byte)0xc7, (byte)0x56, (byte)0xac, (byte)0xd8,
            (byte)0x42, (byte)0xe8, (byte)0x4a, (byte)0xac, (byte)0x9c, (byte)0xd9, (byte)0x8f,
            (byte)0x89, (byte)0x84, (byte)0xc8, (byte)0x46, (byte)0x95, (byte)0xce, (byte)0x22,
            (byte)0xf7, (byte)0x6a, (byte)0x09, (byte)0xde, (byte)0x91, (byte)0x47, (byte)0x9c,
            (byte)0x38, (byte)0x23, (byte)0xa5, (byte)0x4a, (byte)0xfc, (byte)0x08, (byte)0xaf,
            (byte)0x5a, (byte)0xb4, (byte)0x6e, (byte)0x39, (byte)0x8e, (byte)0xe9, (byte)0xf5,
            (byte)0x0e, (byte)0x46, (byte)0x00, (byte)0x69, (byte)0xe1, (byte)0xe5, (byte)0xcc,
            (byte)0x4c, (byte)0x81, (byte)0xb6, (byte)0x82, (byte)0x7b, (byte)0x56, (byte)0xfb,
            (byte)0xf4, (byte)0xdc, (byte)0x04, (byte)0xff, (byte)0x61, (byte)0xe2, (byte)0x7e,
            (byte)0x5f, (byte)0xe2, (byte)0xf9, (byte)0x97, (byte)0x53, (byte)0x93, (byte)0xd4,
            (byte)0x69, (byte)0x9b, (byte)0xba, (byte)0x79, (byte)0x20, (byte)0xcd, (byte)0x1e,
            (byte)0x3e, (byte)0xd5, (byte)0x9a, (byte)0x44, (byte)0x95, (byte)0x7c, (byte)0xcf,
            (byte)0xc1, (byte)0x51, (byte)0xf2, (byte)0x22, (byte)0xfc, (byte)0xec, (byte)0xcc,
            (byte)0x66, (byte)0x18, (byte)0x74, (byte)0x60, (byte)0x2a, (byte)0xa2, (byte)0xbe,
            (byte)0x06, (byte)0xc2, (byte)0x9e, (byte)0x8d, (byte)0x02, (byte)0x03, (byte)0x01,
            (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x81, (byte)0x80, (byte)0x06, (byte)0x41,
            (byte)0xd7, (byte)0x7c, (byte)0x49, (byte)0x9a, (byte)0x7f, (byte)0xe6, (byte)0x7c,
            (byte)0x04, (byte)0x0e, (byte)0xc4, (byte)0x71, (byte)0x0f, (byte)0x46, (byte)0xb7,
            (byte)0xcd, (byte)0x49, (byte)0x7e, (byte)0x10, (byte)0x55, (byte)0x61, (byte)0x51,
            (byte)0x50, (byte)0x09, (byte)0x4d, (byte)0xf7, (byte)0xf3, (byte)0x8d, (byte)0xa6,
            (byte)0x0b, (byte)0x8b, (byte)0x9b, (byte)0xdf, (byte)0xbe, (byte)0xbc, (byte)0xe7,
            (byte)0x9c, (byte)0xba, (byte)0xc8, (byte)0x9e, (byte)0x38, (byte)0x18, (byte)0x10,
            (byte)0x4e, (byte)0xd5, (byte)0xe7, (byte)0xa5, (byte)0x09, (byte)0x51, (byte)0x8c,
            (byte)0x97, (byte)0x4e, (byte)0xd0, (byte)0x79, (byte)0xbb, (byte)0x50, (byte)0x6f,
            (byte)0x05, (byte)0x4d, (byte)0x79, (byte)0x7f, (byte)0x3f, (byte)0x26, (byte)0x76,
            (byte)0xc1, (byte)0xcc, (byte)0x40, (byte)0x0f, (byte)0xde, (byte)0x42, (byte)0x5d,
            (byte)0xc1, (byte)0x5f, (byte)0x70, (byte)0x46, (byte)0x70, (byte)0x8d, (byte)0xff,
            (byte)0x26, (byte)0x35, (byte)0x75, (byte)0x9a, (byte)0x97, (byte)0xd2, (byte)0x74,
            (byte)0x53, (byte)0x11, (byte)0x2b, (byte)0xc1, (byte)0x76, (byte)0x9c, (byte)0x9f,
            (byte)0x93, (byte)0xaa, (byte)0xa8, (byte)0x41, (byte)0x23, (byte)0x9a, (byte)0x04,
            (byte)0x11, (byte)0x6e, (byte)0x56, (byte)0xea, (byte)0xf5, (byte)0xd6, (byte)0x1d,
            (byte)0x49, (byte)0x2a, (byte)0x83, (byte)0x49, (byte)0x7d, (byte)0xb7, (byte)0xd1,
            (byte)0xe6, (byte)0x8d, (byte)0x93, (byte)0x1a, (byte)0x81, (byte)0x8e, (byte)0xc2,
            (byte)0xb9, (byte)0xbf, (byte)0xfd, (byte)0x00, (byte)0xe2, (byte)0xb5, (byte)0x01,
            (byte)0x02, (byte)0x41, (byte)0x00, (byte)0xea, (byte)0xce, (byte)0xc6, (byte)0x11,
            (byte)0x1e, (byte)0xf6, (byte)0xcf, (byte)0x3a, (byte)0x8c, (byte)0xe7, (byte)0x80,
            (byte)0x16, (byte)0x8f, (byte)0x1d, (byte)0xeb, (byte)0xa2, (byte)0xd2, (byte)0x23,
            (byte)0x9e, (byte)0xf9, (byte)0xf1, (byte)0x14, (byte)0x16, (byte)0xc8, (byte)0x87,
            (byte)0xf2, (byte)0x17, (byte)0xdf, (byte)0xc6, (byte)0xe4, (byte)0x1c, (byte)0x74,
            (byte)0x74, (byte)0xb0, (byte)0xbb, (byte)0x40, (byte)0xeb, (byte)0xa6, (byte)0xb2,
            (byte)0x5b, (byte)0x6d, (byte)0xf5, (byte)0x9a, (byte)0x85, (byte)0xf1, (byte)0x73,
            (byte)0x84, (byte)0xec, (byte)0xdb, (byte)0x9b, (byte)0xf9, (byte)0xf8, (byte)0x3d,
            (byte)0xba, (byte)0xeb, (byte)0xd7, (byte)0x6c, (byte)0x45, (byte)0x7b, (byte)0xca,
            (byte)0x12, (byte)0x67, (byte)0x5f, (byte)0xcd, (byte)0x02, (byte)0x41, (byte)0x00,
            (byte)0xe3, (byte)0x10, (byte)0x5b, (byte)0xd0, (byte)0xad, (byte)0x59, (byte)0x90,
            (byte)0x18, (byte)0x17, (byte)0xdc, (byte)0x68, (byte)0xd4, (byte)0x75, (byte)0x55,
            (byte)0xab, (byte)0x7d, (byte)0xd1, (byte)0xb5, (byte)0x5a, (byte)0xc4, (byte)0xb0,
            (byte)0x2d, (byte)0xa9, (byte)0xd1, (byte)0x6f, (byte)0xe9, (byte)0x21, (byte)0x4a,
            (byte)0x27, (byte)0xc4, (byte)0x98, (byte)0x89, (byte)0xfa, (byte)0x65, (byte)0xb6,
            (byte)0x10, (byte)0x5d, (byte)0x66, (byte)0xdd, (byte)0x17, (byte)0xb3, (byte)0xf3,
            (byte)0xd3, (byte)0xe3, (byte)0xa0, (byte)0x1a, (byte)0x93, (byte)0xe4, (byte)0xfb,
            (byte)0x88, (byte)0xa7, (byte)0x3b, (byte)0x97, (byte)0x1b, (byte)0xf1, (byte)0x08,
            (byte)0x0c, (byte)0x66, (byte)0xd0, (byte)0x86, (byte)0x5e, (byte)0x39, (byte)0xf9,
            (byte)0xc1, (byte)0x02, (byte)0x40, (byte)0x24, (byte)0x7c, (byte)0xcd, (byte)0x3a,
            (byte)0x8b, (byte)0xdd, (byte)0x3e, (byte)0x86, (byte)0x92, (byte)0xae, (byte)0xc6,
            (byte)0xb0, (byte)0xba, (byte)0xbc, (byte)0xa3, (byte)0x89, (byte)0x41, (byte)0xae,
            (byte)0x57, (byte)0x5d, (byte)0xef, (byte)0xa0, (byte)0x77, (byte)0x89, (byte)0xe1,
            (byte)0xd6, (byte)0x34, (byte)0xef, (byte)0x89, (byte)0x30, (byte)0x99, (byte)0x5b,
            (byte)0x5f, (byte)0x66, (byte)0xb7, (byte)0x32, (byte)0x77, (byte)0x6c, (byte)0x07,
            (byte)0xfb, (byte)0x3d, (byte)0x33, (byte)0x15, (byte)0x38, (byte)0x0b, (byte)0x35,
            (byte)0x30, (byte)0x4a, (byte)0xbe, (byte)0x35, (byte)0x96, (byte)0xba, (byte)0x84,
            (byte)0x9d, (byte)0x2f, (byte)0x58, (byte)0xe2, (byte)0x72, (byte)0x49, (byte)0xb2,
            (byte)0x34, (byte)0xf9, (byte)0xeb, (byte)0x61, (byte)0x02, (byte)0x40, (byte)0x2a,
            (byte)0xd4, (byte)0x89, (byte)0x1d, (byte)0x21, (byte)0xb5, (byte)0xc5, (byte)0x32,
            (byte)0x66, (byte)0x3d, (byte)0xd3, (byte)0x20, (byte)0x50, (byte)0x49, (byte)0xaa,
            (byte)0xa1, (byte)0x7f, (byte)0x0f, (byte)0x20, (byte)0x61, (byte)0xfd, (byte)0x81,
            (byte)0x7f, (byte)0x88, (byte)0xdb, (byte)0xfd, (byte)0x33, (byte)0xa4, (byte)0x53,
            (byte)0x40, (byte)0x08, (byte)0x2d, (byte)0xee, (byte)0xa7, (byte)0x84, (byte)0xe2,
            (byte)0x2d, (byte)0x5c, (byte)0x1b, (byte)0xd4, (byte)0x3e, (byte)0xc3, (byte)0x7d,
            (byte)0x72, (byte)0x70, (byte)0x5e, (byte)0xd3, (byte)0x0a, (byte)0xdc, (byte)0x4f,
            (byte)0x78, (byte)0x8c, (byte)0x0b, (byte)0x02, (byte)0xe0, (byte)0x42, (byte)0x4e,
            (byte)0x64, (byte)0x8e, (byte)0x6c, (byte)0xea, (byte)0x15, (byte)0x31, (byte)0x81,
            (byte)0x02, (byte)0x40, (byte)0x57, (byte)0x72, (byte)0xb9, (byte)0x78, (byte)0xc0,
            (byte)0x1f, (byte)0x5b, (byte)0x1d, (byte)0xb2, (byte)0xcf, (byte)0x94, (byte)0x42,
            (byte)0xed, (byte)0xbd, (byte)0xe7, (byte)0xaa, (byte)0x14, (byte)0x56, (byte)0xd0,
            (byte)0x94, (byte)0x25, (byte)0x30, (byte)0x87, (byte)0x35, (byte)0x82, (byte)0xa0,
            (byte)0x42, (byte)0xb5, (byte)0x7f, (byte)0x66, (byte)0x77, (byte)0xb0, (byte)0x13,
            (byte)0xbe, (byte)0x57, (byte)0x06, (byte)0x7e, (byte)0x50, (byte)0x67, (byte)0x13,
            (byte)0xa7, (byte)0x09, (byte)0xac, (byte)0xd6, (byte)0xbf, (byte)0x22, (byte)0x74,
            (byte)0x6b, (byte)0x37, (byte)0x92, (byte)0x2b, (byte)0x91, (byte)0xbd, (byte)0x0a,
            (byte)0xd8, (byte)0x0f, (byte)0x8d, (byte)0x86, (byte)0x4b, (byte)0x20, (byte)0x5e,
            (byte)0x50, (byte)0x60, (byte)0x80
    };
    String certificate3 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIC9jCCAl+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJBTjEQ\n"
            + "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k\n"
            + "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBh\n"
            + "bmRyb2lkLmNvbTAeFw0wOTAzMjAxNzAyMzJaFw0xMDAzMjAxNzAyMzJaMIGLMQsw\n"
            + "CQYDVQQGEwJBTjEQMA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEBxMHQW5kcm9pZDEQ\n"
            + "MA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5k\n"
            + "cm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCBnzANBgkq\n"
            + "hkiG9w0BAQEFAAOBjQAwgYkCgYEAtMXt3zBCbYuvS+ScE16DI80vzjTiQ9dscrsD\n"
            + "s7kkAuDMtY3WkkEEK1yUssOcnVbwmbwPga/rVO2ApqDHwkMFBHycfgcDELm9xRbP\n"
            + "Gd3jT3ODcsVm5FsUxJbR4yQLttT3hC6x55MCnfXaqsHZzF426Y+/i9qnRYLysPWn\n"
            + "5OGAoxcCAwEAAaN7MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNT\n"
            + "TCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFDtbPdtF9Y9YcAv8cD4x\n"
            + "K0Njqf4rMB8GA1UdIwQYMBaAFOebfZAp6pALfwhBdk5BI+hDLKkDMA0GCSqGSIb3\n"
            + "DQEBBQUAA4GBABx/kxxZIYgVRUvgnHg6iD5VGYYx6FM9dOJKNJ+SF04TRpJU+EPr\n"
            + "XgNPFFFh0gS4BFox6xRqGLAgA5IMfwfEG/mef1/sA3rI49/TlG5oijo95GHz4Idd\n"
            + "QNjLmU2ae7yVfNKdtwSammOJzTnsMmAKl9rpUKVzSqKqnJuof1og1ki9\n"
            + "-----END CERTIFICATE-----";
    ByteArrayInputStream certArray3 = new ByteArrayInputStream(certificate3
            .getBytes());
    byte[] key3Bytes = new byte[] {
            (byte)0x30, (byte)0x82, (byte)0x02, (byte)0x76, (byte)0x02, (byte)0x01, (byte)0x00,
            (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x2a, (byte)0x86, (byte)0x48,
            (byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x05,
            (byte)0x00, (byte)0x04, (byte)0x82, (byte)0x02, (byte)0x60, (byte)0x30, (byte)0x82,
            (byte)0x02, (byte)0x5c, (byte)0x02, (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x81,
            (byte)0x81, (byte)0x00, (byte)0xb4, (byte)0xc5, (byte)0xed, (byte)0xdf, (byte)0x30,
            (byte)0x42, (byte)0x6d, (byte)0x8b, (byte)0xaf, (byte)0x4b, (byte)0xe4, (byte)0x9c,
            (byte)0x13, (byte)0x5e, (byte)0x83, (byte)0x23, (byte)0xcd, (byte)0x2f, (byte)0xce,
            (byte)0x34, (byte)0xe2, (byte)0x43, (byte)0xd7, (byte)0x6c, (byte)0x72, (byte)0xbb,
            (byte)0x03, (byte)0xb3, (byte)0xb9, (byte)0x24, (byte)0x02, (byte)0xe0, (byte)0xcc,
            (byte)0xb5, (byte)0x8d, (byte)0xd6, (byte)0x92, (byte)0x41, (byte)0x04, (byte)0x2b,
            (byte)0x5c, (byte)0x94, (byte)0xb2, (byte)0xc3, (byte)0x9c, (byte)0x9d, (byte)0x56,
            (byte)0xf0, (byte)0x99, (byte)0xbc, (byte)0x0f, (byte)0x81, (byte)0xaf, (byte)0xeb,
            (byte)0x54, (byte)0xed, (byte)0x80, (byte)0xa6, (byte)0xa0, (byte)0xc7, (byte)0xc2,
            (byte)0x43, (byte)0x05, (byte)0x04, (byte)0x7c, (byte)0x9c, (byte)0x7e, (byte)0x07,
            (byte)0x03, (byte)0x10, (byte)0xb9, (byte)0xbd, (byte)0xc5, (byte)0x16, (byte)0xcf,
            (byte)0x19, (byte)0xdd, (byte)0xe3, (byte)0x4f, (byte)0x73, (byte)0x83, (byte)0x72,
            (byte)0xc5, (byte)0x66, (byte)0xe4, (byte)0x5b, (byte)0x14, (byte)0xc4, (byte)0x96,
            (byte)0xd1, (byte)0xe3, (byte)0x24, (byte)0x0b, (byte)0xb6, (byte)0xd4, (byte)0xf7,
            (byte)0x84, (byte)0x2e, (byte)0xb1, (byte)0xe7, (byte)0x93, (byte)0x02, (byte)0x9d,
            (byte)0xf5, (byte)0xda, (byte)0xaa, (byte)0xc1, (byte)0xd9, (byte)0xcc, (byte)0x5e,
            (byte)0x36, (byte)0xe9, (byte)0x8f, (byte)0xbf, (byte)0x8b, (byte)0xda, (byte)0xa7,
            (byte)0x45, (byte)0x82, (byte)0xf2, (byte)0xb0, (byte)0xf5, (byte)0xa7, (byte)0xe4,
            (byte)0xe1, (byte)0x80, (byte)0xa3, (byte)0x17, (byte)0x02, (byte)0x03, (byte)0x01,
            (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x81, (byte)0x80, (byte)0x53, (byte)0xbc,
            (byte)0x1f, (byte)0x1c, (byte)0x34, (byte)0x09, (byte)0x81, (byte)0x1e, (byte)0xa3,
            (byte)0xfb, (byte)0x5e, (byte)0x90, (byte)0xa1, (byte)0x34, (byte)0x35, (byte)0x40,
            (byte)0x9f, (byte)0x29, (byte)0xd6, (byte)0xb5, (byte)0x8e, (byte)0x5d, (byte)0x68,
            (byte)0x6a, (byte)0xf6, (byte)0x96, (byte)0x03, (byte)0xf7, (byte)0xfa, (byte)0xf9,
            (byte)0x60, (byte)0x4f, (byte)0xea, (byte)0xe2, (byte)0xea, (byte)0x29, (byte)0x8b,
            (byte)0x23, (byte)0x8c, (byte)0x9f, (byte)0xdd, (byte)0x49, (byte)0x8f, (byte)0xa8,
            (byte)0xa6, (byte)0x62, (byte)0x07, (byte)0x44, (byte)0x79, (byte)0xa1, (byte)0xaf,
            (byte)0xf9, (byte)0x1d, (byte)0x98, (byte)0xbf, (byte)0x85, (byte)0x28, (byte)0x03,
            (byte)0x87, (byte)0x14, (byte)0x20, (byte)0xba, (byte)0xd4, (byte)0x96, (byte)0x61,
            (byte)0x2a, (byte)0xd0, (byte)0xaa, (byte)0x30, (byte)0x19, (byte)0x4b, (byte)0x40,
            (byte)0x35, (byte)0xb0, (byte)0x79, (byte)0x0b, (byte)0x7f, (byte)0xd7, (byte)0xcd,
            (byte)0x64, (byte)0xd9, (byte)0x93, (byte)0x38, (byte)0xe2, (byte)0x59, (byte)0xe0,
            (byte)0x9e, (byte)0x3a, (byte)0x25, (byte)0x27, (byte)0xa2, (byte)0xd9, (byte)0x20,
            (byte)0xb0, (byte)0x45, (byte)0x5f, (byte)0x6c, (byte)0x15, (byte)0x6f, (byte)0x10,
            (byte)0x55, (byte)0xa7, (byte)0xf9, (byte)0x3d, (byte)0x92, (byte)0x3c, (byte)0x7c,
            (byte)0x23, (byte)0x1b, (byte)0xc0, (byte)0xb5, (byte)0x17, (byte)0x41, (byte)0x5e,
            (byte)0x8c, (byte)0xdc, (byte)0x25, (byte)0x1d, (byte)0x35, (byte)0x2b, (byte)0xd3,
            (byte)0x97, (byte)0x1a, (byte)0x6f, (byte)0xae, (byte)0xeb, (byte)0xf5, (byte)0xf9,
            (byte)0x02, (byte)0x41, (byte)0x00, (byte)0xd7, (byte)0x3e, (byte)0xed, (byte)0x70,
            (byte)0xfe, (byte)0xee, (byte)0x0e, (byte)0x30, (byte)0x29, (byte)0xfa, (byte)0xd7,
            (byte)0x38, (byte)0xcf, (byte)0x8e, (byte)0xc1, (byte)0x9c, (byte)0x78, (byte)0x06,
            (byte)0x2d, (byte)0xda, (byte)0x33, (byte)0x58, (byte)0xa1, (byte)0x7b, (byte)0xbf,
            (byte)0x00, (byte)0xb9, (byte)0xdf, (byte)0xea, (byte)0x65, (byte)0x86, (byte)0xbb,
            (byte)0xcc, (byte)0x83, (byte)0xce, (byte)0xde, (byte)0xc3, (byte)0xf8, (byte)0x89,
            (byte)0xf5, (byte)0x9f, (byte)0xa6, (byte)0x1d, (byte)0xc9, (byte)0xfb, (byte)0x98,
            (byte)0xa1, (byte)0x2e, (byte)0xe0, (byte)0x57, (byte)0x6e, (byte)0xbd, (byte)0x57,
            (byte)0x20, (byte)0xf9, (byte)0x6b, (byte)0x13, (byte)0x42, (byte)0x9d, (byte)0x8d,
            (byte)0x66, (byte)0x4d, (byte)0x7a, (byte)0x2d, (byte)0x02, (byte)0x41, (byte)0x00,
            (byte)0xd7, (byte)0x00, (byte)0x18, (byte)0x54, (byte)0xe8, (byte)0x37, (byte)0xdb,
            (byte)0xf8, (byte)0x98, (byte)0x7b, (byte)0x18, (byte)0x33, (byte)0xf6, (byte)0x28,
            (byte)0xa8, (byte)0x8c, (byte)0xd9, (byte)0xfd, (byte)0x4c, (byte)0x4e, (byte)0x41,
            (byte)0x73, (byte)0x2e, (byte)0x79, (byte)0x31, (byte)0xcc, (byte)0x7d, (byte)0x42,
            (byte)0xb7, (byte)0xa1, (byte)0xd2, (byte)0xbc, (byte)0x1f, (byte)0x62, (byte)0xcf,
            (byte)0x15, (byte)0x7c, (byte)0x62, (byte)0x97, (byte)0x70, (byte)0xf1, (byte)0x15,
            (byte)0xf1, (byte)0x33, (byte)0xa1, (byte)0x9d, (byte)0xbb, (byte)0x5f, (byte)0xd7,
            (byte)0x5a, (byte)0xf9, (byte)0x24, (byte)0x58, (byte)0xac, (byte)0x86, (byte)0x6a,
            (byte)0xed, (byte)0xd4, (byte)0x84, (byte)0xe4, (byte)0x3f, (byte)0xfe, (byte)0xb0,
            (byte)0xd3, (byte)0x02, (byte)0x41, (byte)0x00, (byte)0xd4, (byte)0xb7, (byte)0x84,
            (byte)0xb2, (byte)0x39, (byte)0xce, (byte)0x0b, (byte)0x49, (byte)0x80, (byte)0x03,
            (byte)0x3c, (byte)0xb5, (byte)0x11, (byte)0x32, (byte)0x34, (byte)0x96, (byte)0xac,
            (byte)0x6a, (byte)0xf6, (byte)0xdf, (byte)0x80, (byte)0x04, (byte)0xe4, (byte)0x39,
            (byte)0xc6, (byte)0x0e, (byte)0x32, (byte)0xa3, (byte)0x5e, (byte)0x23, (byte)0x0d,
            (byte)0x9f, (byte)0x04, (byte)0xc3, (byte)0x72, (byte)0x2a, (byte)0xe6, (byte)0xa2,
            (byte)0xf5, (byte)0xbc, (byte)0x3f, (byte)0x15, (byte)0x4c, (byte)0xb5, (byte)0x33,
            (byte)0x26, (byte)0xa8, (byte)0x8c, (byte)0x09, (byte)0xfb, (byte)0x7e, (byte)0x1e,
            (byte)0x32, (byte)0x40, (byte)0x0d, (byte)0x1d, (byte)0xcb, (byte)0x7f, (byte)0xf6,
            (byte)0xf2, (byte)0x29, (byte)0x9b, (byte)0x01, (byte)0xd5, (byte)0x02, (byte)0x40,
            (byte)0x24, (byte)0x26, (byte)0x1c, (byte)0xf1, (byte)0x31, (byte)0xb6, (byte)0x2a,
            (byte)0xa3, (byte)0x0a, (byte)0xa8, (byte)0x2f, (byte)0xb2, (byte)0x94, (byte)0xe1,
            (byte)0xd3, (byte)0x2d, (byte)0x13, (byte)0x7d, (byte)0xd6, (byte)0x35, (byte)0x96,
            (byte)0x25, (byte)0x92, (byte)0x9b, (byte)0xc7, (byte)0xf6, (byte)0xb4, (byte)0xdc,
            (byte)0xe1, (byte)0xd9, (byte)0x30, (byte)0x80, (byte)0x76, (byte)0xda, (byte)0x7b,
            (byte)0x2d, (byte)0x06, (byte)0xa3, (byte)0xe1, (byte)0x08, (byte)0x99, (byte)0x50,
            (byte)0x72, (byte)0x24, (byte)0x97, (byte)0x38, (byte)0xd9, (byte)0x07, (byte)0x4d,
            (byte)0x43, (byte)0x3b, (byte)0x7e, (byte)0x93, (byte)0xf6, (byte)0x36, (byte)0x07,
            (byte)0x86, (byte)0x83, (byte)0x63, (byte)0xf0, (byte)0xa8, (byte)0x9d, (byte)0xdf,
            (byte)0x07, (byte)0x02, (byte)0x40, (byte)0x3e, (byte)0x58, (byte)0x03, (byte)0xbf,
            (byte)0xea, (byte)0x3e, (byte)0x34, (byte)0x2c, (byte)0xb7, (byte)0xc3, (byte)0x09,
            (byte)0xe9, (byte)0xf4, (byte)0x43, (byte)0x41, (byte)0xc4, (byte)0x7c, (byte)0x6e,
            (byte)0x75, (byte)0x72, (byte)0x5d, (byte)0xfc, (byte)0xa3, (byte)0x75, (byte)0x1d,
            (byte)0xa0, (byte)0xee, (byte)0xc2, (byte)0x1f, (byte)0x71, (byte)0xb0, (byte)0xf3,
            (byte)0x1d, (byte)0xec, (byte)0x81, (byte)0xdb, (byte)0x45, (byte)0xe5, (byte)0x6a,
            (byte)0xe8, (byte)0xe0, (byte)0x64, (byte)0x90, (byte)0xff, (byte)0xb9, (byte)0xf8,
            (byte)0x12, (byte)0xed, (byte)0x55, (byte)0x5c, (byte)0x9b, (byte)0x81, (byte)0xcd,
            (byte)0xbb, (byte)0x06, (byte)0x91, (byte)0xfe, (byte)0x27, (byte)0x2c, (byte)0x3a,
            (byte)0xed, (byte)0x96, (byte)0x3b, (byte)0xfe
    };
    @Override
    protected void setUp() {
        String defAlg = KeyManagerFactory.getDefaultAlgorithm();
        try {
            factory = KeyManagerFactory.getInstance(defAlg);
        } catch (NoSuchAlgorithmException e) {
            fail("could not get default KeyManagerFactory");
        }
    }
    void init(String name) {
      keyType = name;
      try {
          CertificateFactory cf = CertificateFactory.getInstance("X.509");
          KeyFactory kf = KeyFactory.getInstance("RSA");
          keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
          keyTest.load(null, "1234".toCharArray());
          if (keyType.equals(client)) {
              keys = new PrivateKey[3];
              keys[0] = kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
              keys[1] = kf.generatePrivate(new PKCS8EncodedKeySpec(key2Bytes));
              keys[2] = kf.generatePrivate(new PKCS8EncodedKeySpec(key3Bytes));
              cert = new X509Certificate[3];
              cert[0] = (X509Certificate) cf.generateCertificate(certArray);
              cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
              cert[2] = (X509Certificate) cf.generateCertificate(certArray3);
              keyTest.setKeyEntry("clientKey_01", keys[0], password.toCharArray(), new X509Certificate[] {cert[0]});
              keyTest.setKeyEntry("clientKey_02", keys[1], password.toCharArray(), new X509Certificate[] {cert[0], cert[1]});
              keyTest.setKeyEntry("clientKey_03", keys[2], password.toCharArray(), new X509Certificate[] {cert[0], cert[2]});
              keyTest.setCertificateEntry("clientAlias_01", cert[0]);
              keyTest.setCertificateEntry("clientAlias_02", cert[0]);
              keyTest.setCertificateEntry("clientAlias_03", cert[1]);
          } else if (keyType.equals(server)) {
              keys = new PrivateKey[1];
              keys[0] = kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
              cert = new X509Certificate[1];
              cert[0] = (X509Certificate) cf.generateCertificate(certArray3);
              keyTest.setKeyEntry("serverKey_00", keys[0], password.toCharArray(), new X509Certificate[] {cert[0]});
              keyTest.setCertificateEntry("serverAlias_00", cert[0]);
          }
      } catch (Exception ex) {
          ex.printStackTrace();
          throw new IllegalArgumentException(ex.getMessage());
      }
      try {
        factory.init(keyTest, "1234".toCharArray());
      } catch (Exception e) {
        fail("Could't init the KeyManagerFactory");
      }
      manager = (X509KeyManager) factory.getKeyManagers()[0];
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClientAliases",
        args = {java.lang.String.class, java.security.Principal[].class}
    )
    public void test_getClientAliases() {
        init(client);
        assertNull(manager.getClientAliases(null, null));
        assertNull(manager.getClientAliases("", null));
        String[] resArray = manager.getClientAliases(type, null);
        assertNotNull(resArray);
        assertTrue("Incorrect result", compareC(resArray));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseClientAlias",
        args = {java.lang.String[].class, java.security.Principal[].class, java.net.Socket.class}
    )
    public void test_chooseClientAlias() {
        String[] ar = {client};
        init(client);
        assertNull(manager.chooseClientAlias(null, null, new Socket()));
        assertNull(manager.chooseClientAlias(new String[0], null, new Socket()));
        assertNull(manager.chooseClientAlias(ar, null, new Socket()));
        String res = manager.chooseClientAlias(new String[]{type}, null, null);
        assertNotNull(res);
        assertEquals("clientkey_03", res.toLowerCase().toLowerCase());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerAliases",
        args = {java.lang.String.class, java.security.Principal[].class}
    )
    public void test_getServerAliases() {
        init(server);
        assertNull(manager.getServerAliases(null, null));
        assertNull(manager.getServerAliases("", null));
        String[] resArray = manager.getServerAliases(type, null);
        assertNotNull(resArray);
        assertEquals("Incorrect length", 1, resArray.length);
        assertEquals("Incorrect aliase", "serverkey_00", resArray[0].toLowerCase());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseServerAlias",
        args = {java.lang.String.class, java.security.Principal[].class, java.net.Socket.class}
    )
    public void test_chooseServerAlias() {
        init(server);
        assertNull(manager.chooseServerAlias(null, null, new Socket()));
        assertNull(manager.chooseServerAlias("", null, new Socket()));
        String res = manager.chooseServerAlias(type, null, null);
        assertNotNull(res);
        assertEquals("serverkey_00", res.toLowerCase());
        res = manager.chooseServerAlias(type, null, new Socket());
        assertNotNull(res);
        assertEquals("serverkey_00", res.toLowerCase());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateChain",
        args = {java.lang.String.class}
    )
    public void test_getCertificateChain() {
        init(server);
        assertNull("Not NULL for NULL parameter", manager.getCertificateChain(null));
        assertNull("Not NULL for empty parameter",manager.getCertificateChain(""));
        assertNull("Not NULL for clientAlias_01 parameter", manager.getCertificateChain("clientAlias_01"));
        assertNull("Not NULL for serverAlias_00 parameter", manager.getCertificateChain("serverAlias_00"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateKey",
        args = {java.lang.String.class}
    )
    public void test_getPrivateKey() {
        init(client);
        assertNull("Not NULL for NULL parameter", manager.getPrivateKey(null));
        assertNull("Not NULL for serverAlias_00 parameter", manager.getPrivateKey("serverAlias_00"));
        assertNull("Not NULL for clientAlias_02 parameter", manager.getPrivateKey("clientAlias_02"));
    }
    private boolean compareC(String[] ar) {
        if (ar.length != 3) {
            return false;
        }
        for (int i = 0; i < ar.length; i++) {
            if (!ar[i].toLowerCase().equals("clientkey_01") && !ar[i].toLowerCase().equals("clientkey_02") && !ar[i].toLowerCase().equals("clientkey_03")) {
                return false;
            }
        }
        return true;
    }
}
