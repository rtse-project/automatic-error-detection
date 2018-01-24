package smt.evaluation;

import converter.ClassAnalyzer;
import converter.Statistic;
import debugger.Debugger;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.indexing.IndexingProject;
import smt.exception.VariableNotCorrect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Main {

    public static long timeSpent, timeSpentWriting, timeSpentInit, timeSpentParsing, start, end;
    static Debugger debug = Debugger.getInstance();

    public static void main(String[] args) throws IOException {
        if(args.length < 3){
            System.out.println("Usage with: name root_path output_path");
            System.exit(0);
        }
        try {
            new Main().do_job(args);
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            debug.stop();
        }
    }

    public void do_job(String[] args) throws Exception {
        timeSpent = 0;
        timeSpentWriting = 0;
        timeSpentParsing = 0;
        //get root path
        String name = args[0];
        debug.setName(name);
        String root_path = args[1];
        String output = args[2];
        if(!output.endsWith("/")){
            output = output + "/";
        }

        //index return times
        {
            File loadFromHD = new File("config/" + name + "_types.csv");
            if(loadFromHD.exists()){
                debug.log("Indexing types from file: " + loadFromHD.getAbsolutePath());
                long s = System.currentTimeMillis();
                TemporalInfo.getInstance().loadUserTypes(loadFromHD.getAbsolutePath());
                long e = System.currentTimeMillis();
                timeSpentInit = (e - s);
            } else {
                long s = System.currentTimeMillis();
                debug.log("Indexing types of the project");
                List<TimeTypes> t = IndexingProject.getMethodReturnTime(name, root_path, true);
                long e = System.currentTimeMillis();
                timeSpentInit = (e - s);
                System.out.println(String.format("Get %d methods", t.size()));
                System.out.println("Took: " + timeSpentInit + "ms");
                for (TimeTypes tt : t) {
                    System.out.println(tt.toString());
                }
                TemporalInfo.getInstance().addTimeTypes(t);
            }
        }

        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);

        BufferedWriter writer = new BufferedWriter(new FileWriter(output + name + ".csv"));
        writer.write("class;method;line;variable\n");

        //stats
        int nClass = 0;
        int nFiles = 0;
        int nMethod = 0;
        int nMethodError = 0;
        int nClassError = 0;
        int nError = 0;

        boolean atLeastOneError;

        List<String> errors = new ArrayList<>();
        long sParse,eParse;
        long init = System.currentTimeMillis();
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
            debug.log("Working on " + filename);
            nFiles++;
            //each class
            sParse = System.currentTimeMillis();
            List<ASTClass> result = JDTVisitor.parse(filename, root_path, false);
            eParse = System.currentTimeMillis();
            timeSpentParsing += (eParse - sParse);
            for(ASTClass c : result){
                nClass++;
                nMethod = nMethod + c.getCountMethod();
                ClassAnalyzer ca = new ClassAnalyzer(c);
                HashMap<IASTMethod, List<VariableNotCorrect>> err = new HashMap<>();
                try {
                    err = ca.getErrors();
                } catch (Exception x){
                    System.out.println("Error in class " + c.fullName());
                    System.out.println(c.getPath());
                    System.out.println(x.getMessage());
                    debug.log("Error in file:" + filename);
                    //throw e;
                }
                start();
                atLeastOneError = false;
                for(IASTMethod m : err.keySet()) {
                    List<VariableNotCorrect> vars = err.get(m);
                    if(vars.size() > 0) {
                        nMethodError++;
                        atLeastOneError = true;
                    }
                    for(VariableNotCorrect v : vars) {
                        nError++;
                        String e = String.format(
                                "%s;%s;%d;%s\n",
                                c.fullName(), m.getName(), v.getWhere().getLine(), v.getVarName()
                        );
                        if(!errors.contains(e)) {
                            errors.add(e);
                            writer.write(e);
                            writer.flush();
                        }
                    }
                }
                if(atLeastOneError){
                    nClassError++;
                }
                stop();
            }
        }
        long ending = System.currentTimeMillis() - init ;
        writer.close();
        debug.log("--FINISH--");
        timeSpent = ending - timeSpentWriting;
        System.out.println("Initialization: " + timeSpentInit);
        System.out.println("Parsing: " + timeSpentParsing);
        System.out.println("Total Writing: " + timeSpentWriting);
        System.out.println("Total Net: " + timeSpent);
        System.out.println("Total Time: " + ending);
        BufferedWriter stats = new BufferedWriter(new FileWriter(name + ".log"));
        stats.write("\nTime Writing: " + timeSpentWriting + "ms");
        stats.write("\nTime Parsing: " + timeSpentParsing + "ms");
        stats.write("\nTime Net: " + timeSpent + "ms");
        stats.write("\nTotal Time: " + ending + "ms");
        stats.write("\n# Files: " + nFiles);
        stats.write("\n# Classes: " + nClass);
        stats.write("\n# Methods: " + nMethod);
        stats.write("\n# Error: " + nError);
        stats.write("\n# Classes w/ error: " + nClassError);
        stats.write("\n# Methods w/ error: " + nMethodError);
        //stats.write("\n# Methods Analyzed: " + Statistic.getnMethod());
        stats.write("\n# Methods Path Analyzed: " + Statistic.getnMethodPath());
        stats.flush();
        stats.close();
    }

    public static void start(){
        start = System.currentTimeMillis();
    }


    public static void stop(){
        end = System.currentTimeMillis();
        timeSpentWriting += (end-start);
    }
}
