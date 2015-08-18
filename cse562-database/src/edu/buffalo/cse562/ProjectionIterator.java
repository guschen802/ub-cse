package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.ProjectionNode;
import edu.buffalo.cse562.checkpoint1.plan.ProjectionNode.Target;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.schema.Column;

import java.sql.SQLException;
import java.util.List;

public class ProjectionIterator implements SqlIterator {

	private SqlIterator mInput;
	private List<Column> mOldSchema;
	private List<Target> mProjectSchema;
	private List<Column> mNewSchema;
	
	public ProjectionIterator(SqlIterator mInput, ProjectionNode projecttionNode) {
		super();
		this.mInput = mInput;
		this.mOldSchema = mInput.getInputSchema();
		this.mProjectSchema = projecttionNode.getColumns();
		this.mNewSchema = projecttionNode.getSchemaVars();
	}

	@Override
	public LeafValue[] readNextTuple() {
		LeafValue[] newTuple = new LeafValue[mNewSchema.size()];
		LeafValue[] oldTuple = mInput.readNextTuple();
			
		if (oldTuple == null) {
			return null;
		}
		Evaluator eval = new Evaluator(mOldSchema, oldTuple);
		
		for (int i = 0; i < newTuple.length; i++) {
			try {
				//TODO: need modified to fit aggregation
				newTuple[i] = eval.eval(mProjectSchema.get(i).expr);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.err.println("eval null");
			}
		}
		
		return newTuple;
	}

	@Override
	public void reset() {
		mInput.reset();

	}

	@Override
	public List<Column> getInputSchema() {
		return this.mNewSchema;
	}
}
