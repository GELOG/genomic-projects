package controllers;

import java.util.List;

import models.ColumnFamily;
import models.Variation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.*;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        addCorsHeaders();
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result tables() {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	HBase hbase = new HBase();
    	List<ColumnFamily> families = hbase.getSchema();
    	
    	String json = "";
    	try {
			json = mapper.writeValueAsString(families);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
        addCorsHeaders();
    	return ok(json);
    }
    
    public static Result variation(String id) {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	HBase hbase = new HBase();
    	Variation variation = hbase.getVariationById(id);
    	
    	String json = "";
    	try {
			json = mapper.writeValueAsString(variation);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
        addCorsHeaders();
    	if (json.equals("{}")) {
    		return notFound("Variation ID was not found.");
    	}
    	else {
    		return ok(json);
    	}
    }
    
    public static Result variationsSearch(String query, String fields, int offset, int limit) {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	HBase hbase = new HBase();
    	List<Variation> variations = hbase.searchVariations(query, fields, offset, limit);
    	
    	String json = "";
    	try {
			json = mapper.writeValueAsString(variations);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
        addCorsHeaders();
    	return ok(json);
    }

    public static void addCorsHeaders()
    {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Methods", "*");
        response().setHeader("Access-Control-Request-Headers", "*");
        response().setHeader("Access-Control-Allow-Credentials", "true");
    }

    public static Result cors(String path) {
        addCorsHeaders();
        return ok();
    }

}
