import PCFG.Main;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import parser.Java2AST;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestBugs {

	@Test
	public void TestBug23() throws Exception {
		IM2PCFG p = new IM2PCFG();

		//first method
		String f =  TestBugs.class.getClassLoader().getResource("bugs/Thread_1.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		String method = "run";
		p.addClass(c, method);

		//add the second method
		f =  TestBugs.class.getClassLoader().getResource("bugs/Thread_2.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast);
		ast.accept(v);
		c = v.listOfClasses.get(0);
		p.addClass(c, method);

		// build
		PCFG g = p.buildPCFG();

		System.out.println(g.toGraphViz(false));

		assertEquals(g.getV().size(), 6);
		assertEquals(g.getE().size(), 4);
		assertEquals(g.getSyncNodes().size(), 0 );
		assertEquals(g.getProcesses().size(), 2 );
		assertEquals(g.getESync().size(), 4 );

	}
}
