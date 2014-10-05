import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

//TODO: Manage additions (batch + individual) and updates past initial batch load
//TODO: OPTIMIZATION (mostly field types, currently all strings) <--- This will severely affect the API. A lot more work than it might sound.
public class Main {
	
	//TODO: Load this from config file?
	//TODO: Finish remaining fields for API
	private final static String[] SCHEMA =  {
		//"variations:id|string",
		"variations:chrom|string",
		"variations:start|int",
		"variations:end|int",
		"variations:ref|string",
		"variations:alt|string",
		"variations:gene|string",
		"variations:qual|float",
		"variations:callRate|float",
		"variations:rsIds|string",
		"variations:pfamDomain|string",
		"variations:rmsk|byte",
		"variations:inCpgIsland|byte",
		"variations:inSegdup|byte",
		"variations:isConserved|byte",
		"variations:gmsIllumina|byte",
		"variations:gmsSolid|byte",
		"variations:inCse|byte",
		"variations:gerpBpScore|byte",
		"variations:depth|int",
		"variations:rmsMapQual|byte",
		"variations:impactSeverity|string",
		"variations:aafEspAll|float",
		"variations:aafEspEa|float",
		"variations:aaf1kgAll|float",
		"variations:aaf1kgEa|float",
		"variations:numHomRef|short",
		"variations:numHet|short",
		"variations:numHomAlt|short",
		"variations:totalFg1|short",
		//"variations:percentFg1|float",
		"variations:totalFg2|short",
		//"variations:percentFg2|float",
		"variations:totalFg3|short",
		//"variations:percentFg3|float",
		//"variations:totalAis|short",
		//"variations:percentAis|float",
		"variations:totalCtrl|short",
		//"variations:percentCtrl|float",
		"variations:oddRatio|float",
		"variations:chi2|float",
//		"variations:totalGreen|short",
//		"variations:percentGreen|float",
//		"variations:totalYellow|short",
//		"variations:percentYellow|float",
//		"variations:totalRed|short",
//		"variations:percentRed|float",
		"samples:sampleId|short",
		"samples:gtType|byte",
		"samples:gtQual|byte",
		"samples:group|byte",
//		"nosamples:sampleId|short",
//		"nosamples:gtType|byte",
//		"nosamples:gtQual|byte",
//		"nosamples:group|byte",
	};

	public static void main(String[] args) {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "node1");
		
