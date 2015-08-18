package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.plan.AggregateNode;
import edu.buffalo.cse562.checkpoint1.plan.AggregateNode.AType;
import edu.buffalo.cse562.checkpoint1.plan.AggregateNode.AggColumn;
import edu.buffalo.cse562.checkpoint1.plan.ProjectionNode.Target;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.LeafValue.InvalidLeaf;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AggregationIterator implements SqlIterator {
	private SqlIterator mInput;
	private List<Column> mInputSchema;
	private List<Column> mOutputSchema;
	private List<Target> mGroupBy;
	private List<AggColumn> mAggColumns;
	private List<LeafValue[]> mGroupKey;
	private List<SqlIterator> mGroupList;
	private int index = 0;

	// private Map<K, V>
	public AggregationIterator(SqlIterator input, AggregateNode aggregateNode) {
		this.mInput = input;
		this.mInputSchema = input.getInputSchema();
		this.mOutputSchema = aggregateNode.getSchemaVars();
		this.mGroupBy = aggregateNode.getGroupByVars();
		this.mGroupList = new ArrayList<SqlIterator>();
		this.mAggColumns = aggregateNode.getAggregates();
		if (mGroupBy.size() > 0) {
			prepareGroupKey();
		}
		prepareGroup();
	}

	@Override
	public LeafValue[] readNextTuple() {
		if (index < mGroupList.size()) {
			LeafValue[] aggValue = aggregate(mGroupList.get(index));
			LeafValue[] groupByVaule = null;
			LeafValue[] finalValues = aggValue;
			if (mGroupBy.size() > 0) {
				groupByVaule = mGroupKey.get(index);
				finalValues = new LeafValue[aggValue.length
						+ groupByVaule.length];
				int index1 = 0;
				int index2 = 0;
				for (int i = 0; i < finalValues.length; i++) {
					if (index1 < groupByVaule.length) {
						finalValues[i] = groupByVaule[index1];
						index1++;
					} else {
						finalValues[i] = aggValue[index2];
						index2++;
					}
				}
			}
			index++;
			return finalValues;
		} else {
			return null;
		}
	}

	@Override
	public void reset() {
		index = 0;
		mInput.reset();
	}

	@Override
	public List<Column> getInputSchema() {
		return mOutputSchema;
	}

	private void prepareGroupKey() {
		LeafValue[] tuple = null;
		LeafValue[] key;
		mGroupKey = new ArrayList<LeafValue[]>();
		tuple = mInput.readNextTuple();
		while (tuple != null) {

			Evaluator eval = new Evaluator(mInput.getInputSchema(), tuple);
			key = new LeafValue[mGroupBy.size()];
			for (int i = 0; i < key.length; i++) {
				try {
					key[i] = eval.eval(mGroupBy.get(i).expr);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					System.err.println("eval error");
				}
			}
			if (!inKeyValues(key) && key.length > 0) {
				mGroupKey.add(key);
			}
			tuple = mInput.readNextTuple();
		}
		mInput.reset();
	}

	private void prepareGroup() {
		if (mGroupKey == null) {
			mGroupList.add(mInput);
		} else {
			for (int i = 0; i < mGroupKey.size(); i++) {
				LeafValue[] key = mGroupKey.get(i);
				String content = "";
				for (int j = 0; j < key.length; j++) {
					content = content + mGroupBy.get(j).expr.toString() + " = ";
					if (key[j] instanceof StringValue) {
						content = content + "\'"
								+ ((StringValue) key[j]).getNotExcapedValue()
								+ "\'";

					} else if (key[j] instanceof DateValue) {
						content = content + "DATE(\'" + key[j].toString()
								+ "\')";
					} else {
						content = content + key[j].toString();
					}
					if (j < key.length - 1) {
						content = content + " AND ";
					}
				}
				InputStream is = new ByteArrayInputStream(content.getBytes());
				CCJSqlParser parser;
				parser = new CCJSqlParser(is);
				Expression condition;
				try {
					condition = parser.Expression();
					mGroupList.add(new SelectionIterator(mInput, condition));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private boolean inKeyValues(LeafValue[] key) {
		for (LeafValue[] lv : mGroupKey) {
			if (this.keyEqual(lv, key)) {
				return true;
			}
		}
		return false;
	}

	private boolean keyEqual(LeafValue[] valueA, LeafValue[] valueB) {
		for (int i = 0; i < valueA.length; i++) {
			if (valueA[i].equals(valueB[i])) {
			} else {
				return false;
			}
		}
		return true;
	}

	private LeafValue[] aggregate(SqlIterator input) {
		LeafValue[] row = input.readNextTuple();
		LeafValue[] aggValue = new LeafValue[mAggColumns.size()];
		int count = 0;

		if (row == null) {
			System.err.println("first row == null");
			for (int i = 0; i < aggValue.length; i++) {
				if (mAggColumns.get(i).aggType.equals(AType.COUNT)) {
					aggValue[i] = new LongValue(0);
				} else {
					aggValue[i] = new StringValue(" null ");
				}
			}
			return aggValue;
		}

		// base value
		count++;
		Evaluator eval = new Evaluator(mInputSchema, row);
		for (int i = 0; i < aggValue.length; i++) {
			try {
				if (!(mAggColumns.get(i).aggType.equals(AType.COUNT))) {
					// for now we only need 1 argument
					LeafValue value = eval.eval(mAggColumns.get(i).expr[0]);
					if (value instanceof LongValue) {
						aggValue[i] = new LongValue(value.toLong());
					} else if (value instanceof DoubleValue) {
						aggValue[i] = new DoubleValue(value.toDouble());
					}

				}
			} catch (SQLException | InvalidLeaf e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		row = input.readNextTuple();
		while (row != null) {
			count++;
			eval = new Evaluator(mInputSchema, row);
			for (int i = 0; i < aggValue.length; i++) {
				try {
					if (!(mAggColumns.get(i).aggType.equals(AType.COUNT))) {
						// for now we only need 1 argument
						LeafValue value = eval.eval(mAggColumns.get(i).expr[0]);
						try {

							if (mAggColumns.get(i).aggType.equals(AType.AVG)
									|| mAggColumns.get(i).aggType
											.equals(AType.SUM)) {

								if (value instanceof LongValue) {

									((LongValue) aggValue[i])
											.setValue(aggValue[i].toLong()
													+ value.toLong());

								} else if (value instanceof DoubleValue) {
									((DoubleValue) aggValue[i])
											.setValue(aggValue[i].toDouble()
													+ value.toDouble());
								}
							} else if (mAggColumns.get(i).aggType
									.equals(AType.MAX)) {
								if (value instanceof LongValue) {
									if (value.toLong() > aggValue[i].toLong()) {
										aggValue[i] = new LongValue(
												value.toLong());
									}
								} else if (value instanceof DoubleValue) {
									if (value.toDouble() > aggValue[i]
											.toDouble()) {
										aggValue[i] = new DoubleValue(
												value.toDouble());
									}
								}
							} else if (mAggColumns.get(i).aggType
									.equals(AType.MIN)) {
								if (value instanceof LongValue) {
									if (value.toLong() < aggValue[i].toLong()) {
										aggValue[i] = new LongValue(
												value.toLong());
									}
								} else if (value instanceof DoubleValue) {
									if (value.toDouble() < aggValue[i]
											.toDouble()) {
										aggValue[i] = new DoubleValue(
												value.toDouble());
									}
								}
							}
						} catch (InvalidLeaf e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			row = input.readNextTuple();
		}// end while
		for (int i = 0; i < aggValue.length; i++) {
			if (mAggColumns.get(i).aggType.equals(AType.COUNT)) {
				aggValue[i] = new LongValue(count);
			} else if (mAggColumns.get(i).aggType.equals(AType.AVG)) {
				try {
					aggValue[i] = new DoubleValue(aggValue[i].toDouble()
							/ (double) count);
				} catch (InvalidLeaf e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// need to append group key
		input.reset();
		return aggValue;
	}
}
