package server.handler;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import server.handler.middleware.indexMW;
import server.handler.outputFormat.OutputData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GetAllFilesByType extends indexMW {

	String par = "type";

	@Override
	protected void handle(HttpExchange he, Map<String, String> parameters, String name) throws IOException {
		if(!parameters.containsKey(par)){
			String response = "Incorrect format of parameters.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		String type = parameters.get(par);
		MongoConnector db = MongoConnector.getInstance(name);
		if(!db.getIndexStatus()){
			String response = "Indexes are not yet computed.";
			he.sendResponseHeaders(406, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		List<IndexData> threads = db.getType(type);
		List<OutputData> files = new ArrayList<>();
		for(IndexData d : threads){
			if(! files.contains(d.getPath()) )
				files.add(new OutputData(d));
		}
		ObjectMapper json = new ObjectMapper();
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(files);
		he.getResponseHeaders().add("Content-Type","application/json");
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();

	}
}