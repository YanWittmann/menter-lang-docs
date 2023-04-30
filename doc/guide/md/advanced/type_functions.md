# Type Functions

Every value in Menter has functions that can be called on it using the `.` dot syntax. The following will show most of
these functions, most importantly on `object` values.

## General information

### List all functions

All functions on any type can be listed using the `functions` function.

```result=[toLowerCase, isEmpty, replace, replaceFirst, hasText, matches, replaceAll, substring, lastIndexOf, contains, split, size, trim, equals, endsWith, toUpperCase, equalsIgnoreCase, indexOf, charAt, startsWith]
"text".functions()
```

### Bind functions to values

Bound functions can be stored in variables and be called later. This is useful for having quick access to functions that
you use often on a specific value.

```result=<<lambda>>;;;[Hello, World]
splitWords = r/(\s+)/.split;;;splitWords("Hello World")
```

```result=<<lambda>>;;;[Hello];;;[Hello, World];;;World
list = []
pop = list.pop
push = list.push;;;push("Hello");;;push("World");;;pop()
```

### Chaining function calls

Chaining function calls like this is possible. But what about chaining calls that are not calls on functions of the
value? This is what the pipeline operators `|>` and `>|` are for.

```result=(x) -> { x * 2 };;;4
double = (x) -> x * 2;;;"Hello World".toUpperCase().split(" ").size() >| double 
```

## Functions for all types

### `type`

Returns the type of the value as a string.

```result=string
"text".type()
```

### `isNull`

Returns whether the value is `null`.

```result=false;;;true
"text".isNull();;;null.isNull()
```

## Object functions

```result=[foldr, keys, values, distinct, sum, containsValue, frequency, pop, head, avg, min, removeKey, foldl, join, map, reduce, filterKeys, max, containsKey, tail, cross, mapKeys, sort, push, filter, entries, contains, sortKey, size, rename, retainKey]
[].functions()
```

As you can see, there are quite a few functions on objects. Most of them are self-explanatory, but some are not.
The following will explain the most important ones.

### `foldl`, `foldr`, `reduce`

These functions are used to reduce a list of values to a single value. The difference between `foldl` and `foldr` is
that `foldl` starts aggregating values from the left, while `foldr` starts from the right. `reduce` is the same as
`foldl`.

```result=10;;;6
[1, 2, 3, 4].foldl(10, (-));;;[1, 2, 3, 4].foldr(10, (-))
```
