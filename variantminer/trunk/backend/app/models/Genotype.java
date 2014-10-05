package models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public class Genotype {
	private Map<String, String> fields;
	
	public Genotype() {
		fields = new HashMap<String, String>();
	}

	@JsonValue
	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
	
	public void addField(String field, String value) {
		fields.put(field, value);
	}
}
