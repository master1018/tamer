public abstract class JDKAlgorithmParameters
    extends AlgorithmParametersSpi
{
    public static class IVAlgorithmParameters
        extends JDKAlgorithmParameters
    {
        private byte[]  iv;
        protected byte[] engineGetEncoded() 
            throws IOException
        {
            return engineGetEncoded("ASN.1");
        }
        protected byte[] engineGetEncoded(
            String format) 
            throws IOException
        {
            if (format == null)
            {
                return engineGetEncoded("ASN.1");
            }
            if (format.equals("RAW"))
            {
                byte[]  tmp = new byte[iv.length];
                System.arraycopy(iv, 0, tmp, 0, iv.length);
                return tmp;
            }
            else if (format.equals("ASN.1"))
            {
                return new DEROctetString(engineGetEncoded("RAW")).getEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == IvParameterSpec.class)
            {
                return new IvParameterSpec(iv);
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IvParameterSpec))
            {
                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
            }
            this.iv = ((IvParameterSpec)paramSpec).getIV();
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            if ((params.length % 8) != 0
                    && params[0] == 0x04 && params[1] == params.length - 2)
            {
                ASN1InputStream         aIn = new ASN1InputStream(params);
                ASN1OctetString         oct = (ASN1OctetString)aIn.readObject();
                params = oct.getOctets();
            }
            this.iv = new byte[params.length];
            System.arraycopy(params, 0, iv, 0, iv.length);
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equals("RAW"))
            {
                engineInit(params);
                return;
            }
            else if (format.equals("ASN.1"))
            {
                ASN1InputStream         aIn = new ASN1InputStream(params);
                try
                {
                    ASN1OctetString         oct = (ASN1OctetString)aIn.readObject();
                    engineInit(oct.getOctets());
                }
                catch (Exception e)
                {
                    throw new IOException("Exception decoding: " + e);
                }
                return;
            }
            throw new IOException("Unknown parameters format in IV parameters object");
        }
        protected String engineToString() 
        {
            return "IV Parameters";
        }
    }
    public static class PKCS12PBE
        extends JDKAlgorithmParameters
    {
        PKCS12PBEParams params;
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            try
            {
                dOut.writeObject(params);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Oooops! " + e.toString());
            }
            return bOut.toByteArray();
        }
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equals("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == PBEParameterSpec.class)
            {
                return new PBEParameterSpec(params.getIV(),
                                params.getIterations().intValue());
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PKCS12 PBE parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof PBEParameterSpec))
            {
                throw new InvalidParameterSpecException("PBEParameterSpec required to initialise a PKCS12 PBE parameters algorithm parameters object");
            }
            PBEParameterSpec    pbeSpec = (PBEParameterSpec)paramSpec;
            this.params = new PKCS12PBEParams(pbeSpec.getSalt(),
                                pbeSpec.getIterationCount());
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            this.params = PKCS12PBEParams.getInstance(aIn.readObject());
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equals("ASN.1"))
            {
                engineInit(params);
                return;
            }
            throw new IOException("Unknown parameters format in PKCS12 PBE parameters object");
        }
        protected String engineToString() 
        {
            return "PKCS12 PBE Parameters";
        }
    }
    public static class DH
        extends JDKAlgorithmParameters
    {
        DHParameterSpec     currentSpec;
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            DHParameter             dhP = new DHParameter(currentSpec.getP(), currentSpec.getG(), currentSpec.getL());
            try
            {
                dOut.writeObject(dhP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding DHParameters");
            }
            return bOut.toByteArray();
        }
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == DHParameterSpec.class)
            {
                return currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to DH parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof DHParameterSpec))
            {
                throw new InvalidParameterSpecException("DHParameterSpec required to initialise a Diffie-Hellman algorithm parameters object");
            }
            this.currentSpec = (DHParameterSpec)paramSpec;
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            try
            {
                DHParameter dhP = new DHParameter((ASN1Sequence)aIn.readObject());
                if (dhP.getL() != null)
                {
                    currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG(), dhP.getL().intValue());
                }
                else
                {
                    currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG());
                }
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid DH Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid DH Parameter encoding.");
            }
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
        protected String engineToString() 
        {
            return "Diffie-Hellman Parameters";
        }
    }
    public static class DSA
        extends JDKAlgorithmParameters
    {
        DSAParameterSpec     currentSpec;
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            DSAParameter            dsaP = new DSAParameter(currentSpec.getP(), currentSpec.getQ(), currentSpec.getG());
            try
            {
                dOut.writeObject(dsaP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding DSAParameters");
            }
            return bOut.toByteArray();
        }
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == DSAParameterSpec.class)
            {
                return currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to DSA parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof DSAParameterSpec))
            {
                throw new InvalidParameterSpecException("DSAParameterSpec required to initialise a DSA algorithm parameters object");
            }
            this.currentSpec = (DSAParameterSpec)paramSpec;
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            try
            {
                DSAParameter dsaP = new DSAParameter((ASN1Sequence)aIn.readObject());
                currentSpec = new DSAParameterSpec(dsaP.getP(), dsaP.getQ(), dsaP.getG());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid DSA Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid DSA Parameter encoding.");
            }
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
        protected String engineToString() 
        {
            return "DSA Parameters";
        }
    }
    public static class IES
        extends JDKAlgorithmParameters
    {
        IESParameterSpec     currentSpec;
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            try
            {
                ASN1EncodableVector    v = new ASN1EncodableVector();
                v.add(new DEROctetString(currentSpec.getDerivationV()));
                v.add(new DEROctetString(currentSpec.getEncodingV()));
                v.add(new DERInteger(currentSpec.getMacKeySize()));
                dOut.writeObject(new DERSequence(v));
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding IESParameters");
            }
            return bOut.toByteArray();
        }
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == IESParameterSpec.class)
            {
                return currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to ElGamal parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IESParameterSpec))
            {
                throw new InvalidParameterSpecException("IESParameterSpec required to initialise a IES algorithm parameters object");
            }
            this.currentSpec = (IESParameterSpec)paramSpec;
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            try
            {
                ASN1Sequence    s = (ASN1Sequence)aIn.readObject();
                this.currentSpec = new IESParameterSpec(
                                        ((ASN1OctetString)s.getObjectAt(0)).getOctets(),
                                        ((ASN1OctetString)s.getObjectAt(0)).getOctets(),
                                        ((DERInteger)s.getObjectAt(0)).getValue().intValue());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid IES Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid IES Parameter encoding.");
            }
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
        protected String engineToString() 
        {
            return "IES Parameters";
        }
    }
    public static class OAEP
        extends JDKAlgorithmParameters
    {
        OAEPParameterSpec     currentSpec;
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            AlgorithmIdentifier     hashAlgorithm = new AlgorithmIdentifier(
                                                            JCEDigestUtil.getOID(currentSpec.getDigestAlgorithm()),
                                                            DERNull.THE_ONE);
            MGF1ParameterSpec       mgfSpec = (MGF1ParameterSpec)currentSpec.getMGFParameters();
            AlgorithmIdentifier     maskGenAlgorithm = new AlgorithmIdentifier(
                                                            PKCSObjectIdentifiers.id_mgf1, 
                                                            new AlgorithmIdentifier(JCEDigestUtil.getOID(mgfSpec.getDigestAlgorithm()), DERNull.THE_ONE));
            PSource.PSpecified      pSource = (PSource.PSpecified)currentSpec.getPSource();
            AlgorithmIdentifier     pSourceAlgorithm = new AlgorithmIdentifier(
                                                            PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(pSource.getValue()));
            RSAESOAEPparams         oaepP = new RSAESOAEPparams(hashAlgorithm, maskGenAlgorithm, pSourceAlgorithm);
            try
            {
                dOut.writeObject(oaepP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding OAEPParameters");
            }
            return bOut.toByteArray();
        }
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == OAEPParameterSpec.class && currentSpec instanceof OAEPParameterSpec)
            {
                return currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to OAEP parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof OAEPParameterSpec))
            {
                throw new InvalidParameterSpecException("OAEPParameterSpec required to initialise an OAEP algorithm parameters object");
            }
            this.currentSpec = (OAEPParameterSpec)paramSpec;
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            try
            {
                RSAESOAEPparams oaepP = new RSAESOAEPparams((ASN1Sequence)aIn.readObject());
                currentSpec = new OAEPParameterSpec(
                                       oaepP.getHashAlgorithm().getObjectId().getId(), 
                                       oaepP.getMaskGenAlgorithm().getObjectId().getId(), 
                                       new MGF1ParameterSpec(AlgorithmIdentifier.getInstance(oaepP.getMaskGenAlgorithm().getParameters()).getObjectId().getId()),
                                       new PSource.PSpecified(ASN1OctetString.getInstance(oaepP.getPSourceAlgorithm().getParameters()).getOctets()));
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            }
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
        protected String engineToString() 
        {
            return "OAEP Parameters";
        }
    }
    public static class PSS
        extends JDKAlgorithmParameters
    {  
        PSSParameterSpec     currentSpec;
        protected byte[] engineGetEncoded() 
            throws IOException
        {
            PSSParameterSpec    pssSpec = (PSSParameterSpec)currentSpec;
            AlgorithmIdentifier hashAlgorithm = new AlgorithmIdentifier(
                                                JCEDigestUtil.getOID(pssSpec.getDigestAlgorithm()),
                                                DERNull.THE_ONE);
            MGF1ParameterSpec   mgfSpec = (MGF1ParameterSpec)pssSpec.getMGFParameters();
            AlgorithmIdentifier maskGenAlgorithm = new AlgorithmIdentifier(
                                                PKCSObjectIdentifiers.id_mgf1, 
                                                new AlgorithmIdentifier(JCEDigestUtil.getOID(mgfSpec.getDigestAlgorithm()), DERNull.THE_ONE));
            RSASSAPSSparams     pssP = new RSASSAPSSparams(hashAlgorithm, maskGenAlgorithm, new DERInteger(pssSpec.getSaltLength()), new DERInteger(pssSpec.getTrailerField()));
            return pssP.getEncoded("DER");
        }
        protected byte[] engineGetEncoded(
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
            return null;
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == PSSParameterSpec.class && currentSpec instanceof PSSParameterSpec)
            {
                return currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PSS parameters object.");
        }
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof PSSParameterSpec))
            {
                throw new InvalidParameterSpecException("PSSParameterSpec required to initialise an PSS algorithm parameters object");
            }
            this.currentSpec = (PSSParameterSpec)paramSpec;
        }
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
            try
            {
                RSASSAPSSparams pssP = new RSASSAPSSparams((ASN1Sequence)aIn.readObject());
                currentSpec = new PSSParameterSpec(
                                       pssP.getHashAlgorithm().getObjectId().getId(), 
                                       pssP.getMaskGenAlgorithm().getObjectId().getId(), 
                                       new MGF1ParameterSpec(AlgorithmIdentifier.getInstance(pssP.getMaskGenAlgorithm().getParameters()).getObjectId().getId()),
                                       pssP.getSaltLength().getValue().intValue(),
                                       pssP.getTrailerField().getValue().intValue());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid PSS Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid PSS Parameter encoding.");
            }
        }
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
        protected String engineToString() 
        {
            return "PSS Parameters";
        }
    }
}
