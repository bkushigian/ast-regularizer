package io.bkushigian.regularizer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Regularize an AST
 */
public class ASTRegularizerVisitor extends ModifierVisitor<MethodData> {
  /**
   * This structure keeps track of which nodes need to be guarded against breaks.
   */
  protected Map<BlockStmt, Set<Statement>> breakGuards = new HashMap<>();

  @Override
  public Visitable visit(BreakStmt n, MethodData md) {
    LoopData loopData = md.loops.peek();
    loopData.setLoopBreaks(true);
    super.visit(n, md);
    markBreakGuards(n, loopData.breaksVarName());
    return new ExpressionStmt(new AssignExpr(new NameExpr(loopData.breaksVarName()),
            new BooleanLiteralExpr(true),
            AssignExpr.Operator.ASSIGN));
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
    return super.visit(n, arg);
  }

  @Override
  public Statement visit(WhileStmt n, MethodData arg) {
    arg.pushLoop(n);
    super.visit(n, arg);
    final LoopData loopData = arg.peekLoop();
    Statement result = n;
    final String breaksVarName = loopData.breaksVarName();
    if (loopData.loopBreaks()) {
      VariableDeclarator varDecl = new VariableDeclarator(PrimitiveType.intType(),
              breaksVarName,
              new BooleanLiteralExpr(false));
      VariableDeclarationExpr varDeclExpr = new VariableDeclarationExpr(varDecl);
      NodeList<Statement> nl = new NodeList<>();
      nl.add(new ExpressionStmt(varDeclExpr));
      nl.add(new WhileStmt(and(not(name(breaksVarName)), n.getCondition()),
             n.getBody()));
      result = new BlockStmt(nl);
    }
    arg.popLoop();
    return result;
  }

  @Override
  public MethodDeclaration visit(MethodDeclaration n, MethodData arg) {
    MethodData md = new MethodData();
    return (MethodDeclaration)super.visit(n, md);
  }

  @Override
  public BlockStmt visit(BlockStmt n, MethodData arg) {
    final Set<Statement> breakGuardStmts = new HashSet<>();
    breakGuards.put(n, breakGuardStmts);
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

    final List<Integer> idxs = breakGuardStmts.stream()
            .map(s -> n.getStatements().indexOf(s))
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());
    for (final int idx : idxs) {
      assert loopData != null;
      final NodeList<Statement> stmts = n.getStatements();
      final NodeList<Statement> nestedNodeList = new NodeList<>();

      while (idx < stmts.size()) {
        final Statement stmt = stmts.remove(idx);
        assert stmt != null;
        nestedNodeList.add(stmt);
      }

      final IfStmt guard = new IfStmt(not(new NameExpr(loopData.breaksVarName())),
              new BlockStmt(null, nestedNodeList),
              null);
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

  private IfStmt ite(Expression cond, Statement then, Statement els) {
    return new IfStmt(cond, then, els);
  }

  private BlockStmt block(NodeList<Statement> stmts) {
    return new BlockStmt(stmts);
  }

  // track statements we've introduced as guards
  private Set<Statement> guards = new HashSet<>();
  /**
   * Traverse up the parent tree until a loop is encountered. Each time a BlockStmt is found, update the backing
   * NodeList to guard after a break was encountered.
   * @param b
   */
  protected void markBreakGuards(final BreakStmt b, final String guardVarName) {

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
        final NodeList<Statement> nl = blockStmt.getStatements();
        final int idx = nl.indexOf(stmt) + 1;
        if (idx < nl.size()) {
          if (!breakGuards.containsKey(blockStmt)) {
            throw new IllegalStateException("Key miss: expected to find BlockStmt in hash lookup, but missed");
          }
          final Set<Statement> statements = breakGuards.get(blockStmt);
          statements.add(nl.get(idx));
        }
      }
      stmt = parentStmt;  // traverse up the AST
    }
  }

  // guard a statement
  private void guard(final BlockStmt blockStmt, final int idx, final String guardVarName) {
    final NodeList<Statement> nl = blockStmt.getStatements();
    if (idx == nl.size()) {
      return;
    }
    if (guards.contains(nl.get(idx))) {
      return;
    }
    final List<Statement> unguarded = nl.subList(0, idx);
    final List<Statement> toGuard = nl.subList(idx, nl.size());

    final Statement g = guard(toGuard, guardVarName);
    g.setParentNode(blockStmt);
    unguarded.add(g);
    blockStmt.setStatements(new NodeList<>(unguarded));
  }

  private IfStmt guard(final List<Statement> nl, final String guardVarName) {
    final IfStmt g = ite(not(name(guardVarName)), block(new NodeList<>(nl)), null);
    for (Statement s : nl) {
      s.setParentNode(g);
    }
    guards.add(g);
    return g;
  }
}
