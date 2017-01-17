package mc.compiler;

import org.json.JSONObject;

public class JSONToASTConverter {

	public void convert(String json){
		JSONObject ast = new JSONObject(json);
		System.out.println("done");
	}

}
