package it.aljava.simplemetronome

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import it.aljava.simplemetronome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var metronome: MediaPlayer
    private val tracks: List<Uri> = listOf(
        Uri.parse("android.resource://it.aljava.simplemetronome/" + R.raw.loop_40_bpm),
        Uri.parse("android.resource://it.aljava.simplemetronome/" + R.raw.loop_120_bpm))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        metronome = MediaPlayer.create(this, tracks.elementAt(1))
        metronome.isLooping = true
        metronome.setOnErrorListener { _, what, extra ->
            Log.e("MainActivity" , "Metronome error what $what" +
                    ", extra $extra")
            true
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mainViewModel = viewModel
        binding.setLifecycleOwner(this)
        // observer start-stop
        viewModel.metronomeOn.observe(this, Observer {
            if (it == true) {
                binding.playButton.setText(R.string.play_button_stop)
                binding.playButton.setBackgroundResource(R.color.red)
                if (!metronome.isPlaying)
                    metronome.start()
            } else if (it == false) {
                binding.playButton.setText(R.string.play_button_play)
                binding.playButton.setBackgroundResource(R.color.green)
                if (metronome.isPlaying)
                    metronome.pause()
            }
        })
        // observer metronome params changed
        viewModel.metronomeParams.observe(this, Observer {
            if (it.first == null) {
                // do nothing
            } else if (!it.first!!) {
                updateSpeed()
            } else if (it.first!!) {
                updateSpeedAndTrack(it.second)
            }
        })
    }

    private fun updateSpeed() {
        val preventRestart = !metronome.isPlaying
        val newPlaybackParams = PlaybackParams()
        newPlaybackParams.setSpeed(viewModel.speedRate.value!!)
        metronome.playbackParams = newPlaybackParams
        if (preventRestart) {
            metronome.pause()
            viewModel.switchOffMetronome()
        }
    }

    private fun updateSpeedAndTrack(trackId: Int) {
        val restart = this.metronome.isPlaying
        restart.let {
            if (it) metronome.pause()
            metronome.stop()
        }
        metronome.release()
        metronome = MediaPlayer()
        metronome.setDataSource(this, tracks.elementAt(trackId))
        val newPlaybackParams = PlaybackParams()
        viewModel.speedRate.value?.let { it1 -> newPlaybackParams.setSpeed(it1) }
        metronome.playbackParams = newPlaybackParams
        metronome.prepare()
        metronome.isLooping = true
        restart.let {
            if (it) metronome.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        metronome.stop()
        metronome.release()
    }
}