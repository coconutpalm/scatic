# Scatic ( Scala Static Site Generator)

_Scatic_ is a minimal static site generator written in [Scala](http://www.scala-lang.org/) using [Ammonite](https://github.com/lihaoyi/Ammonite) as my personal blog/notes site. I know, there is a plenty of static site generators out there so why _Scatic_? Well, it is a pure personal playground! I love the DIY approach sometimes.

It should work well on any operating system with JRE8 installed, even though is has been tested only on OSX and Linux Ubuntu. Ammonite-REPL does not support Windows, even though Ammonite-Ops (used by _Scatic_) does. It means that you should be able to use _Scatic_ on your Windows machine.

It is freely inspired by:

- [Scala Scripting and the 15 Minute Blog Engine](http://www.lihaoyi.com/post/ScalaScriptingandthe15MinuteBlogEngine.html)
- [s2gen - static site generator](http://appliedscala.com/s2gen/)


## Features

- Parsing content from Markdown files using [Atlassian CommonMark](https://github.com/atlassian/commonmark-java)
- YAML front matter for post's metadata (category, tags, date, ...) using [Atlassian CommonMark Extension YAML front matter](https://github.com/atlassian/commonmark-java/tree/master/commonmark-ext-yaml-front-matter)
- Posts by category
- Posts by tag
- Rendering HTML using [Scalatags](http://www.lihaoyi.com/scalatags/)
- Project skeleton generation
- Embedded [Jetty](http://www.eclipse.org/jetty/) server for easy development

## What does it use?

- [Bootstrap](http://getbootstrap.com/) v3.3.7
- Blog template by [@mdo](http://getbootstrap.com/examples/blog/)

## Quickstart

The `Main.sc` script works in three operational modes (generate, serve and clean). Pass the right arg to the script in order to execute it.

1. install [Ammonite](https://github.com/lihaoyi/Ammonite)
2. Open a new terminal windows
3. Clone or download the repos `$ git clone https://github.com/indaco/scatic`
4. Start generating with sample content `$ amm Main.sc -- --mode generate`
5. Serve the result `$ amm Main.sc -- --mode serve`
6. Open a browser windows to `localhost:8080`

## Write your own content

Add your blog posts to `resources/posts` as Markdown files and:

- Generate the site: `$ amm Main.sc -- --mode generate`
- Serve the result: `$ amm Main.sc -- --mode serve`
- Open a browser windows: `localhost:8080`

## Copyright and License

Licensed under the MIT License, see the LICENSE file.
