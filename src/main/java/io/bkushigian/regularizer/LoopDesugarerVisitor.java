package io.bkushigian.regularizer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.util.Optional;

/**
 * This class desugars looping constructs, transforming all loops into equivalent while loops
 */
public class LoopDesugarerVisitor extends ModifierVisitor<Void> {

  @Override
  public BlockStmt visit(ForStmt fs, Void arg) {

    // First, visit recursively
    super.visit(fs, arg);

    // If the ForStmt has a compare field, use it as the condition of the while loop. Otherwise, use `true`.
    final Expression condition = fs.getCompare().orElseGet(() -> new BooleanLiteralExpr(true));

    // Make sure body is a BlockStmt since we will need to add the `update` fields at the end
    final BlockStmt body = fs.getBody().isBlockStmt() ? (BlockStmt)fs.getBody():
            new BlockStmt(new NodeList<>(fs.getBody()));

    // Update each update to the end of the while loop's body
    for (Expression expr : fs.getUpdate()) {
      body.addStatement(expr);
    }

    // We want to return a BlockStmt to be used as our parent. If our parent is already a BlockStmt, great---we'll
    // use that! Otherwise, we create a new BlockStmt.

    final Optional<Node> parentNode = fs.getParentNode();
    assert parentNode.isPresent() && parentNode.get() instanceof Statement;
    final Statement parentStmt = (Statement) parentNode.get();
    BlockStmt block;

    // If the parent is already a block, we want to update that block
    if (parentStmt.isBlockStmt()) {
      block = (BlockStmt) parentStmt;
      int idx = block.getStatements().indexOf(fs);
      for (Expression expr: fs.getInitialization()) {
        block.addStatement(idx++, expr);
      }
      block.replace(fs, new WhileStmt(condition, body));
    }
    // otherwise, we want to create a new block
    else {
      block = new BlockStmt();
      for (Expression expr: fs.getInitialization()) {
        block.addStatement(expr);
      }
      block.addStatement(new WhileStmt(condition, body));
    }

    return block;
  }

  @Override
  public BlockStmt visit(DoStmt n, Void arg) {
    throw new NotImplementedException("DoStmt is not implemented");
    // super.visit(n, arg);
  }

  @Override
  public BlockStmt visit(ForEachStmt n, Void arg) {
    throw new NotImplementedException("ForEachStmt is not implemented");
    // super.visit(n, arg);
  }
}
