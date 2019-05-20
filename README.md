[![](https://jitpack.io/v/d-shapiro/wikiClade.svg)](https://jitpack.io/#d-shapiro/wikiClade)

# wikiClade

Pass in a list of species, get out a cladogram containing them.
Taxonomical relationships are scraped from Wikipedia's infoboxes.

Web version hosted here: www.wikiclade.com

## Usage

(from sbt console) `run [-v verbosity] [-f format] [-o outputFile] <input>...`

### Inputs

Inputs should be any number of organisms (or clades of organisms) that can be found on Wikipedia.

### Options

`-v`: How verbose to make the output. One of `complete`, `normal`, `less`, and `minimal`. `complete` means show all clades that any of the input organisms belong to, `minimal` means only show the input organisms and the branching points. Default is `normal`.

`-f`: Format of the output, one of PNG, SVG, and XDOT. Default is XDOT.

`-o`: File to write the output to.

### Examples

`run -v normal -f png -o animals.png Cat Dog Pigeon`

`run -v minimal -f svg -o plants.png "Euphorbia milii" "Nepenthes sanguinea" "Dracaena marginata" "Araucaria heterophylla" "Pachira aquatica" "Kalanchoe blossfeldiana" "Calathea rufibarba" "Zantedeschia aethiopica"`
