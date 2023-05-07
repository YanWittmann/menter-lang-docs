# Examples

This page contains some cool code snippets that I wrote whilst developing Menter. They are meant to show off some of the
features of Menter and how they can be used.

Most of them are useless without a proper Guide Server, so make sure to [set one up](execute_code.html)!

## Derivative

This code snippet defines a function called `derivative` which takes two parameters: `p` (precision) and `f` (the
function to differentiate). This function calculates the approximate derivative of the given function using the
symmetric difference quotient formula.

When plotting the `sin` function, the function itself is passed and then called by the `plot` method. Even more
interesting is the second part with `derivative(0.1, sin)`, which works, since the `derivative` function returns a
function that takes one parameter.

```result=# requires server
import cmdplot inline
derivative = (p, f) -> (x -> (f(x + p) - f(x - p)) / (2 * p))
plot(80, 15, space(1, 3 * PI), sin, derivative(0.1, sin))
```

## Table-like structures (Database)

This code snippet generates synthetic data related to card shops, products, and offers, and performs some data filtering
and visualization.

It initializes generator functions for creating random data, such as the names of legendary duelists, crystal beasts,
and attack styles. It also sets up the producers and shops associated with the card products.

Using these generators, the code creates tables for card shops, products, and offers by generating a specified number of
entries for each. The tables are populated with randomly generated data, including unique product identifiers (GTIN),
names, producers, prices, and shop names.

After generating the data, the code applies a filter to the offer table, selecting only the offers for products with the
name "Pokemon Ultra" and a price less than 5. This filtered data is then processed to retain only specific keys (name,
shop, price) and rename them for clarity.

Finally, the filtered data is visualized using the `cmdplot` module. The resulting table displays the name, shop, and
price of the filtered offers. This allows users to easily analyze and explore the generated data in a structured format.

```
import cmdplot inline

print("Setting up generator functions")

pick(x) = x[x.size() |> random(0) |> floor]
data.names = ["Legendary Duelists", "Legend of the Crystal Beasts", "Flowing Attack Wulaosu", "Rapid Strike Urshifu", "Pokemon Ultra", "Premium Glurak", "Kamigawa Neon", "Strixhaven"]
data.producer = ["Konami Digital", "Pokemon Company", "Wizards of the Coast"]
data.shops = ["Card Stop", "Shop It!", "Card Gate"]

generator.product = () -> {
	GTIN: floor(random(1, 1000000)),
    name: pick(data.names),
    producer: pick(data.producer)
}
generator.offer = (products) -> {
	GTIN: pick(products).GTIN,
    price: round(random(1, 10), 2),
    shop: pick(data.shops)
}

print("Generating data")

tables.shops = []
for ((k, v) in data.shops) tables.shops[k] = {id: k, name: v}

tables.products = range(1, 300).map(x -> generator.product())
tables.offers = range(1, 300).map(x -> generator.offer(tables.products))

print("Applying data filter")

filtered = tables.offers
    .cross(tables.products, (l, r) -> l.GTIN == r.GTIN)
    .filter(x -> x.name == "Pokemon Ultra" && x.price < 5)

filtered.forEach(x -> x.retainKey(x -> x == "name" || x == "shop" || x == "price"))
filtered.forEach(x -> x.rename("name", "Name").rename("shop", "Shop").rename("price", "Price"))

print("Plotting data");;;table(filtered);;;# table(tables.products);;;# table(tables.offers)
```

Some special things to note about this code:

This line defines a function that picks a random element from an array. It uses the `|>` operator to pipe the array size
into the `random` function, which is then piped into the `floor` function.

```static
pick(x) = x[x.size() |> random(0) |> floor]
```

This line creates 300 products by mapping the `generator.product` function over a range of 300 numbers. This is a
convenient way to do something a specific number of times.

```static
tables.products = range(1, 300).map(x -> generator.product())
```

This line uses the `retainKey` function to keep only the keys "name", "shop", and "price" in each offer. It uses a
lambda function to specify which keys to keep.

```static
filtered.forEach(x -> x.retainKey(x -> x == "name" || x == "shop" || x == "price"))
```

This snippet and the one below are equivalent, but differ in the way they are written.

- The first one separates the merging of the tables on their "primary key" from the filtering of the data, making it
  easier to read.
- The second one merges the tables and filters the data in a single step, making it more concise.

```static
tables.offers
    .cross(tables.products, (l, r) -> l.GTIN == r.GTIN)
    .filter(x -> x.name == "Pokemon Ultra" && x.price < 5)
```

```static
tables.offers
    .cross(tables.products, (l, r) -> l.GTIN == r.GTIN && r.name == "Pokemon Ultra" && l.price < 5)
```

## Normal distribution

This code snippet defines a function `normalDistribution` that calculates the probability density function of a normal
distribution for a given value `x`, mean `mu`, and standard deviation `sigma`. It is exported as a module under the name
`normal`. This is so that the other code boxes below can access it.

```id=examples-normal-module
normalDistribution = (x, mu, sigma) ->
  (1 / (sigma * sqrt(2 * PI))) * E ^^ (-0.5 * ((x - mu) / sigma) ^^ 2);;;export [normalDistribution] as normal
```

The next box uses this `normalDistribution` function to plot a normal distribution with mean 0 and standard deviation 1.

```after=examples-normal-module
import cmdplot inline
import normal inline

plot(80, 20, space(-3, 3), x -> normalDistribution(x, 0, 1))
```

And another big example. This one generates a large amount of ages from 0 to 30 using the normal distribution function
defined above. It then plots the frequency of each age. More details about specific parts can be found below.

```after=examples-normal-module
import cmdplot inline
import normal inline;;;numberComp = (a, b) -> { if (a > b) 1 elif (a < b) -1 else 0 };;;age = (min, max, attempts) ->
    values = range(1, attempts)
        .map(x -> random(min, max))
        .map(x -> [x, normalDistribution(x, 0, max / 3)])
        .max((a, b) -> numberComp(a[1], b[1]));;;maxAge = 30
values = range(1, 1000)
    .map(x -> age(-maxAge / 2, maxAge / 2, 3))
    .map(x -> (x[0] |> floor))
    .map(x -> x + maxAge / 2);;;freq = values.frequency();;;plot(false, 90, 24, freq.keys(), freq.values())
```

A function is defined that generates a random age between `min` and `max` using the normal distribution function. It
has a parameter `attempts` that specifies how many times the function should be called to generate a single age, where
the one with the highest probability is chosen.

```static
age = (min, max, attempts) ->
    values = range(1, attempts)
        .map(x -> random(min, max))
        .map(x -> [x, normalDistribution(x, 0, max / 3)])
        .max((a, b) -> numberComp(a[1], b[1]))
```

The most interesting part of this code is the `frequency` function. It takes an array of values and returns a map that
maps each value to its frequency in the array. This is used to plot the frequency of each age.

```static
freq = values.frequency()
```
