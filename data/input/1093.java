public abstract class NetCDFReplacement extends BinBaseResultZeroReplaceable {
    private class CalculationThread implements Runnable {
        FormatObject bin;
        NetCDFData data;
        Logger logger;
        ContentObject<Double> object;
        public CalculationThread(NetCDFData data, ContentObject<Double> object, FormatObject bin) {
            super();
            this.data = data;
            this.object = object;
            this.bin = bin;
            logger = Logger.getLogger(getClass());
        }
        public void run() {
            increaseNumberOfThreads();
            try {
                Double time = new Double(bin.getAttributes().get("retention_index").toString());
                double[][] spectra = null;
                double resolution = 0.1;
                double increment = 0.1;
                boolean found = true;
                while (spectra == null) {
                    spectra = data.getSpectra(time / 1000, resolution, true);
                    resolution = resolution + increment;
                    if (resolution >= 2) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    logger.info("found spectra with a resolution of: " + resolution);
                    if (isValidForReplaceMent(object)) {
                        replaceAction(spectra, object, data.getNoise(), data);
                    }
                } else {
                    logger.info("don't find any scans with a resolution +/-: " + resolution);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            decreaseNumberOfThreads();
        }
    }
    private Logger logger = Logger.getLogger(NetCDFReplacement.class);
    private int runningThreads = 0;
    public NetCDFReplacement() {
        super();
    }
    private synchronized boolean CanStartThread() {
        return runningThreads < this.getMaxThreads();
    }
    private synchronized void decreaseNumberOfThreads() {
        runningThreads--;
    }
    private void findSpectra(NetCDFData node, ContentObject<Double> object, FormatObject bin) {
        if (CanStartThread()) {
            new Thread(new CalculationThread(node, object, bin)).start();
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            findSpectra(node, object, bin);
        }
    }
    private Source getFile(String nameOfSample) throws ConfigurationException {
        nameOfSample = nameOfSample + ".cdf";
        ConnectionFactory fact = ConnectionFactory.getFactory();
        fact.setProperties(Configable.CONFIG.getConfigProvider().getProperties());
        Connection connection = fact.getConnection();
        Map<String, Connection> map = new HashMap<String, Connection>();
        map.put("CONNECTION", connection);
        try {
            Source source = SourceFactory.newInstance(DatabaseSourceFactoryImpl.class.getName()).createSource(nameOfSample, map);
            if (source.exist()) {
                return source;
            } else {
                logger.info("net cdf file " + nameOfSample + " not found in database, looking in local paths");
            }
            List list = null;
            try {
                list = Configable.CONFIG.getElement("rawdata.path").getChildren();
                if (list == null) {
                    logger.warn("no dirs defined!");
                    return new FileSource(new File(nameOfSample));
                }
                if (list.isEmpty()) {
                    logger.warn("no dirs defined!");
                    return new FileSource(new File(nameOfSample));
                }
            } catch (Exception e) {
                logger.debug("no directories specified for cdf file search");
                return new FileSource(new File(nameOfSample));
            }
            for (int i = 0; i < list.size(); i++) {
                String dir = ((Element) list.get(i)).getText();
                File data = new File(dir + nameOfSample);
                if (data.exists()) {
                    logger.info("file found: " + data);
                    return new FileSource(data);
                } else {
                    logger.debug("file not found: " + data);
                }
            }
            logger.warn("file not found in all specified paths!");
            return new FileSource(new File(nameOfSample));
        } catch (Exception e) {
            logger.info("error: " + e.getMessage());
            return new FileSource(new File(nameOfSample));
        } finally {
            try {
                fact.close(connection);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    private synchronized void increaseNumberOfThreads() {
        runningThreads++;
    }
    protected abstract boolean isListValid(List list);
    @Override
    public boolean isValid() {
        List<SampleObject<String>> samples = getFile().getSamples();
        boolean successfull = true;
        for (int i = 0; i < samples.size(); i++) {
            try {
                Source sampleFile = getFile(samples.get(i).getValue());
                if (sampleFile.exist() == false) {
                    successfull = false;
                    logger.warn(sampleFile.getSourceName() + " does not exist in all available sources!");
                }
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
                return false;
            }
        }
        return successfull;
    }
    protected abstract boolean isValidForReplaceMent(ContentObject<Double> object);
    private void replace(Iterator<ContentObject<Double>> it, ContentObject<Double> object, NetCDFData node) {
        FormatObject bin = this.getFile().getBin(Integer.parseInt(object.getAttributes().get("id").toString()));
        findSpectra(node, object, bin);
        while (it.hasNext()) {
            object = it.next();
            bin = this.getFile().getBin(Integer.parseInt(object.getAttributes().get("id").toString()));
            findSpectra(node, object, bin);
        }
        while (this.runningThreads > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    protected abstract void replaceAction(double[][] spectra, ContentObject<Double> object, Map<Integer, Double> noises, NetCDFData data);
    @SuppressWarnings("unchecked")
    @Override
    public final List replaceZeros(List list) {
        if (this.isListValid(list) == false) {
            logger.info("this data doesn't qualify for replacement using this method");
        }
        List<ContentObject<Double>> data = list;
        Iterator<ContentObject<Double>> it = data.iterator();
        if (this.isSampleBased()) {
            logger.info("sample based replacement");
            if (it.hasNext()) {
                ContentObject<Double> object = it.next();
                SampleObject<String> sample = getFile().getSample(Integer.parseInt((object.getAttributes().get("sample_id"))));
                logger.info("current sample: " + sample.getValue());
                Regression regression = riCorrection(sample);
                if (regression == null) {
                    return list;
                }
                NetCDFData node = null;
                try {
                    node = new MZMineImpl(getFile(sample.getValue()));
                    node.setCorrectionCurve(regression);
                    replace(it, object, node);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    try {
                        node.close();
                    } catch (Exception e) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
        } else {
            logger.error("not supported mode do nothing!");
        }
        return list;
    }
    private Regression riCorrection(SampleObject<String> sample) {
        List<ContentObject<Double>> standards = getFile().getStandardForSample(sample.getValue());
        Regression poly = new CombinedRegression();
        int size = standards.size();
        double[] x = new double[size];
        double[] y = new double[size];
        Collections.sort(standards, new Comparator<ContentObject<Double>>() {
            public int compare(ContentObject<Double> o1, ContentObject<Double> o2) {
                return new Integer(o1.getAttributes().get("retentionindex").toString()).compareTo(new Integer(o2.getAttributes().get("retentionindex").toString()));
            }
        });
        for (int i = 0; i < x.length; i++) {
            ContentObject<Double> assigned = standards.get(i);
            FormatObject bin = this.getFile().getBin(Integer.parseInt(assigned.getAttributes().get("id").toString()));
            x[i] = Double.parseDouble(assigned.getAttributes().get("retentiontime").toString()) / 1000;
            y[i] = Double.parseDouble(bin.getAttributes().get("retention_index").toString()) / 1000;
            logger.info("assigened/bin: " + x[i] + " - " + y[i]);
        }
        if (x.length <= Integer.parseInt(Configable.CONFIG.getValue("bin.correction.polynome"))) {
            logger.info("correction failed not enough standards found");
            return null;
        }
        poly.setData(x, y);
        return poly;
    }
}
