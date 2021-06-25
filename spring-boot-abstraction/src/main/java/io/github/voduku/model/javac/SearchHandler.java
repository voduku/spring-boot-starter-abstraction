package io.github.voduku.model.javac;

import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Flags.FINAL;
import static com.sun.tools.javac.code.Flags.INTERFACE;
import static com.sun.tools.javac.code.Flags.PRIVATE;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.util.List.nil;
import static lombok.javac.handlers.JavacResolver.CLASS;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import io.github.voduku.model.annotation.Search;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import org.kohsuke.MetaInfServices;

/**
 * @author VuDo
 * @since 6/19/2021
 */
@HandlerPriority(-1)
@MetaInfServices(JavacAnnotationHandler.class)
public class SearchHandler extends JavacAnnotationHandler<Search> {

  private static final Set<String> excludedAnnotations = Set.of(
      OneToMany.class.getSimpleName(),
      ManyToOne.class.getSimpleName(),
      OneToOne.class.getSimpleName(),
      ManyToMany.class.getSimpleName()
  );

  @Override
  public void handle(AnnotationValues<Search> annotation, JCAnnotation source, JavacNode node) {
    Object entityType = annotation.getActualExpression("value");
    if (entityType == null) {
      node.addError(String.format("@%s has no effect since no entity type was specified.", Search.class.getName()));
      return;
    }
    System.out.println("\n\n\n\n\nI AM RUNNNINGNAIGNIGNGINGI\n\n\n\n\n\n");
    final List<VarSymbol> vars = getColumns(node, entityType);
    if (vars.isEmpty()) {
      return;
    }
    JavacTreeMaker factory = node.getTreeMaker();

    JavacNode holderClass = node.up();
    addCriteriaImport(factory, holderClass);
    ListBuffer<JCExpression> varNames = new ListBuffer<>();
    for (VarSymbol var : vars) {
      addInstanceVar(factory, holderClass, var);
      varNames.append(factory.Literal(var.name.toString()));
    }
    addSearchVar(factory, holderClass, varNames.toList());
    addGetCriteriaMethod(factory, holderClass, vars);

    for (JCTree def : ((JCClassDecl) holderClass.get()).defs) {
      if (def instanceof JCVariableDecl && ((JCVariableDecl) def).init != null) {
        JCVariableDecl v = (JCVariableDecl) def;
        if (v.init instanceof JCNewClass) {
          System.out
              .println(v + " : " + ((JCNewClass) v.init).args.map(arg -> ((JCMethodInvocation) arg).meth.getClass()).toString() + " : " + v.init.isPoly());
        }

      }
    }
    System.out.println();
    node.rebuild();
  }

  /* PROCESS ENTITY CLASS */

  private List<VarSymbol> getColumns(final JavacNode typeNode, Object obj) {
    if (!(obj instanceof JCFieldAccess)) {
      return List.of();
    }
    JCFieldAccess entity = (JCFieldAccess) obj;
    if (!("class".equals(entity.name.toString()))) {
      return List.of();
    }
    Type entityType = CLASS.resolveMember(typeNode, entity.selected);
    if (entityType == null || (entityType.tsym.flags() & (INTERFACE | ANNOTATION)) != 0) {
      return List.of();
    }

    List<VarSymbol> columns = new ArrayList<>();

    TypeSymbol tsym = entityType.asElement();
    if (tsym != null) {
      for (Symbol member : tsym.getEnclosedElements()) {
        extractEntityColumns(columns, member);
      }
      if (((ClassSymbol) tsym).getSuperclass() != null) {
        for (Symbol member : ((ClassSymbol) tsym).getSuperclass().tsym.getEnclosedElements()) {
          extractEntityColumns(columns, member);
        }
      }
    }

    return columns;
  }

  private void extractEntityColumns(List<VarSymbol> columns, Symbol member) {
    if (member.getKind() != ElementKind.FIELD
        || member.getModifiers().contains(Modifier.TRANSIENT)
        || member.getAnnotationMirrors()
        .map(cp -> cp.getAnnotationType().asElement().getSimpleName().toString())
        .stream()
        .anyMatch(excludedAnnotations::contains)
    ) {
      return;
    }
    columns.add((VarSymbol) member);
  }

  /* CREATE NECESSARY IMPORTS */

