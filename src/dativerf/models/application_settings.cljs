(ns dativerf.models.application-settings
  (:require [dativerf.specs.application-settings :as spec]
            [dativerf.utils :as utils]
            [clojure.string :as str]))

(defn parse-inventory
  ([inventory] (parse-inventory inventory {}))
  ([inventory names]
   (mapv (fn [graph]
           (let [graph (utils/remove-enclosing-brackets graph)]
             {:graph graph
              :graphemes (utils/unicode-inspect graph names)}))
         (utils/parse-comma-delimited-string inventory))))

(defn parse-punctuation
  ([punctuation] (parse-punctuation punctuation {}))
  ([punctuation names]
   (mapv (fn [character]
           {:graph character
            :graphemes (utils/unicode-inspect character names)})
         (str/replace punctuation #"\s" ""))))

(defn process-fetch-response [response]
  (let [current-application-settings (-> response
                                         last
                                         utils/->kebab-case-recursive)]
    (when-not (spec/application-settings-valid? current-application-settings)
      (throw (ex-info "Invalid Application Settings"
                      {:application-settings current-application-settings
                       :error-code :invalid-application-settings
                       :explain-data (spec/application-settings-explain-data
                                      current-application-settings)})))
    (update current-application-settings :datetime-modified
            utils/parse-datetime-string)))
