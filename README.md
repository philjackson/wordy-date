# human-dates

A library that converts a date described as a human into an actual
date relative to now. Clojurescript or Clojure compatible.

## Usage

In your project.clj; add the following line to the `:dependencies`
section:

    [philjackson/wordy-date "0.1.4"]

In your source:

Making the assumption today's time is `2016-10-11T12:13:14.000Z`:

    (use 'wordy-date.core)

Some examples yanked straight from the tests:

    (parse "December 1st @ 1pm")
    (parse "now")
    (parse "tomorrow")
    (parse "10 seconds")
    (parse "ten seconds")
    (parse "10 mins")
    (parse "20 mins")
    (parse "twenty mins")
    (parse "20 hours")
    (parse "28 hours")
    (parse "-28 hours")
    (parse "20 mins and -10 mins")
    (parse "12:30")
    (parse "12am")
    (parse "10 minutes ago")
    (parse "wednesday")
    (parse "wed")
    (parse "sunday")
    (parse "sun")
    (parse "wed 12:30")
    (parse "sun 12am")
    (parse "monday")
    (parse "tuesday")
    (parse "mon 11pm")
    (parse "tuesday 1")
    (parse "tuesday 1pm")
    (parse "1st")
    (parse "22nd")
    (parse "1pm 22nd")
    (parse "22nd 1am")
    (parse "22nd @ 1am")
    (parse "22nd at 1am")

I'd suggest having a look at the tests for all examples.

## TODO

* Allow user to specify date relative to expressions.
* Account for local time(?)
* Instaparse string a bit crufty?

## License

Copyright © 2015 Phil Jackson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