  private void addCriteriaImport(JavacTreeMaker factory, JavacNode holderClass) {
    JCTree.JCFieldAccess criteriaFieldAccess = factory.Select(factory.Ident(holderClass.toName("io.github.voduku.model.criteria")), holderClass.toName("*"));
    JCTree.JCImport criteriaImport = factory.Import(criteriaFieldAccess, false);
    JCTree.JCFieldAccess mapFieldAccess = factory.Select(factory.Ident(holderClass.toName("java.util")), holderClass.toName("Map"));
    JCTree.JCImport mapImport = factory.Import(mapFieldAccess, false);
    JCTree.JCFieldAccess hashMapFieldAccess = factory.Select(factory.Ident(holderClass.toName("java.util")), holderClass.toName("HashMap"));
    JCTree.JCImport hashMapImport = factory.Import(hashMapFieldAccess, false);
    JCTree.JCFieldAccess listFieldAccess = factory.Select(factory.Ident(holderClass.toName("java.util")), holderClass.toName("Set"));
    JCTree.JCImport listImport = factory.Import(listFieldAccess, false);
    JCTree.JCFieldAccess arrayListFieldAccess = factory.Select(factory.Ident(holderClass.toName("java.util")), holderClass.toName("LinkedHashSet"));
    JCTree.JCImport arrayListImport = factory.Import(arrayListFieldAccess, false);
    JCCompilationUnit unit = (JCCompilationUnit) holderClass.top().get();
    unit.defs.tail = unit.defs.tail.prepend(arrayListImport).prepend(listImport).prepend(hashMapImport).prepend(mapImport)
        .prepend(criteriaImport);
//    System.out.println(unit.defs);
  }

  /* CREATE SEARCH FIELDS */
  private void addSearchVar(JavacTreeMaker factory, JavacNode holderClass, com.sun.tools.javac.util.List<JCExpression> vars) {
    JCTree.JCModifiers excludablesMod = factory.Modifiers(PRIVATE + STATIC + FINAL);
    JCExpression excludablesArrayType = factory.TypeArray(factory.Ident(holderClass.toName("String")));
    JCExpression excludablesNewArray = factory.NewArray(factory.Ident(holderClass.toName("String")), com.sun.tools.javac.util.List.nil(), vars);
    JCTree.JCVariableDecl excludablesVar = factory.VarDef(excludablesMod, holderClass.toName("excludables"), excludablesArrayType, excludablesNewArray);

    // includes, excludes
    JCTree.JCModifiers inclExclMod = factory.Modifiers(PRIVATE);
    JCExpression listStringType = factory.TypeApply(factory.Ident(holderClass.toName("Set")), com.sun.tools.javac.util.List.of(factory.Ident(holderClass.toName("String"))));
    JCFieldAccess asListAccess = factory.Select(factory.getUnderlyingTreeMaker().QualIdent(holderClass.getSymbolTable().arraysType.tsym), holderClass.toName("asList"));
    JCExpression asListCall = factory.Apply(nil(), asListAccess, com.sun.tools.javac.util.List.of(factory.Ident(holderClass.toName("excludables"))));
    JCExpression includesInitType = factory.TypeApply(factory.Ident(holderClass.toName("LinkedHashSet")), nil());
    JCExpression includesInit = factory.NewClass(null, nil(), includesInitType, com.sun.tools.javac.util.List.of(asListCall), null);
    JCTree.JCVariableDecl includesVar = factory.VarDef(inclExclMod, holderClass.toName("includes"), listStringType, includesInit);
    JCTree.JCVariableDecl excludesVar = factory.VarDef(inclExclMod, holderClass.toName("excludes"), listStringType, null);

    JavacHandlerUtil.injectField(holderClass, excludesVar);
    JavacHandlerUtil.injectField(holderClass, includesVar);
    JavacHandlerUtil.injectField(holderClass, excludablesVar);
  }

  private void addInstanceVar(JavacTreeMaker factory, JavacNode holderClass, VarSymbol var) {
    JCTree.JCModifiers fieldMod = factory.Modifiers(PRIVATE);
    JCTree.JCIdent classType = resolveClassType(factory, holderClass, var);
    JCTree.JCVariableDecl instanceVar = factory.VarDef(fieldMod, var.name, classType, null);
    JavacHandlerUtil.injectField(holderClass, instanceVar);
  }

