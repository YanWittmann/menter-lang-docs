# Custom Java Types / Native Functions

> This chapter is about custom types written in Java. See [Menter Types](Advanced_types.html) for more
> information about types natively written in Menter.

## Types of Types

You can extend the functionality of Menter in different ways:

- Native functions
- Custom Java Type

Both of these are registered statically via the `EvaluationContext` class. This means that you can't add new types or
native functions per interpreter instance. This is a limitation of the current implementation, but as I see it, this is
not too big of a problem.

More on validating the arguments passed to native functions and custom types can be found at the end of this chapter.

#### When to use Native Functions

- Native functions are ideal for operations that manipulate primitive values, such as mathematical functions, string
  manipulation, and date/time handling.
- If you need to interact with external systems or APIs, you may want to use a native function to access those resources
  directly.
- If your operation is relatively simple and only needs a few lines of code.

#### When to use Custom Java Types

- If you need to implement complex data structures or algorithms.
- If you want to provide an object-oriented interface for working with data.
- If you want to provide a domain-specific language (DSL) for your application, custom types can be used to define the
  syntax and semantics of your language.

## Native functions

Native functions are functions written in Java that can be called from Menter. Creating one consists of two steps:

1. Register your function in the `EvaluationContext` class. For the function, you can choose between two different
   signatures, where one contains significantly more (powerful) information about the context in which the function is.
2. Create a context with that native function and exports it from the module

### 1. Register your function

The two signatures are:

```static---lang=java
Value execute(List<Value> parameters)
Value execute(GlobalContext context, EvaluationContextLocalInformation localInformation, List<Value> parameters)
```

Both of them receive the parameters the function was called with as a list of `Value` objects. The second one also
receives the `GlobalContext` and `EvaluationContextLocalInformation` objects. More on these in a different chapter.

When registering the function, you have to specify the exact context name the native function definition will be in and
the actual function name.

```static---lang=java
EvaluationContext.registerNativeFunction("contextName", "functionName", parameters -> {
    return parameters.get(0);
});
```

### 2. Create a context with that native function

Now you can create a context with that native function:

```static---lang=java
interpreter.evaluateInContextOf("contextName", "native functionName()");
interpreter.evaluateInContextOf("contextName", "functionName(1)"); // 1 (number)
```

To access them from other contexts, you can obviously export and import them.

### How I usually do it

When you look at the source code of Menter, you will see the following pattern for the core native functions:

- Statically:
    - All native modules are each encapsulated in a separate class
    - Each class contains a static block that registers all native functions
    - Native functions in Java are referenced using the `::` operator and use either signatures
    - The classes are loaded using `Class.forName("...");` inside some main class that is always loaded
- Per interpreter instance:
    - Add the native functions to the interpreter via a file from the resources folder (this, however, you can load from
      anywhere you want) using the `loadContext()` method of the `MenterInterpreter` class
    - Call the `finishLoadingContexts()` method to finish loading the contexts

I will now demonstrate this with some of the core modules:

```static---lang=java
public abstract class CoreModuleMath {
    static {
        EvaluationContext.registerNativeFunction("math.mtr", "sin", CoreModuleMath::sin);
        EvaluationContext.registerNativeFunction("math.mtr", "cos", CoreModuleMath::cos);
        EvaluationContext.registerNativeFunction("math.mtr", "tan", CoreModuleMath::tan);
        ...
    }

    // short signature
    private static Value floor(List<Value> values) {
        return applySingleValueFunction("floor", values, v -> v.setScale(0, RoundingMode.FLOOR));
    }
    
    // example from the reflection module that uses the long signature
    public static Value setVariable(GlobalContext context, EvaluationContextLocalInformation localInformation, List<Value> parameters) {
        return anyAnyAction(parameters, "setVariable", () -> {
            context.addVariable(parameters.get(0).toDisplayString(), parameters.get(1));
            return Value.empty();
        });
    }
}
```

Now, before adding the native functions to my interpreter, I load all the core modules:

