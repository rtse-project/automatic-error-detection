package daikon.instrumentation;


import daikon.instrumentation.preprocess.ReadConfFile;
import daikon.instrumentation.preprocess.WatchPoints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;

/**
 * Created by giovanni on 27/04/2017.
 */
public class Agent {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.debug("[DEBUG] Agent Injected");
        if(agentArgs != null){
            System.out.println("Args: " + agentArgs + "\n");
            String confPath = agentArgs;
            ReadConfFile readConfFile = new ReadConfFile(confPath);
            readConfFile.start();
            System.out.println("Points: " + WatchPoints.getInstance().size());
        }

        // registers the transformer
        inst.addTransformer(new InstrumentDaikon());
    }
}
