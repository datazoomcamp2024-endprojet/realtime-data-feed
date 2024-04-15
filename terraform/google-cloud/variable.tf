variable "gcp_project_id" {
  description = "my gcp project id"
}

variable "location" {
  description = "my gcs project location"
  default     = "EU"
}

variable "bigquery_table_name" {
  description = "my big query table name"
  default     = "window_btc_volumes_one_minute"
}
