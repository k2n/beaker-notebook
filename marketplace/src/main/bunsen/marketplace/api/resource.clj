(ns bunsen.marketplace.api.resource
  (:require [liberator.core :refer [defresource]]
            [bunsen.marketplace.helper.resource :as resource]
            [bunsen.marketplace.api.domain :as domain]
            [bunsen.marketplace.api.models.datasets :as datasets]
            [bunsen.marketplace.api.models.categories :as categories]
            [bunsen.marketplace.api.models.subscriptions :as subscriptions]))

(defresource status [_ _] resource/defaults
  :handle-ok domain/get-status)

(defresource categories [config _] resource/defaults
  :allowed? (some-fn resource/get? (partial resource/admin? config))
  :allowed-methods #{:get :post}
  :handle-ok #(categories/get-with-params config (resource/get-params %))
  :post! (partial resource/pass-body categories/create-bulk config))

(defresource seed-datasets [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods #{:post}
  :processable? (partial resource/pass-body datasets/create-datasets config))

(defresource seed-subscriptions [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods [:delete]
  :delete! (subscriptions/retract-all-subscriptions! (:conn request)))

(defresource datasets [config {:keys [index-name]}] resource/defaults
  :allowed? (some-fn resource/get? (partial resource/admin? config))
  :allowed-methods #{:get :post}
  :post! #(datasets/create-dataset config index-name (resource/get-body %))
  :handle-ok #(datasets/find-matching config index-name (dissoc (resource/get-params %) :index-name)))

(defresource dataset [config  {:keys  [index-name id]}] resource/defaults
  :allowed? (some-fn resource/get? (partial resource/admin? config))
  :allowed-methods #{:put :delete :get}
  :delete! (fn [_] (datasets/delete-dataset config index-name id))
  :put! #(datasets/update-dataset config index-name id (resource/get-body %))
  :handle-ok (datasets/get-dataset (:db request) config index-name id))

(defresource refresh [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods #{:put}
  :put! (partial resource/pass-body domain/refresh-index config))

(defresource counts [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods #{:put}
  :put! (partial resource/pass-body domain/update-counts config))

(defresource mappings [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods #{:put}
  :put! (partial resource/pass-body domain/update-mappings config))

(defresource indices [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :allowed-methods #{:post :get}
  :handle-ok (partial domain/get-indicies config)
  :post! (partial resource/pass-body domain/create-index config))

(defresource formats [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :handle-ok (domain/get-formats config))

(defresource tags [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :handle-ok (domain/get-tags config))

(defresource vendors [config _] resource/defaults
  :allowed? (partial resource/admin? config)
  :handle-ok (domain/get-vendors config))

(defresource default [_ _] resource/defaults
  :exists? (constantly false))

(defresource subscription [config {:keys [index-name data-set-id]}] resource/defaults
  :allowed-methods #{:post :put :delete}
  :put! (fn [_] (let [user-id (-> request :session :id)]
          (subscriptions/subscribe (:conn request) index-name data-set-id user-id)))
  :delete! (fn [_] (let [user-id (-> request :session :id)]
             (subscriptions/unsubscribe (:conn request) index-name data-set-id user-id))))

(defresource subscriptions [config _] resource/defaults
  :allowed-methods #{:get}
  :handle-ok (fn [_] (let [user-id (-> request :session :id)]
                       (subscriptions/get-subscriptions config (:db request) user-id))))