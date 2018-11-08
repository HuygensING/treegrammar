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
  | group
  | choice
  | terminalMarkup
  | textNode
  | zeroOrOne
  | zeroOrMore
  | oneOrMore
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

repeatableChild
  : nonTerminalMarkup
  | group
  | choice
  ;

zeroOrOne
  : repeatableChild '?'
  ;

zeroOrMore
  : repeatableChild '*'
  ;

oneOrMore
  : repeatableChild '+'
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
