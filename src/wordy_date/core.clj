(ns wordy-date.core
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [instaparse.core :as insta]))

(def wordy-date-parser (insta/parser
                        (str/join "\n" ["S = duration | dow | quickie"
                                        "quickie = 'tomorrow' | 'now'"
                                        "duration = (<pre-superfluous> <whitespace>)? digits <whitespace> period"
                                        "period = #'(min(ute)?|day|hour|week|month|year)s?'"
                                        "dow = (dow-modifier <whitespace>)? long-days"
                                        "long-days = 'monday' | 'tuesday' | 'wednesday' | 'thursday' | 'friday' | 'saturday' | 'sunday'"
                                        "dow-modifier = 'next' | 'this'"
                                        "<pre-superfluous> = 'in' | '+' | 'plus'"
                                        "whitespace = #'\\s+'"
                                        "digits = #'-?[0-9.]+'"])
                        :string-ci true))

(defn parse [st]
  (let [S (insta/transform {:digits clojure.edn/read-string
                            :period #(case (subs % 0 3)
                                       "min" t/minutes
                                       "day" t/days
                                       "hou" t/hours
                                       "wee" t/weeks
                                       "mon" t/months
                                       "yea" t/years)
                            :duration (fn [d p] (t/plus (t/now) (p d)))
                            :quickie (fn [s]
                                       (case s
                                         "tomorrow" (t/plus (t/now) (t/days 1))
                                         "now" (t/now)))}
                           (wordy-date-parser st))]
    (when (= (first #spy/p S) :S)
      (second S))))
