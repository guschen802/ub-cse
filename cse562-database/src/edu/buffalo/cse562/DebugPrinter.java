package edu.buffalo.cse562;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

public class DebugPrinter {
	public static void printColumnSchema(Column[] schema) {
		if (schema.length == 0) {
			System.err.println("Schema: No column!");
			return;
		}
		System.err.print("Column Schema: ");
		for (Column column : schema) {
			System.err.print(column.getColumnName()+"|");
		}System.err.println("");
	}
	public static void printTuple(LeafValue[] tuple) {
		if (tuple.length == 0) {
			System.err.println("PrintTuple: No data!");
			return;
		}
		System.err.print("PrintTuple: ");
		for (LeafValue column : tuple) {
			System.err.print(column.toString()+"|");
		}System.err.println("");
	}
}


