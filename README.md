# Real-Time Fraud Detection System

A backend system to detect and flag fraudulent financial transactions using Java, Kafka, Hadoop MapReduce, and HBase.

## 🚀 Features
- Simulates real-time transactions via Kafka producer
- Consumes and analyzes transaction streams with fraud detection logic
- Processes batch data using Hadoop MapReduce to aggregate transaction data
- Alerts on suspicious transactions (e.g., amount:1000)
- (Optional) Store flagged transactions in HBase or any persistent store

## 🧱 Tech Stack
- Java 11
- Apache Kafka
- Apache Hadoop (MapReduce)
- HBase (optional)
- Spring Boot (optional for API endpoints)
- Git & Maven

## 📁 Folder Structure

real-time-fraud-detection-system/
├── producer/ # Kafka Producer
├── consumer/ # Kafka Consumer + Fraud Detector
├── mapreduce/ # Hadoop MapReduce Job
├── README.md # Project Documentation






