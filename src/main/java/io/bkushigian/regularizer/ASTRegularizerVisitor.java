package io.bkushigian.regularizer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.*;

/**
 * Regularize an AST
 */
public class ASTRegularizerVisitor extends ModifierVisitor<MethodData> {

  @Override
  public Visitable visit(BreakStmt n, MethodData md) {
    LoopData loopData = md.loops.peek();
    loopData.setLoopBreaks(true);
    super.visit(n, md);
    markBreakGuards(n);
    return assign(loopData.breaksVarName(), true);
  }

  @Override
  public Visitable visit(ContinueStmt n, MethodData md) {
    md.loops.peek().setLoopContinues(true);
    return super.visit(n, md);
  }

  @Override
  public Visitable visit(ReturnStmt n, MethodData arg) {
    if (!arg.loops.isEmpty()) {
      arg.loops.peek().setLoopReturns(true);
    }
    arg.returns++;
    super.visit(n, arg);
    markReturnGuards(n);

    final Optional<Node> optParent = n.getParentNode();
    if (optParent.isPresent() && optParent.get() instanceof BlockStmt) {
      final BlockStmt parentBlock = (BlockStmt) optParent.get();
      final NodeList<Statement> statements = parentBlock.getStatements();
      final int idx = statements.indexOf(n);
      statements.remove(idx);
      if (!arg.methodDecl.getType().isVoidType() && n.getExpression().isPresent()) {
        statements.add(idx, assign(arg.resultVarName(), n.getExpression().get()));
      }
      statements.add(idx, assign(arg.returnsVarName(), true));
      return null;

    } else {
      final BlockStmt parentBlock = new BlockStmt();
      if (!arg.methodDecl.getType().isVoidType() && n.getExpression().isPresent()) {
        parentBlock.addStatement(assign(arg.resultVarName(), n.getExpression().get()));
      }
      parentBlock.addStatement(assign(arg.returnsVarName(), true));

      return parentBlock;
    }
  }

  @Override
  public Statement visit(WhileStmt n, MethodData arg) {
    arg.pushLoop(n);
    super.visit(n, arg);
    final LoopData loopData = arg.peekLoop();
    Statement result = n;
    final String breaksVarName = loopData.breaksVarName();
    final String returnsVarName = arg.returnsVarName();
    Expression guardCondition = null;

    if (loopData.loopReturns()) {
      guardCondition = name(returnsVarName);
    }

    if (loopData.loopBreaks()) {
      guardCondition = not(guardCondition == null ? name(breaksVarName) : or(name(breaksVarName), guardCondition));

      // Create a new varDecl to track if the loop has returned or not:
      //    boolean __loop_breaks_3__ = false;
      if (loopData.loopBreaks()) {
        final VariableDeclarationExpr varDeclExpr = guardVarDeclExpr(breaksVarName);
        NodeList<Statement> nl = new NodeList<>();
        nl.add(new ExpressionStmt(varDeclExpr));
        nl.add(new WhileStmt(and(guardCondition, n.getCondition()),
                n.getBody()));

        if (n.getParentNode().isPresent()) {
          final Statement parentStmt = (Statement)n.getParentNode().get();
          if (parentStmt.isBlockStmt()) {
            final BlockStmt parentBlockStmt = parentStmt.asBlockStmt();
            final NodeList<Statement> statements = parentBlockStmt.getStatements();
            int idx = statements.indexOf(n);
            statements.remove(idx);
            for (Statement s : nl) {
              statements.add(idx++, s);
            }
          }
        }
        result = block(nl);
      }
    }
    arg.popLoop();
    return result;
  }

  @Override
  public MethodDeclaration visit(MethodDeclaration n, MethodData arg) {
    MethodData md = new MethodData(n);
    super.visit(n, md);
    final VariableDeclarationExpr resultVar = n.getType().isVoidType() ? null
            : varDeclWithDefaultInit(n.getType(), md.resultVarName());
    final VariableDeclarationExpr returnsVar = varDecl(PrimitiveType.booleanType(), md.returnsVarName(),
            new BooleanLiteralExpr(false));
    Optional<BlockStmt> optBody = n.getBody();
    if (!optBody.isPresent()) {
      throw new RuntimeException("No body in method, but visit shows it returns");
    }
    final BlockStmt body = optBody.get();
    body.getStatements().addFirst(new ExpressionStmt(returnsVar));
    if (!n.getType().isVoidType()) {
      body.getStatements().addFirst(new ExpressionStmt(resultVar));
      body.getStatements().addLast(new ReturnStmt(new NameExpr(md.resultVarName())));
    }
    return n;
  }

