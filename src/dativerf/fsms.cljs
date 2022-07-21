(ns dativerf.fsms)

(defn update-state
  [db state-machine current-state-key event]
  (let [current-state (current-state-key db)]
    (if-let [new-state (get-in state-machine [current-state event])]
      (assoc db current-state-key new-state)
      db)))

(defn update-in-state
  [db state-machine current-state-path event]
  (let [current-state (get-in db current-state-path)]
    (if-let [new-state (get-in state-machine [current-state event])]
      (assoc-in db current-state-path new-state)
      db)))