```static---lang=java
static {
    try {
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleIo");
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleSystem");
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleDebug");
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleMath");
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleReflection");
        Class.forName("de.yanwittmann.menter.interpreter.core.CoreModuleCmdPlot");
    } catch (ClassNotFoundException e) {
        LOG.error("Failed to load core module", e);
    }
}
```

after which I finally load the files that contain the native functions:

```static---lang=java
private final static String[] MENTER_SOURCE_FILES = {
        "io.mtr",
        "system.mtr",
        "math.mtr",
        "debug.mtr",
        "reflect.mtr",
        "cmdplot.mtr",
};

private void loadMenterCoreFiles() {
    try {
        for (String file : MENTER_SOURCE_FILES) {
            loadContext(readLinesFromResource("/src/" + file), file);
        }
        finishLoadingContexts();
    } catch (Exception e) {
        throw new MenterExecutionException("Failed to load Menter core files", e);
    }
}
```

The contents of such a file can be found in the documentation of each core module,
[example with the `math.mtr` file](Modules_math.html).

## Custom Java Type

Custom Java types are classes that extend the `CustomType` class and have the `@TypeMetaData` annotation. In contrast to
native functions, these actually represent `Value` types in Menter, meaning that you truly create instances of your type
in Menter using the `new` keyword and call Java functions on them.

Internally, this is solved using reflection.

### @TypeMetaData

The `@TypeMetaData` annotation is used to specify the name of the type in Menter and the name of the module it is in.

```static---lang=java
@TypeMetaData(typeName = "TypeName", moduleName = "moduleName")
```

If multiple types are registered in the same module, they will be accessible via that one module, as the module name is
unique and all types are merged into one module.

### @TypeFunction

In contrast to native functions, there is only one signature available:

```static---lang=java
Value functionName(List<Value> parameters)
```

The parameters are the parameters passed to the function call in Menter. Functions, that should be callable inside
Menter, have to be annotated with `@TypeFunction`. If a static function is annotated with `@TypeFunction`, it will be
callable using the class name.

```static---lang=java
@TypeFunction
public Value functionName(List<Value> parameters) {
    return parameters.get(0);
}
```

### CustomType

The `CustomType` class is the base class for all custom types. It has no abstract methods, as all of them have default
implementations. However, you can override them if you want to enable a certain functionality on your type.

```static---lang=java
// custom truthiness
public boolean isTrue() { return false; }

// enables `for` and `forEach` loops
public Value iterator() { return null; }

// used by functions like `sum` and `avg`
public BigDecimal getNumericValue() { return null; }

// self-explanatory
public String toString() { return "instance of " + getClass().getSimpleName(); }

// currently unused
public int size() { return 0; }
```

### Example

In this case, I think an example speaks a thousand words. Here is a custom type that represents a list of users and
another one that represents a user. There are a few things to note, but I will add those at the bottom.

#### UserList

```static---lang=java
@TypeMetaData(typeName = "UserList", moduleName = "users")
public class UserList extends CustomType {

    private final List<Value> users = new ArrayList<>();

    public UserList(List<Value> parameters) {
        super(parameters);
    }

    @TypeFunction
    public Value addUser(List<Value> parameters) {
        final String[][] parameterCombinations = {
                {PrimitiveValueType.STRING.getType(), PrimitiveValueType.NUMBER.getType()},
                {"User"}
        };
        final int parameterCombination = CustomType.checkParameterCombination(parameters, parameterCombinations);

        switch (parameterCombination) {
            case 0:
                final User user = new User(Collections.emptyList());
                user.setName(Collections.singletonList(parameters.get(0)));
                user.setAge(Collections.singletonList(parameters.get(1)));
                users.add(new Value(user));
                break;
            case 1:
                users.add(parameters.get(0));
                break;
            case -1:
                throw invalidParameterCombinationException(getClass().getSimpleName(), "registerUser", parameters, parameterCombinations);
        }

        return Value.empty();
    }

    @TypeFunction
    public Value getUsers(List<Value> parameters) {
        return new Value(users);
    }

    @Override
    public Value iterator() {
        return new Value(users.iterator());
    }
}
```

