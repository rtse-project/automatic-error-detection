import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.CreateIntemediateModel;
import intermediateModel.visitors.JDTVisitor;
import IntermediateModelHelper.heuristic.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void java8_main(String[] args) throws Exception {

		if(args.length < 1){
			usage();
			return;
		}
		Java2AST a = new Java2AST(args[0]);
		a.convertToAST(Java2AST.VERSION.Java_8);
		ParserRuleContext ast = a.getContext();
		ParseTreeWalker walker = new ParseTreeWalker();
		CreateIntemediateModel sv = new CreateIntemediateModel();
		System.out.println("Create IM");
		walker.walk(sv, ast);


		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);

		for(ASTClass c : sv.listOfClasses){

			ah.analyze(c);
			String s = Arrays.toString( ah.getTimeConstraint().toArray() );
			System.err.println(s);
			System.err.println("__________");
		}


	}

	public static void main(String[] args) throws Exception {

		if(args.length < 1){
			usage();
			return;
		}

		List<String> files = new ArrayList<>();
		files.add( Main.class.getResource("JavaTimerExampleTask.java").getFile() );
		files.add( Main.class.getResource("FailoverTimeoutTest.java").getFile() );
		files.add( Main.class.getResource("MCGroupImpl.java").getFile() );
		files.add( Main.class.getResource("UpnPImpl.java").getFile() );

		files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
			CompilationUnit ast = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(ast);
			ast.accept(v);


			ApplyHeuristics ah = new ApplyHeuristics();
			ah.subscribe(ThreadTime.class);
			ah.subscribe(SocketTimeout.class);
			ah.subscribe(TimeoutResources.class);
			ah.subscribe(TimerType.class);
			ah.subscribe(AnnotatedTypes.class);

			for(ASTClass c : v.listOfClasses){

				ah.analyze(c);
				String s = Arrays.toString( ah.getTimeConstraint().toArray() );
				System.err.println("[" + f + "]");
				System.err.println(s);
				System.err.println("__________");
			}
		}

	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}