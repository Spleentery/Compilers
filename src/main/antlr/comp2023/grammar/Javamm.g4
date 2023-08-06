grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

//INTEGER : [0-9]+ ;
INTEGER : [0]|[1-9][0-9]* ; // CHANGES
ID : [a-zA-Z_$][a-zA-Z_0-9$]* ;

WS : [ \t\n\r\f]+ -> skip ;

//SLC : '//' ~[\n]* -> skip ;
SLC
    : '/*' .*? '*/' -> skip // CHANGES
    ;
//MLC : '/*' .*? '*/' -> skip ;
MLC
    :   '//' ~('\n'|'\r')* -> skip // CHANGES
    ;

program
    : importList? classDeclaration EOF
    ;

importList
    : importDeclaration+
    ;

importDeclaration
    : 'import' packageId ';'
    ;

packageId
    : id=ID ('.' packageId)*
    ;

classDeclaration
    : 'class' name=ID ('extends' superclass=ID)? '{' varList? methodList? '}'
    ;

varList
    : varDeclaration+
    ;

varDeclaration
    : type name=ID ';'
    ;

methodList
    : methodDeclaration+
    ;

methodDeclaration
    : (pub='public')? (stat='static')? type name=ID '(' parameterList? ')' '{' varList? statementList? methodReturn? '}'
    ;

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : type name=ID
    ;

statementList
    : statement+
    ;

methodReturn
    : ('return' expression ';')
    ;

type
    : 'int' '[' ']' #TypeIntegerArray
    | 'boolean' #TypeBoolean
    | 'int' #TypeInteger
    | id = ID '[' ']' #TypeArray
    | id = ID #TypeID
    ;

statement
    : '{' ( statement )* '}' #BracketsStatement
    | 'if' '(' expression ')' statement ('else' statement)? #IfStatement
    | 'while' '(' condition=expression ')' (statements+=statement |'{' statements+=statement* '}') #WhileStatement
    | expression ';' #ExpressionStatement
    | (value='this' . )? name = ID '=' expression ';' #AssignStatement
    | name = ID '[' expression ']' '=' expression ';' #AccessAndAssignStatement
    ;

// CHANGES
expression
    : '(' expression ')' #Parentheses
    | op='!' expression #UnaryOp
    | expression '[' expression ']' #ArrayAccess
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op=('<' | '>' | '<=' | '>=')  expression #ComparisonOp
    | expression op=('&&' | '||') expression #LogicalOp
    | 'new' 'int' '[' expression ']' #ArrayInit
    | 'new' value=ID '(' ')' #Init
    | expression '.' 'length' #Length
    | expression '.' value=ID '(' (expression (',' expression)*)? ')' #Function
    | value=ID '(' (expression (',' expression)*)? ')' #MethodCall
    | value=INTEGER #Integer
    | value='true' #BooleanLiteral
    | value='false' #BooleanLiteral
    | value='this' #This
    | value=ID #Identifier
    ;