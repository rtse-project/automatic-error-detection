package PCFG;


import PCFG.converter.IConverter;
import PCFG.converter.ToXAL;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTVariable;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GeneralOnlyCFGMain {

	List<ASTClass> classes = new ArrayList<>();
	static final String db_name = "coseacaso";

	public static void main(String[] args) throws Exception {
		GeneralOnlyCFGMain m = new GeneralOnlyCFGMain();
		m.run(args[0], args[1]);
	}


	public void run(String base, String outputdir)  throws Exception{
		if(!outputdir.endsWith("/")){
			outputdir += "/";
		}
		List<String> files = new ArrayList<>();
		{
			Iterator<File> f = IndexingProject.getJavaFiles(base);
			while (f.hasNext()) {
				files.add(f.next().getAbsolutePath());
			}
		}
		for(String f : files) {
			List<ASTClass> classes = JDTVisitor.parse(f, base);
			for(ASTClass c : classes) {
				for(IASTMethod m : c.getMethods()) {
					IM2CFG p = new IM2CFG();
					p.addClass(c, m);
					if(p.getConstraintsSize() == 0){
						continue;
					}
					String className = c.getName();
					String methodName = m.getName();
					String sign = m.getSignature().stream().reduce("", String::concat);
					String qualified = String.format("%s.%s(%s)", className, methodName, sign);
					String qualifiedSafe = qualified.replace(".", "_");
					qualifiedSafe = qualifiedSafe.replace("$","_");
					qualifiedSafe = qualifiedSafe.replace(":","_");
					qualifiedSafe = qualifiedSafe.replace("(","_");
					qualifiedSafe = qualifiedSafe.replace(")","_");
					qualifiedSafe = qualifiedSafe.replace("[","_");
					qualifiedSafe = qualifiedSafe.replace("]","_");

					PCFG graph = p.buildPCFG();
					graph.optimize();
					graph.optimize(new OptimizeTimeAutomata());

					BufferedWriter writer = null;
					writer = new BufferedWriter(new FileWriter(outputdir + qualifiedSafe + ".xal"));
					IConverter toGraphViz = new ToXAL(c);
					writer.write(toGraphViz.convert(graph));
					writer.close();

				}
			}
		}
	}

	public void run() throws Exception {
		String f =  "/Users/giovanni/repository/sources/progs/oMPC.java";
		String base = f.substring(0, f.lastIndexOf("/"));
		List<ASTClass> classes = JDTVisitor.parse(f, base);
		ASTClass c = classes.get(0);
		IM2CFG p = new IM2CFG();
		p.addClass(c, c.getMethods().get(1));
		PCFG graph = p.buildPCFG();
		graph.optimize();
		graph.optimize(new OptimizeTimeAutomata());

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.xal"));
		IConverter toGraphViz = new ToXAL(c);
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}



}
