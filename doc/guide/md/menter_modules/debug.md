# debug.mtr

> content.description: The 'debug' module provides functions for debugging purposes, including the 'breakFlow' function to break program execution
> content.keywords: debug module, debugging functions, breakFlow, execution flow, visual debugger, debugger mode, debugging, programming

```static
{{ file: {{ property: menter-dir }}/src/main/resources/src/debug.mtr }}
```

The `debug` module provides functions for debugging purposes. It is not meant to be used in production code.

This module is still a work in progress. It is not very useful yet at the moment. More information on debugging will be
added in the future.

### explain

The `explain` function is useful for finding out why a certain value is what it is. It will print out all steps that
were taken to get to the value.

The function with no arguments as first parameter will be called and evaluated. The result of that function will be
returned.

```
import debug inline;;;explain(() -> [1, 2, 3, 4].foldr(10, (-)))
```

### breakFlow

The `breakFlow` function is used to break the execution flow of the program. Seeing as there is no visual debugger, this
happens on the command line. Calling this function will activate debugger mode.

> Do not call this method in the code boxes below, as this will halt the execution of all code boxes without the option
> to reactivate them.

```static
breakFlow()
```