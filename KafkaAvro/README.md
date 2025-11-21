# Kafka Avro Order Processing System

A Spring Boot application demonstrating Apache Kafka integration with Avro serialization for reliable order processing. This project implements message production, consumption, retry mechanisms, and dead letter queues (DLQ) for robust event-driven architecture.

## 🏗️ Architecture Overview

This application showcases:
- **Kafka Producer**: Publishes order messages using Avro serialization
- **Kafka Consumer**: Consumes order messages with configurable concurrency
- **Schema Registry**: Manages Avro schema evolution and validation
- **Retry Mechanism**: Configurable retry logic with exponential backoff
- **Dead Letter Queue (DLQ)**: Handles failed messages after retry exhaustion
- **REST API**: Exposes endpoints for order creation

## 📋 Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.6+

## 🚀 Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- **Zookeeper** (port 2181)
- **Kafka Broker** (ports 9092, 29092)
- **Schema Registry** (port 8081)

### 2. Set Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:29092
SPRING_KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081
SPRING_KAFKA_CONSUMER_GROUP_ID=order-service-group

# Application Configuration (Optional - defaults provided)
SERVER_PORT=8080
KAFKA_REPLICATION_FACTOR=1
KAFKA_PARTITIONS=5
KAFKA_RETRY_ATTEMPTS=5
KAFKA_RETRY_BACKOFF_MS=3000
KAFKA_CONSUMER_CONCURRENCY=3
```

### 3. Build and Run Application

```bash
mvn clean compile
mvn spring-boot:run
```

### 4. Test the API

Send a POST request to create an order:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "product": "Laptop",
    "price": 999.99
  }'
```

## 🔧 Configuration Details

### Kafka Topics Configuration

The application automatically creates the following topics:

#### Main Topic: `order`
- **Partitions**: 5 (configurable via `KAFKA_PARTITIONS`)
- **Replication Factor**: 3 (configurable via `KAFKA_REPLICATION_FACTOR`)
- **Purpose**: Primary topic for order messages

#### Dead Letter Queue: `order-dlq`
- **Partitions**: 1 (fixed for simplicity)
- **Replication Factor**: 3 (configurable via `KAFKA_REPLICATION_FACTOR`)
- **Purpose**: Stores messages that failed after all retry attempts

### Partitioning Strategy

- **Producer**: Uses order ID as the message key for consistent partitioning
- **Consumer**: Messages with the same order ID always go to the same partition
- **Benefits**: Ensures message ordering per order ID

### Replication & Fault Tolerance

```yaml
app:
  kafka:
    replication-factor: 3  # Each message replicated across 3 brokers
```

**Replication ensures**:
- High availability
- Data durability
- Fault tolerance against broker failures

### Consumer Configuration

```yaml
app:
  kafka:
    consumer-concurrency: 3  # Number of concurrent consumer threads
```

**Consumer Features**:
- **Group ID**: `order-service-group`
- **Concurrency**: 3 parallel consumer threads
- **Acknowledgment Mode**: `RECORD` (commits after each message)
- **Auto Offset Reset**: `earliest` (start from beginning if no offset)
- **Specific Avro Reader**: Enabled for type-safe deserialization

### Retry Mechanism

```yaml
app:
  kafka:
    retry-attempts: 5        # Maximum retry attempts
    retry-backoff-ms: 3000   # Delay between retries (3 seconds)
```

**Retry Logic**:
1. **Fixed Backoff**: 3-second delay between retries
2. **Maximum Attempts**: 5 retries per message
3. **Retry Conditions**: All exceptions except non-retryable ones
4. **Non-Retryable Exceptions**: 
   - `IllegalArgumentException`
   - `NullPointerException`
   - `ClassCastException`
   - `SerializationException`

**Retry Flow**:
```
Message Processing → Exception → Retry (3s delay) → ... → DLQ (after 5 attempts)
```

### Dead Letter Queue (DLQ)

**Purpose**: Capture messages that cannot be processed after all retry attempts