		try {
			HBaseAdmin hbase = new HBaseAdmin(conf);
			hbase.disableTable("main");
			hbase.deleteTable("main");
			hbase.disableTable("schema");
			hbase.deleteTable("schema");
			
			HTableDescriptor main = new HTableDescriptor(TableName.valueOf("main"));
			HColumnDescriptor variations = new HColumnDescriptor("variations");
			HColumnDescriptor samples = new HColumnDescriptor("samples");
			HColumnDescriptor nosamples = new HColumnDescriptor("nosamples");
			main.addFamily(variations);
			main.addFamily(samples);
			main.addFamily(nosamples);
			hbase.createTable(main);
			
			HTableDescriptor schema = new HTableDescriptor(TableName.valueOf("schema"));
			HColumnDescriptor exists = new HColumnDescriptor("info");
			schema.addFamily(exists);
			hbase.createTable(schema);
			
			HTable mainTable = new HTable(conf, "main");
			HTable schemaTable = new HTable(conf, "schema");
			populateSchema(schemaTable);
			//TODO: Parameterize path (program argument?)
			loadData(mainTable, "hdfs:///user/home/data.tsv");
			
			hbase.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void populateSchema(HTable schema) {
		byte[] exists = { 1 };
		
		try {
			for (String column : SCHEMA) {
				String[] columnInfo = column.split("\\|");
				Put put = new Put(Bytes.toBytes(columnInfo[0]));
				put.add(Bytes.toBytes("info"), Bytes.toBytes("exists"), exists);
				put.add(Bytes.toBytes("info"), Bytes.toBytes("type"), Bytes.toBytes(columnInfo[1]));
				schema.put(put);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadData(HTable table, String filename) {
		Path path = new Path(filename);
		try {
	        FileSystem fs = FileSystem.get(new Configuration());
	        BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(path)));

	        Short increment = 0;
	        HashMap<String, Short> keyMap = new HashMap<String, Short>(10000);
	        HashMap<String, Integer> sampleColumnMap = new HashMap<String, Integer>(100);
	        
	        String line;
	        line = br.readLine();
	        String[] headers = line.split("\t");
	        for (int i = 44; i < headers.length; i = i+2) {
	        	sampleColumnMap.put(headers[i].split("\\.")[1], i);
	        }
	        
	        line = br.readLine();
	        while (line != null) {
	                String[] values = line.split("\t");
	                
	                String keyPrefix = values[0] + ":" + values[1];
                	Short count = keyMap.get(keyPrefix);
	                if (count != null) {
	                	increment = ++count;
	                	keyMap.put(keyPrefix, increment);
	                }
	                
	                String key = keyPrefix + ":" + increment;
	                Put put = new Put(Bytes.toBytes(key));

	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("chrom"), Bytes.toBytes(values[0]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("start"), Bytes.toBytes(values[1]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("end"), Bytes.toBytes(values[2]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("ref"), Bytes.toBytes(values[3]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("alt"), Bytes.toBytes(values[4]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("gene"), Bytes.toBytes(values[5]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("qual"), Bytes.toBytes(Float.valueOf(values[6])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("callRate"), Bytes.toBytes(Float.valueOf(values[23])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("rsIds"), Bytes.toBytes(values[24]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("pfamDomain"), Bytes.toBytes(values[25]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("rmsk"), Bytes.toBytes(values[26]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("inCpgIsland"), Bytes.toBytes(values[27]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("inSegdup"), Bytes.toBytes(values[28]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("isConserved"), Bytes.toBytes(values[29]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("gmsIllumina"), Bytes.toBytes(values[30]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("gmsSolid"), Bytes.toBytes(values[31]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("inCse"), Bytes.toBytes(values[32]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("gerpBpScore"), Bytes.toBytes(values[33]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("depth"), Bytes.toBytes(values[34]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("rmsMapQual"), Bytes.toBytes(values[35]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("impactSeverity"), Bytes.toBytes(values[36]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("aafEspAll"), Bytes.toBytes(Float.valueOf(values[37])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("aafEspEa"), Bytes.toBytes(Float.valueOf(values[38])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("aaf1kgAll"), Bytes.toBytes(Float.valueOf(values[39])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("aaf1kgEa"), Bytes.toBytes(Float.valueOf(values[40])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("numHomRef"), Bytes.toBytes(Float.valueOf(values[41])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("numHet"), Bytes.toBytes(Float.valueOf(values[42])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("numHomAlt"), Bytes.toBytes(Float.valueOf(values[43])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("totalFg1"), Bytes.toBytes(Float.valueOf(values[7])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("totalFg2"), Bytes.toBytes(Float.valueOf(values[10])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("totalFg3"), Bytes.toBytes(Float.valueOf(values[13])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("totalCtrl"), Bytes.toBytes(Float.valueOf(values[18])));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("oddRatio"), Bytes.toBytes(values[21]));
	                put.add(Bytes.toBytes("variations"), Bytes.toBytes("chi2"), Bytes.toBytes(Float.valueOf(values[22])));
	                
	                String[] group0 = values[20].trim().split(";");
	                String[] group1 = values[9].trim().split(";");
	                String[] group2 = values[12].trim().split(";");
	                String[] group3 = values[15].trim().split(";");
	                
	                List<String> usedValues = new ArrayList<String>();
	                if (!group0[0].equals("0")) {
	                	for (String sample : group0) {
	                		usedValues.add(sample);
	                		put.add(Bytes.toBytes("samples"), Bytes.toBytes(sample), Bytes.toBytes(values[sampleColumnMap.get(sample)] + ":" + values[sampleColumnMap.get(sample) +  1] + ":0"));
	                	}
	                }
	                if (!group1[0].equals("0")) {
	                	for (String sample : group1) {
	                		usedValues.add(sample);
	                		put.add(Bytes.toBytes("samples"), Bytes.toBytes(sample), Bytes.toBytes(values[sampleColumnMap.get(sample)] + ":" + values[sampleColumnMap.get(sample) +  1] + ":1"));
	                	}
	                }
	                if (!group2[0].equals("0")) {
	                	for (String sample : group2) {
	                		usedValues.add(sample);
	                		put.add(Bytes.toBytes("samples"), Bytes.toBytes(sample), Bytes.toBytes(values[sampleColumnMap.get(sample)] + ":" + values[sampleColumnMap.get(sample) +  1] + ":2"));
	                	}
	                }
	                if (!group3[0].equals("0")) {
	                	for (String sample : group3) {
	                		usedValues.add(sample);
	                		put.add(Bytes.toBytes("samples"), Bytes.toBytes(sample), Bytes.toBytes(values[sampleColumnMap.get(sample)] + ":" + values[sampleColumnMap.get(sample) +  1] + ":3"));
	                	}
	                }
	                
	    	        for (Map.Entry<String, Integer> nsColumn : sampleColumnMap.entrySet()) {
	    	        	String sampleId = nsColumn.getKey();
	    	        	if (!usedValues.contains(sampleId)) {
	    	        		int index = nsColumn.getValue() + 1;
	    	        		put.add(Bytes.toBytes("nosamples"), Bytes.toBytes(sampleId), Bytes.toBytes(values[index]));
	    	        	}
	    	        }
	                
	                table.put(put);
	                line = br.readLine();
	        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
