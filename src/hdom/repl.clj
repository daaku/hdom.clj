(ns hdom.repl
  "repl helpers"
  {:author "Naitik Shah"}
  (:require
    [clojure.string]
    [clojure.tools.logging]
    [auto-reload.core :only [auto-reload]])
  (:use
    [hdom.core]))

(auto-reload.core/auto-reload ["src"])
