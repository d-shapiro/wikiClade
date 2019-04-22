# wikiClade

Work in progress. Pass in a list of species, get out a cladogram containing them.
Taxonomical relationships are scraped from Wikipedia's infoboxes.

## Usage

(from sbt console) `run [-v verbosity] [-f format] [-o outputFile] <input>...`

### Inputs

Inputs should be any number of organisms (or clades of organisms) that can be found on Wikipedia.

### Options

`-v`: An integer from 0 to 100 specifying how verbose to make the output. 100 means show all clades that any of the input organisms belong to, 0 means only show the input organisms, their direct supergroups, and the branching points. Default value is 100.

`-f`: Format of the output, one of PNG, SVG, and XDOT. Default is XDOT.

`-o`: File to write the output to.

### Examples

`run -v 50 -f png -o animals.png Cat Dog Pigeon`

`run -v 0 -f svg -o plants.png "Euphorbia milii" "Nepenthes sanguinea" "Dracaena marginata" "Araucaria heterophylla" "Pachira aquatica" "Kalanchoe blossfeldiana" "Calathea rufibarba" "Zantedeschia aethiopica"`
