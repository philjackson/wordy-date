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

    (parse "tomorrow")                 => 2016-10-12T12:13:14.000Z
    (parse "this time tomorrow")       => 2016-10-12T12:13:14.000Z
    (parse "midnight")                 => 2016-10-11T00:00:00.000Z
    (parse "now")                      => 2016-10-11T12:13:14.000Z
    (parse "tomorrow @ 1pm")           => 2016-10-12T13:00:00.000Z
    (parse "3am tomorrow")             => 2016-10-12T03:00:00.000Z
    (parse "10 seconds in the future") => 2016-10-11T12:13:24.000Z
    (parse "10 seconds")               => 2016-10-11T12:13:24.000Z
    (parse "ten seconds")              => 2016-10-11T12:13:24.000Z
    (parse "10 mins")                  => 2016-10-11T12:23:14.000Z
    (parse "in 10 mins")               => 2016-10-11T12:23:14.000Z
    (parse "20 mins")                  => 2016-10-11T12:33:14.000Z
    (parse "in 20 mins")               => 2016-10-11T12:33:14.000Z
    (parse "twenty mins")              => 2016-10-11T12:33:14.000Z
    (parse "in twenty mins")           => 2016-10-11T12:33:14.000Z
    (parse "20 hours")                 => 2016-10-12T08:13:14.000Z
    (parse "in 20 hours")              => 2016-10-12T08:13:14.000Z
    (parse "28 hours")                 => 2016-10-12T16:13:14.000Z
    (parse "in 28 hours")              => 2016-10-12T16:13:14.000Z
    (parse "-28 hours")                => 2016-10-10T08:13:14.000Z
    (parse "in -28 hours")             => 2016-10-10T08:13:14.000Z
    (parse "20 mins and -10 mins")     => 2016-10-11T12:23:14.000Z
    (parse "in 20 mins and -10 mins")  => 2016-10-11T12:23:14.000Z
    (parse "10 hours and 30 mins")     => 2016-10-11T22:43:14.000Z
    (parse "in 10 hours and 30 mins")  => 2016-10-11T22:43:14.000Z
    (parse "10 hours, 30 mins")        => 2016-10-11T22:43:14.000Z
    (parse "12:30")                    => 2016-10-11T12:30:00.000Z
    (parse "12am")                     => 2016-10-11T12:00:00.000Z
    (parse "10 minutes ago")           => 2016-10-11T12:03:14.000Z
    (parse "wednesday")                => 2016-10-12T00:00:00.000Z
    (parse "wed")                      => 2016-10-12T00:00:00.000Z
    (parse "sunday")                   => 2016-10-16T00:00:00.000Z
    (parse "sun")                      => 2016-10-16T00:00:00.000Z
    (parse "tues")                     => 2016-10-18T00:00:00.000Z
    (parse "tue")                      => 2016-10-18T00:00:00.000Z
    (parse "wed 12:30")                => 2016-10-12T12:30:00.000Z
    (parse "sun 12am")                 => 2016-10-16T12:00:00.000Z
    (parse "next week")                => 2016-10-17T00:00:00.000Z
    (parse "monday")                   => 2016-10-17T00:00:00.000Z
    (parse "tuesday")                  => 2016-10-18T00:00:00.000Z
    (parse "next mon")                 => 2016-10-17T00:00:00.000Z
    (parse "next monday")              => 2016-10-17T00:00:00.000Z
    (parse "next thursday")            => 2016-10-20T00:00:00.000Z
    (parse "next sun")                 => 2016-10-23T00:00:00.000Z
    (parse "mon 11pm")                 => 2016-10-17T23:00:00.000Z
    (parse "tuesday 1pm")              => 2016-10-18T13:00:00.000Z
    (parse "tuesday at 1")             => 2016-10-18T01:00:00.000Z
    (parse "tuesday 1pm")              => 2016-10-18T13:00:00.000Z
    (parse "1st")                      => 2016-11-01T00:00:00.000Z
    (parse "22nd")                     => 2016-10-22T00:00:00.000Z
    (parse "1pm 22nd")                 => 2016-10-22T13:00:00.000Z
    (parse "22nd 1am")                 => 2016-10-22T01:00:00.000Z
    (parse "22nd @ 1am")               => 2016-10-22T01:00:00.000Z
    (parse "22nd at 1am")              => 2016-10-22T01:00:00.000Z
    (parse "July 1st")                 => 2017-07-01T00:00:00.000Z
    (parse "July 1st 12:00")           => 2017-07-01T12:00:00.000Z
    (parse "July 1st 12:01")           => 2017-07-01T12:01:00.000Z
    (parse "July 1st 12:11")           => 2017-07-01T12:11:00.000Z
    (parse "1st July")                 => 2017-07-01T00:00:00.000Z
    (parse "July 1st 13:21")           => 2017-07-01T13:21:00.000Z
    (parse "1st July 13:21")           => 2017-07-01T13:21:00.000Z
    (parse "1st July 2012")            => 2012-07-01T00:00:00.000Z
    (parse "1st July 2012 12pm")       => 2012-07-01T12:00:00.000Z
    (parse "December 22nd")            => 2016-12-22T00:00:00.000Z
    (parse "December 1st @ 1pm")       => 2016-12-01T13:00:00.000Z
    (parse "January")                  => 2017-01-01T00:00:00.000Z
    (parse "Jan")                      => 2017-01-01T00:00:00.000Z
    (parse "january")                  => 2017-01-01T00:00:00.000Z
    (parse "jan")                      => 2017-01-01T00:00:00.000Z
    (parse "nov")                      => 2016-11-01T00:00:00.000Z
    (parse "Nov")                      => 2016-11-01T00:00:00.000Z
    (parse "November")                 => 2016-11-01T00:00:00.000Z
    (parse "november")                 => 2016-11-01T00:00:00.000Z
    (parse "January 2019")             => 2019-01-01T00:00:00.000Z
    (parse "January 2011")             => 2011-01-01T00:00:00.000Z
    (parse "feb 1967")                 => 1967-02-01T00:00:00.000Z
    
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
