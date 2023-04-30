# While loop

> content.description: Learn about while loops and their usage in Menter
> content.keywords: while loop, programming, code block, condition, execution

While loops function just the way they do in other languages. As long as a certain condition is `true`, the code block
is executed.

Syntax:

```static
while (condition) { ... }
```

There isn't much more to say, so here is a small example:

```result=(x) -> { x % 15 == 0 };;;1;;;15
condition = x -> x % 15 == 0;;;i = 1;;;while (!condition(i))
  ++i
```
