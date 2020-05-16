package io.bkushigian.regularizer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.*;

/**
 * A data structure to store info we learn about our method. This includes
 * <ul>
 *   <li>Info about loops, and whether they continue or not</li>
 *   <li>Mappings from all interesting nodes to their corresponding info</li>
 * </ul>
 */
public class MethodData {
  int loopCount = 0;
  Stack<LoopData> loops = new Stack<>();
  int returns = 0;
  final MethodDeclaration methodDecl;

  public MethodData(MethodDeclaration n) {
    methodDecl = n;
  }

  String returnsVarName() {
    return "__method_has_returned__";
  }

  String resultVarName() {
    return "__RETURN_RESULT__";
  }

  private Map<Node, LoopData> loopData = new HashMap<>();

  List<Node> nodesToBeRegularized = new LinkedList<>();

  void pushLoop(ForStmt forStmt) {
    LoopData d = new LoopData(forStmt, loopCount++);
    loops.push(d);
    loopData.put(forStmt, d);
  }

  void pushLoop(ForEachStmt forEachStmt) {
    LoopData d = new LoopData(forEachStmt, loopCount++);
    loops.push(d);
    loopData.put(forEachStmt, d);
  }

  void pushLoop(DoStmt doStmt) {
    LoopData d = new LoopData(doStmt, loopCount++);
    loops.push(d);
    loopData.put(doStmt, d);
  }

  void pushLoop(WhileStmt whileStmt) {
    LoopData d = new LoopData(whileStmt, loopCount++);
    loops.push(d);
    loopData.put(whileStmt, d);
  }

  LoopData peekLoop() {
    return loops.peek();
  }

  void popLoop() {
    loops.pop();
  }

  LoopData getLoopData(ForStmt n) {
    return loopData.get(n);
  }

  LoopData getLoopData(DoStmt n) {
    return loopData.get(n);
  }

  LoopData getLoopData(WhileStmt n) {
    return loopData.get(n);
  }

  LoopData getLoopData(ForEachStmt n) {
    return loopData.get(n);
  }
}
