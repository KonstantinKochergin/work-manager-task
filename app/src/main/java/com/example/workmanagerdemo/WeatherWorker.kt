package com.example.workmanagerdemo

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.workmanagerdemo.dto.TempDto
import org.json.JSONObject
import org.json.JSONTokener
import java.io.InputStream
import java.net.URL
import java.util.*

const val INPUT_CITY_KEY = "city"

class WeatherWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams)
{
    override fun doWork(): Result {
        val API_KEY = this.applicationContext.getString(R.string.open_weather_api_key)
        val city = inputData.getString(INPUT_CITY_KEY)
        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$API_KEY&units=metric";
        try {
            val stream = URL(weatherURL).getContent() as InputStream
            val data = Scanner(stream).nextLine()
            val jsonObject = JSONObject(data)
            val temp = jsonObject.getJSONObject("main").getDouble("temp")
            val inputDataMap = mutableMapOf<String, String>()
            for ((key, value) in inputData.keyValueMap) {
                if (key != INPUT_CITY_KEY) {
                    inputDataMap.put(key, value.toString())
                }
            }
            val dataBuilder = Data.Builder()
            val inputDataFiltered = dataBuilder.putAll(inputDataMap.toMap()).build()
            val cityKey = if (city != null) city else "CITY"
            val current =  workDataOf(cityKey to temp.toString())
            val merger = OverwritingInputMerger()
            val output = merger.merge(listOf(inputDataFiltered, current))
            return Result.success(output)
        }
        catch (e: Exception) {
            Log.e("mytag", "error $e")
        }
        return Result.success()
    }
}