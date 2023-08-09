public class InvalidBitString {
    static String signerCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIBtDCCAR2gAwIBAgIEemxRHjANBgkqhkiG9w0BAQUFADANMQswCQYDVQQDEwJDQTAeFw0xMDA2\n" +
        "MDMwODA2MjlaFw0xMDA5MDEwODA2MjlaMA0xCzAJBgNVBAMTAkNBMIGfMA0GCSqGSIb3DQEBAQUA\n" +
        "A4GNADCBiQKBgQCp2G7pGwMOw4oM7zFFeRKrByuPLNAXClGsh+itdRiOeUgEby6OB9IAgXm93086\n" +
        "Z9dWCfRYbzJbDRSnUE7FS1iQsIRIeOEuFMIMogcBK+sOf364ONwMXsI4gtYVmxn4BaaajVWt6C/g\n" +
        "FBGZQxp81aORDyUIrlCkMIxhZBSsNPIJYwIDAQABoyEwHzAdBgNVHQ4EFgQUKrvzNhJmdKoqq2li\n" +
        "utCzKkwA1N0wDQYJKoZIhvcNAQEFBQADgYEAEIaegsW7fWWjXk4YOMlcl893vx6tnU8ThuQSjwGI\n" +
        "rIs93sBYuY7lQIpQw8+XM89WT1XuBB6R2SsnxeW+gHtsU/EE6iJJAEMeCILwEGUL02blwHBQWmpa\n" +
        "i3YeGXw+IFe/4OAysPT7ZRbUb7mPt37Ht6hIjain71ShR5anXIuawVE=\n" +
        "-----END CERTIFICATE-----\n";
    static String normalCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIB1DCCAT2gAwIBAgIEae+u1TANBgkqhkiG9w0BAQUFADANMQswCQYDVQQDEwJDQTAeFw0xMDA2\n" +
        "MDMwODA2NTNaFw0xMDA5MDEwODA2NTNaMAwxCjAIBgNVBAMTAUEwgZ8wDQYJKoZIhvcNAQEBBQAD\n" +
        "gY0AMIGJAoGBAKZ7C6bC8AJmXIRNwuPJcgIPW1ygN3rE5PIKPAkeK/dYnPmUJNuiSxOFPJCrLMuL\n" +
        "sweQh82Dq/viu+KBb27xVzJ4pK02fbcWdJDo7cIms0Wm+HckK5myA6xmqnpmPOjb/vWCLE6pN2Xg\n" +
        "pJyrdeWV77eBvqE9OiCsMTP8WgHI9zLvAgMBAAGjQjBAMB0GA1UdDgQWBBTtIKqCHnL9QeFn+YrX\n" +
        "+k00NUk9mjAfBgNVHSMEGDAWgBQqu/M2EmZ0qiqraWK60LMqTADU3TANBgkqhkiG9w0BAQUFAAOB\n" +
        "gQAAOcQsEruDAY/z3eXJ7OtWSZlLC0yTVNVdUVNLQ58xNqPrmKNBXNpj/72N8xrTB++ApW+DLgLy\n" +
        "cwGU5PVRtsYeiV6prUkpqUf62SQgwI4guAQy1ileeP1CNQJI3cHQExMAHvQT8fJtlD0WZD3nfesq\n" +
        "mmQDOpoJLkmO/73Z7IibVA==\n" +
        "-----END CERTIFICATE-----\n";
    static String longerCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIB1TCCAT2gAwIBAgIEae+u1TANBgkqhkiG9w0BAQUFADANMQswCQYDVQQDEwJDQTAeFw0xMDA2\n" +
        "MDMwODA2NTNaFw0xMDA5MDEwODA2NTNaMAwxCjAIBgNVBAMTAUEwgZ8wDQYJKoZIhvcNAQEBBQAD\n" +
        "gY0AMIGJAoGBAKZ7C6bC8AJmXIRNwuPJcgIPW1ygN3rE5PIKPAkeK/dYnPmUJNuiSxOFPJCrLMuL\n" +
        "sweQh82Dq/viu+KBb27xVzJ4pK02fbcWdJDo7cIms0Wm+HckK5myA6xmqnpmPOjb/vWCLE6pN2Xg\n" +
        "pJyrdeWV77eBvqE9OiCsMTP8WgHI9zLvAgMBAAGjQjBAMB0GA1UdDgQWBBTtIKqCHnL9QeFn+YrX\n" +
        "+k00NUk9mjAfBgNVHSMEGDAWgBQqu/M2EmZ0qiqraWK60LMqTADU3TANBgkqhkiG9w0BAQUFAAOB\n" +
        "ggAAADnELBK7gwGP893lyezrVkmZSwtMk1TVXVFTS0OfMTaj65ijQVzaY/+9jfMa0wfvgKVvgy4C\n" +
        "8nMBlOT1UbbGHoleqa1JKalH+tkkIMCOILgEMtYpXnj9QjUCSN3B0BMTAB70E/HybZQ9FmQ9533r\n" +
        "KppkAzqaCS5Jjv+92eyIm1Q=\n" +
        "-----END CERTIFICATE-----\n";
    static String shorterCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIB0zCCAT2gAwIBAgIEae+u1TANBgkqhkiG9w0BAQUFADANMQswCQYDVQQDEwJDQTAeFw0xMDA2\n" +
        "MDMwODA2NTNaFw0xMDA5MDEwODA2NTNaMAwxCjAIBgNVBAMTAUEwgZ8wDQYJKoZIhvcNAQEBBQAD\n" +
        "gY0AMIGJAoGBAKZ7C6bC8AJmXIRNwuPJcgIPW1ygN3rE5PIKPAkeK/dYnPmUJNuiSxOFPJCrLMuL\n" +
        "sweQh82Dq/viu+KBb27xVzJ4pK02fbcWdJDo7cIms0Wm+HckK5myA6xmqnpmPOjb/vWCLE6pN2Xg\n" +
        "pJyrdeWV77eBvqE9OiCsMTP8WgHI9zLvAgMBAAGjQjBAMB0GA1UdDgQWBBTtIKqCHnL9QeFn+YrX\n" +
        "+k00NUk9mjAfBgNVHSMEGDAWgBQqu/M2EmZ0qiqraWK60LMqTADU3TANBgkqhkiG9w0BAQUFAAOB\n" +
        "gAA5xCwSu4MBj/Pd5cns61ZJmUsLTJNU1V1RU0tDnzE2o+uYo0Fc2mP/vY3zGtMH74Clb4MuAvJz\n" +
        "AZTk9VG2xh6JXqmtSSmpR/rZJCDAjiC4BDLWKV54/UI1AkjdwdATEwAe9BPx8m2UPRZkPed96yqa\n" +
        "ZAM6mgkuSY7/vdnsiJtU\n" +
        "-----END CERTIFICATE-----\n";
    public static void main(String args[]) throws Exception {
        Certificate signer = generate(signerCertStr);
        Certificate normal = generate(normalCertStr);
        Certificate longer = generate(longerCertStr);
        Certificate shorter = generate(shorterCertStr);
        if (!test(normal, signer, " normal", true) ||
                !test(longer, signer, " longer", false) ||
                !test(shorter, signer, "shorter", false)) {
            throw new Exception("Test failed.");
        }
    }
    private static Certificate generate(String certStr) throws Exception {
        InputStream is = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            is = new ByteArrayInputStream(certStr.getBytes());
            return cf.generateCertificate(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    private static boolean test(Certificate target, Certificate signer,
            String title, boolean expected) throws Exception {
        System.out.print("Checking " + title + ": expected: " +
                (expected ? "    verified" : "NOT verified"));
        boolean actual;
        try {
            PublicKey pubKey = signer.getPublicKey();
            target.verify(pubKey);
            actual = true;
        } catch (SignatureException se) {
            actual = false;
        }
        System.out.println(", actual: " +
                (actual ? "    verified" : "NOT verified"));
        return actual == expected;
    }
}
