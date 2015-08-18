package edu.buffalo.cse562;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.checkpoint1.plan.PlanNode;
import edu.buffalo.cse562.checkpoint1.plan.ProductNode;
import edu.buffalo.cse562.checkpoint1.plan.SelectionNode;
import edu.buffalo.cse562.checkpoint2.plan.HashJoinNode;

public class RATreeOptimizer {
	private static int optimizeCount = 30;
	public static PlanNode optimize(PlanNode top) {
		PlanNode newTop = breaker(top);
		int count = 0;
		while (count < optimizeCount) {
			newTop = moveAndMerge(newTop);
			// System.out.println("=== Rewrite PLAN-"+count+" ===");
			// System.out.println(newTop);
			count++;
			//outPutWriter.write("=== NO."+count + " ===\n" + newTop + "\n\n");
			
		}
		count = 0;
		
		while (count < optimizeCount) {
			newTop = mergeSelection(newTop); //
			// System.out.println("=== Rewrite PLAN-" + count + " ==="); //
			// System.out.println(newTop);
			count++;
		}

		return newTop;
		
	}

	private static PlanNode breaker(PlanNode node) {
		if (node instanceof PlanNode.Unary) {
			if (node instanceof SelectionNode) {
				return selectionBreaker((SelectionNode) node);
			} else {
				PlanNode.Unary unary = (PlanNode.Unary) node;
				unary.setChild(breaker(unary.getChild()));
				return unary;
			}
		}
		return node;

	}

	private static PlanNode moveAndMerge(PlanNode node) {
		if (node instanceof SelectionNode) {
			SelectionNode sNode = (SelectionNode) node;

			if (sNode.getChild() instanceof PlanNode.Binary) {
				// push down selection to left or right
				PlanNode.Binary binaryNode = (PlanNode.Binary) sNode.getChild();

				List<Column> columnInCondition = new ArrayList<Column>();
				getColumnsFromExpression(sNode.getCondition(),
						columnInCondition);

				if (inSchema(columnInCondition, binaryNode.getLHS()
						.getSchemaVars())) {
					// column all in left, push down to left child node
					sNode.setChild(binaryNode.getLHS());
					binaryNode.setLHS(sNode);
					return binaryNode;
				} else if (inSchema(columnInCondition, binaryNode.getRHS()
						.getSchemaVars())) {
					// column all in right, push down to right child node
					sNode.setChild(binaryNode.getRHS());
					binaryNode.setRHS(sNode);
					return binaryNode;
				}

			}

			// selecttion contain columns from both child node, then check
			// HashJoin availability
			if (sNode.getChild() instanceof ProductNode && !(sNode.getChild() instanceof HashJoinNode)) {
				ProductNode pNode = (ProductNode) sNode.getChild();
				if ((sNode.getCondition()) instanceof EqualsTo) {
					// if condition is equalto, check hashjoin
					EqualsTo equalToExpr = (EqualsTo) (sNode.getCondition());
					Expression leftExpr = equalToExpr.getLeftExpression();
					Expression rightExpr = equalToExpr.getRightExpression();

					List<Column> leftColumns = new ArrayList<Column>();
					List<Column> rightColumns = new ArrayList<Column>();
					getColumnsFromExpression(leftExpr, leftColumns);
					getColumnsFromExpression(rightExpr, rightColumns);

					if (inSchema(leftColumns, pNode.getLHS().getSchemaVars())
							&& inSchema(rightColumns, pNode.getRHS()
									.getSchemaVars())) {
						// A Join B where A.c = B.c
						HashJoinNode hJNode = new HashJoinNode(pNode.getLHS(),
								pNode.getRHS(), leftExpr, rightExpr);
						return hJNode;
					} else if (inSchema(leftColumns, pNode.getRHS()
							.getSchemaVars())
							&& inSchema(rightColumns, pNode.getLHS()
									.getSchemaVars())) {
						// A Join B where B.c = A.c
						HashJoinNode hJNode = new HashJoinNode(pNode.getLHS(),
								pNode.getRHS(), rightExpr, leftExpr);
						return hJNode;
					}
				}
			} else if (sNode.getChild() instanceof SelectionNode) {
				// rearrange selection
				SelectionNode child = (SelectionNode) sNode.getChild();
				SelectionPriorityComparator comparator = new SelectionPriorityComparator();
				
				if (hasConflict(child, child.getChild()) || comparator.compare(sNode.getCondition(),
						child.getCondition()) == 1 ) {
					sNode.setChild(child.getChild());
					child.setChild(sNode);
					return child;
				}
			}
		}

		if (node instanceof PlanNode.Unary) {
			PlanNode.Unary unary = (PlanNode.Unary) node;
			unary.setChild(moveAndMerge(unary.getChild()));
		} else if (node instanceof PlanNode.Binary) {
			PlanNode.Binary binary = (PlanNode.Binary) node;
			PlanNode left = binary.getLHS();
			PlanNode right = binary.getRHS();
			binary.setLHS(moveAndMerge(left));
			binary.setRHS(moveAndMerge(right));
		}
		return node;
	}// end moveAndMerge