**DLQ Configuration**:
- **Topic**: `order-dlq`
- **Routing**: Messages sent to partition 0
- **Metadata**: Includes original message key and exception details

**DLQ Trigger Scenarios**:
- Retry exhaustion (5 failed attempts)
- Non-retryable exceptions
- Serialization/Deserialization errors

## 📊 Avro Schema

The application uses Avro for schema evolution and efficient serialization:

```json
{
  "type": "record",
  "name": "Order",
  "namespace": "com.ruhcom.avro",
  "fields": [
    {
      "name": "orderId",
      "type": "string"
    },
    {
      "name": "product", 
      "type": "string"
    },
    {
      "name": "price",
      "type": "float"
    }
  ]
}
```

**Schema Registry Benefits**:
- Schema versioning and compatibility
- Automatic schema validation
- Efficient binary serialization
- Type safety with generated classes

## 🔍 Message Flow

1. **API Request** → `OrderController` receives JSON request
2. **DTO Conversion** → `OrderService` converts DTO to Avro object
3. **Kafka Production** → `OrderProducer` sends message to `order` topic
4. **Consumer Processing** → `OrderConsumer` processes the message
5. **Retry Logic** → Failed messages retry up to 5 times with 3s delay
6. **DLQ Handling** → Exhausted retries send messages to `order-dlq`

## 🎯 Consumer Behavior Simulation

The `OrderConsumer` includes intentional failure simulation:

```java
int dice = (int)(Math.random()*10)+1;
if(dice%2==0) {
    throw new RuntimeException("Transient error for order " + order.getOrderId());
}
```

This creates a 50% failure rate to demonstrate retry and DLQ mechanisms.

## 📈 Monitoring and Observability

### Logging Configuration

- **Application Logs**: DEBUG level for `com.ruhcom.KafkaAvro`
- **Kafka Logs**: INFO level for Spring Kafka
- **Apache Kafka**: WARN level to reduce noise

### Key Metrics to Monitor

1. **Message Production Rate**
2. **Consumer Lag**
3. **Retry Attempts**
4. **DLQ Message Count**
5. **Processing Latency**

## 🛠️ Development Commands

### Build Avro Classes

```bash
mvn generate-sources
```

### Run Tests

```bash
mvn test
```

### Package Application

```bash
mvn clean package
```

### View Generated Avro Classes

Generated classes are available at:
```
target/generated-sources/avro/com/ruhcom/avro/Order.java
```

## 🔧 Production Considerations

### Scaling

- **Partitions**: Increase partitions for higher throughput
- **Consumer Concurrency**: Scale consumers based on partition count
- **Replication Factor**: Use odd numbers (3, 5) for production

### Security

- Enable SASL authentication
- Use SSL encryption
- Implement schema registry authentication
- Configure network security

### Performance Tuning

```yaml
spring:
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 5
      buffer-memory: 33554432
    consumer:
      max-poll-records: 500
      fetch-min-size: 1024
```

### Monitoring Integration

- Integrate with Prometheus/Grafana
- Set up Kafka Manager/AKHQ
- Configure alerting for DLQ accumulation
- Monitor consumer lag metrics

## 🐛 Troubleshooting

### Common Issues

1. **Schema Registry Connection**
   ```
   Error: Schema registry not available
   Solution: Ensure schema registry is running on port 8081
   ```

2. **Consumer Lag**
   ```
   Issue: Messages accumulating in topic
   Solution: Check consumer concurrency and processing time
   ```

3. **DLQ Accumulation**
   ```
   Issue: Too many messages in DLQ
   Solution: Review retry configuration and fix processing logic
   ```

### Health Checks

- **Kafka**: `docker-compose ps`
- **Topics**: Access Kafka container and run `kafka-topics --list`
- **Consumer Groups**: Check consumer group status
- **Schema Registry**: `curl http://localhost:8081/subjects`

## 📚 API Documentation

Once the application is running, access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Available Endpoints

- `POST /orders` - Create a new order
  ```json
  {
    "orderId": "string",
    "product": "string", 
    "price": 0.0
  }
  ```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Update documentation
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
