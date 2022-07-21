(ns dativerf.db)

(def always-visible-form-fields
  #{:form/transcription
    :form/grammaticality
    :form/translations
    :form/id})

(def default-form-state
  {:form/narrow-phonetic-transcription {:visible? false}
   :form/phonetic-transcription {:visible? false}
   :form/transcription {:visible? true}
   :form/grammaticality {:visible? true}
   :form/morpheme-break {:visible? true}
   :form/morpheme-gloss {:visible? true}
   :form/translations {:visible? true}
   :form/comments {:visible? true}
   :form/speaker-comments {:visible? true}
   :form/elicitation-method {:visible? true}
   :form/tags {:visible? true}
   :form/syntactic-category {:visible? true}
   :form/syntactic-category-string {:visible? true}
   :form/break-gloss-category {:visible? true}
   :form/date-elicited {:visible? true}
   :form/speaker {:visible? true}
   :form/enterer {:visible? true}
   :form/modifier {:visible? true}
   :form/elicitor {:visible? true}
   :form/verifier {:visible? false}
   :form/datetime-entered {:visible? true}
   :form/datetime-modified {:visible? true}
   :form/files {:visible? true}
   :form/source {:visible? true}
   :form/syntax {:visible? true}
   :form/semantics {:visible? true}
   :form/status {:visible? true}
   :form/uuid {:visible? true}
   :form/id {:visible? true}})

(def default-new-form-state
  {:new-form/narrow-phonetic-transcription ""
   :new-form/phonetic-transcription ""
   :new-form/transcription ""
   :new-form/grammaticality ""
   :new-form/morpheme-break ""
   :new-form/morpheme-gloss ""
   :new-form/translations [{:transcription "" :grammaticality ""}]
   :new-form/comments ""
   :new-form/speaker-comments ""
   :new-form/elicitation-method nil
   :new-form/tags #{}
   :new-form/files #{}
   :new-form/syntactic-category nil
   :new-form/date-elicited nil
   :new-form/speaker nil
   :new-form/elicitor nil
   :new-form/verifier nil
   :new-form/source nil
   :new-form/syntax ""
   :new-form/semantics ""
   :new-form/status "tested"})

(def default-visible-form-fields
  (->> default-form-state (filter (comp :visible? val)) (map key) set))

(def default-db
  (-> {:name "Dative"
       :active-route {:handler :home}
       :olds [] ;; the OLDs from servers.json from Dative static
       :old nil
       :old-states {} ;; the cache of OLD-specific state: forms, resources, form-specific view state, etc.
       :user nil
       :system/error nil
       ;; settings state
       :settings/active-tab :server
       ;; edited settings state
       :settings/edited-settings-state :dativerf.fsms.edit-settings/ready
       :settings/edited-settings {}
       ;; login state
       :login/username ""
       :login/password ""
       :login/state :dativerf.fsms.login/is-ready
       :login/invalid-reason nil
       :login/retries 0
       ;; forms browse state
       :forms-paginator/items-per-page 10
       :forms-paginator/current-page-forms []
       :forms-paginator/current-page 1
       :forms-paginator/last-page nil
       :forms-paginator/count 0
       :forms-paginator/first-form 0
       :forms-paginator/last-form 0
       :forms-paginator/cache {}
       :forms/labels-on? false
       :forms/expanded? false
       :forms/export-interface-visible? false
       :forms/settings-interface-visible? false
       :forms/settings-field-visibility-interface-visible? false
       :forms/visible-fields default-visible-form-fields
       :forms/export-format :plain-text
       :forms/new-form-interface-visible? false
       :forms/new-form-secondary-fields-visible? false
       :forms/form-to-delete nil
       :forms/force-reload? false
       :forms-page/retries 0
       ;; routing state
       :forms/previous-route nil
       :forms/previous-browse-route nil
       :old-settings/previous-route nil
       ;; new form data
       :new-form-state :dativerf.fsms.new-form/ready
       :new-form nil
       :new-form-field-specific-validation-error-messages {}
       :new-form-general-validation-error-message nil}
      (merge default-new-form-state)))

(defn default-form-view-state [{:as _db :keys [forms/expanded?]}]
  {:edit-field-specific-validation-error-messages {}
   :edit-fsm-state :dativerf.fsms.edit-form/ready
   :edit-general-validation-error-message nil
   :edit-interface-visible? false
   :edit-secondary-fields-visible? false
   :edit-state {}
   :expanded? expanded?
   :export-interface-visible? false
   :export-format :plain-text})

(defn soft-reset-dative-state
  "Reset state to default but keep our OLDs so we can login again."
  [{:keys [olds]}]
  (if (seq olds)
    (assoc default-db :olds olds)
    default-db))
