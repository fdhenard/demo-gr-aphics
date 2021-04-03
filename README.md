# demo-gr-aphics

Demographic record processing with [files](#file-processing) and a [web API](#web-api-processing).

## Table of Contents

- [Record Formatting](#record-formatting)
- [File Processing](#file-processing)
    - [Usage](#file-processing-usage)
- [Web API Processing](#web-api-processing)
    - [Usage](#web-api-usage)
- [Assumptions](#assumptions)
- [License](#license)

## Record Formatting

A single record with pipe `' | '` delimited formatting:

`LastName | FirstName | Gender | FavoriteColor | DateOfBirth`

eg.

`Smith | Jim | M | Blue | 1955-11-03`

comma `', '` and space `' '` are the other [delimiter options](#delimiter-options)

### Field formats and options

- Gender options - `m`, `male`, `f`, `female`, `M`, `MALE`, `F`, `FEMALE`, `Male`, `Female`
- FavoriteColor options - `red`, `maroon`, `pink`, `brown`, `orange`, `apricot`, `olive`, `yellow`, `beige`, `lime`, `green`, `mint`, `teal`, `cyan`, `navy`, `blue`, `indigo`, `purple`, `lavender`, `magenta`, `violet`, `black`, `white`, `grey`
    - note: this list was taken from [here](https://sashat.me/2017/01/11/list-of-20-simple-distinct-colors/), and also added violet.  Tried to come up with a list of simple colors from which most people would choose
- DateOfBirth - `YYYY-MM-DD`

[Back to TOC](#table-of-contents)

## File Processing

Process a file of demographic data lines, and output them in various sort orders.

The input file should be a list of the records with the format as indicated in [Record Formatting](#record-formatting).  Each record should be separated with return (newline) characters.  If there are any formatting errors, the record will be ignored, but a description of the error will be shown as part of the output.  After the line errors, the program will display the records in three different sort orders:

1. sorted by gender (females before males) then by last name ascending.
2. sorted by birthdate, ascending
3. sorted by last name, descending


### File Processing Usage

`$ lein run <filepath> <delimiter>`

#### Delimiter options

- pipe - `' | '`
- comma - `', '`
- space - `' '`

[Back to TOC](#table-of-contents)

## Web API Processing

`POST` data lines to the web server will add the records to memory storage.  `GET` request methods to the various endpoints can get the records in their corresponding sorting orders.  More info below.

### Web API Usage

#### Run the web server

Run webserver: `$ lein run webserver`

Webserver Options:

`-p`, `--port PORT`  Port Number (Default is 3000)

#### Web API Endpoints

webserver can be accessed via `http://localhost:<port>`

- `POST` request method to `/records` - Post a single [data line](#record-formatting) in any of the [three delimiter options](#delimiter-options).
    - Add a header named `Delimiter` with the value of either `pipe`, `comma`, or `space` (see [Delimiter Options](#delimiter-options))
- `GET` request method to `/records/gender` to get the records in gender order in JSON formatting
- `GET` request method to `/records/birthdate` - same as above sorted by Birthdate
- `GET` request method to `/records/name` - same as above sorted by LastName, then FirstName

[Back to TOC](#table-of-contents)

## Assumptions

- user has leiningen
- only one input file per run
- all records from the file can fit into available memory
- delimiter types are described in cli run usage, or header on web
- any records with problems will be rejected
- input date must coerce to `java-time/local-date` - recommend `YYYY-MM-DD`
- webserver
    - no security concerns - not SSL, no authentication

[Back to TOC](#table-of-contents)

## License

Copyright © 2021 Frank Henard

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

[Back to TOC](#table-of-contents)
