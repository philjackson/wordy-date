(ns wordy-date.core
  (:require [clojure.string :as str]
            [instaparse.core :as insta]
            [wordy-date.numbers :refer [insta-nums number-map]]
            #?(:clj  [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            #?(:clj [clojure.edn])))

(defn make-insta-strings [things]
  (str/join " | " (map #(str "'" % "'") things)))

(def valid-days (make-insta-strings (range 1 32)))
(def valid-hours (make-insta-strings (range 0 25)))
(def valid-mins (make-insta-strings (range 0 61)))
(def valid-secs valid-mins)

(def wordy-date-parser
  (insta/parser
   (str/join "\n" ["S = neg-duration | pos-duration | dow-ts | dow | quickie | lone-time-stamp | ordinal-day | ts-ordinal-day | ordinal-day-ts"
                   "quickie = 'tomorrow' | 'now'"

                   ;; durations
                   "neg-duration = _multi-duration <ws> <'ago'>"
                   "pos-duration = _multi-duration"
                   "<_multi-duration> = _duration <(',' | <ws> 'and')?> (<ws> _duration)*"
                   "_duration = (<pre-superfluous> <ws>)? ( signed-digits | wordy-numbers ) <ws> period"
                   "<pre-superfluous> = 'in' | '+' | 'plus'"

                   ;; list of numbers as words 'one' | 'two'...
                   (str "wordy-numbers = " insta-nums)

                   "period = #'(sec(ond)?|min(ute)?|day|hour|week|month|year)s?'"
                   "dow = long-days | short-days"
                   "lone-time-stamp = ts"
                   "ts = <(( 'at' | '@' ) ws)>? valid-hours (<':'> valid-mins)? meridiem?"
                   "meridiem = ( 'am' | 'pm' )"

                   (str "valid-hours = " valid-hours)
                   (str "valid-mins = " valid-mins)
                   (str "valid-secs = " valid-secs)

                   "dow-ts = dow <ws> ts"
                   "ts-ordinal-day = ts <ws> ordinal-day"
                   "ordinal-day-ts = ordinal-day <ws> ts"

                   "short-days = 'mon' | 'tue' | 'wed' | 'thur' | 'fri' | 'sat' | 'sun'"
                   "long-days = 'monday' | 'tuesday' | 'wednesday' | 'thursday' | 'friday' | 'saturday' | 'sunday'"

                   ;; 1st, 2nd, 31st etc.
                   "ordinal-day = day-digits <( 'th' | 'nd' | 'rd' | 'st')>"

                   (str "day-digits = " valid-days)
                   "ws = #'\\s+'"
                   "signed-digits = #'[-+]?[0-9]+'"])
   :string-ci true))

(defn handle-duration [modifier & args]
  (reduce (fn [now [_ amount f]]
            (modifier now (f amount))) (t/now) args))

(defn handle-neg-duration [& args]
  (apply handle-duration t/minus args))

(defn handle-pos-duration [& args]
  (apply handle-duration t/plus args))

(defn handle-dow [dow]
  (let [now (t/now)
        our-dow (t/day-of-week now)]
    (if (< our-dow dow)
      ;; this occurs this week as the day given is "after" now
      (t/plus now (t/days (- dow our-dow)))

      ;; we move to next week as the day asked for is "before" now
      (let [next-week (t/plus (t/now) (t/weeks 1))
            our-dow (t/day-of-week next-week)]
        (t/plus next-week (t/days (- dow our-dow)))))))

(defn handle-ordinal-day [day]
  (let [now (t/now)]
    (if (< day (t/day now))
      ;; clj-time is smart and won't take us two months ahead
      (let [next-month (t/plus now (t/months 1))]
        (t/date-time (t/year next-month) (t/month next-month) day))
      (t/date-time (t/year now) (t/month now) day))))

(defn midnight [ts]
  (t/date-time (t/year ts) (t/month ts) (t/day ts) 0 0 0))

(defn timestamp-to-day [date {:keys [hour min]}]
  (-> (midnight date)
      (t/plus (t/hours hour) (t/minutes min))))

(defn handle-dow-ts [date ts]
  (timestamp-to-day date ts))

(defn handle-ts-ordinal-day [ts day]
  (timestamp-to-day day ts))

(defn handle-ordinal-day-ts [day ts]
  (timestamp-to-day day ts))

(defn day-number* [day]
  (case (subs day 0 3)
    "mon" 1
    "tue" 2
    "wed" 3
    "thu" 4
    "fri" 5
    "sat" 6
    "sun" 7))

(def day-number (memoize day-number*))

(defn period-translation* [st]
  (case (subs st 0 3)
    "sec" t/seconds
    "min" t/minutes
    "day" t/days
    "hou" t/hours
    "wee" t/weeks
    "mon" t/months
    "yea" t/years))

(def period-translation (memoize period-translation*))

(def parse-int #?(:clj clojure.edn/read-string
                  :cljs js/parseInt))

(defn handle-ts [& values]
  (let [{:keys [hour min meridiem]} (into {} values)]
    (cond-> {:hour hour :min 0}
      ;; parse the mins if they're there
      min (assoc :min min)

      ;; 11pm becomes 23
      (and (= meridiem "pm") (< hour 12)) (update :hour #(+ % 12)))))

(defn parse [st]
  (let [S (insta/transform {:signed-digits parse-int
                            :period period-translation
                            :long-days day-number
                            :short-days day-number
                            :lone-time-stamp #(timestamp-to-day (t/now) %)
                            :day-digits parse-int
                            :day-half #(vector :day-half %)
                            :valid-hours #(vector :hour (parse-int %))
                            :valid-mins #(vector :min (parse-int %))
                            :ts handle-ts
                            :ordinal-day handle-ordinal-day
                            :ts-ordinal-day handle-ts-ordinal-day
                            :ordinal-day-ts handle-ordinal-day-ts
                            :wordy-numbers #(get number-map %)
                            :neg-duration handle-neg-duration
                            :pos-duration handle-pos-duration
                            :dow handle-dow
                            :dow-ts handle-dow-ts
                            :quickie #(case %
                                        "tomorrow" (t/plus (t/now) (t/days 1))
                                        "now" (t/now))}
                           (wordy-date-parser st))]
    (if (= (first S) :S)
      (second S)
      S)))
