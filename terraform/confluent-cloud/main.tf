terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.67.0"
    }
  }
}
provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

resource "confluent_environment" "prod" {
  display_name = "prod"
  stream_governance {
    package = "ESSENTIALS"
  }
  lifecycle {
    prevent_destroy = false
  }
}

resource "confluent_kafka_cluster" "datacamp2024_project_cluster" {
  cloud = "GCP"
  environment {
    id = resource.confluent_environment.prod.id
  }
  availability = "SINGLE_ZONE"
  basic {
  }
  region       = "europe-west1"
  display_name = "datacamp2024_project_cluster"
  lifecycle {
    prevent_destroy = false
  }
}

resource "confluent_api_key" "app-manager-kafka-api-key" {
  display_name = "app-manager-kafka-api-key"
  description  = "Kafka API Key that is owned by admin user"
  owner {
    id          = var.user_account_id
    api_version = "iam/v2"
    kind        = "User"
  }
  managed_resource {
    id          = confluent_kafka_cluster.datacamp2024_project_cluster.id
    api_version = confluent_kafka_cluster.datacamp2024_project_cluster.api_version
    kind        = confluent_kafka_cluster.datacamp2024_project_cluster.kind

    environment {
      id = confluent_environment.prod.id
    }
  }

  lifecycle {
    prevent_destroy = false
  }
}

output "kafka_cluster_api_secret" {
  value     = confluent_api_key.app-manager-kafka-api-key.secret
  sensitive = true
}


resource "confluent_kafka_topic" "orders_btc_3" {
  partitions_count = 6
  rest_endpoint    = confluent_kafka_cluster.datacamp2024_project_cluster.rest_endpoint
  kafka_cluster {
    id = confluent_kafka_cluster.datacamp2024_project_cluster.id
  }
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
  topic_name = "orders_btc"
  config = {
    "cleanup.policy"                      = "delete"
    "delete.retention.ms"                 = "86400000"
    "max.compaction.lag.ms"               = "9223372036854775807"
    "max.message.bytes"                   = "2097164"
    "message.timestamp.after.max.ms"      = "9223372036854775807"
    "message.timestamp.before.max.ms"     = "9223372036854775807"
    "message.timestamp.difference.max.ms" = "9223372036854775807"
    "message.timestamp.type"              = "CreateTime"
    "min.compaction.lag.ms"               = "0"
    "min.insync.replicas"                 = "2"
    "retention.bytes"                     = "-1"
    "retention.ms"                        = "604800000"
    "segment.bytes"                       = "104857600"
    "segment.ms"                          = "604800000"
  }
  lifecycle {
    prevent_destroy = false
  }
}

resource "confluent_kafka_topic" "one-minute-windowed-volume" {
  partitions_count = 6
  rest_endpoint    = confluent_kafka_cluster.datacamp2024_project_cluster.rest_endpoint
  kafka_cluster {
    id = confluent_kafka_cluster.datacamp2024_project_cluster.id
  }
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
  topic_name = "one-minute-windowed-volume"
  config = {
    "cleanup.policy"                      = "delete"
    "delete.retention.ms"                 = "86400000"
    "max.compaction.lag.ms"               = "9223372036854775807"
    "max.message.bytes"                   = "2097164"
    "message.timestamp.after.max.ms"      = "9223372036854775807"
    "message.timestamp.before.max.ms"     = "9223372036854775807"
    "message.timestamp.difference.max.ms" = "9223372036854775807"
    "message.timestamp.type"              = "CreateTime"
    "min.compaction.lag.ms"               = "0"
    "min.insync.replicas"                 = "2"
    "retention.bytes"                     = "-1"
    "retention.ms"                        = "604800000"
    "segment.bytes"                       = "104857600"
    "segment.ms"                          = "604800000"
  }
  lifecycle {
    prevent_destroy = false
  }
}
