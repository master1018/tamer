package org.signserver.client;

import java.util.ArrayList;

public interface PerformanceTestTask {

    /**
	 * Run once for each new class
	 * @param setupData the result from the last setup with the same Task-type or null if this is the first. 
	 * @param timeToRun in ms
	 * @param baseURLString in the form http://localhost:8080/signserver/
	 * @return the data to pass on to other new classes of the same type
	 */
    Object setup(Object setupData, long timeToRun, String baseURLString);

    /**
	 * Run over and over until testtime runs out
	 * @param threadId
	 * @return false to break test
	 */
    boolean invoke(int threadId);

    /**
	 * Create relevant diagrams for this module.
	 * @param statisticsDirectory is where output should be written
	 * @param explanationRow is the explanation string for each row
	 * @param processedData is all the data
	 */
    void createDiagrams(String currentFileName, String statisticsDirectory, ArrayList<String> explanationRow, ArrayList<ArrayList<Double>> processedData);
}
