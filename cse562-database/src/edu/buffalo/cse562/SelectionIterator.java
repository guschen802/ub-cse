package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.SelectionNode;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.sql.SQLException;
import java.util.List;

public class SelectionIterator implements SqlIterator {
	private SqlIterator mInput;
	private List<Column> mSchema;
	private Expression mCondition;
	
	
	public SelectionIterator(SqlIterator input,SelectionNode selecttionNode) {
		mInput = input;
		mSchema = mInput.getInputSchema();
		mCondition = selecttionNode.getCondition();
	}

	public SelectionIterator(SqlIterator input,Expression condition) {
		mInput = input;
		mSchema = mInput.getInputSchema();
		mCondition = condition;
	}
	@Override
	public LeafValue[] readNextTuple() {	
		LeafValue[] tuple = null;
		do {
			tuple = mInput.readNextTuple();
			if (tuple == null) {
				return null;
			}
			Evaluator eval = new Evaluator(mSchema, tuple);
			try {
				
				if(eval.eval(mCondition).equals(BooleanValue.FALSE)){
					tuple = null;
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}catch (NullPointerException e) {
				System.err.println(mCondition);
				System.err.println("eval null");
				/*
				System.err.println(mCondition.toString());
				for (Column col : mInput.getInputSchema()) {
					System.err.print(col.toString() +":" +col.getColumnName() + "|");
				}
				System.err.println("");
				*/
			}
		} while (tuple == null);
		return tuple;
	}

	@Override
	public void reset() {
		mInput.reset();
	}

	@Override
	public List<Column> getInputSchema() {
		return mSchema;
	}

}
