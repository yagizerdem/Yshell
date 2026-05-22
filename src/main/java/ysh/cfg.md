
# grammar

list ::= conditional ( (";" | "&" | "\n") conditional)*

conditional ::= pipeline ( ("&&" | "||" ) pipeline)*

pipeline ::= command ("|" command)*

command ::= simple_command | compound_command | function_definition

simple_command ::= word  (word | redirection)* 

redirection   ::= (file_redirection_operator filename) | stream_redirection_operator


file_redirection_operator ::= ">" | ">>" | "<" | "2>" | "2>>" | "1>" | "1>>"

stream_redirection_operator ::=  "2>&1" | "1>&2"

word ::= word_part+

word_part ::= double_quoted
| single_quoted
| unquoted
| expansion
| substitution

expansion ::= ("\$"char(char | digit)*) | 
"\$"digit | 
("\${"char(char | digit)\*)"}" |
"%" alfa-numeric "%" |
"\$" special_parameter

double_quoted ::= '"' double_part* '"'

double_part ::= double_text
| expansion
| command_substitution
| escaped_char

substitution ::= "$\`" list  "`" 

char ::= \[a-zA-Z]
digit ::= \[0-9]
alfa-numeric ::= (char | digit)+

special_parameter ::= "?" | "@" | "*" | "$" | "#"

escape ::= "^"