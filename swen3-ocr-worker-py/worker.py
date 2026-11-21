import logging
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
GENAI_QUEUE = "genai.queue"
RESULT_QUEUE = "genai.result.queue"
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

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


def publish(channel, queue, payload):
    channel.basic_publish(
        exchange="",
        routing_key=queue,
        body=json.dumps(payload),
        properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        mandatory=True,
    )


def on_message(channel, method, properties, body):
    try:
        job = json.loads(body)
        identity = {"documentId": job["documentId"], "objectKey": job["objectKey"]}
        bucket, file_name = job["bucket"], job["fileName"]
    except (ValueError, KeyError, TypeError):
        logger.exception("Invalid OCR job")
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
        return

    logger.info("OCR started for document id=%s", identity["documentId"])
    try:
        obj = s3.get_object(Bucket=bucket, Key=identity["objectKey"])
        with obj["Body"] as stream:
            text = extract_text(stream.read())
    except Exception:
        logger.exception("OCR failed for document id=%s", identity["documentId"])
        publish(channel, RESULT_QUEUE, {**identity, "status": "FAILED"})
    else:
        publish(channel, INDEX_QUEUE, {
            "documentId": identity["documentId"], "fileName": file_name, "text": text,
        })
        publish(channel, GENAI_QUEUE, {**identity, "text": text})
        logger.info("OCR forwarded document id=%s to search and GenAI", identity["documentId"])
    channel.basic_ack(delivery_tag=method.delivery_tag)


def connect() -> pika.BlockingConnection:
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    while True:
        try:
            return pika.BlockingConnection(
                pika.ConnectionParameters(RABBIT_HOST, credentials=credentials, heartbeat=600)
            )
        except pika.exceptions.AMQPConnectionError:
            print("RabbitMQ not ready, retrying in 5s...", flush=True)
            time.sleep(5)


def main():
    connection = connect()
    channel = connection.channel()
    channel.queue_declare(queue=OCR_QUEUE, durable=True)
    channel.queue_declare(queue=INDEX_QUEUE, durable=True)
    for queue in (GENAI_QUEUE, RESULT_QUEUE):
        channel.queue_declare(queue=queue + ".dead", durable=True)
        channel.queue_declare(queue=queue, durable=True, arguments={
            "x-dead-letter-exchange": "", "x-dead-letter-routing-key": queue + ".dead",
        })
    channel.confirm_delivery()
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=OCR_QUEUE, on_message_callback=on_message)
    print("OCR worker waiting for messages...", flush=True)
    channel.start_consuming()


if __name__ == "__main__":
    main()