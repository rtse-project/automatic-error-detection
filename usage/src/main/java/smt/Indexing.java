package smt;

import debugger.Debugger;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Indexing {

    public static long timeSpent, timeSpentWriting, timeSpentInit, start, end;
    static Debugger debug = Debugger.getInstance();

    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.out.println("Usage with: name root_path");
            System.exit(0);
        }
        try {
            new Indexing().do_job(args);
        } catch (Exception e){

        } finally {
            debug.stop();
        }
    }

    public void do_job(String[] args) throws Exception {
        timeSpent = 0;
        timeSpentWriting = 0;
        //get root path
        String name = args[0];
        debug.setName(name);
        String root_path = args[1];
        String inputFile = args[2];


        //index return times
        {
            File loadFromHD = new File(inputFile);
            if(loadFromHD.exists()){
                debug.log("Indexing types from file: " + loadFromHD.getAbsolutePath());
                long s = System.currentTimeMillis();
                TemporalInfo.getInstance().loadUserTypes(loadFromHD.getAbsolutePath());
                long e = System.currentTimeMillis();
                timeSpentInit = (e - s);
            }

            long s = System.currentTimeMillis();
            debug.log("Indexing types of the project");
            List<TimeTypes> t = IndexingProject.getMethodReturnTime(name, root_path, false);
            long e = System.currentTimeMillis();
            timeSpentInit += (e - s);
            System.out.println(String.format("Get %d methods", t.size()));
            System.out.println("Took: " + timeSpentInit + "ms");
            for (TimeTypes tt : t) {
                System.out.println(tt.toString());
            }
            TemporalInfo.getInstance().addTimeTypes(t);

        }

        System.out.println("Initialization: " + timeSpentInit);
    }

}
