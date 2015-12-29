(ns wordy-date.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [wordy-date.core :refer :all]))

;; October 2016
;; Su Mo Tu We Th Fr Sa
;;  1
;;  2  3  4  5  6  7  8
;;  9 10 11 12 13 14 15
;; 16 17 18 19 20 21 22
;; 23 24 25 26 27 28 29
;; 30 31

(def fake-now (t/date-time 2016 10 11 12 13 14)) ; => "2016-10-11T12:13:14.000Z"

(deftest human-date-test
  (let [tomorrow (t/plus fake-now (t/days 1))
        match (fn [st date] (is (= (parse st) date)))]
    (with-redefs-fn {#'t/now (constantly fake-now)}
      #(do
         (testing "redef worked"
           (is (= "2016-10-11T12:13:14.000Z" (str (t/now)))))

         (testing "quickies"
           (match "now" fake-now)
           (match "tomorrow" tomorrow))

         (testing "periods"
           (match "10 seconds" (t/plus fake-now (t/seconds 10)))
           (match "10 mins" (t/plus fake-now (t/minutes 10)))
           (match "20 mins" (t/plus fake-now (t/minutes 20)))
           (match "20 hours" (t/plus fake-now (t/hours 20)))
           (match "28 hours" (t/plus fake-now (t/hours 28)))

           (match "10 hours and 30 mins" (-> fake-now
                                             (t/plus (t/hours 10))
                                             (t/plus (t/minutes 30))))

           (match "10 hours, 30 mins" (-> fake-now
                                          (t/plus (t/hours 10))
                                          (t/plus (t/minutes 30)))))

         (testing "dow"
           (match "this monday" (t/date-time 2015 10 10 12 13 14)))))))
