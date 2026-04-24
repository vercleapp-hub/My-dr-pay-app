package dr.pay.app.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import dr.pay.app.data.Transaction
import java.io.OutputStream
import java.util.*

class ThermalPrinterManager {

    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun connect(device: BluetoothDevice): Boolean {
        return try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            false
        }
    }

    fun printReceipt(transaction: Transaction, merchantName: String) {
        val out = outputStream ?: return
        
        val receiptText = StringBuilder()
        receiptText.append("\n      ELDoctor Pay\n")
        receiptText.append("      الـدكتور باي\n")
        receiptText.append("--------------------------------\n")
        receiptText.append("Merchant: $merchantName\n")
        receiptText.append("Trans ID: ${transaction.id?.take(8)}\n")
        receiptText.append("Service ID: ${transaction.service_id}\n")
        receiptText.append("Date: ${transaction.created_at}\n")
        receiptText.append("--------------------------------\n")
        receiptText.append("Amount: ${transaction.amount} EGP\n")
        receiptText.append("Fee:    ${transaction.fee} EGP\n")
        receiptText.append("Total:  ${transaction.total_amount} EGP\n")
        receiptText.append("--------------------------------\n")
        receiptText.append("   Thank you for using our\n")
        receiptText.append("      financial services\n\n\n\n")

        try {
            out.write(receiptText.toString().toByteArray(Charsets.UTF_8))
            out.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) { }
    }
}
