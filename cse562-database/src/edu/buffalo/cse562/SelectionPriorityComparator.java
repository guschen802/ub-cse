package edu.buffalo.cse562;
import java.util.Comparator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;

	public class SelectionPriorityComparator implements Comparator<Expression>{
		@Override
		public int compare(Expression arg0, Expression arg1) {
			if (RATreeOptimizer.isSimpleMath(arg0)) {
				if (RATreeOptimizer.isSimpleMath(arg1)) {
					return 0;
				}else {
					return 1;
				}	
			}
			if (arg0 instanceof EqualsTo) {
				if (RATreeOptimizer.isSimpleMath(arg1)) {
					return -1;
				}else if (arg1 instanceof EqualsTo) {
					return 0;
				}else {
					return 1;
				}
			}
			return -1;
		}
	}