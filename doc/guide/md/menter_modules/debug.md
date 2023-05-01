# debug.mtr

> content.description: The 'debug' module provides functions for debugging purposes, including the 'breakFlow' function
> to break program execution
> content.keywords: debug module, debugging functions, breakFlow, execution flow, visual debugger, debugger mode,
> debugging, programming

```static
{{ file: {{ property: menter-dir }}/src/main/resources/src/debug.mtr }}
```

The `debug` module provides functions for debugging purposes.

### explain

The `explain` function is useful for finding out why a certain value is what it is. It will print out all steps that
were taken to get to the value. The way this works is by printing a tree-like structure with each step being either the
entrypoint with the input or exit of a node with its result. This also allows you to see how the parser parsed your
input amd the internal representation of your code.

The function with no arguments as first parameter will be called and evaluated. The result of that function will be
returned.

The function can optionally take up to three more boolean arguments, changing what details are printed. All of these
flags are located inside the `MenterDebugger` class.

- The first argument sets the value of `logInterpreterEvaluationStyle` to either `0` or `2`,
- the second one sets `logInterpreterResolveSymbols` to either `true` or `false`
- and the third one sets `logInterpreterAssignments` to either `true` or `false`

```result=null;;;# server required;;;# server required;;;# server required
import debug inline;;;explain(() -> {
  -4 |> abs |> x -> x * 2
}, true, false, false);;;explain(() -> {
  -4 |> abs |> x -> x * 2
}, false, true, false);;;explain(() -> {
  result = 0
  for (i in range(1, 4)) result += i
}, false, false, true)
```

### breakFlow

The `breakFlow` function is used to break the execution flow of the program. Seeing as there is no visual debugger, this
happens on the command line. Calling this function will activate debugger mode.

> Do not call this method in the code boxes on this page, as this will halt the execution of all code boxes without the
> option to reactivate them via the code boxes below.

```static
breakFlow()
```