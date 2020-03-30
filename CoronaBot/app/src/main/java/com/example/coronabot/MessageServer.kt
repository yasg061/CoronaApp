package com.example.coronabot

import android.os.AsyncTask
import java.io.DataOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class MessageServer: AsyncTask<String, Void, Void>() {
    internal lateinit var s: Socket
    internal lateinit var dos: DataOutputStream
    internal lateinit var pw: PrintWriter
    protected override fun doInBackground(vararg voids:String): Void? {
        val message = voids[0]
        try {
            s = Socket("192.168.0.19", 2020)
            pw = PrintWriter(s.getOutputStream())
            pw.write(message)
            pw.flush()
            pw.close()
            s.close()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}