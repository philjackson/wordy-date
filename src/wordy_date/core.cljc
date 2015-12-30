(ns wordy-date.core
  (:require [clojure.string :as str]
            [instaparse.core :as insta]
            #?(:clj  [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            #?(:clj [clojure.edn])))

(def wordy-date-parser (insta/parser
                        (str/join "\n" ["S = neg-duration | pos-duration | dow | quickie"
                                        "quickie = 'tomorrow' | 'now'"

                                        ;; durations
                                        "neg-duration = _multi-duration <ws> <'ago'>"
                                        "pos-duration = _multi-duration"
                                        "<_multi-duration> = _duration <(',' | <ws> 'and')?> (<ws> _duration)*"
                                        "_duration = (<pre-superfluous> <ws>)? digits <ws> period"
                                        "<pre-superfluous> = 'in' | '+' | 'plus'"

                                        "period = #'(sec(ond)?|min(ute)?|day|hour|week|month|year)s?'"
                                        "dow = long-days | short-days"

                                        "short-days = 'mon' | 'tue' | 'wed' | 'thur' | 'fri' | 'sat' | 'sun'"
                                        "long-days = 'monday' | 'tuesday' | 'wednesday' | 'thursday' | 'friday' | 'saturday' | 'sunday'"

                                        "ws = #'\\s+'"
                                        "digits = #'-?[0-9]+'"])
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

(defn parse [st]
  (let [S (insta/transform {:digits #?(:clj clojure.edn/read-string
                                       :cljs js/parseInt)
                            :period period-translation
                            :long-days day-number
                            :short-days day-number
                            :neg-duration handle-neg-duration
                            :pos-duration handle-pos-duration
                            :dow handle-dow
                            :quickie (fn [s]
                                       (case s
                                         "tomorrow" (t/plus (t/now) (t/days 1))
                                         "now" (t/now)))}
                           (wordy-date-parser st))]
    (when (= (first S) :S)
      (second S))))