	private static PlanNode mergeSelection(PlanNode node) {
		if (node instanceof SelectionNode) {
			SelectionNode sNode = (SelectionNode) node;
			if (sNode.getChild() instanceof SelectionNode) {
				SelectionNode sChildNode = (SelectionNode) sNode.getChild();
				AndExpression andExpr = new AndExpression(sNode.getCondition(),
						sChildNode.getCondition());
				SelectionNode mergeSNode = new SelectionNode(andExpr);
				mergeSNode.setChild(sChildNode.getChild());
				return mergeSNode;
			} else {
				sNode.setChild(mergeSelection(sNode.getChild()));
				;
			}
		} else if (node instanceof PlanNode.Unary) {
			PlanNode.Unary unaryNode = (PlanNode.Unary) node;
			unaryNode.setChild(mergeSelection(unaryNode.getChild()));
		} else if (node instanceof PlanNode.Binary) {
			PlanNode.Binary binaryNode = (PlanNode.Binary) node;
			binaryNode.setLHS(mergeSelection(binaryNode.getLHS()));
			binaryNode.setRHS(mergeSelection(binaryNode.getRHS()));
		}
		return node;
	}

	private static PlanNode selectionBreaker(SelectionNode originNode) {

		if (originNode.conjunctiveClauses().size() > 1) {
			List<Expression> conditionList = new ArrayList<Expression>(
					originNode.conjunctiveClauses());
			conditionList.sort(new SelectionPriorityComparator());
			Expression condition = conditionList.get(0);
			SelectionNode top = new SelectionNode(condition);
			SelectionNode tail = top;
			for (int i = 1; i < conditionList.size(); i++) {
				condition = conditionList.get(i);
				SelectionNode node = new SelectionNode(condition);
				tail.setChild(node);
				tail = node;
			}
			tail.setChild(originNode.getChild());
			return top;
		}
		return originNode;
	}

	public static boolean isSimpleMath(Expression expr) {
		if (expr instanceof BinaryExpression) {
			BinaryExpression biExpr = (BinaryExpression) expr;
			Expression leftExpr = biExpr.getLeftExpression();
			Expression rightExpr = biExpr.getRightExpression();

			List<Column> leftColumns = new ArrayList<Column>();
			List<Column> rightColumns = new ArrayList<Column>();
			getColumnsFromExpression(leftExpr, leftColumns);
			getColumnsFromExpression(rightExpr, rightColumns);
			int result = leftColumns.size() * rightColumns.size();
			if (result == 0) {
				return true;
			}else {
				return false;
			}
		}
		return false;
		
		/*
		if (expr instanceof BinaryExpression) {
			BinaryExpression biExpr = (BinaryExpression) expr;
			if ((biExpr.getLeftExpression() instanceof Column)
					&& (biExpr.getRightExpression() instanceof LeafValue)) {
				return true;
			} else if ((biExpr.getLeftExpression() instanceof LeafValue)
					&& (biExpr.getRightExpression() instanceof Column)) {
				return true;
			} else if ((biExpr.getLeftExpression() instanceof Column)
					&& (biExpr.getRightExpression() instanceof Function)) {
				Function fn = (Function) biExpr.getRightExpression();
				if (fn.getName().toLowerCase().equals("date")) {
					return true;
				}
			} else if ((biExpr.getLeftExpression() instanceof Function)
					&& (biExpr.getRightExpression() instanceof Column)) {
				Function fn = (Function) biExpr.getLeftExpression();
				if (fn.getName().toLowerCase().equals("date")) {
					return true;
				}
			}
		}
		return false;
		*/
	}

	public static void setOptimizeCount(int count) {
		if (count > 0) {
			optimizeCount = count;
		}
	}

