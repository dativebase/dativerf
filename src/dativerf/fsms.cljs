(ns dativerf.fsms)

(defn update-state
  [db state-machine current-state-key event]
  (let [current-state (current-state-key db)]
    (if-let [new-state (get-in state-machine [current-state event])]
      (assoc db current-state-key new-state)
      db)))
