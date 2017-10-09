## Objective of project: ##
I  gained experience about the third-party components for parsing Java code, gaining deeper understanding of the Java
grammar, Java modules and scopes, working with the design pattern Visitor.

I have added dependencies by adding the required libraries of org.eclipse.jdt.{annotation, apt.core, compiler, runtime}in build.gradle
I have created an Abstract Syntax tree from the Java file  
I gained knowledge about AST, ASTParser, ASTNode, TypeDeclaration, FieldDeclaration, MethodInvocation, ExpressionStatement, Assignment and understood how it works and used the JDT AST plugin for viewing the Abstract Syntax Tree structure corresponding to each individual AST
I have successfully mapped the variablesto the scope with line numbers

I also created the instrumentation file by creating Template.instrum
I have created a scope table for mapping the variables to the scopes with line numbers 
The instrumentation program gives the output that containis line number, statement type and the values of the variables associated with the variables declared and used in the program. ####


## What is this repository for? ##
The repository contains the project files of HW1 plus the files that I created for doing HW2.
The files are as follows:
ASTParse - Creates Parse tree 
TraverseManager - contains methods for traversing through nodes by Visitor pattern for computing scopes and then for instrumenting the program.   ####
Template - contains the static instrum method which will be called for printing the logging values/instrumented statements.
Test- On which I worked for instrumentation



## How do I get set up? ##
To get started, clone the project folder and open it on IntelliJ. Once the project is opened on IntelliJ, then access the directory 
structure. 
Follow this path in the directory:
Algorithms_Commons->src->main->java->com->github->pedrovgs-> Now, Run ASTParse.java to perform the following operatins:
### To construct the Abstract Syntax Tree ###
### To compute scopes###
### To instrument the input program###

## What are the limitations of your implementation. ##
I have build the project using Gradle. I should also build it using SBT.

## Configuration
#### Java 1.8.0_144 #### 
#### Scala 2.12.3 (Java HotSpot(TM) 64-Bit Server VM) ####
#### IntelliJ Idea 2017.2.3 #### 

## Dependencies:##
#### group: 'junit', name: 'junit', version:'4.12'####
#### group: org.eclipse.jdt', name: 'org.eclipse.jdt.core', version: '3.7.1'####
#### group: 'org.eclipse.jdt', name: 'org.eclipse.jdt.annotation', version: '2.0.0'####
#### group: 'org.eclipse.jdt.core.compiler', name: 'ecj', version: '4.4.2'####
#### group: 'org.eclipse.core', name: 'org.eclipse.core.runtime', version: '3.7.0'####
#### group: 'org.apache.commons', name: 'commons-io', version: '1.3.2'####
#### group: 'log4j', name: 'log4j', version: '1.2.16'####



### Instructor ###
Mark Grechanik

### Teaching Assistant ###
Sri Phani Mohana Tejaswi Gorti# README #

