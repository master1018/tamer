package chaski.proc.extract;

import static chaski.utils.CommonFileOperations.deleteIfExists;
import static chaski.utils.ProgramLogic.testFileExistOnHDFSOrDie;
import static chaski.utils.ProgramLogic.testFileNotExistOnHDFSOrDie;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import jpfm.configurable.ConfigClass;
import jpfm.configurable.ConfigField;
import jpfm.configurable.ConfigParaSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import chaski.proc.ConfigurableMapper;
import chaski.utils.CommonFileOperations;
import chaski.utils.MapReduceJob;

/**
 * Driver for extracting phrases. For all the parameters you want ot pass
 * to Mapper/Reducer, you may specify
 * @author qing
 *
 */
@ConfigClass(value = "extract", help = "Extract phrase and count occurance of each phrase")
public class ExtractDriver {

    private static final Log LOG = LogFactory.getLog(ExtractDriver.class);

    @ConfigField(name = "max-phrase-len", alias = { "mfl", "extract.mfl" }, required = false, defaultValue = "7", help = "Maximum length we will try to extract")
    public int maxPhraseLength = 7;

    @ConfigField(name = "corpus", alias = { "extract.corpus" }, required = true, help = "The input corpus file. We assume it is on HDFS, and it should be generated by preprocessing stage")
    public String inputFile;

    @ConfigField(name = "phrase", alias = { "extract.phrase" }, required = true, help = "The output directory of the extracted phrases")
    public String outputDir;

    @ConfigField(name = "overwrite", alias = { "extract.overwrite" }, required = false, defaultValue = "false", help = "Whether the output director will be overwritten if exists, by default it is false, and program will die if it exists")
    public boolean overwrite;

    @ConfigField(name = "queues", alias = { "qu", "extract.queue" }, required = false, defaultValue = "m45", help = "Specify the queue that the MR-Job will be submitted, if not specified, a warning will be displayed but the execution will continue.")
    public String queues;

    @ConfigField(name = "num-reducer", alias = { "extract.nr" }, required = false, defaultValue = "40", help = "Specify the number of reducers")
    public int numberReducer;

    @ConfigField(name = "num-mapper", alias = { "extract.nm" }, required = false, defaultValue = "50", help = "Specify the number of mappers")
    public int numberMapper;

    @ConfigField(name = "heap", alias = { "extract.hp", "extract.heap" }, required = false, defaultValue = "1024m", help = "Specify how much memory should each children task (Map/Reduce) use, default is 1024m")
    public String heap;

    @ConfigField(name = "map-per-node", alias = { "extract.mpn" }, required = false, defaultValue = "-1", help = "Specify how many map task can one task tracker run.")
    public int mapPerNode;

    @ConfigField(name = "red-per-node", alias = "extract.rpn", required = false, defaultValue = "-1", help = "Specify how many reduce task can one task tracker run.")
    public int reducePerNode;

    @ConfigField(name = "verbose", alias = { "vbs" }, required = false, defaultValue = "true", help = "The detail information?")
    public boolean verbose;

    @ConfigField(name = "filters", alias = { "extract.filters", "extract.flts" }, required = false, defaultValue = "", help = "Semi-colon separated list of extract stage filters, specify the full class name (including package)")
    public String filterClasses;

    @ConfigParaSet(name = "ex.map.", pattern = "ex\\.map\\..*", help = "All the additional parameters you want to pass to the mapper, some filter may require it. For example, --ex.map.fltp 5")
    public Map<String, String> mapperParameters = null;

    @ConfigParaSet(name = "ex.red.", pattern = "ex\\.red\\..*", help = "All the additional parameters you want to pass to the reducer, some filter may require it. For example, --ex.red.fltp 5")
    public Map<String, String> reducerParameters = null;

    private void putParameters(MapReduceJob job, Map<String, String> m) {
        for (Map.Entry<String, String> ent : m.entrySet()) {
            job.getConfiguration().set(ent.getKey(), ent.getValue());
        }
    }

    public void execute() throws Exception {
        testFileExistOnHDFSOrDie(inputFile, "File not found " + inputFile + " on HDFS ", FileNotFoundException.class, LOG);
        if (!overwrite) {
            testFileNotExistOnHDFSOrDie(outputDir, "File already exists : " + outputDir + " on HDFS ", IOException.class, LOG);
        } else {
            deleteIfExists(outputDir);
        }
        if (maxPhraseLength < 1) {
            LOG.fatal("Maximum phrase length must be larger than 1 ");
            throw new Exception("Maximum phrase length must be larger than 1 ");
        }
        if (maxPhraseLength > 12) {
            LOG.warn("Maximum phrase length is quite large, it may fill your HDFS and takes a long time! ");
        }
        String jobName = "PhraseExtraction[" + inputFile + "==>" + outputDir + "]";
        MapReduceJob job = null;
        if (queues.trim().length() == 0) {
            job = new MapReduceJob(jobName);
        } else {
            job = new MapReduceJob(jobName, queues);
        }
        job.setBasicInfo(TextInputFormat.class, Text.class, Text.class, TextOutputFormat.class, ExtractMapper.class, ExtractReducer.class, ExtractCombiner.class, numberReducer, numberMapper, inputFile, outputDir);
        job.setSplitSizeRange(5 * 1024 * 1024, 10 * 1024 * 1024);
        ConfigurableMapper.packParam(job, ExtractMapper.class, "maxPhraseLength", maxPhraseLength);
        ConfigurableMapper.packParam(job, ExtractMapper.class, "filterClasses", filterClasses);
        putParameters(job, mapperParameters);
        putParameters(job, reducerParameters);
        job.setChildTaskHeapSize(heap);
        job.setMaxMapTasksPerNode(mapPerNode);
        job.setMaxReduceTasksPerNode(reducePerNode);
        job.submit();
        job.waitForCompletion(verbose);
    }
}