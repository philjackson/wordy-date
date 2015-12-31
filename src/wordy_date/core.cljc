(ns wordy-date.core
  (:require [clojure.string :as str]
            [instaparse.core :as insta]
            [wordy-date.numbers :refer [insta-nums number-map]]
            #?(:clj  [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            #?(:clj [clojure.edn])))

(def wordy-date-parser (insta/parser
                        (str/join "\n" ["S = neg-duration | pos-duration | dow-ts | dow | quickie | lone-time-stamp"
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
                                        "ts = #'(\\d{1,2})(?::(\\d{2}))?(am|pm)?'"

                                        "dow-ts = dow <ws> ts"

                                        "short-days = 'mon' | 'tue' | 'wed' | 'thur' | 'fri' | 'sat' | 'sun'"
                                        "long-days = 'monday' | 'tuesday' | 'wednesday' | 'thursday' | 'friday' | 'saturday' | 'sunday'"

                                        "ws = #'\\s+'"
                                        "signed-digits = #'-?[0-9]+'"])
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

(defn midnight [ts]
  (t/date-time (t/year ts) (t/month ts) (t/day ts) 0 0 0))

(defn timestamp-to-day [ts {:keys [hour min]}]
  (-> (midnight ts)
      (t/plus (t/hours hour) (t/minutes min))))

(defn handle-dow-ts [dow ts]
  (timestamp-to-day dow ts))

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

(defn parse-time [st]
  (let [[_ hours mins modifier] (re-find #"(\d{1,2})(?::(\d{2}))?(am|pm)?" st)
        hours (parse-int hours)]
    (cond-> {:hour hours :min 0}
      ;; parse the mins if they're there
      mins (assoc :min (parse-int mins))

      ;; 11pm becomes 23
      (and (= modifier "pm") (< hours 12)) (update :hour #(+ % 12)))))

(defn parse [st]
  (let [S (insta/transform {:signed-digits parse-int
                            :period period-translation
                            :long-days day-number
                            :short-days day-number
                            :lone-time-stamp #(timestamp-to-day (t/now) %)
                            :ts parse-time
                            :wordy-numbers #(get number-map %)
                            :neg-duration handle-neg-duration
                            :pos-duration handle-pos-duration
                            :dow handle-dow
                            :dow-ts handle-dow-ts
                            :quickie (fn [s]
                                       (case s
                                         "tomorrow" (t/plus (t/now) (t/days 1))
                                         "now" (t/now)))}
                           (wordy-date-parser st))]
    (when (= (first S) :S)
      (second S))))
