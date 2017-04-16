# wordy-date

A clojure/clojurescript library that converts a date described as a
human into an actual date relative to now.

## Usage

In your project.clj; add the following line to the `:dependencies`
section:

    [philjackson/wordy-date "0.1.15"]

In your source:

    (use 'wordy-date.core)

### Parse to datetime

Some examples yanked straight from the tests:

    (parse "1st July 2012")
    (parse "1st July 2012 12pm")
    (parse "December 1st @ 1pm")
    (parse "now")
    (parse "tomorrow")
    (parse "tomorrow at 3pm")
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

### Parse to something more composable

    (raw-parse "12th Jan 2052")

produces:

    [:S
     [:ordinal-day-month-year
      [:ordinal-day-month [:day-nums "12"] [:month-words "jan"]]
      [:year "2052"]]]

You can pass this structure through Instaparse's `transform` function
to create your own... things.

## License

Copyright © 2015 Phil Jackson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
