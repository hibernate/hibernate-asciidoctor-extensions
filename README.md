# Hibernate Asciidoctor extensions

This repository contains assorted Asciidoctor extensions developed for the Hibernate projects.

## Extensions

### Save preprocessed output

The point of this extension is to save the merged Asciidoc document after the includes have been processed.

It generates a single document with all the includes resolved, allowing to easily make a diff.

### Custom role block

This extension is quite specific: it's only usable with the DocBook output.

The goal is to preserve the custom roles used in the Asciidoc document in the DocBook output. Typically, we use it to keep the `#tck-testable#` information present in the Bean Validation specification.

## License

The source code is licensed under the Apache License version 2.0.

## Links

 * Issue tracker: https://github.com/hibernate/hibernate-asciidoctor-extensions/issues
 * Continuous integration: http://ci.hibernate.org/job/hibernate-asciidoctor-extensions-master/
