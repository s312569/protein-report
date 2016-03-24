(ns protein-report.core
  (:require [clj-tandem.core :as xt]
            [me.raynes.fs :as fs]
            [clojure.java.io :refer [reader writer]]
            [clojure.string :as st]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; cli
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  [["-i" "--in PATH" "Path to in X! Tandem xml formatted result file."
    :parse-fn #(fs/absolute (fs/file %))
    :validate [#(fs/exists? %)
               "In file list does not exist."]
    :default nil]
   ["-h" "--help" "Print help message."]])

(defn usage [options-summary]
  (->> ["Outputs a protein and peptide report in csv format from a X! Tandem xml result file."
        ""
        "Usage: protein-report [options]"
        ""
        "Options:"
        options-summary
        ""]
       (st/join \newline)))

(defn error-msg [errors]
  (str "Error:\n"
       (->> errors
            (interpose \newline)
            (apply str))))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; utilities
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-name
  [f ext]
  (-> (fs/file (fs/parent f)
               (str (fs/name f) ext))
      str))

(defn to-file
  [file func w]
  (with-open [r (reader file)]
    (doseq [l (func r)]
      (.write w (str l \newline)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options)
      (exit 0 (usage summary))
      errors
      (exit 1 (error-msg errors)))
    ;; Execute program with options
    (with-open [proto (writer (new-name (:in options) "-prot.csv"))
                pepo (writer (new-name (:in options) "-pep.csv"))]
      (to-file (:in options) xt/protein-report proto)
      (to-file (:in options) xt/peptide-report pepo))))

