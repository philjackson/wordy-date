(ns wordy-date.core
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [instaparse.core :as insta]))

(def wordy-date-parser (insta/parser
                        (str/join "\n" ["S = duration | dow | quickie"
                                        "quickie = 'tomorrow' | 'now'"

                                        ;; durations
                                        "duration = _duration <(',' | <whitespace> 'and')?> (<whitespace> _duration)*"
                                        "_duration = (<pre-superfluous> <whitespace>)? digits <whitespace> period"
                                        "<pre-superfluous> = 'in' | '+' | 'plus'"


                                        "period = #'(sec(ond)?|min(ute)?|day|hour|week|month|year)s?'"
                                        "dow = (dow-modifier <whitespace>)? long-days"
                                        "long-days = 'monday' | 'tuesday' | 'wednesday' | 'thursday' | 'friday' | 'saturday' | 'sunday'"

                                        "dow-modifier = 'next' | 'this'"
                                        "whitespace = #'\\s+'"
                                        "digits = #'-?[0-9]+'"])
                        :string-ci true))

(defn handle-duration [& args]
  (reduce (fn [now [_ amount f]]
            (t/plus now (f amount))) (t/now) args))

(defn parse [st]
  (let [S (insta/transform {:digits clojure.edn/read-string
                            :period #(case (subs % 0 3)
                                       "sec" t/seconds
                                       "min" t/minutes
                                       "day" t/days
                                       "hou" t/hours
                                       "wee" t/weeks
                                       "mon" t/months
                                       "yea" t/years)
                            :duration handle-duration
                            :quickie (fn [s]
                                       (case s
                                         "tomorrow" (t/plus (t/now) (t/days 1))
                                         "now" (t/now)))}
                           (wordy-date-parser st))]
    (when (= (first S) :S)
      (second S))))
