# human-dates

A library that converts a date described as a human into an actual
date relative to now. Clojurescript or Clojure compatible.

## Usage

In your project.clj; add the following line to the `:dependencies`
section:

    [philjackson/wordy-date "0.1.2"]

In your source:

    (require 'wordy-date.core)

    (parse "22nd")
    (parse "22nd 1pm")
    (parse "22nd 22:45")
    (parse "monday 12:30")
    (parse "three minutes")
    (parse "two weeks, three minutes")
    (parse "two weeks, 3 hours ago")
    (parse "2 days")
    (parse "-2 hours")
    (parse "monday")
    (parse "now")
    (parse "tomorrow")

See the tests for more possibilities.

## License

Copyright © 2015 Phil Jackson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
