# Developers Guide

This is a short developers guide to ensure you can get up and running quickly to
develop for the ModelChecker tool.

## Development Environment

This project requires the Java Development Kit v. 8+, gradle (note: a wrapper is
provided)

### Recommended tools

We recommend developing this using IntelliJ IDEA.

Note: This will require the installation of the Lombok plugin and enabling
annotation processing in order to satisfy IntelliJs compilation and
autoprediction engine.

In order to make sure this is imported correctly, open the `build.gradle` file
located in the `modelchecker` directory, open this (not import or download from VCS)
as a project (if asked), and this should generate the correct project structure.

### Style Guide

We adhere to the 
["Google Checks"](http://checkstyle.sourceforge.net/google_style.html) style
guidelines provided in `checkstyle` for Java code.

## Project Structure Overview

This project is seperated into several modules.

These are:

- `ast`, this is the abstract syntax tree for the code to develop the models.
- `clientapplication`, this is the UserInterface for the application.
- `compiler`, this is a compilation pipeline which manages the several steps to
  compile a project.
- `core`, this is the spine of the application, and runs the various setup tasks
  required to execute the program, including Java path injection, and plugin
  setup.
- `evaluator`, this performs the boolean operations on generated process models,
  including bisimulation and trace equivalence in order to verify the model.
- `interpreter`, this handles the expansion of indexes and sets, the
  replacement of references within the abstract syntax tree, and the creation of
  the process model itself.
- `operations`, this manages the plugin system for functions, infix functions
  and operations for use within the interpreter.
- `parsing`, this lexes the code into tokens, and parses these tokens into an
  abstract syntax tree.
- `processmodels`, this contains the datastructure and related methods for the
  process models themselves.
- `util`, this contains generic utility classes that may be required at various
  points within the codebase.

## Testing

Testing is currently run through `gradle`, full model generation tests are
stored in `modelchecker/resources/tests/` as a text file.

The naming convention for these tests are:
- the tests MUST end in `.txt`
- tests that should fail to compile entirely must end in `_fail.txt`
- tests that should fail to assert the operations must end in
  `_operationfail.txt`

Other Build related tests are stored within `modelchecker/compiler/src/test/`

##How to report a bug

To report a bug, we recommend leaving an issue on the `github` repository.

Please consider using the following bug report template

```
Operating System:

Description of error:

Steps to reproduce this error:

Expected/Received output:

Any photographs of this issue:

Severity: (Critical,Major,Minor,Trivial,Enhancement)
```

## Suggesting Improvements

Please submit these as an issue on `github`. 

Please consider the following when filing an improvement request:

- A clear descriptive title
- A step-by-step description of the improvement
- Provide specific examples of why this improvement would be good
- Describe the current behaviour, and compare this
- Include screenshots/mockups if possible
