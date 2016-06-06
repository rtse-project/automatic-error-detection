import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.CreateIntemediateModel;
import IntermediateModel.visitors.SearchTimeConstraint;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args) throws Exception {

		if(args.length < 1){
			usage();
			return;
		}
		Java2AST a = new Java2AST(args[0]);
		a.convertToAST();
		ParserRuleContext ast = a.getContext();
		ParseTreeWalker walker = new ParseTreeWalker();
		CreateIntemediateModel sv = new CreateIntemediateModel();

		walker.walk(sv, ast);

		SearchTimeConstraint stc = new SearchTimeConstraint();

		for(ASTClass c : sv.listOfClasses){
			stc.annotateClass(c);
			System.out.println(c.toString());
			System.out.println("__________");
		}
		String s = Arrays.toString( sv.listOfClasses.toArray() );
		System.out.print(s);
	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
