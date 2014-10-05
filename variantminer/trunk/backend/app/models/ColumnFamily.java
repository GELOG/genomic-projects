package models;

import java.util.ArrayList;
import java.util.List;

public class ColumnFamily {
	private String name;
	private List<Field> fields;
	
	public ColumnFamily(String name) {
		this.name = name;
		this.fields = new ArrayList<Field>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

}
