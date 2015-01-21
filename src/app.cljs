(ns app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-utils.core :refer [by-id listen]]
            [cljs.core.async :as async :refer [<! timeout]]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def ctg (aget js/React "addons" "CSSTransitionGroup"))

(defonce app-state (atom {:title "PageSlider"
                          :view :home
                          :pages [{
                                   :title "Page 1"
                                   :id "one"
                                   :class "page1"
                                   :type :employee
                                   :image "images/avatar.png"
                                   :name "Susan Smith"}
                                  {
                                   :title "Page 2"
                                   :id "two"
                                   :class "page2"
                                   :type :logo
                                   :image "images/react.png"}
                                  ]}))

(defn define-routes [data]
  
  (defroute "/home" {:as params}
    (om/update! data :view :home))
  
  (defroute "/pages/:id" [id]
    (om/update! data :view id)))

(defn header
  "Om component for header"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "header")
    om/IRender
    (render [_]
      (dom/header #js {:className "bar bar-nav"}
                  (when (:id data) (dom/a #js {:href "#"
                                               :className "icon icon-left-nav pull-left"
                                               :onClick #(secretary/dispatch! "/home")}))
                  (dom/h1 #js {:className "title"} (:title data))))))

(defn employee
  "Om component for employee page"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tour-item")
    om/IRender
    (render [_]
      (dom/div #js {:className (str "page " (:class data))}
               (om/build header data)
               (dom/div #js {:className "content"}
                        (dom/div #js {:className "card"}
                                 (dom/ul #js {:className "table-view"}
                                         (dom/li #js {:className "table-view-cell media"}
                                                 (dom/a nil (dom/img #js {:className "media-object pull-left"
                                                                          :src (:image data)})
                                                        (dom/div {:className "media-body"} (:name data)))))))))))

(defn logo
  "Om component for logo page"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tour-item")
    om/IRender
    (render [_]
      (dom/div #js {:className (str "page " (:class data))}
               (om/build header data)
               (dom/div #js {:className "content"}
                        (dom/img #js {:src (:image data)}))))))

(defmulti page (fn [data _] (:type data)))

(defmethod page :employee
  [data owner] (employee data owner))

(defmethod page :logo
  [data owner] (logo data owner))

(defn page-list-item
  "Om component for list item"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "tours-view-item")
    om/IRender
    (render [_]
      (dom/li #js {:className "table-view-cell media"
                   :onClick #(secretary/dispatch! (str "/pages/" (:id data)))
                   :style #js {:cursor "pointer"}}
              (:title data) ))))

(defn pages
  "Om component for pages"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "pages")
    om/IRender
    (render [_]
      (dom/div #js {:className "page"}
               (om/build header data)
               (dom/div #js {:className "content"}
                        (apply dom/ul #js {:className "table-view"}
                               (om/build-all page-list-item (:pages data) {:key :id})))))))

(defn home
  "Om component home"
  [data owner]
  (reify
    om/IDisplayName
    (display-name [this]
      "home")
    om/IRender
    (render [_]
      (let [view (:view data)]
        (ctg #js {:transitionName "slider"}
             (condp = view
               :home (om/build pages data)
               (om/build page (some #(when (= (:id %) view) %) (:pages data)) {:key :id})))))))

(defn app [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (define-routes data)
      (.initializeTouchEvents js/React true))
    om/IRender
    (render [this]
      (om/build home data))))

(om/root app app-state
         {:target (by-id "container")})
