package edu.buffalo.cse562;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

public interface SqlIterator {
	public LeafValue[] readNextTuple();
	public void reset();
	public List<Column> getInputSchema();
}
