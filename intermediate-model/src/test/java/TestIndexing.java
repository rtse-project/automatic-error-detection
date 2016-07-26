import IntermediateModelHelper.indexing.Indexing;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTSynchronized;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import parser.Java2AST;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestIndexing {

	public List<ASTClass> init(String filename) throws Exception {
		Java2AST a = new Java2AST( filename );
		a.convertToAST(Java2AST.VERSION.JDT);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		return v.listOfClasses;
	}

	@Test
	public void TestExportChangesJob() throws Exception {
		String filename = getClass().getClassLoader().getResource("ExportChangesJob.java").getFile();
		List<ASTClass> cs = init(filename);
		{
			Indexing index = new Indexing(cs.get(0));
			List<String> methods = index.getListOfMethods();
			List<String> syncMethods = index.getListOfSyncMethods();
			List<String> timedMethods = index.getListOfTimedMethods();
			List<ASTSynchronized> syncBlocks = index.getListOfSyncBlocks();
			assertArrayEquals(methods.toArray(), new String[]{"ExportChangesJob", "run", "findMaxDistinctChanges", "findMaxChanges"});
			assertArrayEquals(syncMethods.toArray(), new String[]{});
			assertArrayEquals(timedMethods.toArray(), new String[]{});
		}
	}

	@Test
	public void TestFailoverTimeoutTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("FailoverTimeoutTest.java").getFile();
		List<ASTClass> cs = init(filename);
		{
			Indexing index = new Indexing(cs.get(0));
			List<String> methods = index.getListOfMethods();
			List<String> syncMethods = index.getListOfSyncMethods();
			List<String> timedMethods = index.getListOfTimedMethods();
			List<ASTSynchronized> syncBlocks = index.getListOfSyncBlocks();
			assertArrayEquals(methods.toArray(), new String[]{"setUp", "tearDown", "getTransportUri",
					"testTimoutDoesNotFailConnectionAttempts","safeClose", "testTimeout", "testInterleaveAckAndException",
					"testInterleaveTxAndException", "doTestInterleaveAndException",
					//"onException","run", //hidden method
					"testUpdateUris"
			});
			assertArrayEquals(syncMethods.toArray(), new String[]{});
			assertArrayEquals(timedMethods.toArray(), new String[]{});
		}
	}

	@Test
	public void TestJavaTimerExampleTask() throws Exception {
		String filename = getClass().getClassLoader().getResource("JavaTimerExampleTask.java").getFile();
		List<ASTClass> cs = init(filename);
		{
			Indexing index = new Indexing(cs.get(0));
			List<String> methods = index.getListOfMethods();
			List<String> syncMethods = index.getListOfSyncMethods();
			List<String> timedMethods = index.getListOfTimedMethods();
			List<ASTSynchronized> syncBlocks = index.getListOfSyncBlocks();
			assertArrayEquals(methods.toArray(), new String[]{"run", "main"});
			assertArrayEquals(syncMethods.toArray(), new String[]{"run"});
			assertArrayEquals(timedMethods.toArray(), new String[]{});
		}
	}

	@Test
	public void TestMCGroupImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("MCGroupImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		{
			Indexing index = new Indexing(cs.get(0));
			List<String> methods = index.getListOfMethods();
			List<String> syncMethods = index.getListOfSyncMethods();
			List<String> timedMethods = index.getListOfTimedMethods();
			List<ASTSynchronized> syncBlocks = index.getListOfSyncBlocks();
			assertArrayEquals(methods.toArray(), new String[]{
				"getSingleton", "setSuspended", "MCGroupImpl",
				//"perform",//hidden
				"setInstanceSuspended",
				//"run","runSupport", //hidden
				"processNetworkInterfaces",
				//"runSupport","run","run", //hidden
				"getControlPort", "interfaceSelected", "validNetworkAddress", "sendToGroup",
				//"runSupport",//hidden
				"sendToGroupSupport", "sendToGroup",
				//"runSupport",//hidden
				"sendToGroupSupport", "handleSocket", "receivePacket", "sendToMember",
			});
			assertArrayEquals(syncMethods.toArray(), new String[]{});
			assertArrayEquals(timedMethods.toArray(), new String[]{});
		}
	}

	@Test
	public void TestProjectServiceImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("ProjectServiceImpl.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TestSmallTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("SmallTest.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TestsocketTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("socketTest.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TestTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("Test.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TesttestLambdas() throws Exception {
		String filename = getClass().getClassLoader().getResource("testLambdas.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TestTimerEvent() throws Exception {
		String filename = getClass().getClassLoader().getResource("TimerEvent.java").getFile();
		List<ASTClass> cs = init(filename);

	}

	@Test
	public void TestUPnPImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("UPnPImpl.java").getFile();
		List<ASTClass> cs = init(filename);

	}


}
