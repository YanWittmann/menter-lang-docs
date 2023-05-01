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
splitWords = r/\s+/.split;;;splitWords("Hello World")
```

```result=<<lambda>>;;;[Hello];;;[Hello, World];;;World
list = []
pop = list.pop
push = list.push;;;push("Hello");;;push("World");;;pop()
```

### Chaining function calls

For chaining function calls, depending on the situation, you can use the:

- `.` operator for chaining calls on the value
- `|>` and `>|` operators for piping the value into the next function call

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

## Object functions (`object` type)

```result=[foldr, keys, values, distinct, sum, containsValue, frequency, pop, head, avg, min, removeKey, get, foldl, join, map, reduce, filterKeys, max, containsKey, tail, cross, mapKeys, sort, push, filter, entries, contains, sortKey, size, rename, retainKey]
[].functions()
```

As you can see, there are quite a few functions on objects. Most of them are self-explanatory, but some are not.
The following will explain the most important ones.

### `foldl`, `foldr`, `reduce`

These functions are used to reduce a list of values to a single value. The difference between `foldl` and `foldr` is
that `foldl` starts aggregating values from the left, while `foldr` starts from the right. `reduce` is the same as
`foldl`.

```result=10;;;6
[1, 2, 3, 4].reduce(10, (-));;;[1, 2, 3, 4].foldl([], (acc, val) -> { acc ::= val });;;[1, 2, 3, 4].foldr([], (acc, val) -> { acc ::= val })
```

### `map`, `mapKeys`

The `map` function is used to transform a list of values or object into another list or object of values. The function
passed to `map`will be called for each value in the list and the result of that function will be added to the new list.
The type of the result does not have to be the same as the type of the original list.

Just as always when looping over a list/object, the function can take one or two arguments: Only the value or the key
and value.

```result=[1, 4, 9, 16]
[1, 2, 3, 4].map((x) -> x * x);;;[1, 2, 3, 4].map((k, v) -> [k, v])
```

### `filter`

The `filter` function is used to filter a list of values or object into another list or object of values. The function
passed to `filter` will be called for each value in the list and the value will be added to the new list if the function
returns `true`.

```result=[1, 3]
[1, 2, 3, 4].filter((x) -> x % 2 == 1)
```

### `sort`, `sortKey`

The `sort` function is used to sort the items in a list or object. If no function is passed to `sort`, the items will be
sorted using their identity. If a function is passed, the items will be sorted using the result of that function.

```result=[1, 2, 3, 4];;;[4, 3, 2, 1]
[4, 2, 1, 3].sort();;;[4, 2, 1, 3].sort((a, b) -> b - a)
```

The same goes for `sortKey`, but it will sort the items using the key.

```result=[1, 2, 3, 4];;;[4, 3, 2, 1]
{ 4: 1, 2: 2, 1: 3, 3: 4 }.sortKey();;;{ 4: 1, 2: 2, 1: 3, 3: 4 }.sortKey((a, b) -> b - a)
```

### `join`

The `join` function is used to join a list of values into a string. The function takes a separator string which will be
used to separate the values in the string.

```result=1, 2, 3, 4
[1, 2, 3, 4].join(", ")
```

### `contains`, `containsKey`, `containsValue`

The `contains` and `containsValue` functions are used to check whether a list or object contains a value. The function
takes a value and returns whether the list or object contains that value.

```result=true;;;false
[1, 2, 3, 4].contains(2);;;[1, 2, 3, 4].contains(5)
```

Similar to that, the `containsKey` function is used to check whether an object contains a key.

```result=true;;;false
{ 1: 2, 3: 4 }.containsKey(1);;;{ 1: 2, 3: 4 }.containsKey(2)
```

### `sum`, `avg`, `min`, `max`

These functions are used to calculate the sum, average, minimum and maximum of a list of values. The `sum` function
takes no arguments, while the `avg`, `min` and `max` functions optionally take a function that serves as the comparator
for the values.

```result=10;;;2.5;;;1;;;Hello
[1, 2, 3, 4].sum();;;[1, 2, 3, 4].avg();;;[1, 2, 3, 4].max();;;["Hello", "you"].max((a, b) -> a.size() - b.size())
```

### `distinct`

The `distinct` function is used to remove duplicate values from a list. The function takes no arguments.

```result=[1, 2, 3, 4]
[1, 2, 3, 4, 1, 2, 3, 4].distinct()
```

### `frequency`

The `frequency` function is used to count the number of occurrences of each value in a list. The function takes no
arguments and returns an object with the values as keys and the number of occurrences as values, ordered by the number
of occurrences.

```result={4: 2, 2: 2, 1: 1, 3: 1}
[2, 3, 4, 1, 2, 4].frequency()
```

Here's a small example:

```result=# requires server
pick(x) = x.size() |> random(0) |> floor |> x.get
names = ["Yan", "Nils", "Holger", "Ute", "Thomas", "Jonas", "Eren"]
createPerson(min, max) = {name: pick(names), age: round(random(min, max))}
db ::= range(1, 100).map(x -> createPerson(12, 26))
db ::= range(1, 100).map(x -> createPerson(27, 32))

print("age frequencies:", db.map(x -> x.age).frequency().sort())
print("people above 20:", db.filter(x -> x.age > 20).size())
print("average age of people above 20:", db.filter(x -> x.age > 20).map(x -> x.age).avg())
```

### `cross`

The `cross` function is used to create a list of all possible combinations of two lists. The function takes a list of
values and returns a list of lists of values.

```result=[[1, 2], [1, 3], [2, 2], [2, 3]]
[1, 2].cross([2, 3])
```

It optionally takes a filter function that will be called for each combination and only the combinations for which the
function returns `true` will be added to the result. The result can also be chained into another call. In this case, the
previous result will be stored in an array that has to be accessed first.

```result=[[1, 4], [2, 3], [3, 2], [4, 1]];;;[[1, 4, 5], [1, 4, 6], [2, 3, 5], [2, 3, 6], [3, 2, 5], [3, 2, 6], [4, 1, 5], [4, 1, 6]]
range(1, 6).cross(range(1, 6), (a, b) -> a + b == 5);;;range(1, 6)
  .cross(range(1, 6), (a, b) -> a + b == 5)
  .cross(range(1, 6), (a, b) -> a[0] + a[1] + b >= 10)
```

A cool application for this can be found in the [examples section](Advanced_examples.html).

### `head`, `tail`

The `head` function is used to get the first element of a list. The `tail` function is used to get all elements of a
list
except the first one. Both functions take no arguments.

```result=1;;;[2, 3, 4]
[1, 2, 3, 4].head();;;[1, 2, 3, 4].tail()
```

### `rename`

The `rename` function is used to rename the keys of an object. The function takes an old key and a new key. Note that
the object is directly modified and not copied.

```result={new: 1, key: 2};;;[{key: 2, new: 1}, {key: 4, new: 3}]
{ "old": 1, "key": 2 }.rename("old", "new");;;lstOfObj = [{ "old": 1, "key": 2 }, { "old": 3, "key": 4 }]
lstOfObj.forEach(x -> x.rename("old", "new"))
lstOfObj
```

### `keys`, `values`

These functions are used to get the keys or values of an object.

```result=[0, 1, 2, 3];;;[10, 20, 30, 40]
[10, 20, 30, 40].keys();;;[10, 20, 30, 40].values()
```