  @Override
  public BlockStmt visit(BlockStmt n, MethodData arg) {
    n.setData(GuardIdxTracker.key, new GuardIdxTracker());
    final LoopData loopData = arg.loops.isEmpty() ? null : arg.loops.peek();
    final BlockStmt result = (BlockStmt)super.visit(n, arg);
    // We've encountered zero or more break statements, and the nodes that we need to guard are held in
    // breakGuardStmts stack in reverse order. Iterate through and nodes and their sublists. For instance, if we
    // have nodelist (n1 n2 n3 n4 n5) and nodes n2 and n4 had breaks, we want to guard sublists
    // (n5), since these nodes will not execute if the break statement in n4 is executed, as well as
    // sublist (n3 n4 n5), since these will not execute if the break statement in n2 is triggered.
    //
    // We traverse in reverse order so that we end up with the following stages:
    // INIT:
    //     list: (n1 n2 n3 n4 n5)
    //     stack: (n2 n4)
    // POP: n4
    //     list: (n1 n2 n3 n4 (guard n5))
    //     stack: (n2)
    // POP: n2
    //     list: (n1 n2 (guard n3 n4 (guard n5)))
    //     stack: ()
    // DONE

    final GuardIdxTracker guardIdxs = n.getData(GuardIdxTracker.key);

    for (final int idx : guardIdxs.descendingIndexes()) {
      assert loopData != null;
      final NodeList<Statement> stmts = n.getStatements();
      final NodeList<Statement> nestedNodeList = new NodeList<>();

      while (idx < stmts.size()) {
        final Statement stmt = stmts.remove(idx);
        assert stmt != null;
        nestedNodeList.add(stmt);
      }

      final Expression guardCond = guardIdxs.buildGuardCondition(idx, arg, loopData);

      final IfStmt guard = ite(
              guardIdxs.buildGuardCondition(idx, arg, loopData),
              new BlockStmt(null, nestedNodeList),
              null);
      guard.setBlockComment(" --- Auto-generated guard statement --- ");
      stmts.add(guard);
    }
    return result;
  }

  @Override
  public Visitable visit(NodeList n, MethodData arg) {
    return super.visit(n, arg);
  }

  @Override
  public Visitable visit(DoStmt n, MethodData arg) {
    throw new IllegalStateException("LoopRegularizer cannot handle DoStmts");
  }

  @Override
  public Visitable visit(ForStmt n, MethodData arg) {
    throw new IllegalStateException("LoopRegularizer cannot handle ForStmts");
  }

  private boolean isWhileLoop(Node n) {
    return (n instanceof Statement) && ((Statement)n).isWhileStmt();
  }

