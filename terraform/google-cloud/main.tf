terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "5.12.0"
    }
  }
}

provider "google" {
  # Configuration options
  project = var.gcp_project_id
  region  = "europe-southwest1-a"
}

resource "google_bigquery_dataset" "default" {
  dataset_id                  = var.bigquery_dataset_name
  friendly_name               = var.bigquery_dataset_name
  description                 = "dataset contains tables for aggregated btc volume"
  location                    = "EU"
  default_table_expiration_ms = 3600000

  labels = {
    env = "default"
  }
}

resource "google_bigquery_table" "default" {
  dataset_id = google_bigquery_dataset.default.dataset_id
  table_id   = var.bigquery_table_name


  labels = {
    env = "default"
  }

  schema = <<EOF
[
  {
    "name": "start",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "start date of volume aggregation"
  },
  {
    "name": "end",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "end date of volume aggregation"
  },
  {
    "name": "side",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "side is either buy or sell"
  },
  {
    "name": "volume",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "volume aggregated by one minute range and side"
  }
]
EOF

}
