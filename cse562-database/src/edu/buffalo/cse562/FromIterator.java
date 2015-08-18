package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.TableScanNode;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.*;
import java.util.List;
import java.util.Map;

public class FromIterator implements SqlIterator {
	private BufferedReader mInput;
	private File mFile;
	private ColumnDefinition[] inputSchema;
	private List<Column> outputSchema;
	
	
	public FromIterator(File dataPatgh, Map<String, CreateTable> tables, TableScanNode tableNode) {
		CreateTable sourceTable = tables.get(tableNode.table.getName());
		if (sourceTable == null) {
			System.err.println("source table mising! ");
		}else if (sourceTable.getTable() == null) {
			System.err.println("table mising! ");
			System.exit(-1);
		}
		this.mFile = new File(dataPatgh,sourceTable.getTable().getName() + ".dat");
		inputSchema = new ColumnDefinition[sourceTable.getColumnDefinitions().size()];
		outputSchema = tableNode.getSchemaVars();
		
		for (int i =0; i<sourceTable.getColumnDefinitions().size();i++){
			ColumnDefinition cd = (ColumnDefinition) sourceTable.getColumnDefinitions().get(i);
			inputSchema[i] = cd;
		}
		reset();
	}

	@Override
	public LeafValue[] readNextTuple() {
		if (mInput == null) {	
					return null;
				}
		String line = null;
		try {
			line = mInput.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (line == null) {
			return null;
		}
		String cols[] = line.split("\\|");
		LeafValue[] tuple = new LeafValue[inputSchema.length];
		for(int i=0;i<inputSchema.length;i++){
			String type[] = inputSchema[i].getColDataType().getDataType().toLowerCase().split(" ");
			if (type[0].equals("string") 
					|| type[0].equals("char") 
					|| type[0].equals("varchar")) {
				tuple[i] = new StringValue(" " + cols[i] + " ");		
			}else if (type[0].equals("int")) {
				tuple[i] = new LongValue(cols[i]);
			}else if (type[0].equals("decimal")) {
				tuple[i] = new DoubleValue(cols[i]);
			}else if (type[0].equals("date")) {
				tuple[i] = new DateValue(" " + cols[i] + " ");
			}
		}
		
		return tuple;
	}

	@Override
	public void reset() {
		
		try {
			mInput = new BufferedReader(new FileReader(this.mFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mInput = null;
		}
		//System.err.println("reset in FromIterator");
	}

	@Override
	public List<Column> getInputSchema() {
		return outputSchema;
	}

}
