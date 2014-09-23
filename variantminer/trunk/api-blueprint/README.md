= VariantMiner API Blueprint

== Introduction

=== What is an API blueprint ?
An [API blueprint](http://apiblueprint.org/) is an open source specification that helps describe an API. The official implementation, [Apiary.io](http://apiary.io/) is web-based. However, this project uses Aglio, to compile the blueprint from the markdown syntax to HTML.

=== Dependencies

To compile and view the HTML output of this blueprint, you'll require these dependencies:

* [Python](https://www.python.org/)
* [Node.js](http://nodejs.org/)
* [Aglio](https://github.com/danielgtaylor/aglio),  (a Node.js module)


== Installation

=== Ubuntu

```
sudo apt-get install nodejs npm
sudo npm install -g aglio
sudo apt-get install apache2
```

=== Mac OS X
It should work, but untested. If you manage to do it, please document it here.

=== Windows
Even though it should work, I was unable to install Aglio on Windows. If you manage to do it, please document it here.



== Viewing the HTML documentation

1. Follow installation instructions
2. Combines all files into a single markdown file (the preprocessor is provided): 
   `python ./scripts/apiblueprint-preprocessor.py api.md /tmp/api-merged.md
3. Compile the merged markdown file into HTML
   `sudo aglio -t "./layout/default-multi.jade" -i /tmp/api-merged.md -o /var/www/html/api.html`
4. Open http://localhost/api.html in your web browser (*)

(*) You can use your web browser, but the files need to be served by a web server (e.g. the 'file://' protocol does not work). The easiest is drop the file in the apache html directory.



== Modifying the API Blueprint


=== Files & Directory Structure

```
blueprint/
  layout/
    *.jade                Jade templates for generating the HTML output.
  
  resources/
    *.json                JSON resources sent & received in the HTTP payload.
  
  scripts/
    apiblueprint-generate-doc-hiro.sh   Generate html documentation using Hiro (deprecated).
    apiblueprint-generate-doc.sh        Generate html documentation using Aglio.
    apiblueprint-preprocessor.py        Pre-processes the root .md file and include dependencies.

  services/
    *.md                  Contains the web services in API Blueprint format.
  
  compile.sh.sample       Sample shortcut that calls both scripts to produce the HTML format in API blueprint.
  icms-api.md             Root .md document that includes the other markdown documents.
  README                  This file.
```


=== Markdown & API Blueprint Syntax

* [API Blueprint: Official website](http://apiblueprint.org/)
* [API Blueprint: Good walkthrough by examples](https://github.com/apiaryio/api-blueprint/tree/master/examples)
* [API Blueprint: The complete specification](https://github.com/apiaryio/api-blueprint/blob/master/API%20Blueprint%20Specification.md)
* [Markdown: Official syntax](http://daringfireball.net/projects/markdown/syntax)
* [Markdown: Metadata and Cross-References of MultiMarkdown syntax](https://github.com/fletcher/MultiMarkdown/blob/master/Documentation/MultiMarkdown%20User%27s%20Guide.md#multimarkdown-syntax-guide)
* [Markdown: GitHub Flavored MarkDown's newlines & fenced code blocks](https://help.github.com/articles/github-flavored-markdown)
* [Yuml: How to generate UML diagrams from text (examples)](http://yuml.me/diagram/scruffy/class/samples)
* [Yuml: Diagram Editor](http://yuml.me/diagram/scruffy/class/draw)

