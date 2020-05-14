package io.bkushigian.regularizer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.Visitable;

import java.io.File;

public class DesugarDriver {
  public static void main(String[] args) throws Exception {
    CompilationUnit cu = StaticJavaParser.parse(new File(args[0]));
    LoopDesugarerVisitor v = new LoopDesugarerVisitor();
    ASTRegularizerVisitor a = new ASTRegularizerVisitor();
    cu = (CompilationUnit)v.visit(cu, null);
    cu = (CompilationUnit) a.visit(cu, null);
    System.out.println(cu);
    System.out.println();
  }
}
