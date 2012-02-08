(defproject hdom "1.0.2"
  :description "Manipulate hiccup data."
  :author "Naitik Shah <n@daaku.org>"
  :url "https://github.com/nshah/hdom.clj"
  :repl-init hdom.repl
  :checksum-deps true
  :exclusions [org.clojure/clojure]
  :dependencies
    [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies
    [[auto-reload "1.0.2"]
     [lein-marginalia "0.7.0-20111019.122151-1"]
     [org.clojure/tools.logging "0.2.3"]
     [vimclojure/server "2.3.1"]])
