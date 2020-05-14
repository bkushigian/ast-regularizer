package io.bkushigian.regularizer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.DoStmt;

public class LoopData extends NodeData {

  /**
   * How many loops have we seen so far in this method? Used for method subscripting
   */
  final int idx;

  private boolean continues = false;
  private boolean breaks = false;
  private boolean returns = false;

  public LoopData(ForStmt node, int idx){
    super(node);
    this.idx = idx;
  }

  public LoopData(WhileStmt node, int idx){
    super(node);
    this.idx = idx;
  }

  public LoopData(ForEachStmt node, int idx){
    super(node);
    this.idx = idx;
  }

  public LoopData(DoStmt node, int idx){
    super(node);
    this.idx = idx;
  }

  public boolean loopContinues() {
    return continues;
  }

  public boolean loopBreaks() {
    return breaks;
  }

  public boolean loopReturns () {
    return returns;
  }

  public void setLoopContinues(boolean c) {
    continues = c;
  }

  public void setLoopBreaks(boolean b) {
    breaks = b;
  }

  public void setLoopReturns(boolean r) {
    returns = r;
  }

  public String breaksVarName() {
    return String.format("__loop_breaks_%d__", idx);
  }

  public String continuesVarName() {
    return String.format("__loop_continues_%d__", idx);
  }

}
