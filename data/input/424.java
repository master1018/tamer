public class CentameterPowerDatumDataSource extends CentameterSupport implements DatumDataSource<PowerDatum>, MultiDatumDataSource<PowerDatum> {
    public static final String DEFAULT_AMPS_FIELD_NAME = "pvAmps";
    public static final String DEFAULT_VOLTS_FIELD_NAME = "pvVolts";
    private String ampsFieldName = DEFAULT_AMPS_FIELD_NAME;
    private String voltsFieldName = DEFAULT_VOLTS_FIELD_NAME;
    @Override
    public Class<? extends PowerDatum> getDatumType() {
        return PowerDatum.class;
    }
    @Override
    public PowerDatum readCurrentDatum() {
        DataCollector dataCollector = null;
        byte[] data = null;
        try {
            dataCollector = getDataCollectorFactory().getObject();
            dataCollector.collectData();
            data = dataCollector.getCollectedData();
        } finally {
            if (dataCollector != null) {
                dataCollector.stopCollecting();
            }
        }
        if (data == null) {
            log.warn("Null serial data received, serial communications problem");
            return null;
        }
        return getPowerDatumInstance(DataUtils.getUnsignedValues(data), getAmpSensorIndex());
    }
    @Override
    public Class<? extends PowerDatum> getMultiDatumType() {
        return PowerDatum.class;
    }
    @Override
    public Collection<PowerDatum> readMultipleDatum() {
        DataCollector dataCollector = null;
        List<PowerDatum> result = new ArrayList<PowerDatum>(3);
        long endTime = isCollectAllSourceIds() && getSourceIdFilter().size() > 1 ? System.currentTimeMillis() + (getCollectAllSourceIdsTimeout() * 1000) : 0;
        Set<String> sourceIdSet = new HashSet<String>(getSourceIdFilter().size());
        try {
            dataCollector = getDataCollectorFactory().getObject();
            do {
                dataCollector.collectData();
                byte[] data = dataCollector.getCollectedData();
                if (data == null) {
                    log.warn("Null serial data received, serial communications problem");
                    return null;
                }
                short[] unsigned = DataUtils.getUnsignedValues(data);
                for (int ampIndex = 1; ampIndex <= 3; ampIndex++) {
                    if ((ampIndex & getMultiAmpSensorIndexFlags()) != ampIndex) {
                        continue;
                    }
                    PowerDatum datum = getPowerDatumInstance(unsigned, ampIndex);
                    if (datum != null) {
                        if (!sourceIdSet.contains(datum.getSourceId())) {
                            result.add(datum);
                            sourceIdSet.add(datum.getSourceId());
                        }
                    }
                }
            } while (System.currentTimeMillis() < endTime && sourceIdSet.size() < getSourceIdFilter().size());
        } finally {
            if (dataCollector != null) {
                dataCollector.stopCollecting();
            }
        }
        return result.size() < 1 ? null : result;
    }
    private PowerDatum getPowerDatumInstance(short[] unsigned, int ampIndex) {
        String addr = String.format(getSourceIdFormat(), unsigned[CENTAMETER_ADDRESS_IDX], ampIndex);
        float amps = (float) CentameterUtils.getAmpReading(unsigned, ampIndex);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Centameter address %s, count %d, amp1 %.1f, amp2 %.1f, amp3 %.1f", addr, (unsigned[2] & 0xF), CentameterUtils.getAmpReading(unsigned, 1), CentameterUtils.getAmpReading(unsigned, 2), CentameterUtils.getAmpReading(unsigned, 3)));
        }
        PowerDatum datum = new PowerDatum();
        if (getAddressSourceMapping() != null && getAddressSourceMapping().containsKey(addr)) {
            addr = getAddressSourceMapping().get(addr);
        }
        if (getSourceIdFilter() != null && !getSourceIdFilter().contains(addr)) {
            if (log.isInfoEnabled()) {
                log.info("Rejecting source [" + addr + "] not in source ID filter set");
            }
            return null;
        }
        datum.setSourceId(addr);
        datum.setCreated(new Date());
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ampsFieldName, amps);
        props.put(voltsFieldName, getVoltage());
        ClassUtils.setBeanProperties(datum, props);
        return datum;
    }
    public String getAmpsFieldName() {
        return ampsFieldName;
    }
    public void setAmpsFieldName(String ampsFieldName) {
        this.ampsFieldName = ampsFieldName;
    }
    public String getVoltsFieldName() {
        return voltsFieldName;
    }
    public void setVoltsFieldName(String voltsFieldName) {
        this.voltsFieldName = voltsFieldName;
    }
}
