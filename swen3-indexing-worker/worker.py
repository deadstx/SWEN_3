import json
import os
import time

import pika
from elasticsearch import Elasticsearch

RABBIT_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBIT_USER = os.getenv("RABBITMQ_USER", "guest")
RABBIT_PASS = os.getenv("RABBITMQ_PASS", "guest")
ES_URL = os.getenv("ELASTICSEARCH_URL", "http://localhost:9200")
INDEX_QUEUE = "index.queue"
ES_INDEX = "documents"

es = Elasticsearch(ES_URL)


def on_message(channel, method, properties, body):
    try:
        job = json.loads(body)
        es.index(
            index=ES_INDEX,
            id=job["documentId"],
            document={
                "documentId": job["documentId"],
                "fileName": job["fileName"],
                "text": job["text"],
            },
        )
        print(f"Indexed documentId={job['documentId']}", flush=True)
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as exc:
        print(f"Indexing failed: {exc}", flush=True)
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def connect() -> pika.BlockingConnection:
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    while True:
        try:
            return pika.BlockingConnection(
                pika.ConnectionParameters(RABBIT_HOST, credentials=credentials)
            )
        except pika.exceptions.AMQPConnectionError:
            print("RabbitMQ not ready, retrying in 5s...", flush=True)
            time.sleep(5)


def main():
    connection = connect()
    channel = connection.channel()
    channel.queue_declare(queue=INDEX_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=INDEX_QUEUE, on_message_callback=on_message)
    print("Indexing worker waiting for messages...", flush=True)
    channel.start_consuming()


if __name__ == "__main__":
    main()