	public static boolean inSchema(List<Column> columns, List<Column> schema) {

		boolean result = false;
		long count = 0;
		for (Column column : columns) {
			// search column in schema
			result = false;
			for (Column cl : schema) {
				if (column.getColumnName().equals(cl.getColumnName())) {
					if (column.getTable().getName()
							.equals(cl.getTable().getName())
							|| cl.getTable().getName().equals("QUERY")) {
						result = true;
						count++;
						break;
					}
				}
			}
			if (result) {
				continue;
			} else {
				return false;
			}
		}
		if (count == columns.size()) {
			return true;
		} else {
			return false;
		}
	}

	public static void getColumnsFromExpression(Expression expr,
			List<Column> columnList) {
		if (expr instanceof Column) {
			columnList.add((Column) expr);
		} else if (expr instanceof BinaryExpression) {
			BinaryExpression binaryExpr = (BinaryExpression) expr;
			getColumnsFromExpression(binaryExpr.getLeftExpression(), columnList);
			getColumnsFromExpression(binaryExpr.getRightExpression(),
					columnList);
		} else if (expr instanceof Parenthesis) {
			Parenthesis parenthesisExpr = (Parenthesis) expr;
			getColumnsFromExpression(parenthesisExpr.getExpression(),
					columnList);
		} else if (expr instanceof Function) {
			Function funExpr = (Function) expr;
			for (Object object : funExpr.getParameters().getExpressions()) {
				if (object instanceof Expression) {
					Expression exprExpr = (Expression) object;
					getColumnsFromExpression(exprExpr, columnList);
				}
			}
		} else if (expr instanceof InverseExpression) {
			InverseExpression inverseExpr = (InverseExpression) expr;
			getColumnsFromExpression(inverseExpr.getExpression(), columnList);
		} else if (expr instanceof Between) {
			Between betweenExpr = (Between) expr;
			getColumnsFromExpression(betweenExpr.getBetweenExpressionEnd(),
					columnList);
			getColumnsFromExpression(betweenExpr.getBetweenExpressionStart(),
					columnList);
			getColumnsFromExpression(betweenExpr.getLeftExpression(),
					columnList);
		} else if (expr instanceof CaseExpression) {
			CaseExpression caseExpr = (CaseExpression) expr;
			getColumnsFromExpression(caseExpr.getElseExpression(), columnList);
			getColumnsFromExpression(caseExpr.getSwitchExpression(), columnList);
		} else if (expr instanceof WhenClause) {
			WhenClause whenExpr = (WhenClause) expr;
			getColumnsFromExpression(whenExpr.getThenExpression(), columnList);
			getColumnsFromExpression(whenExpr.getWhenExpression(), columnList);
		} else if (expr instanceof ExistsExpression) {
			ExistsExpression exisExpr = (ExistsExpression) expr;
			getColumnsFromExpression(exisExpr.getRightExpression(), columnList);
		} else if (expr instanceof InExpression) {
			InExpression inExpr = (InExpression) expr;
			getColumnsFromExpression(inExpr.getLeftExpression(), columnList);
		} else if (expr instanceof IsNullExpression) {
			IsNullExpression isNullExpr = (IsNullExpression) expr;
			getColumnsFromExpression(isNullExpr.getLeftExpression(), columnList);
		}
	}
	private static boolean hasConflict(SelectionNode sNode, PlanNode planNode){
		if (planNode instanceof HashJoinNode) {
			HashJoinNode pNode = (HashJoinNode) planNode;
			if ((sNode.getCondition()) instanceof EqualsTo) {
				// if condition is equalto, check hashjoin
				EqualsTo equalToExpr = (EqualsTo) (sNode.getCondition());
				Expression leftExpr = equalToExpr.getLeftExpression();
				Expression rightExpr = equalToExpr.getRightExpression();

				List<Column> leftColumns = new ArrayList<Column>();
				List<Column> rightColumns = new ArrayList<Column>();
				getColumnsFromExpression(leftExpr, leftColumns);
				getColumnsFromExpression(rightExpr, rightColumns);

				if (inSchema(leftColumns, pNode.getLHS().getSchemaVars())
						&& inSchema(rightColumns, pNode.getRHS()
								.getSchemaVars())) {

					return true;
				} else if (inSchema(leftColumns, pNode.getRHS()
						.getSchemaVars())
						&& inSchema(rightColumns, pNode.getLHS()
								.getSchemaVars())) {
					return true;
				}
			}
		}
		return false;
	}
}




