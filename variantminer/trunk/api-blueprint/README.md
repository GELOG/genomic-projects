= ICMS API Blueprint

API Blueprint is an open source specification that helps describe API.

The official implementation is http://apiary.io/, but ICMS uses Aglio for HTML compilation.


== Dependencies

* Python
* Node.js
* Aglio (Node.js module)

sudo apt-get install nodejs
sudo npm install -g aglio

https://github.com/danielgtaylor/aglio


== Files & Directory Structure

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


== Generating HTML from Markdown files

Steps:

1. Combines all web services into 1 file (Markdown does not support multiple files)
2. Compile the combined blueprint into HTML

For example (the actual contents of the compile.sh.sample file):

```
python ./scripts/apiblueprint-preprocessor.py icms-api.md /tmp/icms.html
./scripts/apiblueprint-generate-doc.sh /tmp/icms.html /var/www/html/icms/backend/david.html
```


== ICMS Blueprint Convention

=== Anatomy of one web service

Each web service is separated in its own .md file in the services/ directory. For each web service, there is usually two json files in the resources/ directory. For example, the Address web service is distributed in 3 files:

  services/address.md
  resources/address.json
  resources/address.new.json

The `address.json` file describes an Address with all its attributes, usually used in the HTTP response payload.
While the `address.new.json` file describes an Address without its read-only attributes, usually used in the HTTP request payload.

Note: most web services follow this convention, but they are allowed to differ if needed.

=== UML Diagrams

Most web services includes a simple UML diagram to describe the conceptual relationships. The tool Yuml.me / Scruffy takes an URL in input and generates a PNG image in output.

For examples, see:
  http://yuml.me/diagram/scruffy/class/samples

To try it out, paste the URL in the browser or go the edit page:
  http://yuml.me/diagram/scruffy/class/draw


== Links

=== API Blueprint

* Official website: 
  http://apiblueprint.org/

* Good walkthrough by examples: 
  https://github.com/apiaryio/api-blueprint/tree/master/examples

* The complete specification:
  https://github.com/apiaryio/api-blueprint/blob/master/API%20Blueprint%20Specification.md


=== API Blueprint & Markdown

The API Blueprint uses an extended version of Markdown (see section 2 of the API Blueprint specification) :

* Official Markdown syntax
  http://daringfireball.net/projects/markdown/syntax

* Metadata and Cross-References of MultiMarkdown syntax
  https://github.com/fletcher/MultiMarkdown/blob/master/Documentation/MultiMarkdown%20User%27s%20Guide.md#multimarkdown-syntax-guide

* GitHub Flavored MarkDown's newlines & fenced code blocks
  https://help.github.com/articles/github-flavored-markdown

