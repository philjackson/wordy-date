(ns wordy-date.core
  (:require [clojure.string :as str]
            [clj-time.core :as time]
            [instaparse.core :as insta]))

(def wordy-date-parser (insta/parser
                        (str/join "\n" ["whitespace = #'\\s+'"
                                        "digits = #'-?[0-9]+'"])
                        :string-ci true))

(defn parse [st]
  )
