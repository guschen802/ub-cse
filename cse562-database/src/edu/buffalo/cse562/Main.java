package edu.buffalo.cse562;

import edu.buffalo.cse562.checkpoint1.SqlToRA;
import edu.buffalo.cse562.checkpoint1.plan.*;
import net.sf.jsqlparser.expression.LeafValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;

import java.io.*;
import java.util.ArrayList;

public class Main {
	private static File dataDir = null;
	private static ArrayList<File> sqlFiles = new ArrayList<File>();
	private static int count = 0;// number for SQL files
	private static long startTime;
	private static long endTime;
	private static SqlToRA translator;

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--data")) {
				dataDir = new File(args[i + 1]);
				i++;
			} else {
				sqlFiles.add(new File(args[i]));
			}
		}
		translator = new SqlToRA();
		for (File sql : sqlFiles) {
			startTime = System.currentTimeMillis();
			CCJSqlParser parser;
			parser = new CCJSqlParser(new FileReader(sql));
			Statement s;

			// CCJSqlParser returns null once it hits EOF.

			while ((s = parser.Statement()) != null) {

				// Figure out what kind of statement we've just encountered

				if (s instanceof CreateTable) {
					// SqlToRA extracts schema information from this create
					// table
					// statement and loads it into 'db'
					translator.loadTableSchema((CreateTable) s);

				} else if (s instanceof Select) {
					System.out.println("=== QUERY ===\n" + s);

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

					System.out.println("=== PLAN ===\n" + plan);
					// TODO: query rewrite (optimize)
					// TODO: evaluate the tree
					SqlIterator output = RAToIt(plan);

					if (output != null) {
						dumpToScreen(output);
//						dumpToFile(output, "E://output//cp2_" + count + ".dat");
						endTime = System.currentTimeMillis();
						System.out.println("Time Cost: "
								+ (endTime - startTime) / 1000.0 + " secs");
						count++;
					} else {
						System.err.println("No result");
					}

				} else {

					// Utility method that produces an "Unsupported Feature"
					// exception
					throw new Exception("Unhandled Statement Type");
				}

			} // END while((s = parser.Statement()) != null)

		}
	}

	private static SqlIterator RAToIt(PlanNode plan) {
		if (plan instanceof AggregateNode) {
			AggregateNode an = (AggregateNode)plan;
			return new AggregationIterator(RAToIt(an.getChild()), an);
		} else if (plan instanceof LimitNode) {
			System.err.println("Limit not finished");
			return null;
		}else if (plan instanceof ProductNode) {
			ProductNode pn = (ProductNode)plan;
			return new SimpleJoinIterator(RAToIt(pn.getLHS()), RAToIt(pn.getRHS()),pn);
		}else if (plan instanceof ProjectionNode) {
			ProjectionNode pn = (ProjectionNode) plan;
			return new ProjectionIterator(RAToIt(pn.getChild()), pn);
		} else if (plan instanceof SelectionNode) {
			SelectionNode sn = (SelectionNode) plan;
			return new SelectionIterator(RAToIt(sn.getChild()), sn);
		} else if (plan instanceof SortNode) {
			SortNode sn = (SortNode)plan;
			return new SortIterator(RAToIt(sn.getChild()), sn);
		}else if (plan instanceof TableScanNode) {
			TableScanNode tbnode = (TableScanNode) plan;
			return new FromIterator(dataDir, translator.db, tbnode);
		}else if (plan instanceof UnionNode) {
			System.err.println("Union not finished");
			return null;
		}else {
			System.err.println("Unknown node");
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static void dumpToScreen(SqlIterator output) {
		LeafValue[] row = output.readNextTuple();
		while (row != null) {
			for (int i = 0; i < row.length; i++) {
				if (row[i] instanceof StringValue) {
					StringValue sv = (StringValue) row[i];
					if (i == row.length - 1) {
						System.out.print(sv.getNotExcapedValue());
					} else {
						System.out.print(sv.getNotExcapedValue() + "|");
					}
				} else {
					if (i == row.length - 1) {
						System.out.print(row[i].toString());
					} else {
						System.out.print(row[i].toString() + "|");
					}
				}
			}
			System.out.print("\n");
			row = output.readNextTuple();
		}
	}

	private static void dumpToFile(SqlIterator output, String path) {
		LeafValue[] row = output.readNextTuple();
		try {
			BufferedWriter outPutWriter = new BufferedWriter(new FileWriter(
					path));
			while (row != null) {
				for (int i = 0; i < row.length; i++) {
					if (row[i] instanceof StringValue) {
						StringValue sv = (StringValue) row[i];
						if (i == row.length - 1) {
							outPutWriter.write(sv.getNotExcapedValue());
						} else {
							outPutWriter.write(sv.getNotExcapedValue() + "|");
						}
					} else {
						if (i == row.length - 1) {
							outPutWriter.write(row[i].toString());
						} else {
							outPutWriter.write(row[i].toString() + "|");
						}
					}
				}
				outPutWriter.write("\n");
				row = output.readNextTuple();
			}
			outPutWriter.close();
			output.reset();
			System.out.println("File:" + path.toString() + "dump finished!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void dumpToBoth(SqlIterator output, String path) {
		LeafValue[] row = output.readNextTuple();
		try {
			BufferedWriter outPutWriter = new BufferedWriter(new FileWriter(
					path));
			PrintWriter printer = new PrintWriter(System.out);
			while (row != null) {
				for (int i = 0; i < row.length; i++) {
					if (row[i] instanceof StringValue) {
						StringValue sv = (StringValue) row[i];
						if (i == row.length - 1) {
							outPutWriter.write(sv.getNotExcapedValue());
							printer.print(sv.getNotExcapedValue());
						} else {
							outPutWriter.write(sv.getNotExcapedValue() + "|");
							printer.print(sv.getNotExcapedValue() + "|");
						}
					} else {
						if (i == row.length - 1) {
							outPutWriter.write(row[i].toString());
							printer.print(row[i].toString());

						} else {
							outPutWriter.write(row[i].toString() + "|");
							printer.print(row[i].toString() + "|");
						}
					}
				}
				outPutWriter.write("\n");
				printer.print("\n");
				row = output.readNextTuple();
			}
			outPutWriter.close();
			printer.close();
			output.reset();
			System.out.println("File:" + path.toString() + "dump finished!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
