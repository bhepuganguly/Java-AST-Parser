package com.github.pedrovgs;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.projection.Fragment;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TraverseManager {

    CompilationUnit unit;
    HashMap<String, String> table = new HashMap<String, String>();
    MethodDeclaration[] methodDeclarationList;
    FieldDeclaration[] fieldDeclarationList;
    List<VariableDeclarationFragment> globalVariableFragmentList;
    String globalVariables = "";
    String package_name = "";
    ;
    String class_name = "";

    public void calculateVariableScopes(CompilationUnit unit) {

        this.unit = unit;
        unit.accept(new ASTVisitor() {
            Set names = new HashSet();
            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());
                System.out.println("Declaration of '" + name + "' at line"+ unit.getLineNumber(name.getStartPosition()));
                return false; // do not continue
            }
            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
                    System.out.println("Usage of '" + node + "' at line "+ unit.getLineNumber(node.getStartPosition()));
                }
                return true;
            }
        });
        unit.accept(new ASTVisitor() {
            @Override
            public boolean visit(PackageDeclaration node) {
                package_name = node.getName().toString();
                return super.visit(node);
            }
        });
        unit.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {

                table.put(package_name+"."+node.getName()+"{}", "");

                methodDeclarationList = node.getMethods();
                for (MethodDeclaration method : methodDeclarationList) {
                    //System.out.println("METHOD "+method);
                    table.put(package_name+"."+method.getName()+"()", "");
                }

                fieldDeclarationList = node.getFields();
                for (FieldDeclaration field : fieldDeclarationList) {
                    globalVariableFragmentList = field.fragments();
                    //System.out.println("FIELD " + field.fragments().toString());

                    for (VariableDeclarationFragment fragment : globalVariableFragmentList) {
                        //System.out.println(fragment);
                        if (globalVariables == "" || globalVariables == null)
                            globalVariables = fragment.getName().getIdentifier();
                        else
                            globalVariables += " , " + fragment.getName().getIdentifier();
                        //System.out.println(globalVariables);
                    }
                }

                for (Map.Entry m : table.entrySet()) {
                    //System.out.println(m.getKey() + " :: " + m.getValue());
                    if (m.getValue() == "" || m.getValue() == null)
                        table.put(m.getKey().toString(), globalVariables);
                    else
                        table.put(m.getKey().toString(), m.getValue() + " , " + globalVariables);
                    //System.out.println(m.getKey() + " " + m.getValue() + "");
                }

                for (int i = 0; i < methodDeclarationList.length; i++) {
                    MethodDeclaration method = methodDeclarationList[i];
                    Block block = method.getBody();
                    method.getBody().statements();

                    block.accept(new ASTVisitor() {

                        public boolean visit(VariableDeclarationFragment var) {
                            //System.out.println(var.resolveBinding().getDeclaringMethod());
                            //System.out.println("Variable " + var.getName() + ", in Method " + method.getName() + "' Method line " + unit.getLineNumber(method.getStartPosition()));
                            String key = package_name+"."+method.getName()+"()";
                            //System.out.println(key + " : " + table.get(key));
                            if (table.get(key) == "")
                                table.put(key, var.getName().getIdentifier()); // variable name
                            else
                                table.put(key, table.get(key) + " , " + var.getName().getIdentifier()); // variable name
                            return false;
                        }

                        @Override
                        public boolean visit(IfStatement ifStatement) {
                            ifStatement.accept(new ASTVisitor() {

                                public boolean visit(VariableDeclarationFragment var) {
                                    //System.out.println(var.resolveBinding().getDeclaringMethod());
                                    //System.out.println("Variable " + var.getName() + ", in Method " + method.getName() + "' Method line " + unit.getLineNumber(method.getStartPosition()));
                                    String key = package_name+"."+method.getName()+"() : if("+ifStatement.getExpression().toString()+") {}";
                                    //System.out.println(key + " : " + table.get(key));
                                    String value = table.get(key);
                                    if ( value == "" || value == null)
                                        table.put(key, var.getName().getIdentifier()); // variable name
                                    else
                                        table.put(key, table.get(key) + " , " + var.getName().getIdentifier()); // variable name
                                    return false;
                                }
                            });
                            return super.visit(ifStatement);
                        }

                        @Override
                        public boolean visit(WhileStatement whileStatement) {
                            whileStatement.accept(new ASTVisitor() {

                                public boolean visit(VariableDeclarationFragment var) {
                                    //System.out.println(var.resolveBinding().getDeclaringMethod());
                                    //System.out.println("Variable " + var.getName() + ", in Method " + method.getName() + "' Method line " + unit.getLineNumber(method.getStartPosition()));
                                    String key = package_name+"."+method.getName()+" : while("+whileStatement.getExpression().toString()+") {}";
                                    //System.out.println(key + " : " + table.get(key));
                                    String value = table.get(key);
                                    if ( value == "" || value == null)
                                        table.put(key, var.getName().getIdentifier()); // variable name
                                    else
                                        table.put(key, table.get(key) + " , " + var.getName().getIdentifier()); // variable name
                                    return false;
                                }
                            });
                            return super.visit(whileStatement);
                        }
                    });
                }

                return super.visit(node);
            }
        });
    }

    public void printTable() {
        for (Map.Entry m : table.entrySet()) {
            System.out.println("INSIDE SCOPE : "+  m.getKey() );
            System.out.println( "VARIABLES ARE : " + m.getValue()+"\n\n");
        }
    }

    public void Instrument2(CompilationUnit unit, File file_new) {

        this.unit = unit;

        AST ast = unit.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        unit.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {
                class_name = node.getName().toString();
                methodDeclarationList = node.getMethods();

                for (int i = 0; i < methodDeclarationList.length; i++) {
                    MethodDeclaration method = methodDeclarationList[i];
                    method.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(Block block) {
                            ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

                            for (Statement s : (List<Statement>) block.statements()) {

                                if (s instanceof ExpressionStatement) {

                                    s.accept(new ASTVisitor() {
                                        @Override
                                        public boolean visit(Assignment assignment) {

                                            //System.out.println(package_name+"."+class_name+"."+method.getName()+"."+assignment.getLeftHandSide());
                                            //System.out.println(((ExpressionStatement) s).getExpression().toString());
                                            //System.out.println(((ExpressionStatement) s).getExpression().getLocationInParent());
                                            //((ExpressionStatement) s).getExpression().
                                            //Getting type of statement
                                            String value[] = s.getClass().toString().split("\\.");
                                            String statement_type = value[value.length - 1];

                                            int l = unit.getLineNumber(s.getStartPosition());

                                            //System.out.println("Statement Type "+((ExpressionStatement) s).getExpression().toString());


                                            assignment.getRightHandSide().accept(new ASTVisitor() {
                                                @Override
                                                public boolean visit(InfixExpression rightSide) {

                                                    String preName = package_name + "." + class_name + "." + method.getName().toString();
                                                    MethodInvocation methodInvocation = ast.newMethodInvocation();
                                                    //QualifiedName qName = ast.newQualifiedName(ast.newSimpleName("System")ast.newSimpleName("out"));
                                                    SimpleName qName = ast.newSimpleName("Template");
                                                    methodInvocation.setExpression(qName);
                                                    methodInvocation.setName(ast.newSimpleName("instrum"));

                                                    StringLiteral literal1 = ast.newStringLiteral();
                                                    StringLiteral literal2 = ast.newStringLiteral();
                                                    literal1.setLiteralValue(String.valueOf(l));
                                                    literal2.setLiteralValue(String.valueOf(statement_type));
                                                    methodInvocation.arguments().add(literal1);
                                                    methodInvocation.arguments().add(literal2);

                                                    HashMap<String, SimpleName> arguments_list = new HashMap<String, SimpleName>();
                                                    arguments_list.put(preName + "." + assignment.getLeftHandSide().toString(), ast.newSimpleName(assignment.getLeftHandSide().toString()));
                                                    arguments_list.put(preName + "." + rightSide.getLeftOperand().toString(), ast.newSimpleName(rightSide.getLeftOperand().toString()));
                                                    arguments_list.put(preName + "." + rightSide.getRightOperand().toString(), ast.newSimpleName(rightSide.getRightOperand().toString()));

                                                    for (Map.Entry<String, SimpleName> argument : arguments_list.entrySet()) {
                                                        //System.out.printf("Key : %s and Value: %s %n", argument.getKey(), argument.getValue());
                                                        StringLiteral a1 = ast.newStringLiteral();
                                                        a1.setLiteralValue(argument.getKey());
                                                        methodInvocation.arguments().add(a1);
                                                        methodInvocation.arguments().add(argument.getValue());
                                                    }

                                                    ASTNode current_statement = s;
                                                    //System.out.println("Statement : "+s);
                                                    ExpressionStatement statement1 = ast.newExpressionStatement(methodInvocation);
                                                    listRewrite.insertAfter(statement1, current_statement, null);
                                                    return super.visit(node);
                                                }
                                            });
                                            return super.visit(node);
                                        }
                                    });
                                    if (((ExpressionStatement) s).getExpression().toString().contains("System.out.println")) {
                                        continue;
                                    }
                                }

                                if (s instanceof ForStatement) {

                                    s.accept(new ASTVisitor() {
                                        @Override
                                        public boolean visit(InfixExpression node) {

                                            return super.visit(node);
                                        }
                                    });

                                    int l = unit.getLineNumber(s.getStartPosition());
                                    MethodInvocation methodInvocation = ast.newMethodInvocation();
                                    SimpleName qName = ast.newSimpleName("Template");
                                    methodInvocation.setExpression(qName);
                                    methodInvocation.setName(ast.newSimpleName("instrum"));
                                    StringLiteral literal = ast.newStringLiteral();
                                    literal.setLiteralValue(String.valueOf(l));
                                    methodInvocation.arguments().add(literal);

                                    ASTNode current_statement = s;
                                    //System.out.println("Statement : "+s);
                                    ExpressionStatement statement1 = ast.newExpressionStatement(methodInvocation);
                                    listRewrite.insertAfter(statement1, current_statement, null);
                                }
                            }

                            return super.visit(block);
                        }
                    });
                }
                return super.visit(node);
            }
        });
        String str = null;
        try {
            str = FileUtils.readFileToString(file_new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document document = new Document(str);
        TextEdit edits = rewrite.rewriteAST(document, null);
        try {
            edits.apply(document);
            //System.out.println(document.get() );
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.writeStringToFile(file_new, document.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}