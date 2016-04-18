(ns wordy-date.core-test
  (:require #?@(:clj [[clj-time.core :as time]
                      [clojure.test :refer [testing deftest is]]]
                :cljs [[cljs-time.core :as time]
                       [cljs.test :refer [testing deftest is]]])
            [wordy-date.core :refer [parse raw-parse]]))

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

(deftest human-date-test
  (with-redefs [time/now (constantly fake-now)]
    (let [tomorrow (time/plus fake-now (time/days 1))]
      (testing "redef worked"
        (is (= "2016-10-11T12:13:14.000Z" (str (time/now)))))

      (testing "quickies"
        (is (= (parse "tomorrow")
               (parse "this time tomorrow")
               tomorrow))
        (is (= (parse "now") fake-now))        
        (is (= (parse "tomorrow @ 1pm") (time/date-time 2016 10 12 13 00)))
        (is (= (parse "3am tomorrow") (time/date-time 2016 10 12 3 00))))

      (testing "periods"
        (is (= (parse "10 seconds")
               (parse "in 10 seconds")
               (time/plus fake-now (time/seconds 10))))
        (is (= (parse "ten seconds")
               (parse "in ten seconds")
               (time/plus fake-now (time/seconds 10))))

        (is (= (parse "10 mins")
               (parse "in 10 mins")
               (time/plus fake-now (time/minutes 10))))
        (is (= (parse "20 mins")
               (parse "in 20 mins")
               (time/plus fake-now (time/minutes 20))))
        (is (= (parse "twenty mins")
               (parse "in twenty mins")
               (time/plus fake-now (time/minutes 20))))

        (is (= (parse "20 hours")
               (parse "in 20 hours")
               (time/plus fake-now (time/hours 20))))
        (is (= (parse "28 hours")
               (parse "in 28 hours")
               (time/plus fake-now (time/hours 28))))
        (is (= (parse "-28 hours")
               (parse "in -28 hours")
               (time/minus fake-now (time/hours 28))))

        (is (= (parse "20 mins and -10 mins")
               (parse "in 20 mins and -10 mins")
               (time/plus fake-now (time/minutes 10))))

        (is (= (parse "10 hours and 30 mins")
               (parse "in 10 hours and 30 mins")
               (-> fake-now
                   (time/plus (time/hours 10))
                   (time/plus (time/minutes 30)))))
        
        (is (= (parse "10 hours, 30 mins") (parse "10 hours and 30 mins")) )

        (testing "timestamp"
          (is (= (parse "12:30") (time/date-time 2016 10 11 12 30 00)))
          (is (= (parse "12am") (time/date-time 2016 10 11 12 00 00)))))

      (testing "negative periods"
        (is (= (parse "10 minutes ago") (time/minus fake-now (time/minutes 10)))))

      (testing "dow"
        (testing "for later this week"
          ;; later this week
          (is (= (parse "wednesday") (time/date-time 2016 10 12 00 00 00)))
          (is (= (parse "wed") (time/date-time 2016 10 12 00 00 00)))
          (is (= (parse "sunday") (time/date-time 2016 10 16 00 00 00)))
          (is (= (parse "sun") (time/date-time 2016 10 16 00 00 00)))
          (is (= (parse "tues") (time/date-time 2016 10 18 00 00 00)))
          (is (= (parse "tue") (time/date-time 2016 10 18 00 00 00)))

          (testing "with timestamp"
            (is (= (parse "wed 12:30") (time/date-time 2016 10 12 12 30 00)))
            (is (= (parse "sun 12am") (time/date-time 2016 10 16 12 00 00)))))

        (testing "next week"
          ;; becomes next week (our test date is a tuesday)
          (is (= (parse "next week") (time/date-time 2016 10 17 00 00 00)))
          (is (= (parse "monday") (time/date-time 2016 10 17 00 00 00)))
          (is (= (parse "tuesday") (time/date-time 2016 10 18 00 00 00)))

          ;; becomes seven days plus/minus whatever
          (is (= (parse "next mon") (time/date-time 2016 10 17 12 13 14)))
          (is (= (parse "next monday") (time/date-time 2016 10 17 12 13 14)))
          (is (= (parse "next thursday") (time/date-time 2016 10 20 12 13 14)))
          (is (= (parse "next sun") (time/date-time 2016 10 23 12 13 14)))

          (testing "with timestamp"
            (is (= (parse "mon 11pm") (time/date-time 2016 10 17 23 00 00)))
            (is (= (parse "tuesday 1pm") (time/date-time 2016 10 18 13 00 00)))
            (is (= (parse "tuesday at 1") (time/date-time 2016 10 18 01 00 00)))
            (is (= (parse "tuesday 1pm") (time/date-time 2016 10 18 13 00 00))))))

      (testing "ordinal days"
        (testing "in the past (translates to next month)"
          (is (= (parse "1st") (time/date-time 2016 11 01 00 00 00))))

        (testing "in the future (translates to this month)"
          (is (= (parse "22nd") (time/date-time 2016 10 22 00 00 00)))
          (is (= (parse "1pm 22nd") (time/date-time 2016 10 22 13 00 00)))
          (is (= (parse "22nd 1am") (time/date-time 2016 10 22 01 00 00)))
          (is (= (parse "22nd @ 1am") (time/date-time 2016 10 22 01 00 00)))
          (is (= (parse "22nd at 1am") (time/date-time 2016 10 22 01 00 00)))))

      (testing "month stuff"
        (testing "in the past (translates to next month)"
          (is (= (parse "July 1st") (time/date-time 2017 07 01 00 00)))
          (is (= (parse "July 1st 12:0") nil))
          (is (= (parse "July 1st 12:00") (time/date-time 2017 07 01 12 00)))
          (is (= (parse "July 1st 12:01") (time/date-time 2017 07 01 12 01)))
          (is (= (parse "July 1st 12:11") (time/date-time 2017 07 01 12 11)))
          (is (= (parse "1st July") (time/date-time 2017 07 01 00 00)))
          (is (= (parse "July 1st 13:21") (time/date-time 2017 07 01 13 21)))
          (is (= (parse "1st July 13:21") (time/date-time 2017 07 01 13 21))))
        
        (testing "month with year"
          (is (= (parse "1st July 2012") (time/date-time 2012 07 01 00 00)))
          (is (= (parse "1st July 2012 12pm") (time/date-time 2012 07 01 12 00))))

        (testing "in the future (translates to this month)"
          (is (= (parse "December 22nd") (time/date-time 2016 12 22 00 00 00)))
          (is (= (parse "December 1st @ 1pm") (time/date-time 2016 12 01 13 00 00))))))
    ))