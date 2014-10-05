package controllers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.ColumnFamily;
import models.Field;
import models.Genotype;
import models.Variation;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

// TODO: Class should probably be a singleton.
public class HBase {
	private static final String delimiter = "&";
	private static final String fieldsDelimiter = ",";
	private static Configuration conf = HBaseConfiguration.create();

	public HBase() {
		conf.set("hbase.zookeeper.quorum", "node1");
	}
	
	public List<ColumnFamily> getSchema() {
		List<ColumnFamily> families = new ArrayList<ColumnFamily>();
		
		try {		
			HTable schema = new HTable(conf, "schema");
			
			ColumnFamily currentFamily = null;
			
			Scan scan = new Scan();
			ResultScanner scanner = schema.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				String id = Bytes.toString(r.getRow());
				String[] columnName = id.split(":");
				if (currentFamily == null || !currentFamily.getName().equals(columnName[0])) {
					currentFamily = new ColumnFamily(columnName[0]);
					families.add(currentFamily);
				}
				
				if (r.getValue(Bytes.toBytes("info"), Bytes.toBytes("exists"))[0] == 1) {
					String type = Bytes.toString(r.getValue(Bytes.toBytes("info"), Bytes.toBytes("type")));
					Field field = new Field(columnName[1], type);
					currentFamily.getFields().add(field);
				}
			}
			
			for (ColumnFamily cf : families) {
				if (cf.getFields().size() == 0) {
					families.remove(cf);
				}
			}
			
			schema.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return families;
	}
	
