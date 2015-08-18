package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.SortNode;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;

import java.sql.SQLException;
import java.util.List;

public class SortableTuple implements Comparable<SortableTuple> {
	LeafValue[] mTuple;
	LeafValue[] key;
	List<SortNode.Ordering> mSortColumn;

	public SortableTuple(LeafValue[] tuple, List<SortNode.Ordering> sortColumn,
			List<Column> schema) {
		this.mTuple = tuple;
		this.mSortColumn = sortColumn;
		Evaluator eval = new Evaluator(schema, tuple);
		key = new LeafValue[sortColumn.size()];
		for (int i = 0; i < key.length; i++) {
			try {
				key[i] = eval.eval(sortColumn.get(i).expr);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.err.println("Eval null");
				e.printStackTrace();
			}
		}
	}

	@Override
	public int compareTo(SortableTuple arg0) {
		// TODO Auto-generated method stub
		for (int i = 0; i < this.key.length; i++) {
			if (leafValueCompare(this.key[i], arg0.key[i]) >0) {
				if (this.mSortColumn.get(i).ascending) {
					return 1;
				}else {
					return -1;
				}
			} else if (leafValueCompare(this.key[i], arg0.key[i]) <0) {
				if (this.mSortColumn.get(i).ascending) {
					return -1;
				}else {
					return 1;
				}
			} else {
				continue;
			}
		}
		return 0;
	}

	private int leafValueCompare(LeafValue value1, LeafValue value2) {
		// TODO Auto-generated method stub
		if ((value1 instanceof LongValue) && (value2 instanceof LongValue)) {
			LongValue long1 = (LongValue) value1;
			LongValue long2 = (LongValue) value2;
			if ((long1.getValue() - long2.getValue()) > 0) {
				return 1;
			} else if ((long1.getValue() - long2.getValue()) < 0) {
				return -1;
			} else {
				return 0;
			}
		} else if ((value1 instanceof DoubleValue)
				&& (value2 instanceof DoubleValue)) {
			DoubleValue double1 = (DoubleValue) value1;
			DoubleValue double2 = (DoubleValue) value2;
			if ((double1.getValue() - double2.getValue()) > 0) {
				return 1;
			} else if ((double1.getValue() - double2.getValue()) < 0) {
				return -1;
			} else {
				return 0;
			}

		} else if ((value1 instanceof StringValue)
				&& (value2 instanceof StringValue)) {
			return value1.toString().compareTo(value2.toString());
		} else if ((value1 instanceof DateValue)
				&& (value2 instanceof DateValue)) {
			DateValue date1 = (DateValue) value1;
			DateValue date2 = (DateValue) value1;
			return date1.getDate() - date2.getDate();
		} else {
			System.err.println("PANIC! LeafValue compare type does not match!");
			return 0;
		}
	}

}
