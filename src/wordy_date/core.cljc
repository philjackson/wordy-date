(ns wordy-date.core
  (:require [clojure.string :as str]
            [instaparse.core :as insta]
            [wordy-date.numbers :refer [number-map]]
            #?(:clj  [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            #?(:clj [clojure.edn])))

(def our-format #?(:cljs goog.string/format
                   :clj clojure.core/format))

(defn make-insta-strings [things]
  (str/join " | " (map #(str "'" % "'") things)))

(def padded (map #(our-format "%02d" %) (range 0 10)))

(def number-words (make-insta-strings (keys number-map)))
(def day-nums (make-insta-strings (range 1 32)))
(def hour-nums (make-insta-strings (concat padded (range 0 24))))
(def min-nums (make-insta-strings (concat padded (range 10 60))))
(def sec-nums min-nums)
(def day-words (make-insta-strings (flatten
                                    (conj (map #(vector % (subs % 0 3))
                                               ["monday"
                                                "tuesday"
                                                "wednesday"
                                                "thursday"
                                                "friday"
                                                "saturday"
                                                "sunday"])
                                          "tues"
                                          "thurs"))))

(def month-words (make-insta-strings ["jan" "january"
                                      "feb" "february"
                                      "mar" "march"
                                      "apr" "april"
                                      "may" "may"
                                      "jun" "june"
                                      "jul" "july"
                                      "aug" "august"
                                      "sep" "september"
                                      "oct" "october"
                                      "nov" "november"
                                      "dec" "december"]))

(def wordy-date-parser
  (insta/parser
   (str/join "\n" ["S = tomorrow-ts | neg-duration | pos-duration | day-words-ts | day-words | quickie | lone-time-stamp | ordinal-day | ts-ordinal-day | ordinal-day-ts | month-ordinal-day | month-ordinal-day-ts | ordinal-day-month | ordinal-day-month-ts | ts-tomorrow | ordinal-day-month-year | ordinal-day-month-year-ts | next-week | last-week | month | month-year"
                   ;; "types"
                   "period-words = #'(sec(ond)?|min(ute)?|day|hour|week|month|year)s?'"
                   "ordinal-day = day-nums <ordinal-modifier>" ; 1st, 2nd..
                   "<ordinal-modifier> = ( 'th' | 'nd' | 'rd' | 'st')"
                   (str "number-words = " number-words) ; one, two...
                   (str "day-nums = " day-nums)         ; 1..31
                   (str "hour-nums = " hour-nums)       ; 0..23
                   (str "min-nums = " min-nums)         ; 0..59
                   (str "sec-nums = " sec-nums)         ; 0..59
                   (str "raw-day-words = " day-words)   ; mon, monday, tue...
                   (str "month-words = " month-words)   ; jan, january, feb...

                   ;; random
                   "quickie = <( 'this time' <ws>)?> ( 'midnight' | 'tomorrow' | 'now' | 'next week' | 'last week' )"
                   "tomorrow-ts = 'tomorrow' <ws> ts"
                   "ts-tomorrow = ts <ws> 'tomorrow'"

                   "day-words = raw-day-words"
                   "next-week = <'next'> <ws> raw-day-words"
                   "last-week = <'last'> <ws> raw-day-words"

                   ;; durations
                   "neg-duration = _multi-duration <ws> <'ago'>"
                   "pos-duration = _multi-duration (<ws> <post-superfluous>)?"
                   "<_multi-duration> = _duration <(',' | <ws> 'and')?> (<ws> _duration)*"
                   "_duration = (<pre-superfluous> <ws>)? ( signed-digits | number-words ) <ws> period-words"
                   "<pre-superfluous> = 'in' | '+' | 'plus'"
                   "<post-superfluous> = 'in the future' | 'from now'"

                   ;; time stamps
                   "lone-time-stamp = ts"
                   "ts = <(( 'at' | '@' ) ws)>? hour-nums (<':'> min-nums)? meridiem?"
                   "meridiem = ( 'am' | 'pm' )"

                   ;; things with years
                   "year = #'[1-9][0-9]{3}'"
                   "ordinal-day-month-year = ordinal-day-month <ws> year"
                   "ordinal-day-month-year-ts = ordinal-day-month-year <ws> ts"

                   "month = month-words"
                   "month-year = month <ws> year"
                   "month-ordinal-day = month-words <ws> (day-nums <ordinal-modifier>)"
                   "month-ordinal-day-ts = month-ordinal-day <ws> ts"
                   "ordinal-day-month = (day-nums <ordinal-modifier>) <ws> month-words"
                   "ordinal-day-month-ts = ordinal-day-month <ws> ts"

                   ;; things with timestamps
                   "day-words-ts = day-words <ws> ts"
                   "ts-ordinal-day = ts <ws> ordinal-day"
                   "ordinal-day-ts = ordinal-day <ws> ts"

                   "ws = #'\\s+'"
                   "signed-digits = #'[-+]?[0-9]+'"])
   :string-ci true))

(defn day-number* [day]
  (case (subs day 0 3)
    "mon" 1
    "tue" 2
    "wed" 3
    "thu" 4
    "fri" 5
    "sat" 6
    "sun" 7))

(defn period-word-translation* [st]
  (case (subs st 0 3)
    "sec" t/seconds
    "min" t/minutes
    "day" t/days
    "hou" t/hours
    "wee" t/weeks
    "mon" t/months
    "yea" t/years))

(defn month-word-translation* [st]
  (case (subs st 0 3)
    "jan" 1
    "feb" 2
    "mar" 3
    "apr" 4
    "may" 5
    "jun" 6
    "jul" 7
    "aug" 8
    "sep" 9
    "oct" 10
    "nov" 11
    "dec" 12))

(def day-number (memoize day-number*))
(def period-word-translation (memoize period-word-translation*))
(def month-word-translation (memoize month-word-translation*))

(defn handle-duration [modifier & args]
  (reduce (fn [now [_ amount f]]
            (modifier now (f amount))) (t/now) args))

(defn handle-neg-duration [& args]
  (apply handle-duration t/minus args))

(defn handle-pos-duration [& args]
  (apply handle-duration t/plus args))

(defn midnight
  "Returns midnight for the date provided at `date`. cljs-time has
  `at-midnight', but clj-time doesn't seem to..."
  ([] (midnight (t/now)))
  ([date] (t/date-time (t/year date) (t/month date) (t/day date) 0 0 0)))

(defn handle-day-words [day-words]
  (let [num (day-number day-words)
        now (t/now)
        our-day-words (t/day-of-week now)]
    (midnight (if (< our-day-words num)
                ;; this occurs this week as the day given is "after" now
                (t/plus now (t/days (- num our-day-words)))

                ;; we move to next week as the day asked for is "before" now
                (let [next-week (t/plus (t/now) (t/weeks 1))
                      our-day-words (t/day-of-week next-week)]
                  (t/plus next-week (t/days (- num our-day-words))))))))

(defn handle-ordinal-day
  "Given a `day` which has been ordinal, transform to either that day
  later this week, or if the day given has already passed, that day
  next month."
  [day]
  (let [now (t/now)]
    (if (< day (t/day now))
      ;; clj-time is smart and won't take us two months ahead
      (let [next-month (t/plus now (t/months 1))]
        (t/date-time (t/year next-month) (t/month next-month) day))
      (t/date-time (t/year now) (t/month now) day))))

(defn timestamp-to-day
  "Given a timestamp in the format `{:hour h :min m}`, set the
  time for given `date` to that time."
  [date {:keys [hour min]}]
  (-> (midnight date)
      (t/plus (t/hours hour) (t/minutes min))))

(def parse-int #?(:clj clojure.edn/read-string
                  :cljs js/parseInt))

(defn handle-ts
  "Convert `[[:hour 2] [:min 3] [:meridiem 'pm']]` into a 24-hour
  timestamp."
  [& values]
  (let [{:keys [hour min meridiem]} (into {} values)]
    (cond-> {:hour hour :min 0}
      ;; parse the mins if they're there
      min (assoc :min min)

      ;; 11pm becomes 23
      (and (= meridiem "pm") (< hour 12)) (update :hour #(+ % 12)))))

(defn month-ordinal-day-handler [month day]
  (let [now (t/now)]
    (if (< month (t/month now))
      (let [next-year (t/plus now (t/years 1))]
        (t/date-time (t/year next-year) month day))
      (t/date-time (t/year now) month day))))

(defn handle-date-year [date year]
  (t/date-time year (t/month date) (t/day date) (t/hour date) (t/minute date)))

(defn handle-last-week [day]
  (let [now (t/now)
        dow (t/day-of-week now)]
    (midnight
     (t/plus
      (t/minus now (t/days (+ 7 dow)))
      (t/days (day-number day))))))

(defn handle-next-week [day]
  (let [now (t/now)
        dow (t/day-of-week now)]
    (midnight
     (t/plus now
             (t/days (+
                      ;; move to the start of next week
                      (- 7 dow)
                      ;; add the days
                      (day-number day)))))))

(defn handle-month [month]
  (midnight
   (let [now (t/now)
         cur-month (t/month now)]
     (t/date-time (if (> month cur-month)
                    (t/year now)
                    (t/year (t/plus now (t/years 1))))
                  month
                  1))))

(defn handle-month-year [month year]
  (t/date-time year (t/month month) (t/day month)))

(def transformations {:signed-digits parse-int
                      :period-words period-word-translation
                      :day-nums parse-int
                      :day-half #(vector :day-half %)
                      :hour-nums #(vector :hour (parse-int %))
                      :month-ordinal-day month-ordinal-day-handler
                      :ordinal-day-month #(month-ordinal-day-handler %2 %1)

                      :min-nums #(vector :min (parse-int %))
                      :month-words month-word-translation
                      :ts handle-ts
                      :_ordinal-day parse-int
                      :ordinal-day handle-ordinal-day
                      :raw-day-words str
                      :next-week handle-next-week
                      :last-week handle-last-week

                      ;; timestamps
                      :lone-time-stamp #(timestamp-to-day (t/now) %)
                      :month-ordinal-day-ts timestamp-to-day
                      :ordinal-day-month-ts timestamp-to-day
                      :ts-ordinal-day #(timestamp-to-day %2 %1)
                      :ordinal-day-ts timestamp-to-day
                      :day-words-ts timestamp-to-day

                      :month handle-month
                      :month-year handle-month-year

                      :number-words #(get number-map %)
                      :neg-duration handle-neg-duration
                      :pos-duration handle-pos-duration
                      :day-words handle-day-words

                      ;; years
                      :year parse-int
                      :ordinal-day-month-year handle-date-year
                      :ordinal-day-month-year-ts timestamp-to-day

                      :quickie #(case %
                                  "tomorrow" (t/plus (t/now) (t/days 1))
                                  "next week" (handle-day-words "monday")
                                  "last week" (midnight (t/minus (t/now) (t/days 7)))
                                  "midnight" (midnight)
                                  "now" (t/now))

                      :ts-tomorrow (fn [ts _] (timestamp-to-day (t/plus (t/now) (t/days 1)) ts))
                      :tomorrow-ts (fn [_ ts] (timestamp-to-day (t/plus (t/now) (t/days 1)) ts))})

(def raw-parse wordy-date-parser)

(defn parse [st]
  (let [S (insta/transform transformations (raw-parse st))]
    (when (= (first S) :S)
      (second S))))
