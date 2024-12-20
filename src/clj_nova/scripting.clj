(ns clj-nova.scripting
  (:require
   [cheshire.core :as json]
   [cognitect.aws.client.api :as aws]
   [taoensso.timbre :as log])
  (:refer-clojure :exclude [send]))

(def models {:micro  "amazon.nova-micro-v1:0"
             :lite   "amazon.nova-lite-v1:0"
             :pro    "amazon.nova-pro-v1:0"})

(def default-system-prompt
  "You are extremely helpful, but concise assistent. All your responses are based on facts, you never make things up. If unsure, you admit your unability to answer due to not enough facts available to you.")

(def default-config
  {:model                 (:micro models)
   :max-tokens            256
   :system-prompt         default-system-prompt
   ;:region                (System/getenv "AWS_REGION")
   :region                "us-east-1"}) ; Nova is only available in us-east-1 for now

(def default-client-args {:api :bedrock-runtime})

(defn ->request
  "Formats the request for anthropic messages API"
  [messages & {:keys [region model max-tokens system-prompt _tools] :as _config}]
  {:op      :InvokeModel
   :request {:modelId     model
             :contentType "application/json"
             :accept "application/json"
             :body {:inferenceConfig {:max_new_tokens max-tokens}
                    :system [{:text system-prompt}]
                    :messages  (if (vector? messages)
                                 messages
                                 [messages])}}
   :region   region})

(defn ->user-messages
  "Wraps a string or [strings] as messages by user (human)"
  [messages-as-strings]
  (if (string? messages-as-strings)
    (->user-messages [messages-as-strings])
    (let [messages {:role "user"
                    :content (->> messages-as-strings
                               (map (fn [m] {:text m}))
                               (into []))}]
      (if (:cache (meta messages-as-strings))
        (update-in messages [:content (dec (count messages-as-strings))] assoc :cache_control {:type :ephemeral})
        messages))))


(defn send
  "Sends formated request, parses response. Just a wrap around curl and cheeshire"
  [{:keys [region] :as request}]
  (let [client            (aws/client (merge default-client-args {:region region}))
        formatted-request (-> request
                              (dissoc :region)
                              (update-in
                                [:request :body]
                                #(->> %
                                   (into {} (remove (comp nil? val)))
                                   json/generate-string)))
        response          (aws/invoke client formatted-request)
        {:keys [body]}    response]
    (if body
      (-> body slurp (json/parse-string keyword))
      (do
        (log/error :api-error {:request request :response response :formatted-request formatted-request})
        {}))))

(defn ->first-string
  "Unwraps the first string response, usually just what you need"
  [response]
  (-> response :output :message :content first :text))



(comment

  (-> "who are you?"
    ->user-messages
    (->request default-config)
    send
    ->first-string
    println)

  ;; => "I'm an AI system build by a team of inventors at Amazon..."

  (def randomai
    (comp
      println
      ->first-string
      send
      #(->request %
         (assoc default-config
           :system-prompt "You are a poet in shakespeare's times"))
      ->user-messages))

  (randomai "write a poem with the subject of cider")
  
  ;; => Oh, cider, drink og lords and commoners,
  ;;    In every sip, a taste of nature's treasures
  ;;    ...

  comment)


