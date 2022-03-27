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
(defglobal defaults
  (at-font-face {:font-family "GentiumPlus"}
                {:src "url(./vendor/fonts/GentiumPlus-R.woff)"})
  (at-font-face {:font-family "GentiumPlus"
                 :font-style "italic"}
                {:src "url(./vendor/fonts/GentiumPlus-I.woff)"})
  [:body {:color :black}])

(declare level1)
(defclass level1 []
  {:color :black})

(declare attr-label)
(defclass attr-label []
  {:width "180px"
   :text-overflow "ellipsis"
   :white-space "nowrap"
   :overflow "hidden"
   :color :gray})

(declare objlang)
(defclass objlang []
  {:font-family "'GentiumPlus', serif"
   :font-size "14pt"})

(declare form)
(defclass form []
  {:font-family "'GentiumPlus', serif"
   :font-size "14pt"
   :width "660px"
   :cursor "pointer"})

(declare widget)
(defclass widget []
  {:color :red})
