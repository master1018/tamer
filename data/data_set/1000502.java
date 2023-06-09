package net.solarnetwork.node.power.impl.centameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.solarnetwork.node.DataCollector;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.centameter.CentameterSupport;
import net.solarnetwork.node.centameter.CentameterUtils;
import net.solarnetwork.node.power.PowerDatum;
import net.solarnetwork.node.util.ClassUtils;
import net.solarnetwork.node.util.DataUtils;

/**
 * Implementation of {@link DatumDataSource} {@link PowerDatum} objects,
 * using a Centameter amp sensor.
 * 
 * <p>Normally Centameters are used to monitor consumption, but in some 
 * situations they can be used as a low-cost montior for generation, 
 * especially if the generation device cannot be communicated with.</p>
 * 
 * <p>This implementation relies on a device that can listen to the radio
 * signal broadcast by a Cent-a-meter monitor and write that data to a
 * local serial port. This class will read the Cent-a-meter data from the
 * serial port to generate consumption data.</p>
 * 
 * <p>It assumes the {@link DataCollector} implementation blocks until 
 * appropriate data is available when the {@link DataCollector#collectData()}
 * method is called.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>ampsFieldName</dt>
 *   <dd>The bean property on {@link PowerDatum} to set the amp reading
 *   value collected from the Centameter. Defaults to 
 *   {@link #DEFAULT_AMPS_FIELD_NAME}.</dd>
 *   
 *   <dt>voltsFieldName</dt>
 *   <dd>The bean property on {@link PowerDatum} to set the {@code voltage}
 *   value. Defaults to {@link #DEFAULT_AMPS_FIELD_NAME}.</dd>
 * </dl>
 * 
 * @author matt
 * @version $Revision$
 */
public class CentameterPowerDatumDataSource extends CentameterSupport implements DatumDataSource<PowerDatum>, MultiDatumDataSource<PowerDatum> {

    /** The default value for the {@code ampsFieldName} property. */
    public static final String DEFAULT_AMPS_FIELD_NAME = "pvAmps";

    /** The default value for the {@code voltsFieldName} property. */
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

    /**
	 * @return the ampsFieldName
	 */
    public String getAmpsFieldName() {
        return ampsFieldName;
    }

    /**
	 * @param ampsFieldName the ampsFieldName to set
	 */
    public void setAmpsFieldName(String ampsFieldName) {
        this.ampsFieldName = ampsFieldName;
    }

    /**
	 * @return the voltsFieldName
	 */
    public String getVoltsFieldName() {
        return voltsFieldName;
    }

    /**
	 * @param voltsFieldName the voltsFieldName to set
	 */
    public void setVoltsFieldName(String voltsFieldName) {
        this.voltsFieldName = voltsFieldName;
    }
}
