# Include Menter in your own Application

After having built and installed (run `mvn install` in the root directory) the Java library yourself using maven, you
can add this dependency to your project:

```static---lang=xml
<dependency>
    <groupId>de.yanwittmann</groupId>
    <artifactId>menter-lang</artifactId>
    <version>{{ property: menter-version }}</version>
</dependency>
```

With gradle:

```static
implementation 'de.yanwittmann:menter-lang:{{ property: menter-version }}'
```

If you prefer, you can also download the latest release from the
[GitHub releases page](https://github.com/YanWittmann/menter-lang/releases) and add it to your project manually.

## List of important classes

- `Lexer`
- `Parser`
- `MenterInterpreter`
- `Operators`, `Operator`
- `MenterExecutionException`, `ParsingException`, `LexerException`
- `Value`
- `MenterDebugger`

## Getting started

### MenterInterpreter

The main interface for interacting with Menter is the `MenterInterpreter` class. It provides methods to execute Menter
code and to get the result of the execution.  
In order to create the parser and lexer, you need to provide an instance of the `Operators` class. This class contains
all the default operators that Menter provides and any additional operators that you might have added. You can
optionally also define your own `Lexer` and `Parser` instances and pass them to the `MenterInterpreter` constructor
instead.

In order to allow your code to add modules before the execution starts, you need to call `finishLoadingContexts()` on
the interpreter manually after optionally adding your modules. This method will from now on be called automatically,
whenever a new context is added.

```static---lang=java
MenterInterpreter interpreter = new MenterInterpreter(new Operators());
interpreter.finishLoadingContexts();
```

Now you can execute Menter code using the `evaluate()` method. This method will return a `Value` object that contains
the result of the execution. The context in which this code is executed is called `eval`.  
If you need multiple contexts, you can use the `evaluateInContextOf()` method instead, which will create a named context
that can be re-accessed later.

```static---lang=java
Value result = interpreter.evaluate(expression);
Value result = interpreter.evaluateInContextOf(contextName, expression);
```

If you want to execute a file, you can use the `loadFile()` method. If the file is a directory, it will be searched for
files with the `*.mtr` extension and all of them will be executed. The context that is used for the execution is named
after the file name.

```static---lang=java
interpreter.loadFile(file);
```

### Value

Instances of the `Value` class represent all values in Menter. They have a value and a dynamically inferred type. It has
several methods to get the value in different formats:

```static---lang=java
Value value = new Value(5);
value.getValue(); // 5 (BigDecimal)
value.toDisplayString(); // "5"
value.getType(); // "number"
value.toString(); // "5 (number)"
```

Some more facts:

- A value can also be tagged with several key-value pairs. This can be used to add meta-information to a value. For more
  information on this, see the [Value markers](Java_value_markers.html) section
- When constructing a value, all number types are converted to `BigDecimal` and all `List` types are converted to
  `LinkedHashMap` with the index (BigDecimal) as key, which matches the way Menter handles these types internally

See the next chapters for more information about the other topics.
