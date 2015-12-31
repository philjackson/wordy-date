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

(t/deftest parse-timestamp-test
  (t/is (= {:hour 12 :min 0} (wd/parse-time "12am")))
  (t/is (= {:hour 12 :min 30} (wd/parse-time "12:30am")))
  (t/is (= {:hour 12 :min 30} (wd/parse-time "12:30")))
  (t/is (= {:hour 23 :min 30} (wd/parse-time "23:30")))
  (t/is (= {:hour 23 :min 30} (wd/parse-time "11:30pm"))))

(t/deftest human-date-test
  (let [tomorrow (time/plus fake-now (time/days 1))
        match (fn [st date] (t/is (= (wd/parse st) date)))]
    (with-redefs [time/now (constantly fake-now)]
      (t/testing "redef worked"
        (t/is (= "2016-10-11T12:13:14.000Z" (str (time/now)))))

      (t/testing "quickies"
        (match "now" fake-now)
        (match "tomorrow" tomorrow))

      (t/testing "periods"
        (match "10 seconds"  (time/plus fake-now (time/seconds 10)))
        (match "ten seconds" (time/plus fake-now (time/seconds 10)))

        (match "10 mins"     (time/plus fake-now (time/minutes 10)))
        (match "20 mins"     (time/plus fake-now (time/minutes 20)))
        (match "twenty mins" (time/plus fake-now (time/minutes 20)))

        (match "20 hours"   (time/plus fake-now (time/hours 20)))
        (match "28 hours"   (time/plus fake-now (time/hours 28)))
        (match "-28 hours"  (time/minus fake-now (time/hours 28)))

        (match "20 mins and -10 mins" (time/plus fake-now (time/minutes 10)))

        (match "10 hours and 30 mins" (-> fake-now
                                          (time/plus (time/hours 10))
                                          (time/plus (time/minutes 30))))

        (match "10 hours, 30 mins" (wd/parse "10 hours and 30 mins")))

      (t/testing "timestamp"
        (match "12:30" (time/date-time 2016 10 11 12 30 00))
        (match "12am" (time/date-time 2016 10 11 12 00 00)))

      (t/testing "negative periods"
        (match "10 minutes ago" (time/minus fake-now (time/minutes 10))))

      (t/testing "dow"
        (t/testing "for later this week"
          ;; later this week
          (match "wednesday" (time/date-time 2016 10 12 12 13 14))
          (match "wed"       (time/date-time 2016 10 12 12 13 14))
          (match "sunday"    (time/date-time 2016 10 16 12 13 14))
          (match "sun"       (time/date-time 2016 10 16 12 13 14))

          (t/testing "with timestamp"
            (match "wed 12:30" (time/date-time 2016 10 12 12 30 00))
            (match "sun 12am"  (time/date-time 2016 10 16 12 00 00))))

        (t/testing "next week"
          ;; becomes next week (our test date is a tuesday)
          (match "monday"  (time/date-time 2016 10 17 12 13 14))
          (match "tuesday" (time/date-time 2016 10 18 12 13 14))

          (t/testing "with timestamp"
            (match "mon 11pm"    (time/date-time 2016 10 17 23 00 00))
            (match "tuesday 1"   (time/date-time 2016 10 18 01 00 00))
            (match "tuesday 1pm" (time/date-time 2016 10 18 13 00 00)))))

      (t/testing "ordinal days"
        (t/testing "in the past (translates to next month)"
          (match "1st" (time/date-time 2016 11 01 00 00 00)))

        (t/testing "in the future (translates to this month)"
          (match "22nd"     (time/date-time 2016 10 22 00 00 00))
          (match "1pm 22nd" (time/date-time 2016 10 22 13 00 00))
          (match "22nd 1am" (time/date-time 2016 10 22 01 00 00))
          (match "22nd @ 1am" (time/date-time 2016 10 22 01 00 00))
          (match "22nd at 1am" (time/date-time 2016 10 22 01 00 00)))))))
