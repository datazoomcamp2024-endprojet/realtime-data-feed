package org.example;

import java.util.HashMap;
import java.util.Map;

import org.example.data.VolumeHourly;
import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.Instant;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.Builder;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteClient;
import com.google.cloud.bigquery.storage.v1.JsonStreamWriter;
import com.google.cloud.bigquery.storage.v1.TableName;
import com.google.cloud.bigquery.storage.v1.WriteStream;

public class BigQueryWriter {

    private Builder insertAllRequestBuilder;
    private BigQuery bigquery;
    private JsonStreamWriter streamWriter;

    public BigQueryWriter(String gcpProjectId, String dataset, String table) {
        bigquery = BigQueryOptions.newBuilder()
                .setProjectId(gcpProjectId)
                .build().getService();
        insertAllRequestBuilder = createBuilder(dataset, table);
        TableName tableName = TableName.of(gcpProjectId, dataset, table);
        try (var client = BigQueryWriteClient.create()) {
            WriteStream stream = WriteStream.newBuilder().setType(WriteStream.Type.COMMITTED).build();
            WriteStream writeStream = client.createWriteStream(tableName, stream);
            streamWriter = JsonStreamWriter.newBuilder(writeStream.getName(), writeStream.getTableSchema()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void writeStream(VolumeHourly volumeHourly) {

        JSONObject firstTableJsonObj = new JSONObject();

        firstTableJsonObj.put("start", stringifyDate(volumeHourly.start));
        firstTableJsonObj.put("end", stringifyDate(volumeHourly.end));
        firstTableJsonObj.put("side", volumeHourly.side);
        firstTableJsonObj.put("volume", volumeHourly.volume);
        JSONArray firstObjArr = new JSONArray();

        firstObjArr.put(firstTableJsonObj);
        try {
            streamWriter.append(firstObjArr).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void write(VolumeHourly volumeHourly) {

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("start", stringifyDate(volumeHourly.start));
        contentMap.put("end", stringifyDate(volumeHourly.end));
        contentMap.put("side", volumeHourly.side);
        contentMap.put("volume", volumeHourly.volume);
        bigquery.insertAll(insertAllRequestBuilder.addRow(contentMap).build());

    }

    private String stringifyDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

    private Builder createBuilder(String dataset, String table) {
        return InsertAllRequest.newBuilder(dataset, table);
    }
}
