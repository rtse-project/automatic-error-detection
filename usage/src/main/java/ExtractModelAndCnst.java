import PCFG.converter.IConverter;
import PCFG.converter.ToUppaal;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.structure.RuntimeConstraint;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by giovanni on 02/05/2017.
 */

/**
 +++++++++++ issue KAFKA-3540 --
 - ecff8544dd45e8cf0fcf04f5e0e716d3e21c9f20
 = f72203ee9223d3b724ee67bdad9912612dd72f63
/Users/giovanni/repository/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/ConsumerCoordinator.java
ConsumerCoordinator
commitOffsetsSync
kafka_3540.xml
constraint_kafka_3540.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue KAFKA-4426 --
 - ecff8544dd45e8cf0fcf04f5e0e716d3e21c9f20
 = f72203ee9223d3b724ee67bdad9912612dd72f63
/Users/giovanni/repository/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/AbstractCoordinator.java
AbstractCoordinator
ensureCoordinatorReady
kafka_4426.xml
constraint_kafka_4426.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue KAFKA-4290 --
 - 72d5675a7cfc64a5547b921672856ea4afc4f4c2
 = 8c367790fa1dd337a8a4bad393cd856dfcd92c31
/Users/giovanni/repository/kafka/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/WorkerCoordinator.java
WorkerCoordinator
poll
kafka_4290.xml
constraint_kafka_4290.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue KAFKA-4306 --
 - 72d5675a7cfc64a5547b921672856ea4afc4f4c2
 = c9e99f297f3090cd348e231dbeaf69c388de1234
/Users/giovanni/repository/kafka/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/DistributedHerder.java
DistributedHerder
stop
kafka_4306.xml
constraint_kafka_4306.conf
/Users/giovanni/repository/kafka/clients/src/main/java


 */
public class ExtractModelAndCnst {

    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            String f =  "/Users/giovanni/repository/java-xal/code-instrumentation/src/test/java/UndefinedTimeBehaviour.java";
            ASTClass c = JDTVisitor.parse(f,System.getProperty("user.dir")).get(0);
            IASTMethod m = c.getAllMethods().get(0);
            def(c,m, "graph.xml", "constraint.conf");
            return;
        }

        String file = args[0];
        String className = args[1];
        String methodName = args[2];
        String modelName = args[3];
        String outputName = args[4];
        List<ASTClass> cs = JDTVisitor.parse( file, args[5]);

        System.out.println(">> Writing model: " + modelName);
        System.out.println(">> Writing conf file: " + outputName);
        ASTClass c = null;
        for(ASTClass cc : cs){
            if(cc.getName().equals(className))
                c = cc;
        }
        if(c == null) {
            System.err.println("Cannot found the correct class");
            System.exit(1);
        }
        IASTMethod m = c.getFirstMethodByName(methodName);
        def(c,m, modelName, outputName);
    }

    public static void def(ASTClass c, IASTMethod m, String nameModel, String nameConstraint) throws Exception {

        IM2CFG p = new IM2CFG();
        p.addClass(c,m);
        PCFG graph = p.buildPCFG();
        graph.optimize();
        graph.optimize(new OptimizeTimeAutomata());

        BufferedWriter writer = null;
        //writer = new BufferedWriter(new FileWriter("graph.xal"));
        writer = new BufferedWriter(new FileWriter(nameModel));
        IConverter toUppaal = new ToUppaal(c, ToUppaal.NAMING.LINE);
        //writer.write(toGraphViz.convert(graph));
        writer.write(toUppaal.convert(graph));
        writer.close();

        writer = new BufferedWriter(new FileWriter(nameConstraint));
        boolean first = true;
        for(RuntimeConstraint cnst : p.getResetRuntimeConstraints()){
            if(first){
                writer.write("class;method;line;var\n");
            }
            writer.write(cnst.toString() + "\n");
            writer.flush();
            first = false;
        }
        writer.close();
    }
}
