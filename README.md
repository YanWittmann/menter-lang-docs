# Menter Lang - Documentation Pages

[![Build and deploy documentation](https://github.com/YanWittmann/menter-lang-docs/actions/workflows/static.yml/badge.svg)](https://github.com/YanWittmann/menter-lang-docs/actions/workflows/static.yml)

Deployed at https://yanwittmann.github.io/menter-lang-docs

Documentation for https://github.com/YanWittmann/menter-lang

Icons from https://feathericons.com


## Building the documentation

Check out the actions workflow in [.github/workflows/static.yml](.github/workflows/static.yml) for the build steps.

## Adding new pages

Add a new markdown page inside the [doc/guide/md](doc/guide/md) folder. Add an entry in the
[doc/guide/md/structure.txt](doc/guide/md/structure.txt) file. Use the same format as the other entries.

Inside the markdown file, add the following at the top of the file, just under the title:

```markdown
> content.description:
> content.keywords:
```

Fill them with the description and keywords for the page. If you want, you can use this template to ask ChatGPT to
generate them for you:

```markdown
---
ChatCPT, you should: Extract 8 google seo (Search Engine Optimization) keywords from this documentation entry in a comma separated string, and a description in max 120 characters:
<meta name="description" content="...">
<meta name="keywords" content="...">
```
