package com.pruhbootlooper.ocdisplaywen

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.*
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.roundToInt

class Utils  {
    companion object{
        //        fun bootImage2Dts(context: Context){
//            unpackBootImage(context)
//        }

        //Lassen constants
        val CONFIG_EXYNOS_DTBH_PAGE_SIZE : String = "2048"
        val CONFIG_EXYNOS_DTBH_PLATFORM_CODE : String = "0x50a6"
        val CONFIG_EXYNOS_DTBH_SUBTYPE_CODE : String = "0x0217584da"

        //set a shared prefs key
        fun setSP(context: Context, key : String, value : String) {
            val sp = context.getSharedPreferences("ocdisplaywen", Context.MODE_PRIVATE)
            var editor = sp.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun getSP(context: Context, key : String) : String? {
            val sp = context.getSharedPreferences("ocdisplaywen", Context.MODE_PRIVATE)
            return sp.getString(key, "default")
        }

        fun fetchTimings(context: Context, index : Int, dir : String) : String {
            val filePath = context.filesDir.absolutePath
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("cd $filePath/$dir\n")
            osw.write("cat $index.dts | grep 'timing,pms'\n")
            osw.write("exit\n");
            osw.flush()
            var output = br.readLine()
            br.close()
            osw.close()
            process.destroy()
            output = output.trim()
            return output
        }

//        fun modifyAllDts(context: Context, P : String, M : String, S : String){
//            val dtbCount = getSP(context, "dtb_count")?.toInt()
//            for (i in 1..dtbCount!!) {
//                modifyDts(context, P, M, S, i)
//            }
//        }

        fun modifyDts(context: Context, P : String, M : String, S : String, response: (Boolean) -> Unit) {
            thread{
                val filePath = context.filesDir.absolutePath
                val process = ProcessBuilder("su").redirectErrorStream(true).start()
                val osw = OutputStreamWriter(process.outputStream)
                val br = BufferedReader(InputStreamReader(process.inputStream))
                val stock = fetchTimings(context, 1, "temp")
                val modified = "timing,pms = <$P $M $S>;"
                println("stocc:$stock")
                println("mod:$modified")

                val tempDir = File("$filePath/temp/")
                if(!tempDir.exists()){
                    osw.write("mkdir $filePath/temp\n")
                    osw.write("cd $filePath/temp\n")
                    osw.write("cp -r $filePath/stock/* $filePath/temp/\n")
                    osw.write("sed -i 's/$stock/$modified/g' *.dts\n")
                }else{
                    osw.write("cd $filePath/temp\n")
                    osw.write("sed -i 's/$stock/$modified/g' *.dts\n")
                }
                osw.write("exit\n")
                osw.flush()
                while(br.readLine() != null){
                }

                br.close()
                osw.close()
                process.destroy()
                dts2dtb(context)
                response(true)
            }
        }

        fun calculateFrequency(P: Double, M: Double, S: Double): Double {
            val frequencyIn = 26.0
            val numerator = M * frequencyIn
            val denominator = P * 2.0.pow(S)
            println("numerator: $numerator, denominator : $denominator")
            return (numerator / denominator)
        }

        fun calculateRefreshRate(pllFrequency : Float) : String {
            val defaultFps : Float = 60F
            var result : Float = (60F / 1100F) * pllFrequency
            result = result.roundToInt().toFloat()
            return result.toString()
        }

        fun dtb_split(context: Context, response : (ResponseObject) -> Unit) {
            thread{
                val filePath = context.filesDir.absolutePath
                val dtb = File("$filePath/stock/extra")
                val dtb_bytes = ByteArray(dtb.length().toInt())
                val fis = FileInputStream(dtb)
                if (fis.read(dtb_bytes) != dtb.length().toInt())
                    fis.close()

                var i = 0
                var dtbcount = 0
                val dtbOffsetList = mutableListOf<Int>()
                var dtbHeaderOffset : Int = 0
                while(i + 8 < dtb.length()){
                    if(
                        //D0 0D FE ED is Flattened Device Tree Magic Number
                        (dtb_bytes[i] == (0xD0).toByte() && dtb_bytes[i + 1] == (0x0D).toByte() && dtb_bytes[i+2] == (0xFE).toByte() && dtb_bytes[i+3] == (0xED).toByte())
                    ){
                        dtbcount+=1
                        println("D00DFEED $i")
                        dtbOffsetList.add(i)
                    }
                    i++
                }

                dtbOffsetList.add(dtb.length().toInt())

                //save dtb count in shared prefs
                setSP(context, "dtb_count", dtbcount.toString())

                println("dtb_count: $dtbcount, size of dtbOffsetList: ${dtbOffsetList.size}, dtbOffsetList: $dtbOffsetList")

                //end of detecting number of dtbs and their offsets

                //start extraction of dtbs
                i = 1
                while(i < dtbOffsetList.size){
                    val process = ProcessBuilder("su").redirectErrorStream(true).start()
                    val osw = OutputStreamWriter(process.outputStream)
                    val br = BufferedReader(InputStreamReader(process.inputStream))
                    println("skip=${dtbOffsetList[i] - dtbOffsetList[i-1]}, of=$filePath/$i.dtb, i is $i")
                    osw.write("dd if=$filePath/stock/extra skip=${dtbOffsetList[i-1]} bs=1 count=${dtbOffsetList[i] - dtbOffsetList[i-1]} of=$filePath/stock/$i.dtb\n")
                    osw.write("exit\n")
                    osw.flush()
                    while(br.readLine() != null){
                    }
                    br.close()
                    osw.close()
                    process.destroy()
                    dtb2dts(context, filePath, i)
                    i++
                }
                val responseObject = ResponseObject()
                responseObject.dtbCount = dtbcount
                responseObject.status = true
                response(responseObject)
            }
        }

        fun dtb2dts(context: Context, filePath : String, i : Int) {
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("cd $filePath/stock/\n")
            osw.write("$filePath/dtc -I dtb -O dts $i.dtb -o $i.dts\n")
            osw.write("rm $i.dtb\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            br.close()
            osw.close()
            process.destroy()
        }

        fun dts2dtb(context: Context) {
            val filePath = context.filesDir.absolutePath
            val dtbCount = getSP(context, "dtb_count")?.toInt()
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            println("path:$filePath")
            osw.write("cd $filePath/temp\n")
            for(i in 1..dtbCount!!){
                println(i)
                osw.write("$filePath/dtc -I dts -O dtb $i.dts -o $i.dtb\n")
            }
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            br.close()
            osw.close()
            process.destroy()
            createDtb(context)
        }

        fun createDtb(context : Context) {
            val filePath = context.filesDir.absolutePath
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("cd $filePath/temp\n")
            //Call Exynos DTB Tool :D
            osw.write("../dtbtool --pagesize $CONFIG_EXYNOS_DTBH_PAGE_SIZE --platform $CONFIG_EXYNOS_DTBH_PLATFORM_CODE --subtype $CONFIG_EXYNOS_DTBH_SUBTYPE_CODE -o $filePath/temp/extra --dtb $filePath/temp/\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            br.close()
            osw.close()
            process.destroy()
            repackBootImage(context)
        }

        fun repackBootImage(context: Context) {
            val filePath = context.filesDir.absolutePath
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("cd $filePath/temp\n")
            osw.write("../magiskboot repack stock_boot.img boot_new.img\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            br.close()
            osw.close()
            process.destroy()
            flashRepackedBootImage(context)
        }

        fun flashRepackedBootImage(context: Context) {
            val filePath = context.filesDir.absolutePath
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("dd if=$filePath/temp/boot_new.img of=/dev/block/by-name/BOOT\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            br.close()
            osw.close()
            process.destroy()
        }

        fun checkBootImageBackup(context: Context) : Boolean {
            val filePath = context.filesDir.absolutePath
            val stockDir = File("$filePath/stock")
            return stockDir.exists()
        }

        fun setupEnv(context: Context) {
            val binaries = listOf<String>("dtc", "magiskboot", "dtbtool")
            for (binary in binaries){
                Utils.exportFiles(context, binary, context.filesDir.absolutePath + "/" + binary)
                val file = File(context.filesDir.absolutePath + "/" + binary)
                file.setExecutable(true)
                if(!file.canExecute()){
                    println("erorr!")
                }
            }
        }

        fun exportFiles(context: Context, src : String, out : String) {
            var filenames :  Array<String> = context.assets.list(src) as Array<String>
            if(filenames.size > 0){
                val file = File(out)
                file.mkdirs()
                for(filename in filenames){
                    exportFiles(context, "$src/$filename", "$out/$filename")
                }
            }else{
                val inputStream = context.assets.open(src)
                val fos = FileOutputStream(File(out))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (inputStream.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush()
                inputStream.close()
                fos.close()
            }
        }

        fun showDialog(context : Context, title : String, message : String, isCancelable : Boolean) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle(title)
            alertDialog.setMessage(message)
            alertDialog.setCancelable(isCancelable)

            if(isCancelable) {
                alertDialog.setPositiveButton("Confirm") { alertDialog : DialogInterface, i ->
                    Utils.reboot()
                }
                alertDialog.setNegativeButton("Deny") { alertDialog : DialogInterface, i ->
                    Toast.makeText(context, "Denied", Toast.LENGTH_SHORT).show()
                }
            }
            alertDialog.show()
        }

        fun checkRoot() : Boolean {
            val processBuilder = ProcessBuilder().command("su", "-c", "ls")
            try{
                val process : Process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.errorStream))
                val line = reader.readText()
                return line == ""
            }catch (e : IOException){
                return false
            }
        }

        fun reboot() {
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("reboot\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            osw.close()
            br.close()
            process.destroy()
        }
    }
}