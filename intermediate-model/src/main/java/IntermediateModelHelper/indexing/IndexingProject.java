package IntermediateModelHelper.indexing;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The following class analyze a project and stores in MongoDB the indexing of it.
 * It creates a database per project. If a class name already exists in the database is not added.
 * Two classes are equals if they share the same name and packaging.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingProject {
	MongoConnector db;
	String projectName;

	/**
	 * Construct the db given the project name.
	 * @param name	Project Name
	 */
	public IndexingProject(String name) {
		this.db = MongoConnector.getInstance(name);
		this.projectName = name;
	}

	public String getProjectName() {
		return projectName;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexProject(String base_path) {
		return indexProject(base_path, false);
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @param deleteOld	If true delete the database so we have fresh data.
	 * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexProject(String base_path, boolean deleteOld){
		//remove old data
		if(deleteOld) delete();

		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			Java2AST a = null;
			try {
				a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (ParseErrorsException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IndexingFile indexing = new IndexingFile();
				IndexData index = indexing.index(c);
				db.add(index);
			}
			n_file++;
		}
		return n_file;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @param delete	If true delete the database so we have fresh data.
	 * @return			A {@link Future} that will contain soon or late the number of file parsed. <u>IT WILL NOT RETURN THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public Future<Integer> asyncIndexProject(String base_path, boolean delete){
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> task = () -> indexProject(base_path, delete);
		return executor.submit(task);
	}
	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return			A {@link Future} that will contain soon or late the number of file parsed. <u>IT WILL NOT RETURN THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public Future<Integer> asyncIndexProject(String base_path){
		return asyncIndexProject(base_path, false);
	}


	private void delete(){
		db.getDb().drop();
	}

}