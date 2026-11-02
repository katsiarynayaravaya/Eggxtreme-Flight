package com.example.eggxtremeflight;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryRecorder {
    private static class TrajectoryPoint {
        float x;
        float y;
        long timestamp;

        TrajectoryPoint(float x, float y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }

    public static class PipeData {
        public float topPipe;
        public float bottomPipe;
        public float spawnTime;
        public boolean used;

        public PipeData(float topPipe, float bottomPipe, float spawnTime) {
            this.topPipe = topPipe;
            this.bottomPipe = bottomPipe;
            this.spawnTime = spawnTime;
            this.used = false;
        }
    }

    private final List<TrajectoryPoint> trajectoryPoints;
    private final List<PipeData> pipeDataList;
    private final long startTime;
    private boolean isRecording;
    private final int sampleRateMs;
    private int finalScore;

    public TrajectoryRecorder(int sampleRateMs) {
        this.trajectoryPoints = new ArrayList<>();
        this.pipeDataList = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.isRecording = false;
        this.sampleRateMs = sampleRateMs;
        this.finalScore = 0;
    }

    public TrajectoryRecorder() {
        this(50);
    }

    public void startRecording() {
        isRecording = true;
        trajectoryPoints.clear();
        finalScore = 0;
    }

    public void stopRecording() {
        isRecording = false;
    }

    public void setFinalScore(int score) {
        this.finalScore = score;
    }

    public void recordPoint(float x, float y) {
        if (!isRecording) return;

        long currentTime = System.currentTimeMillis() - startTime;

        if (trajectoryPoints.isEmpty() || 
            currentTime - trajectoryPoints.get(trajectoryPoints.size() - 1).timestamp >= sampleRateMs) {
            trajectoryPoints.add(new TrajectoryPoint(x, y, currentTime));
        }
    }

    public void recordPipe(float topPipe, float bottomPipe) {
        if (!isRecording) return;

        long currentTime = System.currentTimeMillis() - startTime;
        pipeDataList.add(new PipeData(topPipe, bottomPipe, currentTime));
    }

    public String toJson() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (TrajectoryPoint point : trajectoryPoints) {
                JSONObject pointJson = new JSONObject();
                pointJson.put("x", point.x);
                pointJson.put("y", point.y);
                pointJson.put("timestamp", point.timestamp);
                jsonArray.put(pointJson);
            }

            JSONArray pipesArray = new JSONArray();
            for (PipeData pipe : pipeDataList) {
                JSONObject pipeJson = new JSONObject();
                pipeJson.put("topPipe", pipe.topPipe);
                pipeJson.put("bottomPipe", pipe.bottomPipe);
                pipeJson.put("spawnTime", pipe.spawnTime);
                pipesArray.put(pipeJson);
            }

            JSONObject json = new JSONObject();
            json.put("trajectory", jsonArray);
            json.put("pipes", pipesArray);
            json.put("startTime", startTime);
            json.put("totalPoints", trajectoryPoints.size());
            json.put("totalPipes", pipeDataList.size());
            json.put("finalScore", finalScore);
            
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonArray = json.getJSONArray("trajectory");
            
            trajectoryPoints.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pointJson = jsonArray.getJSONObject(i);
                float x = (float) pointJson.getDouble("x");
                float y = (float) pointJson.getDouble("y");
                long timestamp = pointJson.getLong("timestamp");
                trajectoryPoints.add(new TrajectoryPoint(x, y, timestamp));
            }

            JSONArray pipesArray = json.getJSONArray("pipes");
            pipeDataList.clear();
            for (int i = 0; i < pipesArray.length(); i++) {
                JSONObject pipeJson = pipesArray.getJSONObject(i);
                float topPipe = (float) pipeJson.getDouble("topPipe");
                float bottomPipe = (float) pipeJson.getDouble("bottomPipe");
                long spawnTime = pipeJson.getLong("spawnTime");
                pipeDataList.add(new PipeData(topPipe, bottomPipe, spawnTime));
            }

            finalScore = json.getInt("finalScore");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<TrajectoryPoint> getTrajectoryPoints() {
        return new ArrayList<>(trajectoryPoints);
    }

    public List<PipeData> getPipeDataList() {
        return new ArrayList<>(pipeDataList);
    }

    public int getPointCount() {
        return trajectoryPoints.size();
    }

    public int getPipeCount() {
        return pipeDataList.size();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getFinalScore() {
        return finalScore;
    }
}
