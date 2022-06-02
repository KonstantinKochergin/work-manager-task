package com.example.workmanagerdemo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val irkutskData = workDataOf("city" to "Irkutsk")
        val irkutskRequest = OneTimeWorkRequest.Builder(WeatherWorker::class.java)
            .setInputData(irkutskData)
            .build()

        val moscowData = workDataOf("city" to "Moscow")
        val moscowRequest = OneTimeWorkRequest.Builder(WeatherWorker::class.java)
            .setInputData(moscowData)
            .build()

        val londonData = workDataOf("city" to "London")
        val londonRequest = OneTimeWorkRequest.Builder(WeatherWorker::class.java)
            .setInputData(londonData)
            .build()

        val worksChain = WorkManager.getInstance(this)
            .beginWith(irkutskRequest)
            .then(moscowRequest)
            .then(londonRequest)

        worksChain.enqueue()

        worksChain.workInfosLiveData.observe(this, Observer<List<WorkInfo>>() {
            var isTasksCompleted = true
            for (workInfo in it) {
                if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                    isTasksCompleted = false
                }
            }
            if (isTasksCompleted) {
                val sortedInfos = it.sortedWith(compareBy{it.outputData.keyValueMap.size})
                val lastInfo = sortedInfos.last()
                val irkTv = findViewById<TextView>(R.id.irkutsk_value)
                irkTv.text = "${lastInfo.outputData.getString("Irkutsk")} °C"
                val moscowTv = findViewById<TextView>(R.id.moscow_value)
                moscowTv.text = "${lastInfo.outputData.getString("Moscow")} °C"
                val londonTv = findViewById<TextView>(R.id.london_value)
                londonTv.text = "${lastInfo.outputData.getString("London")} °C"
            }
        })

    }
}