# Conditions: if / else if / else

> content.description: Learn about branching in programming using if, else if, and else statements
> content.keywords: if statement, else if statement, elif statement, else statement, branching, conditions, code block, ternary operator

Branching is done using the `if` statement. The `if` statement is followed by a condition and a code block. If the
condition is `true`, the code block is executed, if it is `false`, the code block is skipped.

The `else if`/`elif` and `else` statements are optional. They can be used to add more conditions and a default code
block. Note that you can use either `else if` or `elif`, depending on your preference. I will be using the `elif`
keyword.

Syntax:

```static
if (condition) { ... }
else if (condition) { ... }  # java style
elif (condition) { ... }     # python style
else { ... }
```

As with all expressions in the Menter language, newlines are optional. Example:

```result=2;;;2
if (false)
  1
elif (true)
  2
else
  3;;;if (false) 1 elif (true) 2 else 3
```

This also replaces the ternary operator from other languages:

```result=15
result = if (false) 5 ** 10 else 5 + 10
```
