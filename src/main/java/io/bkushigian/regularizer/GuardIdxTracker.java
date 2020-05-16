package io.bkushigian.regularizer;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.expr.*;

import java.util.NavigableSet;
import java.util.TreeMap;

public class GuardIdxTracker {
  public static final DataKey<GuardIdxTracker> key = new DataKey<GuardIdxTracker>(){};

  private final TreeMap<Integer, GuardIdx> idxMap;


  public NavigableSet<Integer> descendingIndexes() {
    return idxMap.descendingKeySet();
  }

  /**
   * Track which things at a given index should be guarded against
   */
  public GuardIdxTracker() {
    idxMap = new TreeMap<>();
  }

  public void guardContinue(int idx) {
    idxMap.putIfAbsent(idx, new GuardIdx());
    idxMap.get(idx).gc = true;
  }

  public void guardBreaks(int idx) {
    idxMap.putIfAbsent(idx, new GuardIdx());
    idxMap.get(idx).gb = true;
  }

  public void guardReturns(int idx) {
    idxMap.putIfAbsent(idx, new GuardIdx());
    idxMap.get(idx).gr = true;
  }

  public Expression buildGuardCondition(int idx, MethodData methodData, LoopData loopData) {
    Expression cond = null;
    final GuardIdx g = idxMap.get(idx);
    if (g.guardBreaks()) {
      assert loopData != null;
      cond = new NameExpr(loopData.breaksVarName());
    } if (g.guardContinues()) {
      assert loopData != null;
      cond = buildCond(cond, loopData.continuesVarName());
    } if (g.guardReturns()) {
      cond = buildCond(cond, methodData.returnsVarName());
    }
    if (cond == null) {
      return new BooleanLiteralExpr(true);
    }
    if (cond.isBinaryExpr()) {
      cond = new EnclosedExpr(cond);
    }
    UnaryExpr guardCond = new UnaryExpr(cond, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
    return guardCond;
  }

  private Expression buildCond(final Expression cond, final String name) {
    return buildCond(cond, new NameExpr(name));
  }

  private Expression buildCond(final Expression cond, final Expression newExpr) {
    return cond == null ? newExpr : new BinaryExpr(newExpr, cond, BinaryExpr.Operator.OR);
  }

  public static class GuardIdx {
    private boolean gc = false;
    private boolean gb = false;
    private boolean gr = false;

    public boolean guardContinues() {
      return gc;
    }

    public boolean guardBreaks() {
      return gb;
    }

    public boolean guardReturns() {
      return gr;
    }
  }
}
