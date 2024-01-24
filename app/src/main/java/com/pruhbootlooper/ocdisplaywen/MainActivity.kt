package com.pruhbootlooper.ocdisplaywen

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.pruhbootlooper.ocdisplaywen.database.DBHelper
import com.pruhbootlooper.test.Profile

class MainActivity : AppCompatActivity() {
    private lateinit var backupStockButton : Button
    private lateinit var rebootButton : Button
    private lateinit var unpackDtsButton : Button
    private lateinit var modifyDtsButton : Button
    private lateinit var Pseek : SeekBar
    private lateinit var Mseek : SeekBar
    private lateinit var Sseek : SeekBar
    private lateinit var Ptext : TextView
    private lateinit var Mtext : TextView
    private lateinit var Stext : TextView
    private lateinit var pllFrequencyText : TextView
    private lateinit var refreshRateText : TextView
    private lateinit var Pminus : ImageButton
    private lateinit var Mminus : ImageButton
    private lateinit var Sminus : ImageButton
    private lateinit var Pplus : ImageButton
    private lateinit var Mplus : ImageButton
    private lateinit var Splus : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)
        //Init the DB once
        DBHelper.initDB(applicationContext)


        //buttons declaration
        backupStockButton = findViewById(R.id.backup_stock)
        rebootButton = findViewById(R.id.reboot_btn)
        unpackDtsButton = findViewById(R.id.unpackdts_btn)
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

        //plusminus
        Pminus = findViewById(R.id.imageButton4)
        Mminus = findViewById(R.id.imageButton5)
        Sminus = findViewById(R.id.imageButton6)

        Pplus = findViewById(R.id.imageButton)
        Mplus = findViewById(R.id.imageButton2)
        Splus = findViewById(R.id.imageButton3)

        //Instantiate Unpacc
        val unpacc = Unpacc(this.filesDir.absolutePath)

        //init with stock m30 timings
        var P = 3
        var M = 127
        var S = 0

        Pminus.setOnClickListener{
            P -= 1
            Ptext.text = P.toString()
            Pseek.progress = P
            calculate(P, M, S)
        }

        Mminus.setOnClickListener{
            M -= 1
            Mtext.text = M.toString()
            Mseek.progress = M
            calculate(P, M, S)
        }

        Sminus.setOnClickListener{
            S -= 1
            Stext.text = S.toString()
            Sseek.progress = S
            calculate(P, M, S)
        }

        Pplus.setOnClickListener {
            P += 1
            Ptext.text = P.toString()
            Pseek.progress = P
            calculate(P, M, S)
        }

        Mplus.setOnClickListener {
            M += 1
            Mtext.text = M.toString()
            Mseek.progress = M
            calculate(P, M, S)
        }

        Splus.setOnClickListener {
            S += 1
            Stext.text = S.toString()
            Sseek.progress = S
            calculate(P, M, S)
        }

        val spProfiles : Spinner = findViewById(R.id.spProfiles)
        spProfiles.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                showToast(this@MainActivity, "Loaded ${adapterView?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT)

                when(adapterView?.getItemAtPosition(position).toString()){
                    "stock_profile" -> setPMSFromDB("stock_profile")
                    "oc_profile" -> setPMSFromDB("oc_profile")
                    "current_profile" ->setPMSFromDB("current_profile")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            fun setPMSFromDB(profile_name : String){
                DBHelper.getProfileFromDB(profile_name, response = {
                    runOnUiThread {
                        //set P,M,S vars
                        P = it.P!!
                        M = it.M!!
                        S = it.S!!
                        //Set P,M,S UI text
                        Ptext.text = P.toString()
                        Mtext.text = M.toString()
                        Stext.text = S.toString()
                        //Set P,M,S progress bars
                        Pseek.progress = P
                        Mseek.progress = M
                        Sseek.progress = S
                        //auto calculate PLL Frequency & Estimated refresh rate on profile toggle
                        var freq : Int = (Utils.calculateFrequency(P.toDouble(),M.toDouble(),S.toDouble())).toInt()
                        pllFrequencyText.text = freq.toString() + " MHz"
                        refreshRateText.text = Utils.calculateRefreshRate(freq.toFloat()) + " FPS"
                    }
                })
            }
        }



        //Check if root exist and only then proceed else throw blocking dialog box.
        if(!Utils.checkRoot()) {
            Utils.showDialog(this,":( No Root Access", "Please grant root access to continue", false)
        }else{
            Utils.setupEnv(this)
            Ptext.text = P.toString()
            Mtext.text = M.toString()
            Stext.text = S.toString()
            var freq : Int = (Utils.calculateFrequency(P.toDouble(),M.toDouble(),S.toDouble())).toInt()
            pllFrequencyText.text = freq.toString() + " MHz"
            refreshRateText.text = Utils.calculateRefreshRate(freq.toFloat()) + " FPS"
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
                        runOnUiThread {
                            val builder : AlertDialog = AlertDialog.Builder(this).create()
                            val view : View = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
                            val textView : TextView = view.findViewById(R.id.textView7)
                            val closeBtn : Button = view.findViewById(R.id.button)
                            val progressBar : ProgressBar = view.findViewById(R.id.progressBar)
                            builder.setTitle("Backup Boot Image")
                            builder.setCancelable(false)
                            closeBtn.isEnabled = false
                            closeBtn.setOnClickListener {
                                builder.dismiss()
                            }
                            builder.setView(view)
                            builder.show()
                            closeBtn.isEnabled = true
                            unpacc.renameAsStock(this, response = {
                                closeBtn.isEnabled = true
                                progressBar.progress = 100
                                textView.text = "Complete!"
                            })
                            showToast(this, "Successfully backed up boot image!", Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        }

        rebootButton.setOnClickListener {
            Utils.showDialog(this, "Reboot?", "Do you want to reboot?", true)
        }

        unpackDtsButton.setOnClickListener {
            val builder : AlertDialog = AlertDialog.Builder(this).create()
            val view : View = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
            val textView : TextView = view.findViewById(R.id.textView7)
            val closeBtn : Button = view.findViewById(R.id.button)
            val progressBar : ProgressBar = view.findViewById(R.id.progressBar)
            builder.setTitle("Unpack DTS")
            builder.setCancelable(false)
            closeBtn.isEnabled = false
            closeBtn.setOnClickListener {
                builder.dismiss()
            }
            builder.setView(view)
            builder.show()

            unpacc.unpackBootImage(this, response = {
                if(it){
                    progressBar.progress = 30
                }
            })
            Utils.dtb_split(this, response = {
                    if(it.status){
                        runOnUiThread {
                            closeBtn.isEnabled = true
                            progressBar.progress = 100
                            textView.text = "Complete, detected ${it.dtbCount} dtb(s)"
                        }
                    }else{
                        runOnUiThread {
                            builder.setTitle("Fail!")
                            builder.show()
                            progressBar.progress = 0
                            closeBtn.isEnabled = true
                            textView.text = "Backup stock boot image first!"
                        }
                    }
            })
        }

        modifyDtsButton.setOnClickListener {
            val builder : AlertDialog = AlertDialog.Builder(this).create()
            val view : View = layoutInflater.inflate(R.layout.progress_dialog_layout, null)
            val textView : TextView = view.findViewById(R.id.textView7)
            val closeBtn : Button = view.findViewById(R.id.button)
            val progressBar : ProgressBar = view.findViewById(R.id.progressBar)
            builder.setTitle("Modify DTS")
            builder.setCancelable(false)
            closeBtn.isEnabled = false
            closeBtn.setOnClickListener {
                builder.dismiss()
            }
            builder.setView(view)
            builder.show()

            Utils.modifyDts(this, P.toString(), M.toString(), S.toString(), response = {
                if(it){
                    runOnUiThread {
                        closeBtn.isEnabled = true
                        progressBar.progress = 100
                        textView.text = "Complete!"
                        DBHelper.setProfileInDB("current_profile", P, M, S)
                    }
                }else{
                    runOnUiThread {
                        builder.setTitle("Fail!")
                        builder.show()
                        progressBar.progress = 0
                        closeBtn.isEnabled = true
                        textView.text = "Backup stock boot image first!"
                    }
                }
            })
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
                    calculate(P, M, S)
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
                    calculate(P, M, S)
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
                    calculate(P, M, S)
                }
            }

        })
    }

    private fun showToast(context: Context, message : String, duration: Int){
        runOnUiThread {
            Toast.makeText(this, message, duration).show()
        }
    }

    private fun calculate(P : Int, M : Int, S : Int) {
//            showToast(this, "P: $P, M = $M, S =$S", Toast.LENGTH_SHORT)
        var freq : Int = (Utils.calculateFrequency(P.toDouble(),M.toDouble(),S.toDouble())).toInt()
        pllFrequencyText.text = freq.toString() + " MHz"
        refreshRateText.text = Utils.calculateRefreshRate(freq.toFloat()) + " FPS"
    }
}