  private JCIdent resolveClassType(JavacTreeMaker factory, JavacNode holderClass, VarSymbol var) {
    String superClassName = ((ClassSymbol) var.type.tsym).getSuperclass().toString();
    // String is resolved into Object
    if (Object.class.getTypeName().equals(superClassName)) {
      return factory.Ident(holderClass.toName("StringCriteria"));
    }
    if (Number.class.getTypeName().equals(superClassName)) {
      return factory.Ident(holderClass.toName("NumberCriteria"));
    }
    if (Date.class.getTypeName().equals(superClassName)) {
      return factory.Ident(holderClass.toName("DateCriteria"));
    }
    if (Boolean.class.getTypeName().equals(superClassName)) {
      return factory.Ident(holderClass.toName("BooleanCriteria"));
    }
    if (superClassName.contains(Enum.class.getSimpleName())) {
      return factory.Ident(holderClass.toName("StringCriteria"));
    }
    throw new UnsupportedOperationException("Cannot resolve data type of field if you use primitive class then convert it to wrapper object: " + var.name);
  }

  /* CREATE getCriteria() */

  private void addGetCriteriaMethod(JavacTreeMaker factory, JavacNode holderClass, List<VarSymbol> variables) {
    JCExpression string = factory.Ident(holderClass.toName("String"));
    JCExpression criteriaHandler = factory
        .TypeApply(factory.Ident(holderClass.toName("CriteriaHandler")),
            com.sun.tools.javac.util.List.of(factory.Wildcard(factory.TypeBoundKind(BoundKind.UNBOUND), null)));
    JavacHandlerUtil.injectMethod(
        holderClass,
        factory.MethodDef(
            factory.Modifiers(Flags.PUBLIC),
            holderClass.toName("getCriteria"),
            factory.TypeApply(factory.Ident(holderClass.toName("Map")), com.sun.tools.javac.util.List.of(string, criteriaHandler)),
            nil(),
            nil(),
            nil(),
            createMethodBody(factory, holderClass, variables),
            null
        ));
  }

  private JCBlock createMethodBody(JavacTreeMaker factory, JavacNode holderClass, List<VarSymbol> variables) {
    ListBuffer<JCStatement> statements = new ListBuffer<>();
    statements.append(createMap(factory, holderClass));
    statements.appendList(createMapPuts(factory, holderClass, variables));
    statements.append(factory.Return(factory.Ident(holderClass.toName("criteria"))));
    return factory.Block(0, statements.toList());
  }

  private JCVariableDecl createMap(JavacTreeMaker factory, JavacNode holderClass) {
    JCExpression string = factory.Ident(holderClass.toName("String"));
    JCExpression criteriaHandler = factory
        .TypeApply(factory.Ident(holderClass.toName("CriteriaHandler")),
            com.sun.tools.javac.util.List.of(factory.Wildcard(factory.TypeBoundKind(BoundKind.UNBOUND), null)));
    com.sun.tools.javac.util.List<JCExpression> typeArgs = com.sun.tools.javac.util.List.of(string, criteriaHandler);
    JCExpression varType = factory.TypeApply(factory.Ident(holderClass.toName("Map")), typeArgs);
    Name varName = holderClass.toName("criteria");
    JCExpression varInitType = factory.TypeApply(factory.Ident(holderClass.toName("HashMap")), typeArgs);
    JCNewClass varInit = factory.NewClass(null, nil(), varInitType, nil(), null);
    return factory.VarDef(factory.Modifiers(Flags.PARAMETER), varName, varType, varInit);
  }

  private ListBuffer<JCStatement> createMapPuts(JavacTreeMaker factory, JavacNode holderClass, List<VarSymbol> variables) {
    ListBuffer<JCStatement> puts = new ListBuffer<>();
    for (VarSymbol variable : variables) {
      puts.add(factory.Exec(factory.Apply(
          nil(),
          factory.Select(factory.Ident(holderClass.toName("criteria")), holderClass.toName("put")),
          com.sun.tools.javac.util.List.of(factory.Literal(variable.name.toString()), factory.Ident(holderClass.toName(variable.name.toString())))
      )));
    }
    return puts;
  }
}
