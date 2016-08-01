package org.nest.nestml.prettyprinter;

import de.monticore.types.types._ast.ASTQualifiedName;
import de.se_rwth.commons.Names;
import org.nest.commons._ast.ASTExpr;
import org.nest.nestml._ast.*;
import org.nest.nestml._visitor.NESTMLInheritanceVisitor;
import org.nest.ode._ast.ASTEquation;
import org.nest.ode._ast.ASTODEAlias;
import org.nest.ode._ast.ASTOdeDeclaration;
import org.nest.ode._ast.ASTShape;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTParameter;
import org.nest.spl._ast.ASTParameters;
import org.nest.spl.prettyprinter.ExpressionsPrettyPrinter;
import org.nest.spl.prettyprinter.SPLPrettyPrinter;
import org.nest.utils.ASTUtils;
import org.nest.utils.PrettyPrinterBase;

import java.util.List;
import java.util.Optional;

import static org.nest.spl.prettyprinter.SPLPrettyPrinterFactory.createDefaultPrettyPrinter;
import static org.nest.utils.ASTUtils.printComments;

/**
 * Provides convenient  functions to statically type interfaces astnodes resulting from the Body-grammar
 * production.
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class NESTMLPrettyPrinter extends PrettyPrinterBase implements NESTMLInheritanceVisitor {
  private final ExpressionsPrettyPrinter expressionsPrinter;

  protected NESTMLPrettyPrinter(final ExpressionsPrettyPrinter expressionsPrinter) {
    this.expressionsPrinter = expressionsPrinter;
  }

  /**
   *   NESTMLCompilationUnit = "package" packageName:QualifiedName
   *   BLOCK_OPEN
   *   (Import | NEWLINE)*
   *   (Neuron | Component | SL_COMMENT | NEWLINE)*
   *   BLOCK_CLOSE (SL_COMMENT | NEWLINE)*;
   */
  @Override
  public void visit(final ASTNESTMLCompilationUnit node) {
    print(printComments(node));
  }

  /**
   * Grammar:
   * Import = "import" QualifiedName ([star:".*"])? (";")?;
   */
  @Override
  public void visit(final ASTImport astImport) {
    final String importName = Names.getQualifiedName(astImport.getQualifiedName().getParts());
    print("import " + importName);
    if (astImport.isStar()) {
      print(".*");
    }
    println();
  }

  /**
   * Grammar:
   * Neuron = "neuron" Name Body;
   */
  @Override
  public void visit(final ASTNeuron astNeuron) {
    print(printComments(astNeuron));
    print("neuron " + astNeuron.getName());
    astNeuron.getBase().ifPresent(
        baseNeuron -> print(" extends " + baseNeuron));
  }



  /**
   * Grammar:
   * Neuron = "neuron" Name Body;
   */
  @Override
  public void visit(final ASTComponent astComponent) {
    print("component " + astComponent.getName());
  }
  /**
   * Grammar:
   * Body = BLOCK_OPEN ( SL_COMMENT | NEWLINE | BodyElement)* BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTBody astBody) {
    println(BLOCK_OPEN);
    indent();
  }

  /**
   * Grammar:
   * Body = BLOCK_OPEN ( SL_COMMENT | NEWLINE | BodyElement)* BLOCK_CLOSE;
   */
  @Override
  public void endVisit(final ASTBody astBody) {
    unindent();
    println(BLOCK_CLOSE);
  }

  /**
   * USE_Stmt implements BodyElement = "use" name:QualifiedName "as" alias:Name;
   */
  @Override
  public void visit(final ASTUSE_Stmt astUseStmt) {
    final String referencedName = Names.getQualifiedName(astUseStmt.getName().getParts());
    println("use " + referencedName + " as " + astUseStmt.getAlias());
  }

  @Override
  public void visit(final ASTBodyElement astBodyElement) {
    print(printComments(astBodyElement));
  }
  /**
   * Var_Block implements BodyElement =
   * ([state:"state"]|[para:"parameter"]|[internal:"internal"])
   *  BLOCK_OPEN
   *     (AliasDecl (";" AliasDecl)* (";")?
   *     | SL_COMMENT | NEWLINE)*
   *  BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTVar_Block astVarBlock) {
    printVariableBlockHeader(astVarBlock);

    indent();
  }

  private void printVariableBlockHeader(final ASTVar_Block astVarBlock) {
    if (astVarBlock.isState()) {
      println("state" + BLOCK_OPEN);
    }
    else if (astVarBlock.isInternal()) {
      println("internal" + BLOCK_OPEN);
    }
    else if (astVarBlock.isParameter()) {
      println("parameter" + BLOCK_OPEN);
    }

  }

  @Override
  public void endVisit(final ASTVar_Block astVarBlock) {
    unindent();
    println(BLOCK_CLOSE);
  }

  /**
   * AliasDecl = ([hide:"-"])? ([alias:"alias"])? Declaration ("[" invariants:Expr (";" invariants:Expr)* "]")?;
   */
  @Override
  public void visit(final ASTAliasDecl astAliasDecl) {
    printDeclarationComment(astAliasDecl);
    printAliasPrefix(astAliasDecl);
    printDeclarationStatement(astAliasDecl);
    printInvariants(astAliasDecl);

  }

  private void printDeclarationComment(ASTAliasDecl astAliasDecl) {
    print(printComments(astAliasDecl));
    //print(ASTUtils.printMultilineComments(astAliasDecl));
    println();
  }

  private void printAliasPrefix(final ASTAliasDecl astAliasDecl) {
    if (astAliasDecl.isLog()) {
      print("log ");
    }

    if (astAliasDecl.isSuppress()) {
      print("suppress ");
    }

    if (astAliasDecl.isAlias()) {
      print("alias ");
    }
  }

  private void printDeclarationStatement(final ASTAliasDecl astAliasDecl) {
    final SPLPrettyPrinter splPrettyPrinter = createDefaultPrettyPrinter(getIndentionLevel());
    splPrettyPrinter.printDeclaration(astAliasDecl.getDeclaration()); // TODO refactor as soon a the visitor is
    // generated
    print(splPrettyPrinter.getResult());
  }


  private void printInvariants(final ASTAliasDecl astAliasDecl) {
    if (astAliasDecl.getInvariant().isPresent()) {
      print("[[");
      final ASTExpr astInvariant = astAliasDecl.getInvariant().get();
      print(expressionsPrinter.print(astInvariant));
      print("]]");

    }
  }

  /**
   * Grammar:
   * AliasDecl = ([hide:"-"])? ([alias:"alias"])? Declaration ("[" invariants:Expr (";" invariants:Expr)* "]")?;
   */
  @Override
  public void endVisit(final ASTAliasDecl astAliasDecl) {
    println();
  }

  /**
   * Grammar:
   * Input implements BodyElement = "input"
   * BLOCK_OPEN
   *   (InputLine | SL_COMMENT | NEWLINE)*
   * BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTInput astInput) {
    println("input" + BLOCK_OPEN);
    indent();
  }

  /**
   * Grammar:
   * Input implements BodyElement = "input"
   * BLOCK_OPEN
   *   (InputLine | SL_COMMENT | NEWLINE)*
   * BLOCK_CLOSE;
   */
  @Override
  public void endVisit(final ASTInput astInput) {
    unindent();
    println(BLOCK_CLOSE);
  }

  /**
   * Equations implements BodyElement =
   * "equations"
   * BLOCK_OPEN
   *   OdeDeclaration
   * BLOCK_CLOSE;
   *
   * OdeDeclaration  = (Eq | Shape | ODEAlias | NEWLINE)+;
   * Equation = lhs:Derivative "=" rhs:Expr (";")?;
   * Derivative = name:QualifiedName (differentialOrder:"\'")*;
   * ODEAlias = variableName:Name Datatype "=" Expr;
   */
  @Override
  public void visit(final ASTOdeDeclaration astOdeDeclaration) {
    println("equations" + BLOCK_OPEN);
    astOdeDeclaration.getODEAliass().forEach(this::printODEAlias);
    astOdeDeclaration.getODEs().forEach(this::printEquation);
    astOdeDeclaration.getShapes().forEach(this::printShape);

    indent();
  }

  private void printEquation(final ASTEquation astEquation) {
    println(astEquation.getLhs() + " = " + expressionsPrinter.print(astEquation.getRhs()));
  }

  public void printShape(final ASTShape astShape) {
    println("shape " + astShape.getLhs() + " = " + expressionsPrinter.print(astShape.getRhs()));
  }

  public void printODEAlias(final ASTODEAlias astOdeAlias) {
    final String datatype = ASTUtils.computeTypeName(astOdeAlias.getDatatype(), true);
    final String initExpression = expressionsPrinter.print(astOdeAlias.getExpr());
    println(astOdeAlias.getVariableName() + " " + datatype + " = " + initExpression);
  }

  @Override
  public void endVisit(final ASTEquations astEquations) {
    unindent();
    println(BLOCK_CLOSE);
  }

  /**
   * grammar
   * InputLine = Name "<-" InputType* ([spike:"spike"]|[current:"current"]);
   * InputType = (["inhibitory"]|["excitatory"]);
   */
  @Override
  public void visit(final ASTInputLine astInputLine) {
    print(astInputLine.getName());
    printArrayParameter(astInputLine);
    print(" <- ");
    printInputTypes(astInputLine.getInputTypes());
    printOutputType(astInputLine);
    println();
  }

  private void printInputTypes(final List<ASTInputType> inputTypes) {
    for (final ASTInputType inputType:inputTypes) {
      if (inputType.isInhibitory()) {
        print("inhibitory ");
      }
      else {
        print("excitatory ");
      }

    }

  }

  private void printArrayParameter(final ASTInputLine astInputLine) {
    astInputLine.getSizeParameter().ifPresent(parameter -> print("[" + parameter + "]"));
  }

  private void printOutputType(final ASTInputLine astInputLine) {
    if (astInputLine.isSpike()) {
      print("spike");
    }
    else {
      print("current");
    }

  }

  /**
   * Output implements BodyElement =
   * "output" BLOCK_OPEN ([spike:"spike"]|[current:"current"]) ;
   */
  @Override
  public void visit(final ASTOutput astOutput) {
    print("output: ");
    if (astOutput.isSpike()) {
      print("spike");
    }
    else {
      print("current");
    }

    println();
  }

  /**
   * Grammar:
   * Structure implements BodyElement = "structure"
   * BLOCK_OPEN
   *  (StructureLine | SL_COMMENT | NEWLINE)*
   * BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTStructure astStructure) {
    println("structure" + BLOCK_OPEN);
    indent();
  }

  @Override
  public void endVisit(final ASTStructure astStructure) {
    unindent();
    println(BLOCK_CLOSE);
  }

  /**
   * StructureLine = compartments:QualifiedName ("-" compartments:QualifiedName)*;
   */
  @Override
  public void visit(final ASTStructureLine astStructureLine) {
    final List<ASTQualifiedName> compartments = astStructureLine.getCompartments();
    for (int curCompartmentsIndex = 0; curCompartmentsIndex < compartments.size(); ++ curCompartmentsIndex) {
      final ASTQualifiedName compartmentName = compartments.get(curCompartmentsIndex);
      boolean isLastCompartment = (curCompartmentsIndex + 1) == compartments.size();
      print(Names.getQualifiedName(compartmentName.getParts()) +  " ");
      if (!isLastCompartment) {
        print("- ");
      }

    }

  }

  /**
   * Function implements BodyElement =
     "function" Name "(" Parameters? ")" (returnType:QualifiedName | PrimitiveType)?
     BLOCK_OPEN
       Block
     BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTFunction astFunction) {
    print("function " + astFunction.getName());
    printParameters(astFunction.getParameters());
    printOptionalReturnValue(astFunction);
    println(BLOCK_OPEN);
    indent();
    printSplBlock(astFunction.getBlock());
    unindent();
    println(BLOCK_CLOSE);

  }

  private void printParameters(final Optional<ASTParameters> functionParameters) {
    print("(");
    if (functionParameters.isPresent()) {
      final List<ASTParameter> astParameters = functionParameters.get().getParameters();
      for (int curParameterIndex = 0; curParameterIndex < astParameters.size(); ++curParameterIndex) {
        boolean isLastParameter = (curParameterIndex + 1) == astParameters.size();
        final ASTParameter curParameter = astParameters.get(curParameterIndex);
        print(curParameter.getName() + " " +  ASTUtils.computeTypeName(curParameter.getDatatype(),true));
        if (!isLastParameter) {
          print(", ");
        }

      }

    }
    print(")");
  }

  private void printOptionalReturnValue(final ASTFunction astFunction) {
    if (astFunction.getReturnType().isPresent()) {
      print(ASTUtils.computeTypeName(astFunction.getReturnType().get(),true));
    }

  }


  private void printSplBlock(final ASTBlock astBlock) {
    final  SPLPrettyPrinter splPrettyPrinter = createDefaultPrettyPrinter(getIndentionLevel());

    astBlock.accept(splPrettyPrinter);

    print(splPrettyPrinter.getResult());
  }

  /**
   * Dynamics implements BodyElement =
   * "update"
   *   BLOCK_OPEN
   *     Block
   *   BLOCK_CLOSE;
   */
  @Override
  public void visit(final ASTDynamics astDynamics) {
    print("update");
    println(BLOCK_OPEN);
    indent();
    printSplBlock(astDynamics.getBlock());
    unindent();
    println();
    println(BLOCK_CLOSE);
  }

}
