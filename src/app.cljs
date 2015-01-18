(ns app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-utils.core :refer [by-id listen]]
            [cljs.core.async :as async :refer [<! timeout]]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def ctg (aget js/React "addons" "CSSTransitionGroup"))

(defonce app-state (atom {:text "你好世界 from Om"
                          :test ["testing"]
                          :view :tours
                          :tours [{
                                   :title "耶路撒冷旧城"
                                   :id "old-town"
                                   :image "img/via-dolorosa.jpg"
                                   :length ""
                                   :audiofile ""
                                   :coords {:lat 4 :lon 5}}
                                  {
                                   :title "維亞多勒羅沙"
                                   :id "via-dolorosa"
                                   :image "img/via-dolorosa.jpg"
                                   :length ""
                                   :audiofile ""
                                   :coords {:lat 4 :lon 5}}
                                  {
                                   :title "橄榄山"
                                   :id "mount-of-olives"
                                   :image "img/via-dolorosa.jpg"
                                   :length ""
                                   :audiofile ""
                                   :coords {:lat 4 :lon 5}}]}))


(defn define-routes [data]
  
  (defroute "/tours" {:as params}
    (om/update! data :view :tours))
  
  (defroute tour-path "/tours/:id" [id]
    (om/update! data :view id)))

(defn online? [data]
  (let [network-state (.. js/window -navigator -connection -type)]
    (.dir js/console (.-navigator js/window))
    (om/update! data :model (. js/device -model))
    (om/transact! data :status #(str network-state))
    (println network-state)))

(defn header
  "Om component for new header"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "header")
    om/IRender
    (render [_]
      (dom/header #js {:className "header bar bar-nav"}
                  (dom/a {:href "#" :className "icon icon-left-nav pull-left"})
                  (dom/h1 {:className "title"} "Pageslider")))))

(defn tour-item
  "Om component for new tour-item"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tour-item")
    om/IRender
    (render [_]
      (dom/div nil
               (dom/button #js {:onClick #(secretary/dispatch! "/tours")} "Back")
               (dom/p nil (:title data))
               (dom/div nil
                        (dom/img #js {:src (:image data) :width "100%"}))))))

(defn tours-view-item
  "Om component for new tour-item"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tours-view-item")
    om/IRender
    (render [_]
      (dom/li #js {:className "table-view-cell media"}
              (dom/button #js {:onClick #(secretary/dispatch! (str "/tours/" (:id data)))} (:title data)) ))))

(defn tours-view
  "Om component for new list-view"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tours-view")
    om/IRender
    (render [_]
      (apply dom/ul #js {:className "table-view"}
              (om/build-all tours-view-item data {:key :id})))))


(defn transition-group
  [opts component]
  (let [[group-name enter? leave?] (if (map? opts)
                                     [(:name opts) (:enter opts) (:leave opts)]
                                     [opts true true])]
    (ctg
      #js {:transitionName group-name
           :transitionEnter enter?
           :transitionLeave leave?}
      component)))

(defn article
  "Om component for new article"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "article")
    om/IRender
    (render [_]
      (dom/article #js {:className "article content"}
                   (let [view (:view data)]
                     (transition-group "example"
                                       (condp = view
                                         :tours (om/build tours-view (:tours data))
                                         (om/build tour-item (some #(when (= (:id %) view) %) (:tours data)) {:key :id}))))))))

(defn footer
  "Om component for new footer"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "footer")
    om/IRender
    (render [_]
      (dom/footer #js {:className "footer"}
                  (dom/p nil (:text data))                 
                  (dom/p nil (:model data))
                  (dom/p nil (:status data))
                  (dom/p nil (.-userAgent (.-navigator js/window)))
                  (dom/button #js {:onClick (fn [e] (online? data))} "Status")))))

(defn app [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (define-routes data)
      (.initializeTouchEvents js/React true))
     om/IDidMount
     (did-mount [_]
       (let [chan (listen js/document "deviceready")]
         (go
           (let [e (<! chan)]
             (online? data)
             (println (.-type e))))))
    om/IRender
    (render [this]
      (dom/div #js {:className "container"}
               (om/build header data)
               (om/build article data)
               (om/build footer data)))))

(om/root app app-state
         {:target (by-id "container")})
