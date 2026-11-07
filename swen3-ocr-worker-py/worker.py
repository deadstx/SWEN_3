import io
import json
import os
import time

import boto3
import pika
import pytesseract
from botocore.config import Config
from pdf2image import convert_from_bytes

RABBIT_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBIT_USER = os.getenv("RABBITMQ_USER", "guest")
RABBIT_PASS = os.getenv("RABBITMQ_PASS", "guest")
OCR_QUEUE = "ocr.queue"
INDEX_QUEUE = "index.queue"

s3 = boto3.client(
    "s3",
    endpoint_url=os.getenv("STORAGE_ENDPOINT", "http://localhost:9000"),
    aws_access_key_id=os.getenv("STORAGE_ACCESS_KEY", "paperlessadmin"),
    aws_secret_access_key=os.getenv("STORAGE_SECRET_KEY", "paperless-local-secret"),
    region_name="us-east-1",
    config=Config(s3={"addressing_style": "path"}),
)


def extract_text(pdf_bytes: bytes) -> str:
    pages = convert_from_bytes(pdf_bytes)
    return "\n".join(pytesseract.image_to_string(page, lang="deu+eng") for page in pages)


def on_message(channel, method, properties, body):
    try:
        job = json.loads(body)
        print(f"OCR job: documentId={job['documentId']}, key={job['objectKey']}", flush=True)

        obj = s3.get_object(Bucket=job["bucket"], Key=job["objectKey"])
        text = extract_text(obj["Body"].read())

        channel.basic_publish(
            exchange="",
            routing_key=INDEX_QUEUE,
            body=json.dumps({
                "documentId": job["documentId"],
                "fileName": job["fileName"],
                "text": text,
            }),
            properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        )
        print(f"OCR done: documentId={job['documentId']}, {len(text)} characters", flush=True)
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as exc:
        print(f"OCR failed: {exc}", flush=True)
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
    channel.queue_declare(queue=OCR_QUEUE, durable=True)
    channel.queue_declare(queue=INDEX_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=OCR_QUEUE, on_message_callback=on_message)
    print("OCR worker waiting for messages...", flush=True)
    channel.start_consuming()


if __name__ == "__main__":
    main()