  private UnaryExpr not(Expression e) {
    return new UnaryExpr(e, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
  }

  private NameExpr name(String name) {
    return new NameExpr(name);
  }

  private BinaryExpr and(Expression lhs, Expression rhs) {
    return new BinaryExpr(lhs, rhs, BinaryExpr.Operator.AND);
  }

  private BinaryExpr or(Expression lhs, Expression rhs) {
    return new BinaryExpr(lhs, rhs, BinaryExpr.Operator.OR);
  }

  private IfStmt ite(Expression cond, Statement then, Statement els) {
    return new IfStmt(cond, then, els);
  }

  private BlockStmt block(NodeList<Statement> stmts) {
    return new BlockStmt(stmts);
  }

  /**
   * Traverse up the parent tree until a MethodDecl is encountered. Each time a BlockStmt is found, update the backing
   * NodeList to guard after a return was encountered.
   * @param r
   */
  protected void markReturnGuards(final ReturnStmt r) {
    Statement stmt = r;

    while (true) {
      final Optional<Node> parentNode = stmt.getParentNode();

      if (! parentNode.isPresent()) {
        throw new RuntimeException("Expected parent node for " + stmt);
      }
      if (! (parentNode.get() instanceof Statement)) {
        if (parentNode.get() instanceof MethodDeclaration) {
          break;
        }
        throw new IllegalStateException("Return found non-method non-statement ancestor");
      }

      final Statement parentStmt = (Statement) parentNode.get();
      if (parentStmt.isBlockStmt()) {
        final BlockStmt blockStmt = (BlockStmt) parentStmt;
        final GuardIdxTracker guardIdxs = parentStmt.getData(GuardIdxTracker.key);
        final NodeList<Statement> nl = blockStmt.getStatements();
        final int idx = nl.indexOf(stmt) + 1;
        if (idx < nl.size()) {
          guardIdxs.guardReturns(idx);
        }
      }
      stmt = parentStmt;  // traverse up the AST
    }
  }

  /**
   * Traverse up the parent tree until a loop is encountered. Each time a BlockStmt is found, update the backing
   * NodeList to guard after a break was encountered.
   * @param b
   */
  protected void markBreakGuards(final BreakStmt b) {

    Statement stmt = b;    // The current stmt node we are inspecting...this will traverse up the AST
    while (! isWhileLoop(stmt)) {
      final Optional<Node> parentNode = stmt.getParentNode();

      if (! parentNode.isPresent()) {
        throw new RuntimeException("Expected parent node for " + stmt);
      }
      if (! (parentNode.get() instanceof Statement)) {
        throw new RuntimeException("Expected parent node to be a Statement: " + stmt);
      }

      final Statement parentStmt = (Statement) parentNode.get();
      if (parentStmt.isBlockStmt()) {
        final BlockStmt blockStmt = (BlockStmt) parentStmt;
        final GuardIdxTracker guardIdxs = parentStmt.getData(GuardIdxTracker.key);
        final NodeList<Statement> nl = blockStmt.getStatements();
        final int idx = nl.indexOf(stmt) + 1;
        if (idx < nl.size()) {
          guardIdxs.guardBreaks(idx);
        }
      }
      stmt = parentStmt;  // traverse up the AST
    }
  }

  private VariableDeclarationExpr guardVarDeclExpr(String name) {
    return guardVarDeclExpr(name, false);
  }

  private VariableDeclarationExpr guardVarDeclExpr(String name, boolean init) {
    return new VariableDeclarationExpr(new VariableDeclarator(PrimitiveType.booleanType(), name,
            new BooleanLiteralExpr(init)));
  }

  private VariableDeclarationExpr varDeclWithDefaultInit(Type type, String name) {
    Expression init;
    if (type.isPrimitiveType()) {
      PrimitiveType pt = type.asPrimitiveType();
      switch (pt.getType()) {
        case BOOLEAN:
          init = new BooleanLiteralExpr(false);
          break;
        case CHAR:
          init = new CharLiteralExpr('\0');
          break;
        case BYTE:
          init = new IntegerLiteralExpr(String.valueOf(Byte.MIN_VALUE));
          break;
        case SHORT:
          init = new IntegerLiteralExpr(String.valueOf(Short.MIN_VALUE));
          break;
        case INT:
          init = new IntegerLiteralExpr(String.valueOf(Integer.MIN_VALUE));
          break;
        case LONG:
          init = new IntegerLiteralExpr(String.valueOf(Long.MIN_VALUE));
          break;
        case FLOAT:
          init = new DoubleLiteralExpr(String.valueOf(Float.NaN));
          break;
        case DOUBLE:
          init = new DoubleLiteralExpr(String.valueOf(Double.NaN));
          break;
        default:
          throw new RuntimeException("Unrecognized primitive type");
      }
    } else if (type.isArrayType()) {
      ArrayType at = type.asArrayType();
      init = new NullLiteralExpr();
    } else if (type.isReferenceType()) {
      ReferenceType rt = type.asReferenceType();
      init = new NullLiteralExpr();
    } else throw new RuntimeException("Unknown Type: " + type);
    final VariableDeclarationExpr varDecl = varDecl(type, name, init);
    varDecl.setLineComment("Default value to satisfy Javac's flow checking");
    return varDecl;
  }

  private VariableDeclarationExpr varDecl(Type type, String name, Expression init) {
    return new VariableDeclarationExpr(new VariableDeclarator(type, name, init));
  }

  private VariableDeclarationExpr varDecl(Type type, String name) {
    return new VariableDeclarationExpr(new VariableDeclarator(type, name));
  }

  private ExpressionStmt assign(String name, Expression expr) {
    return new ExpressionStmt(new AssignExpr(name(name), expr, AssignExpr.Operator.ASSIGN));
  }

  private ExpressionStmt assign(String name, boolean bool) {
    return new ExpressionStmt(new AssignExpr(name(name), new BooleanLiteralExpr(bool), AssignExpr.Operator.ASSIGN));
  }

}