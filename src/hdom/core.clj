(ns hdom.core
  "Manipulate hiccup data."
  {:author "Naitik Shah"}
  (:require
    [clojure.tools.logging])
  (:use
    [clojure.string :only [join trim]]))

(defn- as-str [x] (if (instance? clojure.lang.Named x) (name x) (str x)))

(defn gen-id []
  (apply str (repeatedly 5 #(char (rand-nth (concat (range 97 123)))))))

(defn make-class [& args]
  (trim (join " " (map as-str args))))

(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name." :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid tag name."))))
  (let [[_ tag id class] (re-matches re-tag (as-str tag))
        tag-attrs        {:id id
                          :class (if class (.replace ^String class "." " "))}
        map-attrs        (first content)]
    (if (map? map-attrs)
      [tag (merge tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(defn reduce-elements [memo elements f]
  (reduce
    (fn [[memo processed] el]
      (cond
        (vector? el) (let [[memo el] (apply f [memo (normalize-element el)])
                           [tag attrs content] el
                           [memo content] (reduce-elements memo content f)]
                       [memo (conj processed [tag attrs (seq content)])])
        (seq? el) (let [[memo el] (reduce-elements memo el f)]
                    [memo (conj processed (seq el))])
        :else [memo (conj processed el)]))
    [memo []]
    elements))

(defn update-attr [element name f]
  (let [[tag attrs content] (normalize-element element)]
    [tag (assoc attrs name (f (attrs name))) content]))

(defn set-attr [element name value]
  (update-attr element name (fn [_] value)))

(defn dissoc-attr [element name]
  (let [[tag attrs content] (normalize-element element)]
    [tag (dissoc attrs name) content]))

(defn- pad-class [class]
  (str " " class " "))

(def ^{:private true} class-regex
  (memoize (fn [class] (java.util.regex.Pattern/compile (pad-class class)))))

(defn- add-class-string [class existing]
  (str existing " " class))

(defn- remove-class-string [class existing]
  (trim (clojure.string/replace (pad-class existing) (class-regex class) "")))

(defn has-class [element class]
  (let [[tag attrs content] (normalize-element element)]
    (boolean (re-seq (class-regex class) (pad-class (:class attrs))))))

(defn add-class [element class]
  (if (has-class element class)
    element
    (update-attr element :class #(add-class-string class %))))

(defn remove-class [element class]
  (if-not (has-class element class)
    element
    (update-attr element :class #(remove-class-string class %))))
