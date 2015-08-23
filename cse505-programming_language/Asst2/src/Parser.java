import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.ArrayList;
/* 		OO PARSER AND BYTE-CODE GENERATOR FOR TINY PL
 
Grammar for TinyPL (using EBNF notation) is as follows:

 program ->  decls stmts end
 decls   ->  int idlist ;
 idlist  ->  id { , id } 
 stmts   ->  stmt [ stmts ]
 cmpdstmt->  '{' stmts '}'
 stmt    ->  assign | cond | loop
 assign  ->  id = expr ;
 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ]
 loop    ->  while '(' rexp ')' cmpdstmt  
 rexp    ->  expr (< | > | =) expr
 expr    ->  term   [ (+ | -) expr ]
 term    ->  factor [ (* | /) term ]
 factor  ->  int_lit | id | '(' expr ')'
 
Lexical:   id is a single character; 
	      int_lit is an unsigned integer;
		 equality operator is =, not ==

Sample Program: Factorial
 
int n, i, f;
n = 4;
i = 1;
f = 1;
while (i < n) {
  i = i + 1;
  f= f * i;
}
end

   Sample Program:  GCD
   
int x, y;
x = 121;
y = 132;
while (x != y) {
  if (x > y) 
       { x = x - y; }
  else { y = y - x; }
}
end

 */

 
 /*
 Name: Chunyu Chen
 UBMail: chunyuch@buffalo.edu
 */
public class Parser {
	public static void main(String[] args)  {
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();//"int"
		Program p = new Program();
		Code.output();
	}
}

class Program {
	Decls d;
	Stmts sts;
	public Program() {
		d =new Decls();
		Lexer.lex();//next stmts
		sts = new Stmts();
		Code.gen(Token.KEY_END);
	}
}

class Decls {
	Idlist idl;
	public Decls() {
		Lexer.lex();//Idlist
		idl = new Idlist();
	}	 
}

class Idlist {
	public Idlist() {
		Code.idList.add(Lexer.ident);//store id
		while(Lexer.lex()==Token.COMMA)//',' for continue loop and ';' for end loop
		{
			Lexer.lex();//id
			Code.idList.add(Lexer.ident);//store id
		}
	}
}

class Stmt {
	Assign assign;
	Loop loop;
	Cond cond;
	public  Stmt() {
		switch (Lexer.nextToken) {
		case Token.ID:
			assign = new Assign();
			Lexer.lex();//next stmt
			break;
			
		case Token.KEY_WHILE:
			loop = new Loop();
			Lexer.lex();//next stmt
			break;
			
		case Token.KEY_IF:
			cond = new Cond();
			break;

		default:
			break;
		}
	} 
} 

class Stmts {
	Stmt stmt;
	Stmts sts;
	public Stmts(){
		stmt = new Stmt();
		while (Lexer.nextToken!=Token.KEY_END && Lexer.nextToken!=Token.RIGHT_BRACE )//get first word of stmts if it is end, then end loop
		{
			sts = new Stmts();
		}
	}
	 
}

class Assign {
	Expr expr;
	public Assign(){
		int storeIndex = Code.findChInList(Lexer.ident);
		Lexer.lex();//'='
		expr = new Expr();
		Code.gen(Token.ASSIGN_OP, storeIndex);

	}
	 
}


class Loop {
	Rexpr rexpr;
	Cmpdstmt cmp;
	public Loop(){
		int startIndex = Code.jbcList.size();
		Lexer.lex();//'('
		rexpr = new Rexpr();
		int rexpIndex = Code.jbcList.size()-3;
		
		cmp = new Cmpdstmt();
		
		Code.gen(21,startIndex);//goto

		Code.gen(Token.KEY_WHILE, rexpIndex);

	}
	 
}               

class Cond {
	Rexpr rexpr;
	Cmpdstmt cmp1;
	Cmpdstmt cmp2;
	public Cond(){
		Lexer.lex();//(
		rexpr = new Rexpr();
		int rexpIndex = Code.jbcList.size()-3;
		cmp1 = new Cmpdstmt();
		if(Lexer.lex()==Token.KEY_ELSE)
		{
			int elsetIndex = Code.jbcList.size();
			Code.gen(21);
			Code.gen(Token.KEY_WHILE, rexpIndex);

			
			cmp2 = new Cmpdstmt();

			Code.gen(Token.KEY_WHILE, elsetIndex);
			Lexer.lex();//'}'
		}
		else {
			Code.gen(Token.KEY_WHILE, rexpIndex);

		}
	}
	 
}

class Cmpdstmt {
	Stmts sts;
	public Cmpdstmt(){
		Lexer.lex();//'{'
		Lexer.lex();//nextstmts
		sts = new Stmts();
	} 
}

