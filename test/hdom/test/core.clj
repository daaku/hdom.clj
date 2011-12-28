(ns hdom.test.core
  "Test hdom functionality."
  {:author "Naitik Shah"}
  (:use
    [hdom.core :only [reduce-elements]]
    [clojure.test :only [deftest testing is]]))

(defn- el-identity [memo el] [memo el])

(deftest reduce-elements-identity
  (let [input [[:a]]
        result (reduce-elements nil input el-identity)
        [memo [[tag attrs]]] result]
    (is (= nil memo))
    (is (= "a" tag))
    (is (= nil (:id attrs)))
    (is (= nil (:class attrs)))))

(deftest reduce-elements-with-class-and-id
  (let [input [[:a#i.y.z]]
        result (reduce-elements nil input el-identity)
        [memo [[tag attrs]]] result]
    (is (= nil memo))
    (is (= "a" tag))
    (is (= "i" (:id attrs)))
    (is (= "y z" (:class attrs)))))
