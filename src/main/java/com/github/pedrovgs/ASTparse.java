package com.github.pedrovgs;
import java.io.File;
import java.util.List;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;

import org.eclipse.text.edits.MalformedTreeException;


public class ASTparse{

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        ASTparse  test = new ASTparse();
        test.processJavaFile(new File("./src/main/java/com/github/pedrovgs/problem1/BitsCounter.java"));
    }


    public void processJavaFile(File file) throws IOException, MalformedTreeException, BadLocationException {

        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);
        String unitName = "BitsCounterTest.java";
        parser.setUnitName(unitName);
        String[] sources = { "./src/test/java/com/github/pedrovgs/problem1/" };
        String[] classpath = {};

        //String[] classpath = {"./src/main/java/com/github/pedrovgs/binarytree/BinaryNode.java"};
        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(FileUtils.readFileToString(file).toCharArray());
        final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        /* String source = FileUtils.readFileToString(file);
        Document document = new Document(source);
        ASTParser parser = ASTParser.newParser(AST.JLS4);

        parser.setSource(document.get().toCharArray());
        CompilationUnit unit = (CompilationUnit)parser.createAST(null);
        */
        unit.recordModifications();
        TraverseManager manager = new TraverseManager();
        manager.calculateVariableScopes(unit);
        //manager.calculateExpressionStatements(unit);
        manager.printTable();
        manager.Instrument2(unit,file);
        //ExtractingVisitor visitor1=new ExtractingVisitor();
        //unit.accept(visitor1);

        // to get the imports from the file
        List<ImportDeclaration> imports = unit.imports();
        for (ImportDeclaration i : imports) {
            System.out.println(i.getName().getFullyQualifiedName());
        }

        // to create a new import
        AST ast = unit.getAST();

        // to iterate through methods
        List<AbstractTypeDeclaration> types = unit.types();
        for (AbstractTypeDeclaration type : types) {
            if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
                // Class def found
                List<BodyDeclaration> bodies = type.bodyDeclarations();
                for (BodyDeclaration body : bodies) {
                    if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
                        MethodDeclaration method = (MethodDeclaration)body;
                        System.out.println("name: " + method.getName().getFullyQualifiedName());
                    }
                }
            }
        }
    }
}


