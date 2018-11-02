grammar TGS;

script
  : transitionrule+ EOF
  ;

transitionrule
  : lhs '=>' rhs '\n'?
  ;

lhs
  : startNode
  | nonTerminalMarkup
  ;

startNode
  : '#'
  ;

nonTerminalMarkup
  : NONTERMINAL
  ;

rhs
  : root ('[' child+ ']')?
  ;

root
  : nonTerminalMarkup
  | terminalMarkup
  ;

terminalMarkup
  : TERMINAL
  ;

child
  : nonTerminalMarkup
  | terminalMarkup
  | group
  | choice
  | textNode
  ;

group
  : '{' child+ '}'
  ;

choice
  : '(' child ('|' child)+ ')'
  ;

textNode
  : '_'
  ;

NONTERMINAL
  : [A-Z] [A-Za-z0-9_]*
  ;

TERMINAL
  : [a-z] [A-Za-z0-9_]*
  ;

SPACES
  : [ \u000B\t\r\n] -> skip
  ;

UNEXPECTED_CHAR
  : .
  ;
