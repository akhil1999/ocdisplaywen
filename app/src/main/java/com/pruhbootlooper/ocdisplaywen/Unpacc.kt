package com.pruhbootlooper.ocdisplaywen

import android.content.Context
import java.io.*
import kotlin.concurrent.thread

class Unpacc(val filePath : String) {
    fun getBootImage(context: Context, response : (Boolean) -> Unit){
        thread {
            println(filePath)
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("mkdir $filePath/stock/\n")
            osw.write("cd $filePath/stock/\n")
            osw.write("dd if=/dev/block/by-name/BOOT of=$filePath/stock/boot.img\n")
            osw.write("chmod 644 $filePath/stock/boot.img\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            osw.close()
            br.close()
            process.destroy()

            val target = File("$filePath/stock/boot.img")
            if (!target.exists() || !target.canRead()) {
                target.delete()
                response(false)
            }else{

                response(true)
            }
        }
    }

    fun renameAsStock(context: Context,response: (Boolean) -> Unit){
        thread{
            println(filePath)
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val osw = OutputStreamWriter(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            osw.write("mv $filePath/stock/boot.img $filePath/stock/stock_boot.img\n")
            osw.write("exit\n")
            osw.flush()
            while(br.readLine() != null){
            }
            osw.close()
            br.close()
            process.destroy()
            response(true)
        }
    }

    fun unpackBootImage(context: Context, response : (Boolean) -> Unit) {
        val process = ProcessBuilder("su").redirectErrorStream(true).start()
        val osw = OutputStreamWriter(process.outputStream)
        val br = BufferedReader(InputStreamReader(process.inputStream))
        osw.write("cd $filePath/stock/\n")
        osw.write("$filePath/magiskboot unpack $filePath/stock/stock_boot.img .\n")
        osw.write("exit\n")
        osw.flush()
        val log = StringBuilder()
        var s: String?
        while (br.readLine().also { s = it } != null) {
            log.append(s).append("\n")
        }
        br.close()
        osw.close()
        process.destroy()
        response(true)
    }
}