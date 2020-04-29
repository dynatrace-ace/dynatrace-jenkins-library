# Dynatrace Innovation Lab Jenkins Global Script Library
## src directory

### Directory structure

The directory structure of the shared library repository is as follows:

    (root)
     +- src                     # groovy source files
     |   +- org
     |       +- foo
     |           +- Bar.groovy  # for org.foo.Bar class
     
The `src` directory should look like standard Java source directory structure.
This directory is added to the classpath when executing Pipelines.

At this start, this may not not contain many scripts as scripts written here
have the downside of having to explicitly defined in the script and called as below.  

```groovy
// src/org/foo/Zot.groovy
package org.foo;

def checkOutFrom(repo) {
  git url: "git@github.com:jenkinsci/${repo}"
}
```

You can then call such function from your main Pipeline script like this:

```groovy
def z = new org.foo.Zot()
z.checkOutFrom(repo)
```