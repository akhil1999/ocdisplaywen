package com.pruhbootlooper.ocdisplaywen

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var backupStockButton : Button
    private lateinit var rebootButton : Button
    private lateinit var unpackDtsButton : Button
    private lateinit var calculateButton : Button
    private lateinit var modifyDtsButton : Button
    private lateinit var Pseek : SeekBar
    private lateinit var Mseek : SeekBar
    private lateinit var Sseek : SeekBar
    private lateinit var Ptext : TextView
    private lateinit var Mtext : TextView
    private lateinit var Stext : TextView
    private lateinit var pllFrequencyText : TextView
    private lateinit var refreshRateText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //buttons declaration
        backupStockButton = findViewById(R.id.backup_stock)
        rebootButton = findViewById(R.id.reboot_btn)
        unpackDtsButton = findViewById(R.id.unpackdts_btn)
        calculateButton = findViewById(R.id.calculate_btn)
        modifyDtsButton = findViewById(R.id.modifydts_btn)
        //seekBars
        Pseek = findViewById(R.id.seekBar6)
        Mseek = findViewById(R.id.seekBar5)
        Sseek = findViewById(R.id.seekBar4)
        //TextViews
        Ptext = findViewById(R.id.Ptext)
        Mtext = findViewById(R.id.Mtext)
        Stext = findViewById(R.id.Stext)
        pllFrequencyText = findViewById(R.id.pllfrequency)
        refreshRateText = findViewById(R.id.refreshrate)

        //Instantiate Unpacc
        val unpacc = Unpacc(this.filesDir.absolutePath)

        //init with stock m30 timings
        var P = 3
        var M = 127
        var S = 0

        //Check if root exist and only then proceed else throw blocking dialog box.
        if(!Utils.checkRoot()) {
            Utils.showDialog(this,":( No Root Access", "Please grant root access to continue", false)
        }else{
            Utils.setupEnv(this)
            Ptext.text = P.toString()
            Mtext.text = M.toString()
            Stext.text = S.toString()
            var freq : Int = (Utils.calculateFrequency(P.toDouble(),M.toDouble(),S.toDouble())).toInt()
            pllFrequencyText.text = freq.toString()
            refreshRateText.text = Utils.calculateRefreshRate(freq.toFloat())
        }

        //backup stock boot image
        backupStockButton.setOnClickListener {
            if(Utils.checkBootImageBackup(this)){
                showToast(this, "Already backed up!", Toast.LENGTH_SHORT)
            }else{
                unpacc.getBootImage(this) {
                    if(!it){
                        showToast(this, "Error backing up boot image!", Toast.LENGTH_SHORT )
                    }else{
                        unpacc.renameAsStock(this)
                        showToast(this, "Successfully backed up boot image!", Toast.LENGTH_SHORT)
                    }
                }
            }
        }

        rebootButton.setOnClickListener {
            Utils.showDialog(this, "Reboot?", "Do you want to reboot?", true)
        }

        unpackDtsButton.setOnClickListener {
//            showToast(this, "Unpacking DTS...", Toast.LENGTH_SHORT)
            unpacc.unpackBootImage(this)
            Utils.dtb_split(this)
        }

        modifyDtsButton.setOnClickListener {
            Utils.modifyDts(this, P.toString(), M.toString(), S.toString())
            val filePath = this.filesDir.absolutePath
            val actualFilePath = "$filePath/temp"
        }

        calculateButton.setOnClickListener {
//            showToast(this, "P: $P, M = $M, S =$S", Toast.LENGTH_SHORT)
            var freq : Int = (Utils.calculateFrequency(P.toDouble(),M.toDouble(),S.toDouble())).toInt()
            pllFrequencyText.text = freq.toString()
            refreshRateText.text = Utils.calculateRefreshRate(freq.toFloat())
        }

        Pseek.setOnSeekBarChangeListener(object :  SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Ptext.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 != null) {
                    P = p0.progress.toInt()
                }
            }

        })

        Mseek.setOnSeekBarChangeListener(object :  SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Mtext.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 != null) {
                    M = p0.progress.toInt()
                }
            }

        })

        Sseek.setOnSeekBarChangeListener(object :  SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Stext.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 != null) {
                    S = p0.progress.toInt()
                }
            }

        })
    }

    private fun showToast(context: Context, message : String, duration: Int){
        runOnUiThread {
            Toast.makeText(this, message, duration).show()
        }
    }
}