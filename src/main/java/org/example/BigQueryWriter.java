package org.example;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class BigQueryWriter {

    public BigQueryWriter(String gcpProjectId) {
        // BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(gcpProjectId)
        // .build().getService();
    }

    public void write(VolumeHourly volumeHourly) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }
}
