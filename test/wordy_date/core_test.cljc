(ns wordy-date.core-test
  (:require #?@(:clj [[clj-time.core :as time]
                      [clojure.test :as t]]
                :cljs [[cljs-time.core :as time]
                       [cljs.test :as t]])
            [wordy-date.core :as wd]))

;; October 2016
;; Su Mo Tu We Th Fr Sa
;;  1
;;  2  3  4  5  6  7  8
;;  9 10 11 12 13 14 15
;;       ^^
;; 16 17 18 19 20 21 22
;; 23 24 25 26 27 28 29
;; 30 31

(def fake-now (time/date-time 2016 10 11 12 13 14)) ; => "2016-10-11T12:13:14.000Z"

(t/deftest human-date-test
  (let [tomorrow (time/plus fake-now (time/days 1))
        match (fn [st date] (t/is (= (wd/parse st) date)))]
    (with-redefs-fn {#'time/now (constantly fake-now)}
      #(do
         (t/testing "redef worked"
           (t/is (= "2016-10-11T12:13:14.000Z" (str (time/now)))))

         (t/testing "quickies"
           (match "now" fake-now)
           (match "tomorrow" tomorrow))

         (t/testing "periods"
           (match "10 seconds" (time/plus fake-now (time/seconds 10)))
           (match "10 mins" (time/plus fake-now (time/minutes 10)))
           (match "20 mins" (time/plus fake-now (time/minutes 20)))
           (match "20 hours" (time/plus fake-now (time/hours 20)))
           (match "28 hours" (time/plus fake-now (time/hours 28)))

           (match "10 hours and 30 mins" (-> fake-now
                                             (time/plus (time/hours 10))
                                             (time/plus (time/minutes 30))))

           (match "10 hours, 30 mins" (-> fake-now
                                          (time/plus (time/hours 10))
                                          (time/plus (time/minutes 30)))))

         (t/testing "negative periods"
           (match "10 minutes ago" (time/minus fake-now (time/minutes 10))))

         (t/testing "dow"
           (t/testing "for later this week"
             ;; later this week
             (match "wednesday" (time/date-time 2016 10 12 12 13 14))
             (match "wed" (time/date-time 2016 10 12 12 13 14))
             (match "sunday" (time/date-time    2016 10 16 12 13 14))
             (match "sun" (time/date-time    2016 10 16 12 13 14)))

           (t/testing "next week"
             ;; becomes next week (our test date is a tuesday)
             (match "monday" (time/date-time  2016 10 17 12 13 14))
             (match "tuesday" (time/date-time 2016 10 18 12 13 14))))))))
