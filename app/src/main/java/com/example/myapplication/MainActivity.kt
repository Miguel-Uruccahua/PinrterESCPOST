package com.example.myapplication

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.emh.thermalprinter.EscPosPrinter
import com.emh.thermalprinter.connection.usb.UsbConnection
import com.emh.thermalprinter.connection.usb.UsbPrintersConnections
import com.emh.thermalprinter.exceptions.EscPosBarcodeException
import com.emh.thermalprinter.exceptions.EscPosConnectionException
import com.emh.thermalprinter.exceptions.EscPosEncodingException
import com.emh.thermalprinter.exceptions.EscPosParserException
import java.time.LocalDateTime
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun printUsb() {
        val usbConnection = UsbPrintersConnections.selectFirstConnected(this)
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (usbConnection != null && usbManager != null) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            usbManager.requestPermission(usbConnection.device, permissionIntent)
        }
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbManager = context!!.getSystemService(Context.USB_SERVICE) as UsbManager?
                    val usbDevice =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            imprimir(usbManager, usbDevice , context)
                        }
                    }

                }
            }
        }
    }

    fun imprimir(usbManager: UsbManager?, usbDevice: UsbDevice?, context: Context?) {
        try {
            val fecha = Date()
            val printer = EscPosPrinter(UsbConnection(usbManager, usbDevice), 203, 65f, 42)
            printer.printFormattedTextAndCut(
                """
                [L]
                [C]<u><font size='big'>TITULO</font></u>
                
                "[C]<font size='tall'>Customer Info</font>\n" +
                "[L] DNI\n" +
                "[L] NOMBRE\n" 
                "[L] APELLIDO\n"           
                "[L] DETALLE:  ESTE ES UN DETALLE\n" +             
                "[L] Tel : +923040017916\n" +
                [L] _________________________________________
                [L] $fecha \n
                [L] <barcode type='code39' height='10'>72171027</barcode>
                [L]
                [L]
                [L]
                
                """.trimIndent()
            )
            printer.disconnectPrinter()
        } catch (e: EscPosConnectionException) {
            AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage(e.message)
                .show()
            e.printStackTrace()
        } catch (e: EscPosEncodingException) {
            AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage(e.message)
                .show()
            e.printStackTrace()
        } catch (e: EscPosBarcodeException) {
            AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage(e.message)
                .show()
            e.printStackTrace()
        } catch (e: EscPosParserException) {
            AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage(e.message)
                .show()
            e.printStackTrace()
        } catch (e: Exception) {
            AlertDialog.Builder(context)
                .setTitle("Invalid")
                .setMessage(e.message)
                .show()
            e.printStackTrace()
        }
    }

}