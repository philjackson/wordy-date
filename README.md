# wordy-date

A clojure/clojurescript library that converts a date described as a
human into an actual date relative to now. Just supports English at
the moment.

## Usage

In your project.clj; add the following line to the `:dependencies`
section:

    [philjackson/wordy-date "0.1.18"]

In your source:

    (use 'wordy-date.core)

### Parse to datetime

Some examples yanked straight from the tests:

Assuming this was run on 2016-10-11 12:13:14:

    (parse "tomorrow")                 => 2016-10-12 12:13:14
    (parse "this time tomorrow")       => 2016-10-12 12:13:14
    (parse "midnight")                 => 2016-10-11 00:00:00
    (parse "now")                      => 2016-10-11 12:13:14
    (parse "tomorrow @ 1pm")           => 2016-10-12 13:00:00
    (parse "easter sunday")            => 2016-03-27 00-00-00
    (parse "easter sunday 1999")       => 1999-04-04 00-00-00
    (parse "3am tomorrow")             => 2016-10-12 03:00:00
    (parse "10 seconds in the future") => 2016-10-11 12:13:24
    (parse "10 seconds")               => 2016-10-11 12:13:24
    (parse "ten seconds")              => 2016-10-11 12:13:24
    (parse "10 mins")                  => 2016-10-11 12:23:14
    (parse "in 10 mins")               => 2016-10-11 12:23:14
    (parse "20 mins")                  => 2016-10-11 12:33:14
    (parse "in 20 mins")               => 2016-10-11 12:33:14
    (parse "twenty mins")              => 2016-10-11 12:33:14
    (parse "in twenty mins")           => 2016-10-11 12:33:14
    (parse "20 hours")                 => 2016-10-12 08:13:14
    (parse "in 20 hours")              => 2016-10-12 08:13:14
    (parse "28 hours")                 => 2016-10-12 16:13:14
    (parse "in 28 hours")              => 2016-10-12 16:13:14
    (parse "-28 hours")                => 2016-10-10 08:13:14
    (parse "in -28 hours")             => 2016-10-10 08:13:14
    (parse "20 mins and -10 mins")     => 2016-10-11 12:23:14
    (parse "in 20 mins and -10 mins")  => 2016-10-11 12:23:14
    (parse "10 hours and 30 mins")     => 2016-10-11 22:43:14
    (parse "in 10 hours and 30 mins")  => 2016-10-11 22:43:14
    (parse "10 hours, 30 mins")        => 2016-10-11 22:43:14
    (parse "12:30")                    => 2016-10-11 12:30:00
    (parse "12am")                     => 2016-10-11 12:00:00
    (parse "10 minutes ago")           => 2016-10-11 12:03:14
    (parse "wednesday")                => 2016-10-12 00:00:00
    (parse "wed")                      => 2016-10-12 00:00:00
    (parse "sunday")                   => 2016-10-16 00:00:00
    (parse "sun")                      => 2016-10-16 00:00:00
    (parse "tues")                     => 2016-10-18 00:00:00
    (parse "tue")                      => 2016-10-18 00:00:00
    (parse "wed 12:30")                => 2016-10-12 12:30:00
    (parse "sun 12am")                 => 2016-10-16 12:00:00
    (parse "next week")                => 2016-10-17 00:00:00
    (parse "monday")                   => 2016-10-17 00:00:00
    (parse "tuesday")                  => 2016-10-18 00:00:00
    (parse "next mon")                 => 2016-10-17 00:00:00
    (parse "next monday")              => 2016-10-17 00:00:00
    (parse "next thursday")            => 2016-10-20 00:00:00
    (parse "next sun")                 => 2016-10-23 00:00:00
    (parse "mon 11pm")                 => 2016-10-17 23:00:00
    (parse "tuesday 1pm")              => 2016-10-18 13:00:00
    (parse "tuesday at 1")             => 2016-10-18 01:00:00
    (parse "tuesday 1pm")              => 2016-10-18 13:00:00
    (parse "1st")                      => 2016-11-01 00:00:00
    (parse "22nd")                     => 2016-10-22 00:00:00
    (parse "1pm 22nd")                 => 2016-10-22 13:00:00
    (parse "22nd 1am")                 => 2016-10-22 01:00:00
    (parse "22nd @ 1am")               => 2016-10-22 01:00:00
    (parse "22nd at 1am")              => 2016-10-22 01:00:00
    (parse "July 1st")                 => 2017-07-01 00:00:00
    (parse "July 1st 12:00")           => 2017-07-01 12:00:00
    (parse "July 1st 12:01")           => 2017-07-01 12:01:00
    (parse "July 1st 12:11")           => 2017-07-01 12:11:00
    (parse "1st July")                 => 2017-07-01 00:00:00
    (parse "July 1st 13:21")           => 2017-07-01 13:21:00
    (parse "1st July 13:21")           => 2017-07-01 13:21:00
    (parse "1st July 2012")            => 2012-07-01 00:00:00
    (parse "1st July 2012 12pm")       => 2012-07-01 12:00:00
    (parse "December 22nd")            => 2016-12-22 00:00:00
    (parse "December 1st @ 1pm")       => 2016-12-01 13:00:00
    (parse "January")                  => 2017-01-01 00:00:00
    (parse "Jan")                      => 2017-01-01 00:00:00
    (parse "january")                  => 2017-01-01 00:00:00
    (parse "jan")                      => 2017-01-01 00:00:00
    (parse "nov")                      => 2016-11-01 00:00:00
    (parse "Nov")                      => 2016-11-01 00:00:00
    (parse "November")                 => 2016-11-01 00:00:00
    (parse "november")                 => 2016-11-01 00:00:00
    (parse "January 2019")             => 2019-01-01 00:00:00
    (parse "January 2011")             => 2011-01-01 00:00:00
    (parse "feb 1967")                 => 1967-02-01 00:00:00
    
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

Copyright © 2015,2016,2017,2018 Phil Jackson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
