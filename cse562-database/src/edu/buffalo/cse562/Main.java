package edu.buffalo.cse562;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;
import edu.buffalo.cse562.checkpoint1.SqlToRA;
import edu.buffalo.cse562.checkpoint1.eval.TupleIterator;
import edu.buffalo.cse562.checkpoint1.plan.PlanNode;

public class Main {

	/**
	 * Parse arguments and input files and dispatch requests to the appropriate
	 * system components
	 * 
	 * @param argsArray
	 *            An array of command line arguments
	 */
	private static boolean rewriteQuery = true;
	private static boolean loadingPhase = false;
	private static boolean printDebug = false;

	public static void main(String[] args) throws Exception {
		List<File> files = new ArrayList<File>();

		File dataDir = new File("data");
		File dBDir = null;
		Environment myDbEnv;
		EnvironmentConfig envConfig = new EnvironmentConfig();
		

		// Start by parsing the arguments
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--data":
				i++;
				dataDir = new File(args[i]);
				break;
			case "--db":
				i++;
				dBDir = new File(args[i]);
				break;
			case "--load":
				loadingPhase = true;
				break;
			case "-d":
				printDebug = true;
				break;
			default:
				files.add(new File(args[i]));
			}
		}

		// Sql to RA translator:
		// Provides an initial query plan from a SQL query
		SqlToRA translator = new SqlToRA();

		// PlanNode to TupleIterator transator:
		// Converts plans to TupleIterators
		// The checkpoint2 compiler extends the checkpoint1 compiler with
		// support
		// for Grace Hash joins. To extend the checkpoint2 compiler, subclass
		// it and handle the additional iterator compilation steps in compile().
		edu.buffalo.cse562.checkpoint2.PlanCompiler compiler = new edu.buffalo.cse562.checkpoint2.PlanCompiler(
				translator.db, // The compiler needs access to the schema.
				dataDir // The compiler uses dataDir to create TableScans
		);
		envConfig.setAllowCreate(true);
		myDbEnv = new Environment(dBDir, envConfig);

		// For each file name detected in the arguments, parse it in.
		for (File f : files) {

			CCJSqlParser parser = new CCJSqlParser(new FileReader(f));
			Statement s;

			// CCJSqlParser returns null once it hits EOF.
			while ((s = parser.Statement()) != null) {

				// Figure out what kind of statement we've just encountered

				if (s instanceof CreateTable) {
					// SqlToRA extracts schema information from this create
					// table
					// statement and loads it into 'db'
					translator.loadTableSchema((CreateTable) s);
					
					if (loadingPhase == true) {
						
					}

				} else if ((s instanceof Select) && loadingPhase == false) {

					if (printDebug) {
						System.out.println("=== QUERY ===\n" + s);
					}

					// SqlToRA uses the visitor pattern to convert a SelectBody
					// into the
					// corresponding PlanNode subclasses.
					PlanNode plan = translator.selectToPlan(((Select) s)
							.getSelectBody());

					// The visitor pattern doesn't play nicely with exceptions.
					// If an
					// exception occurs during translation, checkError() will
					// re-raise it
					// here.
					translator.checkError();

					if (printDebug) {
						System.out.println("=== PLAN ===\n" + plan);
					}

					// //////////////////////////////////////////////
					// / THIS IS WHERE YOU SHOULD OPTIMIZE `plan` ///
					// //////////////////////////////////////////////
					PlanNode newPlan;
					if (rewriteQuery) {
						newPlan = RATreeOptimizer.optimize(plan);
						if (printDebug) {
							System.out.println("=== New PLAN ===\n" + newPlan);
						}
					}

					// PlanCompiler descends through the PlanNode structure and
					// converts
					// all PlanNode objects to their TupleIterator counterparts.
					TupleIterator iter = compiler.compile(plan);

					// Flush the iterator output
					dump(iter);

				} else {

					// Utility method that produces an "Unsupported Feature"
					// exception
					throw new Exception("Unhandled Statement Type");
				}

			} // END while((s = parser.Statement()) != null)

		} // END for(String f : argsArray)

	} // END main();

	public static void dump(TupleIterator iter) throws SQLException {
		while (!iter.done()) {
			String sep = "";
			for (LeafValue col : iter.read()) {
				System.out.print(sep);
				if (col instanceof StringValue) {
					System.out.print(((StringValue) col).getValue());
				} else if (col instanceof DateValue) {
					DateValue dv = (DateValue) col;
					// System.out.print("" + (dv.getYear() + 1900) + "-"
					// + dv.getMonth() + "-" + dv.getDate());
					System.out.print(dv.toString());
				} else {
					System.out.print(col.toString());
				}
				sep = "|";
			}
			System.out.print("\n");
		}
	}
}