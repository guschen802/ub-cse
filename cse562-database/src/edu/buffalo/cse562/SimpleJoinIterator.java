package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.ProductNode;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

public class SimpleJoinIterator implements SqlIterator {
	private SqlIterator mLeft;
	private SqlIterator mRight;
	private LeafValue[] mLeftTuple;
	private List<Column> mInputSchema;
	public SimpleJoinIterator(SqlIterator left,SqlIterator right,ProductNode productNode) {
		super();
		this.mLeft = left;
		this.mRight = right;
		this.mLeftTuple = mLeft.readNextTuple();
		mInputSchema = productNode.getSchemaVars();
	}

	@Override
	public LeafValue[] readNextTuple() {
		LeafValue[] rightTuple = mRight.readNextTuple();
		if (rightTuple == null) {
			mLeftTuple = mLeft.readNextTuple();
			if (mLeftTuple == null) {
				return null;
			}
			else {
				mRight.reset();
				rightTuple = mRight.readNextTuple();
			}
		}
		
		LeafValue[] joinTuple = new LeafValue[mLeftTuple.length+rightTuple.length];
		int index1 = 0;
		int index2 = 0;
		for(int i =0; i< joinTuple.length;i++){
			if (index1 < mLeftTuple.length) {
				joinTuple[i] = mLeftTuple[index1];
				index1++;
			}
			else {
				joinTuple[i] = rightTuple[index2];
				index2++;
			}
		}
		return joinTuple;
	}

	@Override
	public void reset() {
		mLeft.reset();
		mRight.reset();
		this.mLeftTuple = mLeft.readNextTuple();
	}

	@Override
	public List<Column> getInputSchema() {
		return mInputSchema;
	}

}