	public Variation getVariationById(String id) {
		Variation variation = new Variation();
		
		try {
			List<ColumnFamily> families = getSchema();
			
			HTable table = new HTable(conf, "main");
			Get get = new Get(Bytes.toBytes(id));
			Result result = table.get(get);
			
			ColumnFamily variations = null;
			for (ColumnFamily cf : families) {
				if (cf.getName().equals("variations")) {
					variations = cf;
				}
			}
			
			for (Field f : variations.getFields()) {
				String fieldName = f.getName();
				String value;
				//TODO: This is last-minute hacking for the demo. Has to be redone.
				if (fieldName.startsWith("aaf") || fieldName.equals("qual") || fieldName.equals("callRate") || fieldName.equals("chi2") || fieldName.startsWith("num") || fieldName.startsWith("total")) {
					value = String.valueOf(Bytes.toFloat(result.getValue(Bytes.toBytes("variations"), Bytes.toBytes(fieldName))));
				}
				else {
					value = Bytes.toString(result.getValue(Bytes.toBytes("variations"), Bytes.toBytes(fieldName)));
				}
				if (value.equals("null")) {
					break;
				}
				variation.addField(fieldName, value);
			}
			
			Map<byte[], byte[]> samples = result.getFamilyMap(Bytes.toBytes("samples"));
			for(Map.Entry<byte[],byte[]> entry : samples.entrySet()) {
				String sampleId = Bytes.toString(entry.getKey());
				String[] gtValues = Bytes.toString(entry.getValue()).split(":");
				Genotype gt = new Genotype();
				gt.addField("sampleId", sampleId);
				gt.addField("gtType", gtValues[0]);
				gt.addField("gtQual", gtValues[1]);
				gt.addField("group", gtValues[2]);
				variation.addGenotype(gt);
			}
			
			Map<byte[], byte[]> nosamples = result.getFamilyMap(Bytes.toBytes("nosamples"));
			for(Map.Entry<byte[],byte[]> entry : nosamples.entrySet()) {
				String sampleId = Bytes.toString(entry.getKey());
				String gtQual = Bytes.toString(entry.getValue());
				Genotype gt = new Genotype();
				gt.addField("sampleId", sampleId);
				gt.addField("gtType", "0");
				gt.addField("gtQual", gtQual);
				gt.addField("group", "");
				variation.addGenotype(gt);
			}
			
			table.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return variation;
	}

	//TODO: Add sorting.
	public List<Variation> searchVariations(String query, String fields, int offset, int limit) {
		List<ColumnFamily> schema = getSchema();
		List<Variation> variations = new ArrayList<Variation>();
		
		try {
			HTable table = new HTable(conf, "main");
			FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
			
			String[] criteria = query.split(delimiter);
			
			for (String c : criteria) {
				int startIndex = -1;
				int endIndex = -1;
				CompareOp comparator = CompareOp.NO_OP;
				String value = "";
				ByteArrayComparable comparable = null;
				
				if (c.indexOf("<=") > -1) {
					startIndex = c.indexOf("<=");
					endIndex = startIndex + 2;
					comparator = CompareOp.LESS_OR_EQUAL;
				}
				else if (c.indexOf("<") > -1) {
					startIndex = c.indexOf("<");
					endIndex = startIndex + 1;
					comparator = CompareOp.LESS;
				}
				else if (c.indexOf(">=") > -1) {
					startIndex = c.indexOf(">=");
					endIndex = startIndex + 2;
					comparator = CompareOp.GREATER_OR_EQUAL;
				}
				else if (c.indexOf(">") > -1) {
					startIndex = c.indexOf(">");
					endIndex = startIndex + 1;
					comparator = CompareOp.GREATER;
				}
				else if (c.indexOf("!=") > -1) {
					startIndex = c.indexOf("!=");
					endIndex = startIndex + 2;
					comparator = CompareOp.NOT_EQUAL;
				}
				else if (c.indexOf("=") > -1) {
					startIndex = c.indexOf("=");
					endIndex = startIndex + 1;
					comparator = CompareOp.EQUAL;
				}
				
				if (comparator == CompareOp.NO_OP) {
					if (c.indexOf(" in(") > -1) {
						startIndex = c.indexOf(" in(");
						comparator = CompareOp.EQUAL;
						String[] values = c.substring(startIndex + 4, c.length() - 1).split(",");
						for (String v : values) {
							value += "^" + v + "$|";
						}
						value = value.substring(0, value.length() - 1);
						comparable = new RegexStringComparator(value);
					}
					else if (c.indexOf(" notIn(") > -1) {
						startIndex = c.indexOf(" notIn(");
						comparator = CompareOp.NOT_EQUAL;
						String[] values = c.substring(startIndex + 7, c.length() - 1).split(",");
						for (String v : values) {
							value += "^" + v + "$|";
						}
						value = value.substring(0, value.length() - 1);
						comparable = new RegexStringComparator(value);
					}
					else if (c.indexOf(" startsWith(") > -1) {
						startIndex = c.indexOf(" startsWith(");
						comparator = CompareOp.EQUAL;
						value = "^" + c.substring(startIndex + 12, c.length() - 1);
						comparable = new RegexStringComparator(value);
					}
					else if (c.indexOf(" endsWith(") > -1) {
						startIndex = c.indexOf(" endsWith(");
						comparator = CompareOp.EQUAL;
						value = c.substring(startIndex + 10, c.length() - 1) + "$";
						comparable = new RegexStringComparator(value);
					}
					else if (c.indexOf(" contains(") > -1) {
						startIndex = c.indexOf(" contains(");
						comparator = CompareOp.EQUAL;
						value = c.substring(startIndex + 10, c.length() - 1);
						comparable = new RegexStringComparator(value);
					}
				}
				else {
					value = c.substring(endIndex);
				}
				
				String column = c.substring(0, startIndex);
				
				SingleColumnValueFilter filter = null;
				if (comparable != null) {
					filter = new SingleColumnValueFilter(
							Bytes.toBytes("variations"),
							Bytes.toBytes(column),
							comparator,
							comparable
							);
				}
				else {
					filter = new SingleColumnValueFilter(
						Bytes.toBytes("variations"),
						Bytes.toBytes(column),
						comparator,
						Bytes.toBytes(Float.valueOf(value))
						);
				}
				
				list.addFilter(filter);
			}
			
			String[] fieldsArray;
			String[] gtFieldsArray;
			if (fields == null) {
				ArrayList<String> fieldsList = new ArrayList<String>();
				ArrayList<String> gtFieldsList = new ArrayList<String>();
				for (ColumnFamily cf : schema) {
					if (cf.getName().equals("variations")) {
						for (Field f : cf.getFields()) {
							fieldsList.add(f.getName());
						}
					}
				}
				fieldsArray = fieldsList.toArray(new String[0]);
				gtFieldsArray = gtFieldsList.toArray(new String[0]);
			}
			else {
				String[] allFields = fields.split(fieldsDelimiter);
				ArrayList<String> fieldsList = new ArrayList<String>();
				ArrayList<String> gtFieldsList = new ArrayList<String>();
				for (String s : allFields) {
					if (s.equals("sampleId") || s.equals("gtType") || s.equals("gtQual") || s.equals("group")) {
						gtFieldsList.add(s);
					}
					else {
						fieldsList.add(s);
					}
				}
				fieldsArray = fieldsList.toArray(new String[0]);
				gtFieldsArray = gtFieldsList.toArray(new String[0]);
			}
			
			Scan scan = new Scan();
			scan.setFilter(list);
			ResultScanner scanner = table.getScanner(scan);
			int counter = 0;
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				if (offset == 0) {
					Variation variation;
					variation = new Variation();
					String id = Bytes.toString(r.getRow());
					variation.addField("id", id);
					for (String f : fieldsArray) {
						String value;
						//TODO: This is last-minute hacking for the demo. Has to be redone.
						if (f.startsWith("aaf") || f.equals("qual") || f.equals("callRate") || f.equals("chi2") || f.startsWith("num") || f.startsWith("total")) {
							value = String.valueOf(Bytes.toFloat(r.getValue(Bytes.toBytes("variations"), Bytes.toBytes(f))));
						}
						else {
							value = Bytes.toString(r.getValue(Bytes.toBytes("variations"), Bytes.toBytes(f)));
						}
						variation.addField(f, value);
					}
					
					if (gtFieldsArray.length > 0) {
						Map<byte[], byte[]> samples = r.getFamilyMap(Bytes.toBytes("samples"));
						for(Map.Entry<byte[],byte[]> entry : samples.entrySet()) {
							String sampleId = Bytes.toString(entry.getKey());
							String[] gtValues = Bytes.toString(entry.getValue()).split(":");
							Genotype gt = new Genotype();
							for (String gtf : gtFieldsArray) {
								if (gtf.equals("sampleId")) {
									gt.addField(gtf, sampleId);
								}
								else if (gtf.equals("gtType")) {
									gt.addField(gtf, gtValues[0]);
								}
								else if (gtf.equals("gtQual")) {
									gt.addField(gtf, gtValues[1]);
								}
								else if (gtf.equals("group")) {
									gt.addField(gtf, gtValues[2]);
								}
							}
							variation.addGenotype(gt);
						}
						
						Map<byte[], byte[]> nosamples = r.getFamilyMap(Bytes.toBytes("nosamples"));
						for(Map.Entry<byte[],byte[]> entry : nosamples.entrySet()) {
							String sampleId = Bytes.toString(entry.getKey());
							String gtQual = Bytes.toString(entry.getValue());
							Genotype gt = new Genotype();;
							for (String gtf : gtFieldsArray) {
								if (gtf.equals("sampleId")) {
									gt.addField(gtf, sampleId);
								}
								else if (gtf.equals("gtType")) {
									gt.addField(gtf, "0");
								}
								else if (gtf.equals("gtQual")) {
									gt.addField(gtf, gtQual);
								}
								else if (gtf.equals("group")) {
									gt.addField(gtf, "");
								}
							}
							variation.addGenotype(gt);
						}
					}
					
					variations.add(variation);
					
					if (++counter >= limit) {
						break;
					}
				}
				else {
					offset--;
				}
			}
			
			table.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return variations;
	}
}
