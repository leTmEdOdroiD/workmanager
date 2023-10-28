package com.dew.workmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.dew.workmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<ImageCompressionViewModel>()
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        workManager = WorkManager.getInstance(applicationContext)
        viewModel.workerID.observe(this){ it ->
            if(it!=null){
                workManager.getWorkInfoByIdLiveData(it).observe(this) { workInfo ->
                    val filePath =
                        workInfo?.outputData?.getString(ImageCompressionWorker.KEY_OUTPUT_IMG_URL)
                            ?: return@observe
                    Glide.with(this).load(filePath).into(binding.imgCompressed)
                }
            }
        }
        extractIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        extractIntent(intent)
    }

    private fun extractIntent(intent: Intent?){
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: return

        Glide.with(this).load(uri).into(binding.imgUnCompressed)
        startWorkRequest(uri)
    }

    private fun startWorkRequest(uri:Uri?){
        val request = OneTimeWorkRequestBuilder<ImageCompressionWorker>()
            .setInputData(
                workDataOf(ImageCompressionWorker.KEY_IMG_URL to uri.toString())
            ).build()

        viewModel.workerID.postValue(request.id)
        workManager.enqueue(request)
    }
}