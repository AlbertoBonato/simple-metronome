package it.aljava.simplemetronome

import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val initialBpm = 120
    private val bpmProgressOffset = 20
    private val changeTrackBpmThreshold = 90

    private val _speedRate = MutableLiveData<Float>()
    val speedRate: LiveData<Float>
        get() = _speedRate

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int>
        get() = _progress

    private val bpm = MutableLiveData<Int>()

    private val _metronomeOn = MutableLiveData<Boolean>()
    val metronomeOn: LiveData<Boolean>
        get() = _metronomeOn

    /**
     * 0 --> R.raw.loop_40_bpm
     * 1 --> R.raw.loop_120_bpm
     */
    private val _trackSelected = MutableLiveData<Int>()

    /**
     * Updated when at least one between speedRate and trackSelected changes
     * metronomeParams.first = false   -> only speedRate changes
     * metronomeParams.first = true    -> both speedRate and trackSelected changes
     * metronomeParams.second = id of track selected (0 o 1)
     *
     * Initialization value: (null, 0)
     */
    private val _metronomeParams = MutableLiveData<Pair<Boolean?, Int>>()
    val metronomeParams: LiveData<Pair<Boolean?, Int>>
        get() = _metronomeParams

    val bpmString = MutableLiveData<String>()

    init {
        bpm.value = initialBpm
        bpmString.value = initialBpm.toString()
        _progress.value = initialBpm.minus(bpmProgressOffset)
        _metronomeOn.value = false
        _trackSelected.value = 1
        _speedRate.value = getSpeedRateFromBpmAndTrack(bpm.value!!, _trackSelected.value!!)
        _metronomeParams.value = Pair(null, _trackSelected.value!!)
    }

    fun switchOffMetronome() {
        _metronomeOn.value = false
    }

    fun incrementBpm() {
        if (bpm.value!! < 300) {
            bpm.value = bpm.value?.inc()
            bpmString.value = bpm.value.toString()
            _progress.value = _progress.value?.inc()
            _speedRate.value = getSpeedRateFromBpmAndTrack(bpm.value!!, _trackSelected.value!!)
        }
        val changeTrack = bpm.value == changeTrackBpmThreshold
        _metronomeParams.value = Pair(changeTrack, _trackSelected.value!!)
    }

    fun decrementBpm() {
        if (bpm.value!! > 20) {
            bpm.value = bpm.value?.dec()
            bpmString.value = bpm.value.toString()
            _progress.value = _progress.value?.dec()
            _speedRate.value = getSpeedRateFromBpmAndTrack(bpm.value!!, _trackSelected.value!!)
        }
        val changeTrack = bpm.value == (changeTrackBpmThreshold - 1)
        _metronomeParams.value = Pair(changeTrack, _trackSelected.value!!)
    }

    fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
        bpmString.value = bpmFromProgress(progress).toString()
    }

    fun onStopTrackingTouch(seek: SeekBar) {
        val newBpm = bpmFromProgress(seek.progress)
        val tc = trackChanged(newBpm, bpm.value!!)
        _trackSelected.value = checkTrackToPlay(newBpm)
        _progress.value = seek.progress
        bpm.value = newBpm
        bpmString.value = newBpm.toString()
        _speedRate.value = getSpeedRateFromBpmAndTrack(newBpm, _trackSelected.value!!)
        _metronomeParams.value = Pair(tc, _trackSelected.value!!)
    }

    fun togglePlayback() {
        _metronomeOn.value = _metronomeOn.value?.not() ?: false
    }

    private fun trackChanged(newBpm: Int, oldBpm: Int): Boolean {
        return if (changeTrackBpmThreshold in (newBpm + 1)..oldBpm) true
        else changeTrackBpmThreshold in (oldBpm + 1)..newBpm
    }

    private fun checkTrackToPlay(bpm: Int): Int {
        return if (bpm >= changeTrackBpmThreshold) 1
        else 0
    }

    private fun getSpeedRateFromBpmAndTrack(bpm: Int, track: Int): Float {
        return if (track == 0) {
            bpm.div(this.initialBpm.toFloat()).times(3)
        } else {
            bpm.div(this.initialBpm.toFloat())
        }
    }

    private fun bpmFromProgress(progress: Int) = progress.plus(bpmProgressOffset)

}