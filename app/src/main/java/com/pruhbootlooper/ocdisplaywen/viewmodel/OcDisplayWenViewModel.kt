package com.pruhbootlooper.ocdisplaywen.viewmodel

import android.app.Application
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pruhbootlooper.ocdisplaywen.R
import com.pruhbootlooper.ocdisplaywen.Unpacc
import com.pruhbootlooper.ocdisplaywen.Utils
import com.pruhbootlooper.ocdisplaywen.database.DBHelper
import com.pruhbootlooper.ocdisplaywen.model.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OcDisplayWenViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    private val _shouldShowDialog = MutableLiveData<Boolean>()
    private val _dialogTitle = MutableLiveData<String>()
    private val _dialogText = MutableLiveData<String>()
    private val _P = MutableLiveData<Int>(0)
    private val _M = MutableLiveData<Int>(0)
    private val _S = MutableLiveData<Int>(0)
    private var checkingComplete = false
    private val _freq = MutableLiveData<Int>(1100)
    private val _refreshRate = MutableLiveData<String>("60.0")
    val unpacc = Unpacc(appContext.filesDir.absolutePath)

    var shouldShowDialog : LiveData<Boolean> = _shouldShowDialog
    var dialogTitle : LiveData<String> = _dialogTitle
    var dialogText : LiveData<String> = _dialogText
    var P : LiveData<Int> = _P
    var M : LiveData<Int> = _M
    var S : LiveData<Int> = _S
    var freq : LiveData<Int> = _freq
    var refreshRate : LiveData<String> = _refreshRate

    fun checkRoot(){
        if(checkingComplete){
            return
        }
        if(!Utils.checkRoot()){
            _dialogTitle.postValue("No root found :(")
            _dialogText.postValue("This app requires root access!")
            _shouldShowDialog.postValue(true)
        }else{
            _shouldShowDialog.postValue(false)
            DBHelper.initDB(appContext)
            Utils.setupEnv(appContext)
            setPMSFromDB("stock_profile")
        }
        checkingComplete = true
    }

    fun backupBootImage(){
        _dialogTitle.postValue("Backing up stock boot image...")
        _dialogText.postValue("Please wait, this will close on completion of action!")
        _shouldShowDialog.postValue(true)
        viewModelScope.launch {
            if(Utils.checkBootImageBackup(appContext)){
                Toast.makeText(appContext, "Already backed up!", Toast.LENGTH_SHORT).show()
            }else{
                unpacc.getBootImage(appContext)
                unpacc.renameAsStock(appContext)
                Toast.makeText(appContext, "Successfully backed up boot image!", Toast.LENGTH_SHORT).show()
            }
            _shouldShowDialog.postValue(false)
        }
    }

    fun setPMSFromDB(profile_name : String){
        CoroutineScope(Dispatchers.IO).launch {
            val profile = DBHelper.getProfileFromDB(profile_name)
            _P.postValue(profile.P)
            _M.postValue(profile.M)
            _S.postValue(profile.S)
            delay(100)
            _freq.postValue((Utils.calculateFrequency(_P.value!!.toDouble(), _M.value!!.toDouble(), _S.value!!.toDouble())).toInt())
            _refreshRate.postValue(Utils.calculateRefreshRate(freq.value?.toFloat() ?: 1100.0f))
            println("SetPMSFromDB ${profile.P}, ${profile.M}, ${profile.S}")
        }
    }

    suspend fun increment(sliderText: String){
        when(sliderText){
            "P" -> _P.postValue(_P.value+1)
            "M" -> _M.postValue(_M.value+1)
            "S" -> _S.postValue(_S.value+1)
        }
        delay(100)
        var res = (Utils.calculateFrequency(_P.value!!.toDouble(), _M.value!!.toDouble(), _S.value!!.toDouble())).toInt()
        println("increment res:$res")
        _freq.postValue(res)
        var rr = Utils.calculateRefreshRate(res.toFloat())
        println("increment rr:$rr")
        _refreshRate.postValue(rr)
    }

    suspend fun decrement(sliderText: String){
        when(sliderText){
            "P" -> _P.postValue(_P.value-1)
            "M" -> _M.postValue(_M.value-1)
            "S" -> _S.postValue(_S.value-1)
        }
        delay(100)
        var res = (Utils.calculateFrequency(_P.value!!.toDouble(), _M.value!!.toDouble(), _S.value!!.toDouble())).toInt()
        println("decrement res:$res")
        _freq.postValue(res)
        var rr = Utils.calculateRefreshRate(res.toFloat())
        println("increment rr:$rr")
        _refreshRate.postValue(rr)
    }

    suspend fun setValue(sliderText: String, value : Int){
        when(sliderText){
            "P" -> _P.postValue(value)
            "M" -> _M.postValue(value)
            "S" -> _S.postValue(value)
        }
//        delay(100)
        _freq.postValue((Utils.calculateFrequency(_P.value!!.toDouble(), _M.value!!.toDouble(), _S.value!!.toDouble())).toInt())
        _refreshRate.postValue(Utils.calculateRefreshRate(freq.value?.toFloat() ?: 1100.0f))
    }

    fun unpackDTS(){
        CoroutineScope(Dispatchers.IO).launch {
            if(!unpacc.unpackBootImage(appContext)){
                _dialogTitle.postValue("Error!")
                _dialogText.postValue("Please consult the instructions!")
                _shouldShowDialog.postValue(true)
            }
            if(!Utils.dtb_split(appContext)){
                _dialogTitle.postValue("Error!")
                _dialogText.postValue("Please consult the instructions!")
                _shouldShowDialog.postValue(true)
            }
        }
        _dialogTitle.postValue("Success :D")
        _dialogText.postValue("Unpacked the DTS!")
        _shouldShowDialog.postValue(true)
    }

    fun dismissDialog(){
        _shouldShowDialog.postValue(false)
    }

    fun modifyDTS(){
        CoroutineScope(Dispatchers.IO).launch{
            Utils.modifyDts(appContext, P.value!!.toString(), M.value!!.toString(), S.value!!.toString())
        }
        _dialogTitle.postValue("Success :D")
        _dialogText.postValue("Modified the DTS, reboot now!")
        _shouldShowDialog.postValue(true)
    }
}