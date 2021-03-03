[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/imagej/imagej)

[![Image.sc forum](https://img.shields.io/badge/dynamic/json.svg?label=forum&url=https%3A%2F%2Fforum.image.sc%2Ftags%2Fimagej.json&query=%24.topic_list.tags.0.topic_count&colorB=brightgreen&suffix=%20topics&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAABPklEQVR42m3SyyqFURTA8Y2BER0TDyExZ+aSPIKUlPIITFzKeQWXwhBlQrmFgUzMMFLKZeguBu5y+//17dP3nc5vuPdee6299gohUYYaDGOyyACq4JmQVoFujOMR77hNfOAGM+hBOQqB9TjHD36xhAa04RCuuXeKOvwHVWIKL9jCK2bRiV284QgL8MwEjAneeo9VNOEaBhzALGtoRy02cIcWhE34jj5YxgW+E5Z4iTPkMYpPLCNY3hdOYEfNbKYdmNngZ1jyEzw7h7AIb3fRTQ95OAZ6yQpGYHMMtOTgouktYwxuXsHgWLLl+4x++Kx1FJrjLTagA77bTPvYgw1rRqY56e+w7GNYsqX6JfPwi7aR+Y5SA+BXtKIRfkfJAYgj14tpOF6+I46c4/cAM3UhM3JxyKsxiOIhH0IO6SH/A1Kb1WBeUjbkAAAAAElFTkSuQmCC)](https://forum.image.sc/tag/imagej)
[![](https://travis-ci.org/imagej/imagej.svg?branch=master)](https://travis-ci.org/imagej/imagej)
[![Join the chat at https://gitter.im/imagej/imagej](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/imagej/imagej?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/imagej/imagej)

ImageJ2 is a new version of [ImageJ](https://imagej.net/) seeking to strengthen
both the software and its community. Internally, it is a total redesign of
ImageJ, but it is backwards compatible with ImageJ 1.x via a "legacy layer" and
features a user interface closely modeled after the original.

Under the hood, ImageJ2 completely isolates the image processing logic from the
graphical user interface (UI), allowing ImageJ2 commands to be used in many
contexts, including headless in the cloud or on a server such as
[OMERO](https://openmicroscopy.org/site/support/omero), or from within another
application such as [KNIME](https://www.knime.com/),
[Icy](http://icy.bioimageanalysis.org/) or
[CellProfiler](https://cellprofiler.org/) (a Python application).

ImageJ2 has an N-dimensional data model driven by the powerful
[ImgLib2](http://imglib2.net/) library, which supports image data expressed in
an extensible set of numeric and non-numeric types, and accessed from an
extensible set of data sources. ImageJ2 is driven by a state-of-the-art,
collaborative development process, including version control, unit testing,
automated builds via a continuous integration system, a bug tracker and more.

We are collaborating closely with related projects including
[Fiji](https://fiji.sc/), [SCIFIO](https://scif.io/) and
[OME](https://openmicroscopy.org/), and are striving to deliver a coherent
software stack reusable throughout the life sciences community and beyond. For
more details, see the [SciJava web site](https://scijava.org/).

For more details on the project, see the [ImageJ web site](https://imagej.net/).


# LICENSING

ImageJ2 is distributed under a
[Simplified BSD License](https://en.wikipedia.org/wiki/BSD_licenses);
for the full text of the license, see
[LICENSE.txt](https://github.com/imagej/imagej/blob/master/LICENSE.txt).

For the list of developers and contributors, see
[the parent POM](https://github.com/imagej/pom-imagej/blob/master/pom.xml).


# IMAGEJ AS A LIBRARY

## From Java

This repository is the master ImageJ application, which brings together all of
ImageJ under the artifact
[net.imagej:imagej](https://maven.scijava.org/index.html#nexus-search;gav~net.imagej~imagej~~~~kw,versionexpand).
It is the easiest entry point if you are looking to use ImageJ as a library from
your own software. E.g., in your Maven `pom.xml`:

```
<parent>
  <groupId>org.scijava</groupId>
  <artifactId>pom-scijava</artifactId>
  <version>16.2.0</version>
</parent>
...
<dependency>
  <groupId>net.imagej</groupId>
  <artifactId>imagej</artifactId>
</dependency>
```

We recommend inheriting from the
[pom-scijava](https://github.com/scijava/pom-scijava) parent, although it is not
required. (If you do not, you will need to include the `<version>` of ImageJ in
your `<dependency>` declaration.)

## From other languages

* __JavaScript__: Use the
  [imagej module on npm](https://www.npmjs.com/package/imagej)
  to call ImageJ in-process from node.js code.
* __Python__: Use the
  [imagej module on pypi](https://pypi.org/project/imagej/)
  to call ImageJ in-process or interprocess from Python code.
* __Ruby, R, LLVM and beyond__: Use [GraalVM](https://www.graalvm.org/)
  to combine ImageJ with Truffle-based languages in the same VM,
  with shared objects and memory on a single VM heap.
* __Interprocess__: Use the
  [ImageJ Server](https://github.com/imagej/imagej-server)
  to work with ImageJ via a RESTful web services API, between
  processes on the same machine, or between multiple machines.


# DEPENDENCIES

This component depends on other, lower level components, each of which lives in
its own repository:

* [ImageJ Common](https://github.com/imagej/imagej-common)
* [ImageJ Legacy](https://github.com/imagej/imagej-legacy)
* [ImageJ Ops](https://github.com/imagej/imagej-ops)
* [ImageJ Updater](https://github.com/imagej/imagej-updater)
* [ImgLib2](https://github.com/imglib/imglib)
* [SCIFIO](https://github.com/scifio/scifio)
* [SciJava Common](https://github.com/scijava/scijava-common)

It also includes various "plugin" components at runtime:

* [ImageJ Plugins: Commands](https://github.com/imagej/imagej-plugins-commands)
* [ImageJ Plugins: Tools](https://github.com/imagej/imagej-plugins-tools)
* [ImageJ Plugins: Uploader: SSH](https://github.com/imagej/imagej-plugins-uploader-ssh)
* [ImageJ Plugins: Uploader: WebDAV](https://github.com/imagej/imagej-plugins-uploader-webdav)
* [ImageJ Scripting](https://github.com/imagej/imagej-scripting)
* [SciJava Plugins: Platforms](https://github.com/scijava/scijava-plugins-platforms)
* [SciJava Plugins: Text: Markdown](https://github.com/scijava/scijava-plugins-text-markdown)
* [SciJava Plugins: Text: Plain](https://github.com/scijava/scijava-plugins-text-plain)

See the [pom.xml](pom.xml) for a complete list of dependencies.


# BUGS

For a list of known issues, see the
[GitHub issues](https://github.com/imagej/imagej/issues).

Please report any bugs by following the
[instructions online](https://imagej.net/Bugs).
