package com.github.pedrovgs;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyASTVisitor {

    CompilationUnit unit;
    MethodDeclaration[] methodDeclarationList;
    FieldDeclaration[] fieldDeclarationList;

    HashMap<String, String> table = new HashMap<String, String>();

    public void addTemplateCode(CompilationUnit unit, File file_new) {

        this.unit = unit;

        AST ast = unit.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        unit.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {
                methodDeclarationList = node.getMethods();
                fieldDeclarationList = node.getFields();

                for (int i = 0; i < methodDeclarationList.length; i++) {
                    MethodDeclaration method = methodDeclarationList[i];
                    method.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(Block block) {
                            ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

                            for (Statement s : (List<Statement>) block.statements()) {

                                if (s instanceof ExpressionStatement) {
                                    ((ExpressionStatement) s).getExpression().toString();
                                    s.accept(new ASTVisitor() {
                                        @Override
                                        public boolean visit(VariableDeclarationFragment node) {

                                            return super.visit(node);
                                        }
                                    });

                                    //Getting type of statement
                                    System.out.println("--------------");
                                    String value[] = s.getClass().toString().split("\\.");
                                    String statement_type = value[value.length-1];

                                    int l = unit.getLineNumber(s.getStartPosition());

                                    System.out.println("Statement Type   "+((ExpressionStatement) s).getExpression().toString());

                                    MethodInvocation methodInvocation = ast.newMethodInvocation();
                                    QualifiedName qName = ast.newQualifiedName(ast.newSimpleName("System"),
                                            ast.newSimpleName("out"));
                                    methodInvocation.setExpression(qName);
                                    methodInvocation.setName(ast.newSimpleName("println"));
                                    StringLiteral literal = ast.newStringLiteral();
                                    StringLiteral literal2 = ast.newStringLiteral();
                                    SimpleName literal3 = ast.newSimpleName("");
                                    literal.setLiteralValue(String.valueOf(l));
                                    literal2.setLiteralValue(String.valueOf(statement_type));
                                    methodInvocation.arguments().add(literal);
                                    methodInvocation.arguments().add(literal2);
                                    methodInvocation.arguments().add(literal3);
                                    System.out.println("METHOD INVOCATION" + methodInvocation);

                                    ASTNode current_statement = s;
                                    //System.out.println("Statement : "+s);
                                    ExpressionStatement statement1 = ast.newExpressionStatement(methodInvocation);
                                    listRewrite.insertAfter(statement1, current_statement, null);
                                }

                                if (s instanceof ForStatement) {

                                    int l = unit.getLineNumber(s.getStartPosition());
                                    System.out.println("-----------------"+s.getNodeType());
                                    System.out.println("Statement Type   "+((ForStatement) s).getExpression().toString());
                                    MethodInvocation methodInvocation = ast.newMethodInvocation();
                                    SimpleName qName = ast.newSimpleName("Template");
                                    methodInvocation.setExpression(qName);
                                    methodInvocation.setName(ast.newSimpleName("instrum"));
                                    StringLiteral literal = ast.newStringLiteral();
                                    literal.setLiteralValue(String.valueOf(l));
                                    methodInvocation.arguments().add(literal);
                                    //methodInvocation.arguments().add(statement_type);
                                    //methodInvocation.arguments().add(table);

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
        } catch (org.eclipse.jface.text.BadLocationException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.writeStringToFile(file_new, document.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void add(CompilationUnit unit, File file_new) {
        this.unit = unit;
        unit.accept(new ASTVisitor() {
            ASTRewrite rewrite;

            int lineNumber=0;
            @Override
            public boolean visit(TypeDeclaration node) throws MalformedTreeException {

                methodDeclarationList = node.getMethods();
                for (int i = 0; i < methodDeclarationList.length; i++) {
                    MethodDeclaration method = methodDeclarationList[i];
                    Block block = method.getBody();
                    /*for (Statement s : (List<Statement>) block.statements()) {
                        int l=unit.getLineNumber(s.getStartPosition());
                        if (l==lineNumber){
                            System.out.println(s.toString());
                        }
                    }
                    */

                    AST ast = node.getAST();
                    MethodInvocation methodInvocation = ast.newMethodInvocation();
                    SimpleName qName = ast.newSimpleName("Template");
                    methodInvocation.setExpression(qName);
                    methodInvocation.setName(ast.newSimpleName("instrum"));
                    StringLiteral literal = ast.newStringLiteral();

                    literal.setLiteralValue(String.valueOf(unit.getLineNumber(block.getStartPosition())));
                    methodInvocation.arguments().add(literal);

                    List statements = block.statements();
                    ASTNode second = (ASTNode) statements.get(0);
                    System.out.println("Second: "+second);
                    ExpressionStatement statement1 = ast.newExpressionStatement(methodInvocation);
                    //System.out.println(statement1.toString());

                    rewrite = ASTRewrite.create(ast);
                    ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
                    //rewrite.getListRewrite(block, block.)
                    //listRewrite.remove(second, null);
                    listRewrite.insertBefore(statement1, second, null);

                }

                String str = null;
                try {
                    str = FileUtils.readFileToString(file_new);
                } catch (IOException e) {
                    e.printStackTrace();
                }Document document = new Document(str);
                TextEdit edits = rewrite.rewriteAST(document, null);
                try {
                    edits.apply(document);
                    //System.out.println(document.get() );
                } catch (org.eclipse.jface.text.BadLocationException e) {
                    e.printStackTrace();
                }
                try {
                    FileUtils.writeStringToFile(file_new, document.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return super.visit(node);
            }


        });
    }
     public void doVisit(final CompilationUnit unit) {
        this.unit = unit;


        unit.accept(new ASTVisitor() {


            @Override
            public boolean visit(TypeDeclaration node) {

                table.put(node.getName().toString(), "");


                methodDeclarationList = node.getMethods();
                for (int i = 0; i < methodDeclarationList.length; i++) {
                    MethodDeclaration method = methodDeclarationList[i];
                    table.put(method.getName().toString(), "");
                    // System.out.println("METHOD NAME ---------------"+method.getName().toString());
                }


                fieldDeclarationList = node.getFields();
                //System.out.println("FIELD DECLARATION list----------------"+fieldDeclarationList.length);  //Problem as the value is ZERO
                for (int i = 0; i < fieldDeclarationList.length; i++) {
                    //System.out.println("-------------------------------------------------");
                    FieldDeclaration field = fieldDeclarationList[i];

                    for (Iterator iter = field.fragments().iterator(); iter.hasNext();) {
                        VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
                        // System.out.println("FIELD ----------------" + field);
                        for (Map.Entry m : table.entrySet()) {
                            //System.out.println(m.getValue()+"testing ");
                            if (m.getValue() == "")
                                table.put(m.getKey().toString(), fragment.toString());
                            else {
                                table.put(m.getKey().toString(), m.getValue() + " , " + fragment.toString());
                                //System.out.println(m.getKey() + "--------- " + m.getValue() + "");
                            }
                            //System.out.println(m.getValue()+" 2 testing ");
                        }
                    }
                }
                int number =1;
                //printTable(number);

                for (int i = 0; i < methodDeclarationList.length; i++) {
                    final MethodDeclaration method = methodDeclarationList[i];
                    Block block = method.getBody();

                    block.accept(new ASTVisitor() {

                        public boolean visit(VariableDeclarationFragment var) {
                            String key = method.getName().toString();
                            //System.out.println(table.get(key)+key+" 3 testing ");
                            //System.out.println(key+ " : "+table.get(key));
                            if (table.get(key) == "")
                                table.put(key, var.toString()); // variable name
                            else {
                                table.put(key, table.get(key) + " , " + var.toString()); // variable name
                                //System.out.println("----------------------"+var.toString());
                            }

                            //System.out.println(table.get(key)+" 4 testing ");

                            //table.put(method.getName().toString(), unit.getLineNumber(method.getStartPosition())+""); // method line
                            //multimap.put(method.getName(), method.getName()); // method name

                            //Then what should i do if found variable name, method line and method name more than one in the Multimap?

                            return false;
                        }
                    });
                }


                return super.visit(node);
            }


           /* @Override
            public boolean visit(WhileStatement node) {


                return super.visit(node);

            }
            */






        });
    }
    int k = 0;
    public void printTable() {

        System.out.println("Computation of Scopes and variables declared in scopes: ");
        for (Map.Entry m : table.entrySet()) {

            k=k+1;
            System.out.println(k+"." +" SCOPE: " + m.getKey() + "  VARIABLES:  " + m.getValue());
        }
    }

}

