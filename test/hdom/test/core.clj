(ns hdom.test.core
  "Test hdom functionality."
  {:author "Naitik Shah"}
  (:use
    [hdom.core :only [reduce-elements add-class]]
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

(deftest reduce-elements-multiple-complex
  (let [s-content "hello"
        input [[:h1#i.y.z] [:em#d.e [:strong.f s-content]]]
        [memo elements] (reduce-elements nil input el-identity)
        [h1 h1-attrs] (first elements)
        [em em-attrs em-content] (second elements)
        [[strong strong-attrs strong-content]] em-content]
    (is (= nil memo))
    (is (= "h1" h1))
    (is (= "i" (:id h1-attrs)))
    (is (= "y z" (:class h1-attrs)))
    (is (= "em" em))
    (is (= "d" (:id em-attrs)))
    (is (= "e" (:class em-attrs)))
    (is (= "strong" strong))
    (is (= nil (:id strong-attrs)))
    (is (= "f" (:class strong-attrs)))
    (is (= s-content (first strong-content)))))

(deftest add-new-class
  (let [input [:a#b.c.d]
        [tag attrs content] (add-class input "e")]
    (is (= "c d e" (:class attrs)))))
