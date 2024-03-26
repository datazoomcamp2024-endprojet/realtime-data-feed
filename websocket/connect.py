import os

os.system("pip install websocket-client confluent-kafka")
from websocket import create_connection, WebSocketConnectionClosedException
from confluent_kafka import Producer
import json
import time
import argparse


def main(params):
    config = read_config(params)
    topic = "orders_btc"

    # creates a new producer instance
    producer = Producer(config)


    ws = create_connection("wss://ws-feed.exchange.coinbase.com")
    ws.send(
        json.dumps(
            {
                "type": "subscribe",
                "product_ids": ["BTC-USD"],
                "channels": ["matches"],
            }
        )
    )

    try:
        while True:
            data_raw = ws.recv()
            data_json = json.loads(data_raw)

            if "price" in data_json:
                key = data_json["side"]
                time.sleep(1)
                producer.produce(topic, key=key, value=json.dumps(
                {
                    "size":data_json["size"],
                    "time":data_json["time"], 
                    "trade_id":data_json["trade_id"],
                    "side":data_json["side"], 
                    "price":data_json["price"]}))
                print(f"Produced message to topic {topic}: key = {key} value = {data_json}")
            
        
    except Exception as inst:
        raise inst
    finally:
        producer.flush()


def read_config(params):
  return {
    "bootstrap.servers": params.bootstrap_servers,
    "security.protocol":"SASL_SSL",
    "sasl.mechanisms":"PLAIN",
    "sasl.username":params.cluster_key,
    "sasl.password":params.cluster_secret
  }


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Subscribe to coinbase websocket and publish to kafka.')

    parser.add_argument('--bootstrap_servers',  help='kafka bootstrap servers ( use your confluent cloud or local server)', required=True)
    parser.add_argument('--cluster_key',  help='kafka cluster key ( use your confluent cloud if needed)')
    parser.add_argument('--cluster_secret',  help='kafka cluster secret ( use your confluent cloud if needed)', required=True)
    args = parser.parse_args()
    main(args)


