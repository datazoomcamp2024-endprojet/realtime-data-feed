variable "gcp_project_id" {
  description = "my gcp project id"
}

variable "location" {
  description = "my gcs project location"
  default     = "EU"
}

variable "bigquery_table_name" {
  description = "my big query table name"
  default     = "one_minute_window_btc_volume"
}

variable "bigquery_dataset_name" {
  description = "my big query dataset name"
  default     = "prod"
}
