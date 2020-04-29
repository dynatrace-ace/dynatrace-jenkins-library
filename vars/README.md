# Dynatrace Innovation Lab Jenkins Global Script Library
## vars directory

The vars directory contains global scripts that can be accessed from any other pipeline script
for ease of identification which are written by memebers of the Key Bank devops team, and which
are standard functionality provided the built in pipeline steps, all the vars in here should 
be prefixed with `key` so foo.groovy would become keyFoo.groovy and would be called in a pipline script
as keyFoo.

### Directory structure

The directory structure of the shared library repository is as follows:

    (root)
     +- vars
         +- foo.groovy          # for global 'foo' variable/function
         +- foo.txt             # help for 'foo' variable/function

The `vars` directory hosts scripts that define global variables accessible from
Pipeline scripts.

The basename of each `*.groovy` file should be a Groovy (~ Java) identifier, conventionally `camelCased`.
The matching `*.txt`, if present, can contain documentation, processed through the system’s configured markup formatter
(so may really be HTML, Markdown, etc., though the `txt` extension is required).
