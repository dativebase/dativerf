(ns dativerf.styles
  #_(:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.stylesheet :refer [at-font-face]]
    #_[garden.units :refer [deg px]]
    #_[garden.color :refer [rgba]]
    #_[garden.core :as garden]))

;; Declares here just to make clj-kondo linter happy ...
(declare defaults)
(declare level1)
(declare widget)

(defglobal defaults
  (at-font-face {:font-family "GentiumPlus"}
                {:src "url(./vendor/fonts/GentiumPlus-R.woff)"})
  (at-font-face {:font-family "GentiumPlus"
                 :font-style "italic"}
                {:src "url(./vendor/fonts/GentiumPlus-I.woff)"})
  [:body {:color :black}])

(defclass level1 []
  {:color :black})

(defclass attr-label []
  {:width "180px"})

(defclass objlang []
  {:font-family "'GentiumPlus', serif"
   ;; :line-height "0.5em"
   :font-size "16pt"})

(defclass widget []
  {:color :red})
