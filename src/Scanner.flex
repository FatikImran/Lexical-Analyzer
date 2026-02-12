%%
%class Yylex
%unicode
%line
%column
%type Token
%xstate COMMENT
%xstate STRING
%xstate CHAR
%xstate CHAR_ERROR

%{
    private final StringBuilder commentBuffer = new StringBuilder();
    private final StringBuilder stringBuffer = new StringBuilder();
    private final StringBuilder charBuffer = new StringBuilder();
    private int commentStartLine = 1;
    private int commentStartColumn = 1;
    private int stringStartLine = 1;
    private int stringStartColumn = 1;
    private int charStartLine = 1;
    private int charStartColumn = 1;
    private boolean stringInvalidEscape = false;
    private boolean charInvalidEscape = false;
    private boolean charHasValue = false;

    private Token token(TokenType type, String lexeme) {
        return new Token(type, lexeme, yyline + 1, yycolumn + 1);
    }

    private Token tokenAt(TokenType type, String lexeme, int line, int column) {
        return new Token(type, lexeme, line, column);
    }
%}

SIGN = [+-]
DIGIT = [0-9]
UPPER = [A-Z]
LOWER = [a-z]
IDENT_BODY = ({LOWER}|{DIGIT}|_)
IDENT = {UPPER}{IDENT_BODY}{0,30}
IDENT_TOO_LONG = {UPPER}{IDENT_BODY}{31,1000}
IDENT_UPPER_INVALID = {UPPER}{IDENT_BODY}*{UPPER}({LOWER}|{UPPER}|{DIGIT}|_)*
INVALID_IDENT = {LOWER}([A-Za-z0-9_])*
INT = {SIGN}?{DIGIT}+
FLOAT = {SIGN}?{DIGIT}+\.{DIGIT}{1,6}([eE]{SIGN}?{DIGIT}+)?
FLOAT_TOO_LONG = {SIGN}?{DIGIT}+\.{DIGIT}{7,1000}([eE]{SIGN}?{DIGIT}+)?
FLOAT_NO_FRACTION = {SIGN}?{DIGIT}+\.
FLOAT_BAD_EXP = {SIGN}?{DIGIT}+\.{DIGIT}+[eE]{SIGN}?
BACKSLASH = \\
SL_COMMENT = "##"[^\n]*
ML_COMMENT = "#*"([^*]|\*+[^*#])*\*+#
WS = [ \t\r\n]+

%%

{ML_COMMENT}    { /* skip */ }
"#*"           { commentBuffer.setLength(0); commentStartLine = yyline + 1; commentStartColumn = yycolumn + 1; yybegin(COMMENT); }
{SL_COMMENT}    { /* skip */ }

"**"           { return token(TokenType.OP_ARITHMETIC, yytext()); }
"=="           { return token(TokenType.OP_RELATIONAL, yytext()); }
"!="           { return token(TokenType.OP_RELATIONAL, yytext()); }
"<="           { return token(TokenType.OP_RELATIONAL, yytext()); }
">="           { return token(TokenType.OP_RELATIONAL, yytext()); }
"&&"           { return token(TokenType.OP_LOGICAL, yytext()); }
"||"           { return token(TokenType.OP_LOGICAL, yytext()); }
"++"           { return token(TokenType.OP_INC_DEC, yytext()); }
"--"           { return token(TokenType.OP_INC_DEC, yytext()); }
"+="           { return token(TokenType.OP_ASSIGNMENT, yytext()); }
"-="           { return token(TokenType.OP_ASSIGNMENT, yytext()); }
"*="           { return token(TokenType.OP_ASSIGNMENT, yytext()); }
"/="           { return token(TokenType.OP_ASSIGNMENT, yytext()); }

"start"|"finish"|"loop"|"condition"|"declare"|"output"|"input"|"function"|"return"|"break"|"continue"|"else" {
                return token(TokenType.KEYWORD, yytext());
            }
"true"|"false" { return token(TokenType.BOOLEAN_LITERAL, yytext()); }

{IDENT_TOO_LONG} { return token(TokenType.ERROR, yytext()); }
{IDENT_UPPER_INVALID} { return token(TokenType.ERROR, yytext()); }
{IDENT}          { return token(TokenType.IDENTIFIER, yytext()); }
{INVALID_IDENT}  { return token(TokenType.ERROR, yytext()); }

{FLOAT_TOO_LONG} { return token(TokenType.ERROR, yytext()); }
{FLOAT_BAD_EXP} { return token(TokenType.ERROR, yytext()); }
{FLOAT}          { return token(TokenType.FLOAT_LITERAL, yytext()); }
{FLOAT_NO_FRACTION} { return token(TokenType.ERROR, yytext()); }
{INT}            { return token(TokenType.INT_LITERAL, yytext()); }

"\""           { stringBuffer.setLength(0); stringBuffer.append(yytext()); stringStartLine = yyline + 1; stringStartColumn = yycolumn + 1; stringInvalidEscape = false; yybegin(STRING); }
"\'"           { charBuffer.setLength(0); charBuffer.append(yytext()); charStartLine = yyline + 1; charStartColumn = yycolumn + 1; charInvalidEscape = false; charHasValue = false; yybegin(CHAR); }

"+"|"-"|"*"|"/"|"%" { return token(TokenType.OP_ARITHMETIC, yytext()); }
"<"|">" { return token(TokenType.OP_RELATIONAL, yytext()); }
"!" { return token(TokenType.OP_LOGICAL, yytext()); }
"=" { return token(TokenType.OP_ASSIGNMENT, yytext()); }

[(){}\[\],;:] { return token(TokenType.PUNCTUATOR, yytext()); }

{WS}            { /* skip */ }

.               { return token(TokenType.ERROR, yytext()); }

<<EOF>>         { return token(TokenType.EOF, ""); }

<STRING>"\""    { stringBuffer.append(yytext()); yybegin(YYINITIAL); return stringInvalidEscape ? tokenAt(TokenType.ERROR, stringBuffer.toString(), stringStartLine, stringStartColumn) : tokenAt(TokenType.STRING_LITERAL, stringBuffer.toString(), stringStartLine, stringStartColumn); }
<STRING>{BACKSLASH}[\"\\ntr] { stringBuffer.append(yytext()); }
<STRING>{BACKSLASH}[^\"\\ntr] { stringBuffer.append(yytext()); stringInvalidEscape = true; }
<STRING>[^\n\r\"\\]+ { stringBuffer.append(yytext()); }
<STRING>[\n\r]  { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, stringBuffer.toString(), stringStartLine, stringStartColumn); }
<STRING><<EOF>>  { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, stringBuffer.toString(), stringStartLine, stringStartColumn); }

<CHAR>"\'"      { charBuffer.append(yytext()); yybegin(YYINITIAL); return !charHasValue || charInvalidEscape ? tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn) : tokenAt(TokenType.CHAR_LITERAL, charBuffer.toString(), charStartLine, charStartColumn); }
<CHAR>{BACKSLASH}['\\ntr] { if (charHasValue) { charBuffer.append(yytext()); yybegin(CHAR_ERROR); } else { charBuffer.append(yytext()); charHasValue = true; } }
<CHAR>{BACKSLASH}[^'\\ntr] { if (charHasValue) { charBuffer.append(yytext()); yybegin(CHAR_ERROR); } else { charBuffer.append(yytext()); charInvalidEscape = true; charHasValue = true; } }
<CHAR>[^'\n\r\\] { if (charHasValue) { charBuffer.append(yytext()); yybegin(CHAR_ERROR); } else { charBuffer.append(yytext()); charHasValue = true; } }
<CHAR>[\n\r]    { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn); }
<CHAR>.          { charBuffer.append(yytext()); yybegin(CHAR_ERROR); }
<CHAR><<EOF>>    { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn); }

<CHAR_ERROR>"\'" { charBuffer.append(yytext()); yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn); }
<CHAR_ERROR>[\n\r] { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn); }
<CHAR_ERROR>.    { charBuffer.append(yytext()); }
<CHAR_ERROR><<EOF>> { yybegin(YYINITIAL); return tokenAt(TokenType.ERROR, charBuffer.toString(), charStartLine, charStartColumn); }

<COMMENT>"*#"   { yybegin(YYINITIAL); }
<COMMENT>[\n\r] { commentBuffer.append(yytext()); }
<COMMENT>.      { commentBuffer.append(yytext()); }
<COMMENT><<EOF>> { return tokenAt(TokenType.ERROR, "#*" + commentBuffer, commentStartLine, commentStartColumn); }
