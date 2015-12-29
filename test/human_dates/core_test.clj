(ns wordy-date.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [wordy-date.core :refer :all]))

(deftest human-date-test
  (let [time-now (time/date-time 2016 01 01)]
    (with-redefs-fn {#'time/now (constantly time-now)}
      #(testing "redef worked"
         (is (= "2016-01-01T00:00:00.000Z" (str (time/now))))))))
