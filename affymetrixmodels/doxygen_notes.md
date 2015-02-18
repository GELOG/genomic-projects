# Doxygen Notes

## Installation

Doxygen depends on GraphViz. GraphViz must be installed and available in the path.

### Ubuntu
```
sudo apt-get install doxygen
sudo apt-get install graphviz
```

### Windows
TODO


## Initial Setup

To create the initial config file, run `doxygen -g`. This will generate a file named `Doxyfile`; edit the file to taste and run `doxygen` again.

To Generate UML diagrams, some properties must be enabled. It seems easier to use the GUI to play around with the settings : `doxywizard`.

See the `Doxyfile` of this project for an exemple. This exemple will generates the documentation into the `doc/` folder.
