package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.SortNode;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortIterator implements SqlIterator {
	private SqlIterator mInput;
	private List<Column> mSchema;
	List<SortNode.Ordering> mSortColumn;
	List<SortableTuple> table;
	int index = 0;
	public SortIterator(SqlIterator input,SortNode sortNode) {
		this.mInput = input;
		this.mSchema = input.getInputSchema();
		this.mSortColumn = sortNode.getSorts();
		table = new ArrayList<SortableTuple>();
		
		LeafValue[] row = mInput.readNextTuple();
		while (row != null) {
			
			table.add(new SortableTuple(row, mSortColumn, mSchema));
			row = mInput.readNextTuple();
		}
		Collections.sort(table);
	}

	@Override
	public LeafValue[] readNextTuple() {
		if (table.size() == 0) {
			return null;
		}
		if (index < table.size()) {
			index++;
			return table.get(index-1).mTuple;
		}else {
			return null;
		}
	}

	@Override
	public void reset() {
		mInput.reset();
		index = 0;
	}

	@Override
	public List<Column> getInputSchema() {
		return mSchema;
	}

}
