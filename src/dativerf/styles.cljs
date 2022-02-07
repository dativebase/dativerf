(ns dativerf.styles
  #_(:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    #_[garden.units :refer [deg px]]
    #_[garden.color :refer [rgba]]
    #_[garden.core :as garden]))

;; Declares here just to make clj-kondo linter happy ...
(declare defaults)
(declare level1)
(declare widget)

(defglobal defaults
  [:body {:color :black}])

(defclass level1 []
  {:color :black})

(defclass widget []
  {:color :red})
