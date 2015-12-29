(ns wordy-date.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [wordy-date.core :refer :all]))

(deftest human-date-test
  (let [now (t/date-time 2016 10 11 12 13 14)
        tomorrow (t/plus now (t/days 1))
        match (fn [st date] (is (= (parse st) date)))]
    (with-redefs-fn {#'t/now (constantly now)}
      #(do
         (testing "redef worked"
           (is (= "2016-10-11T12:13:14.000Z" (str (t/now)))))

         (testing "quickies"
           (match "now" now)
           (match "tomorrow" tomorrow))

         (testing "periods"
           (match "10 mins" (t/plus now (t/minutes 10)))
           (match "20 mins" (t/plus now (t/minutes 20)))
           (match "20 hours" (t/plus now (t/hours 20)))
           (match "28 hours" (t/plus now (t/hours 28)))

           (match "10 hours and 30 mins" (-> now
                                             (t/plus (t/hours 10))
                                             (t/plus (t/minutes 30))))

           (match "10 hours, 30 mins" (-> now
                                          (t/plus (t/hours 10))
                                          (t/plus (t/minutes 30)))))))))