#### User

```static---lang=java
@TypeMetaData(typeName = "User", moduleName = "users")
public class User extends CustomType {

    private Value name;
    private Value age;

    public User(List<Value> parameters) {
        super(parameters);

        final String[][] parameterCombinations = {
                {},
                {PrimitiveValueType.STRING.getType(), PrimitiveValueType.NUMBER.getType()}
        };
        final int parameterCombination = CustomType.checkParameterCombination(parameters, parameterCombinations);

        switch (parameterCombination) {
            case 0:
                name = Value.empty();
                age = Value.empty();
                break;
            case 1:
                name = parameters.get(0);
                age = parameters.get(1);
                break;
            case -1:
                throw invalidParameterCombinationException(getClass().getSimpleName(), "User", parameters, parameterCombinations);
        }
    }

    @TypeFunction
    public Value getName(List<Value> parameters) {
        return new Value(name);
    }

    @TypeFunction
    public Value setName(List<Value> parameters) {
        final String[][] parameterCombinations = {
                {},
                {PrimitiveValueType.STRING.getType()}
        };
        final int parameterCombination = CustomType.checkParameterCombination(parameters, parameterCombinations);

        switch (parameterCombination) {
            case 0:
                name = Value.empty();
                break;
            case 1:
                name = parameters.get(0);
                break;
            case -1:
                throw invalidParameterCombinationException(getClass().getSimpleName(), "setName", parameters, parameterCombinations);
        }

        return Value.empty();
    }

    @TypeFunction
    public Value getAge(List<Value> parameters) {
        return new Value(age);
    }

    @TypeFunction
    public Value setAge(List<Value> parameters) {
        final String[][] parameterCombinations = {
                {},
                {PrimitiveValueType.NUMBER.getType()}
        };
        final int parameterCombination = CustomType.checkParameterCombination(parameters, parameterCombinations);

        switch (parameterCombination) {
            case 0:
                age = Value.empty();
                break;
            case 1:
                age = parameters.get(0);
                break;
            case -1:
                throw invalidParameterCombinationException(getClass().getSimpleName(), "setAge", parameters, parameterCombinations);
        }

        return Value.empty();
    }

    @Override
    public String toString() {
        return "User{" +
                "name=" + name.toDisplayString() +
                ", age=" + age.toDisplayString() +
                '}';
    }
}
```

And now we can use them in Menter with all their methods:

```static---lang=java
MenterInterpreter interpreter = new MenterInterpreter(new Operators());
interpreter.finishLoadingContexts();

EvaluationContext.registerCustomValueType(UserList.class);
EvaluationContext.registerCustomValueType(User.class);

interpreter.evaluateInContextOf("users-context",
        "import users inline\n" +
        "userList = new UserList()\n" +
        "userList.addUser(\"Yan\", 22)\n" +
        "userList.addUser(new User(\"Thomas\", 36))");

System.out.println(interpreter.evaluateInContextOf("users-context", "userList.getUsers()").toDisplayString());

interpreter.evaluateInContextOf("users-context", "userList.getUsers().get(1).setName(\"Martin\")");
System.out.println(interpreter.evaluateInContextOf("users-context", "userList.getUsers().get(1).getName()").toDisplayString());
```

Where the result is:

```static
[INFO] Registered custom type [users.UserList]
[INFO] Registered custom type [users.User]
[User{name=Yan, age=22}, User{name=Thomas, age=36}]
Martin
```

Now, the things to note:

By importing the `users` module inline, both types were imported into the current context, since both were registered
in this module.

The `UserList.addUser` method allows for passing a `User` instance or a `String` and a `Number` to create a new
`User` instance.

Checking for parameter combinations can be done using the `CustomType.checkParameterCombination` method, which
returns the index of the parameter combination that matches the given parameters. If no combination matches, it
returns `-1`. You can also call the `assertAtLeastOneOfParameterCombinationExists` method, which throws an exception
if no combination or the `invalidParameterCombinationException` method, which creates, but does not throw, an
exception.