class Rexpr {
	Expr expr1;
	Expr expr2;
	public Rexpr(){
		expr1 = new Expr();
		switch (Lexer.nextToken) {//(<|>|=|!=) or ')'
		case Token.LESSER_OP:
			expr2 = new Expr();
			Code.gen(Token.LESSER_OP);

			break;
			
		case Token.GREATER_OP:
			expr2 = new Expr();
			Code.gen(Token.GREATER_OP);

			break;
			
		case Token.ASSIGN_OP:
			expr2 = new Expr();
			Code.gen(Token.ASSIGN_OP);

			break;

		case Token.NOT_EQ:
			expr2 = new Expr();
			Code.gen(Token.NOT_EQ);

			break;
			
		default:
			break;
		}
	}
	 
}

class Expr {  
	Term term;
	Expr expr1;
	Expr expr2;
	public Expr(){
		term = new Term();	
		switch (Lexer.nextToken) {//(+|-) or (<|>|=|!=) or ')'
		case Token.ADD_OP:
			expr1 = new Expr();
			Code.gen(Token.ADD_OP);

			break;
			
		case Token.SUB_OP:
			expr2 = new Expr();
			Code.gen(Token.SUB_OP);

			break;

		default:
			break;
		}
	}
	 
}

class Term {  
	Factor factor;
	Term term1;
	Term term2;
	public Term(){
		factor = new Factor();
		switch (Lexer.lex()) {//(*|/) or (+|-) or (<|>|=|!=) or ')'
		case Token.MULT_OP:
			term1 = new Term();	
			Code.gen(Token.MULT_OP);

			break;

		case Token.DIV_OP:
			term2 = new Term();
			Code.gen(Token.DIV_OP);
			
			break;
			
		default:
			break;
		}
	}
}

class Factor { 
	Expr expr;
	public Factor(){
		switch (Lexer.lex()) {//factor
		case Token.INT_LIT:
			Code.gen(Token.INT_LIT);

			break;
			
		case Token.ID:
			Code.gen(Token.ID);
			
			break;
			
		case Token.LEFT_PAREN:
			expr = new Expr();
			break;
			
		default:
			break;
		}
	}
	 
}

class Code {
	static public List<Character> idList= new ArrayList<Character>();
	static public List<String> jbcList= new ArrayList<String>();
	
	static public int findChInList(char ch){
		for (int i = 0; i < idList.size(); i++) {
			if(idList.get(i)==ch)
				return i;
		}
		return -1;
	}
	
	static public void output(){
		for (int i = 0; i < jbcList.size(); i++) {
			if (jbcList.get(i)!="") {
				System.out.println(i+": "+jbcList.get(i));
			}
		}
	}
	
	static public void gen(int thisToken,int index){
		switch (thisToken) {
		case Token.ASSIGN_OP:
			if(index<=3){
				jbcList.add("istore_"+index);
			}
			else {
				jbcList.add("istore "+index);
				jbcList.add("");
			}
			break;
			
		case 21://goto
			jbcList.add("goto "+ index);
			jbcList.add("");
			jbcList.add("");
			break;
			
		case Token.KEY_WHILE:
			String newCode = Code.jbcList.get(index).concat(String.valueOf(Code.jbcList.size()));
			Code.jbcList.set(index, newCode);

		default:
			break;
		}
	}
	
	static public void gen(int thisToken){
		switch (thisToken) {
		case Token.INT_LIT:
			if(Lexer.intValue<=5)
				jbcList.add("iconst_"+Lexer.intValue);
			else if (Lexer.intValue>=6 && Lexer.intValue<=127) {
				jbcList.add("bipush "+Lexer.intValue);
				jbcList.add("");
			}
			else {
				jbcList.add("sipush "+Lexer.intValue);
				jbcList.add("");
				jbcList.add("");
			}
			break;
			
		case Token.ID:
			if (findChInList(Lexer.ident)<=3) {
				jbcList.add("iload_"+Code.findChInList(Lexer.ident));
			}
			else {
				jbcList.add("iload "+Code.findChInList(Lexer.ident));
				jbcList.add("");
			}
			
			break;
			
		case Token.DIV_OP:
			jbcList.add("idiv");
			break;
			
		case Token.MULT_OP:
			jbcList.add("imul");
			break;
			
		case Token.ADD_OP:
			jbcList.add("iadd");
			break;
			
		case Token.SUB_OP:
			jbcList.add("isub");
			break;
			
		case Token.LESSER_OP:
			jbcList.add("if_icmple ");
			jbcList.add("");
			jbcList.add("");
			break;
			
		case Token.GREATER_OP:
			jbcList.add("if_icmpge ");
			jbcList.add("");
			jbcList.add("");
			break;
			
		case Token.ASSIGN_OP:
			jbcList.add("if_icmpeq ");
			jbcList.add("");
			jbcList.add("");
			break;

		case Token.NOT_EQ:
			jbcList.add("if_icmpne ");
			jbcList.add("");
			jbcList.add("");
			break;
		
		case Token.KEY_END:
			jbcList.add("return");
			break;
			
		case 21://goto
			jbcList.add("goto ");
			jbcList.add("");
			jbcList.add("");
			
		default:
			break;
		}
		
	}
}




