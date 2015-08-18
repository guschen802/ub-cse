package edu.buffalo.cse562;

import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.sql.SQLException;
import java.util.List;

public class Evaluator extends Eval{
	private List<Column> mCols;
	private LeafValue[] mTuple;
	public Evaluator(List<Column> cols,LeafValue[] tuple ) {
		super();
		mCols = cols;
		mTuple = tuple;
	}

	@Override
	
	public LeafValue eval(Column column) throws SQLException {
		int i=0;
		
		for(i = 0;i<mCols.size();i++){
			if (column.getColumnName().equals(mCols.get(i).getColumnName())) {
				try {
					if (column.getTable().getName().equals(mCols.get(i).getTable().getName()) || mCols.get(i).getTable().getName().equals("QUERY")) {
						break;
					}
				} catch (NullPointerException e) {
					System.err.println("Evaluator: Table null!");
					break;
				}
			}
		}	
		
		if (i < mCols.size()) {
			return mTuple[i];
		}else {
			System.err.println("Evaluate column not found: " + column.toString());
			for (Column cl : mCols) {
				System.err.println(cl.toString());
			}
			return null;
		}	
	}
	
}
