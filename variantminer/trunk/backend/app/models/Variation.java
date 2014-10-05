package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Variation {
	private Map<String, String> fields;
	@JsonInclude(Include.NON_EMPTY)
	private List<Genotype> genotypes;
	
	public Variation() {
		fields = new HashMap<String, String>();
		genotypes = new ArrayList<Genotype>();
	}

	@JsonAnyGetter
	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
	
	public List<Genotype> getGenotypes() {
		return genotypes;
	}
	
	public void setGenotypes(List<Genotype> genotypes) {
		this.genotypes = genotypes;
	}
	
	public void addField(String field, String value) {
		fields.put(field, value);
	}
	
	public void addGenotype(Genotype genotype) {
		genotypes.add(genotype);
	}
	
}
