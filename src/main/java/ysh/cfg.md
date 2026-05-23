
# grammar

list ::= newline* conditional ( separator conditional)* separator? newline*

separator ::= ";" | "&" | newline+
newline ::= "\n"

conditional ::= pipeline ( ("&&" | "||" ) pipeline)*

pipeline ::= command ("|" command)*

command ::= simple_command | grouped_command

simple_command ::= word (word | redirection)*

grouped_command  ::= "(" list ")"  redirection*

redirection   ::= (file_redirection_operator filename) | stream_redirection_operator


file_redirection_operator ::= ">" | ">>" | "<" | "2>" | "2>>" | "1>" | "1>>"

stream_redirection_operator ::=  "2>&1" | "1>&2"

word ::= word_part+

word_part ::= double_quoted
| single_quoted
| unquoted
| variable_expansion
| substitution
| tilde_expansion

tilde_expansion ::= "~" | "~" identifier

variable_expansion ::= "\$"identifier | 
("\${" identifier "}" |
"%" identifier "%" |
"\$" special_parameter

double_quoted ::= '"' double_part* '"'

unquoted ::= any char

single_quoted ::= '\'' (any char)* '\''

double_part ::= double_text
| variable_expansion
| substitution

substitution ::= "$\`" list  "`" 

char ::= \[a-zA-Z]
digit ::= \[0-9]
alfa-numeric ::= (char | digit)+

special_parameter ::= "?" | "@" | "*" | "$" | "#"

escape ::= "^"
identifier ::= ( \[a-zA-Z_]\[a-zA-Z0-9_]* ) | \[0-9]+
filename ::= word