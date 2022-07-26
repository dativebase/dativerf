(ns dativerf.utils.igt
  "IGT Utilities.

  IGT is 'interliner-gloss text'. It is a method for displaying linguistic data
  where multiple representations of each word are aligned into columns in order
  to make it easier for the reader to see a words and its analysis, for example,
  or its literal translation. The simplest use case is for a sentence in
  language A and its literal translation into language B:

    les chiens dorment
    the dogs   (they)sleep

  Notice above how each French word is aligned with its literal English
  translation.

  When there are more types of representation and the sentences are longer,
  things get more complicated. We want successive lines to be progressively
  indented. For example, consider a longer sentence with three representations:
  an orthographic transcription line, a morphemic breakdown line, and a
  morpheme-by-morpheme gloss line:

    Les    chiens  qui     étaient     vraiment fatigués   et  qui
    le-s   chien-s qui     ét-ai-ent   vraiment fatigu-é-s et  qui
    DET-PL dog-PL  REL.PRO be-IMPF-3PL really   tire-PP-PL and REL.PRO

      avaient       mangé       tous leur     dîners    étaient     par terre
      av-ai-ent     mang-é      tous leur     dîner-s   ét-ai-ent   par terre
      have-IMPF-3PL eat-3SG.PRS all  POSS.3PL dinner-PL be-IMPF-3PL LOC land

        à  l'extérieur en train         de dormir    tranquillement.
        à  l=extérieur en train         de dormir    tranquille-ment
        at DET=outside in the.course.of of sleep.INF calm-ADVZ

    'The dogs that were really tired and had eaten all their dinners were on the
     ground outside sleeping quietly.'

  In the above there are 9 lines of representation, grouped into 3 logical
  rows. Notice how the first three lines (belonging to row 1) have no
  indentation, while the next set of three lines (belonging to row 2) are
  indented at the same level, and the last row is even further indented.

  Note also that the words are aligned into columns by row but that rows across
  columns do NOT align; for example, 'chiens' and 'avaient' are **not**
  horizontally aligned.

  The logic in this namespace culminates in a pure public function, `igt-data`,
  which takes a form as input and returns a sequence of maps, each representing
  an IGT line. The concepts of lines and rows, as described above, is used
  throughout the implementation."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [dativerf.specs.common :as common]))

(def ^:private default-max-row-length 40)
(def ^:private default-step 2)
(def ^:private default-max-indent 20)

(def ^:private igt-keys
  "These are the form keys that can be used in the IGT representation. We only
   use those with non-empty values."
  [:narrow-phonetic-transcription
   :phonetic-transcription
   :transcription
   :morpheme-break
   :morpheme-gloss])

(def ^:private grammaticality-fields-rank
  "These are the fields (keys) to which the grammaticality value should be
  prefixed, in order. The first field with a non-empty value receives the
  grammaticality value."
  [:transcription
   :narrow-phonetic-transcription
   :phonetic-transcription
   :morpheme-break
   :morpheme-gloss])

(def ^:private igt-sorter
  "A map from IGT keys to indices, for sorting IGT lines correctly"
  (->> (map vector igt-keys (range)) (into {})))

(defn- equalize-word-counts
  "Takes a sequence of sequences of words, e.g., [[a dog] [un]] and returns that
   same sequence where each sequence of words has the same number of words. If a
   sequence has fewer words than the longest sequence, append sufficient
   empty-string words to make up the difference. The output for the example
   input would be ((a dog) (un ''))."
  [coll-of-word-colls]
  (let [max-word-count (apply max (map count coll-of-word-colls))]
    (for [word-coll coll-of-word-colls]
      (concat word-coll (repeat (- max-word-count (count word-coll)) "")))))

(defn- split-into-words [s] (-> s str/trim (str/split #"\s+")))

(defn- analyze-word-set
  "Given an arbitrary number of word (string) parameters, e.g., 'faz' 'faz'
  'do.3SG.PRS', return a map containing the input words under :items. The map's
  :length key holds the length of the longest word among the inputs. The output
  from the example input would be {:length 10 :items (faz faz do.3SG.PRS)}."
  [& words]
  (let [max-word-count (apply max (map count words))]
    {:length max-word-count
     :items words}))

(defn- group-words-into-rows
  "Given configuration and a sequence of analyzed words, return a sequence of
   rows. Each row contains the keys :indent and :items. The indent indicates how
   far the row should be indented from the left margin. The items are a sequence
   of words. Consider the following words input:

     ({:length 6 :items (le-s DET-PL)}
      {:length 7 :items (chien-s dog-PL)})

   With a max row length of 8, the return value would be as follows, since only
   one word can fit on each row:

     [{:indent 0 :items [{:length 6 :items (le-s DET-PL)}]}
      {:indent 5 :items [{:length 7 :items (chien-s dog-PL)}]}]

   With a larger max row length, the return value would consist of one row:

     [{:indent 0 :items [{:length 6 :items (le-s DET-PL)}
                         {:length 7 :items (chien-s dog-PL)}]}]
  "
  [{:keys [max-row-length step max-indent]} words]
  (let [{:keys [rows current-row]}
        (reduce
         (fn [{:as agg
               {:as current-row :keys [indent] current-row-items :items}
               :current-row} {:as word-meta :keys [length]}]
           (if (empty? current-row-items)
             (update-in agg [:current-row :items] conj word-meta)
             (let [projected-length (+ length
                                       indent
                                       (apply + (map :length current-row-items)))]
               (if (> projected-length max-row-length)
                 (-> agg
                     (update :rows conj current-row)
                     (assoc :current-row {:indent (min max-indent (+ indent step))
                                          :items [word-meta]}))
                 (update-in agg [:current-row :items] conj word-meta)))))
         {:current-row {:indent 0 :items []}
          :rows []}
         words)]
    (conj rows current-row)))

(def ^:private formatters
  {:morpheme-break (fn [mb] (str "/" mb "/"))
   :narrow-phonetic-transcription (fn [mb] (str "[" mb "]"))
   :phonetic-transcription (fn [mb] (str "[" mb "]"))})

(defn- format-form-entry [[k v]] ((get formatters k identity) v))

(defn- format-form-entries [form]
  (->> form (map (juxt key format-form-entry)) (into {})))

(defn- igt-fields
  "Given a valid form map, return the sorted keys and values of its IGT-relevant
   fields. The return value is a 2-ary vector containing the keys and values.
   The first element is a sorted vec of keywords. These are the non-empty keys
   from the form that will be represented in the IGT display. The second element
   is a vec in the same order. It consists of the (string) values of the keys.
   The grammaticality value is prefixed to the appropriate value. Sample input:

     {:transcription chiens
      :morpheme-break chien-s
      :morpheme-gloss dog-PL
      :grammaticality *}

   Sample output:

     [[:transcription :morpheme-break :morpheme-gloss] [*chiens chien-s dog-PL]]
  "
  [{:keys [grammaticality] :as form}]
  (let [non-empty-igt-form (->> (select-keys form igt-keys)
                                (filter (comp seq val))
                                (into {}))
        grammaticality-field (some (set (keys non-empty-igt-form))
                                   grammaticality-fields-rank)]
    (->> (update non-empty-igt-form
                 grammaticality-field
                 (partial str grammaticality))
         format-form-entries
         (sort-by (comp igt-sorter key))
         (reduce (fn [[ks vs] [k v]] [(conj ks k) (conj vs v)])
                 [[] []]))))

(defn- split-row-into-lines
  "Given a vec of IGT keys, a row index, and a row map, return a sequence of
   line maps. Each line map is about a single form key, e.g., :morpheme-break.
   Example input might be:

     [:morpheme-break :morpheme-gloss]
     0
     {:indent 0 :items [{:length 6 :items (le-s DET-PL)}
                        {:length 7, :items (chien-s dog-PL)}]}

   Since there are only two form attributes in this row, the output for the
   above is the following sequence of two lines:

     ({:key :morpheme-break :indent 0 :row 0
       :words [{:length 6 :word le-s} {:length 7 :word chien-s}]}
      {:key :morpheme-gloss :indent 0 :row 0
       :words [{:length 6, :word DET-PL} {:length 7, :word dog-PL}]})
  "
  [igt-keys row-index {:keys [items indent]}]
  (->> items
       (reduce
        (fn [lines {:keys [length] word-parts :items}]
          (reduce
           (fn [agg [igt-key word]]
             (update-in agg [igt-key :words] conj {:length length :word word}))
           lines
           (zipmap igt-keys word-parts)))
        (zipmap igt-keys
                (map (fn [k] {:key k :indent indent :row row-index :words []})
                     igt-keys)))
       vals
       (sort-by igt-sorter)))


(s/def :word/word ::common/non-blank-string)
(s/def ::length pos-int?)
(s/def ::word (s/keys :req-un [::length
                               :word/word]))
(s/def ::words (s/coll-of ::word :min-count 1))
(s/def ::row nat-int?)
(s/def ::key #{:transcription
               :narrow-phonetic-transcription
               :phonetic-transcription
               :morpheme-break
               :morpheme-gloss})
(s/def ::indent nat-int?)
(s/def ::line
  (s/keys :req-un [::key
                   ::indent
                   ::row
                   ::words]))
(s/def ::igt-data (s/coll-of ::line :min-count 1))
(def valid-igt-data? (partial s/valid? ::igt-data))

(defn igt-data
  "Given a form, return IGT data. The return value is a valid ::igt-data. It is
   an ordered list of line maps. Each line map should have sufficient
   information to render (in isolation) a single non-wrapping line of IGT. Each
   line contains keys for the :key (i.e., the field, e.g., :transcription) how
   much to :indent the line, which :row the line is a part of, and the sequence
   of :words in the line. Each word has keys for :length (int) and a :word
   (str). Example output for form {:transcription the}:

     ({:key :transcription
       :indent 0
       :row 0
       :words [{:length 3, :word the}]})

  The optional second argument is a map of options to control the max-row-length,
  the step (how much to indent each successive row), and the max-indent (the
  point at which we stop indenting). All values are integers and are presumed to
  represent some typographic unit, likely em."
  ([form] (igt-data form {}))
  ([form {:keys [max-row-length step max-indent]
          :or {max-row-length default-max-row-length
               step default-step
               max-indent default-max-indent}}]
   (let [[ks vs] (igt-fields form)]
     (->> vs
          (map split-into-words)
          equalize-word-counts
          (apply map analyze-word-set)
          (group-words-into-rows {:max-row-length max-row-length
                                  :step step
                                  :max-indent max-indent})
          (mapcat (partial split-row-into-lines ks) (range))))))
