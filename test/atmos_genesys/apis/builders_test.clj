(ns atmos-genesys.apis.builders-test
  (:require
    [atmos-genesys.apis.builders :as builder]
    [clojure.test :refer :all])
  (:import (java.util Map)))

(def test-verbose? (Boolean/parseBoolean (System/getenv "TEST_VERBOSE")))

(extend-protocol atmos-logs.core/PLoggerActions
  Map
  (error [_ data] (if test-verbose? (println data)))
  (exception [_ data] (if test-verbose? (println data))))

(deftest API-responses

  (let [exception-message-keys [:type :extra-data :message :cause]
        code #(first (keys %))
        data #(first (vals %))]


    (testing "Method returns 200 status code when is valid"

      (let [response (builder/try-ok-or-400 (+ 1 1))]

        (is (= (code response) 200))))


    (testing "Method returns 400 status code when exception occurs"

      (let [response (builder/try-ok-or-400 (try
                                              (/ 1 0)
                                              (catch Exception e (throw (ex-info "Error" {} e)))))]

        (is (= (code response) 400))))


    (testing "Method returns 400 status code and a valid exception map when exception occurs"

      (let [response (builder/try-ok-or-400 (try
                                              (/ 1 0)
                                              (catch Exception e (throw (ex-info "Error" {} e)))))]

        (are [expected result] (= expected result)

                               400 (code response)
                               exception-message-keys (keys (data response)))))


    (testing "Method returns 404 status code when there is no result"

      (let [response (builder/try-ok-or-404 nil)]

        (is (= 404 (code response)))))))