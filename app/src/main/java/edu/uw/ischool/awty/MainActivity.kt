package edu.uw.ischool.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.widget.*

class IntentListener : BroadcastReceiver() {

    val MESSAGE_KEY = "Message"
    val PHONE_KEY = "Phone"
    override fun onReceive(context: Context?, intent: Intent?) {
        val smsManager = SmsManager.getDefault()
        val message = intent!!.getStringExtra(MESSAGE_KEY)
        val destination = intent!!.getStringExtra(PHONE_KEY)
        smsManager.sendTextMessage(destination, null, message, null, null)
    }

//    override fun onReceive(context: Context?, intent: Intent?) {
//        val message = intent!!.getStringExtra(MESSAGE_KEY)
//        val phone = intent!!.getStringExtra(PHONE_KEY)
//        val areaCode = phone!!.substring(0, 3)
//        val prefix = phone!!.substring(3, 6)
//        val lineNumber = phone!!.substring(6)
//
//        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val activity = context as MainActivity
//        val toastView = activity.findViewById<LinearLayout>(R.id.customToast)
//        var layout = inflater.inflate(R.layout.toast, toastView)
//        var captionLabel = layout.findViewById<TextView>(R.id.tvCaption)
//        var body = layout.findViewById<TextView>(R.id.tvBody)
//        captionLabel.text = String.format(captionTemplate, areaCode, prefix, lineNumber)
//        body.text = message
//
//        with(Toast(context)) {
//            duration = Toast.LENGTH_LONG
//            view = layout
//            show()
//        }
////        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
////        Log.i("MainActivity", "Are we there yet?")
//    }


}

class MainActivity : AppCompatActivity() {

    lateinit var etMessage: EditText
    lateinit var etPhone: EditText
    lateinit var etTime: EditText
    lateinit var btn: Button

    private val START = "Start"
    private val STOP = "Stop"
    private var requestCode = 0
    val MESSAGE_KEY = "Message"
    val PHONE_KEY = "Phone"
    private val missingMessage = "Message can not be empty!"
    private val incorrectPhoneNumber = "Incorrect Phone Number!"
    private val missingTimeInterval = "Missing Time Interval!"
    private val smsPermission = "android.permission.SEND_SMS"
    private val smsPermissionRequestCode = 42
    private val permissionDelied = "Failed to request permission!"

    private lateinit var receiver: IntentListener
    private lateinit var intentFilter: IntentFilter
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMessage = findViewById(R.id.etMessage)
        etPhone = findViewById(R.id.etPhone)
        etTime = findViewById(R.id.etTime)
        btn = findViewById(R.id.button)

        btn.text = getString(R.string.btn_start)

        receiver = IntentListener()
        intentFilter = IntentFilter()
        intentFilter.addAction(START)
        registerReceiver(receiver, intentFilter)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        btn.setOnClickListener {
            val button = it as Button
            when(button.text){
                START -> {
                    if(hasMessage()) {
                        if(hasPhone()) {
                            if (hasTime()) {
                                if (checkSelfPermission(smsPermission) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                    start()
                                }
                                else {
                                    requestPermissions(arrayOf(smsPermission), smsPermissionRequestCode)
                                }
                            }else Toast.makeText(this, missingTimeInterval, Toast.LENGTH_LONG).show()
                        }else Toast.makeText(this, incorrectPhoneNumber, Toast.LENGTH_LONG).show()
                    } else Toast.makeText(this, missingMessage, Toast.LENGTH_LONG).show()
                }
                STOP -> end()
            }
        }
    }

    private fun hasMessage() : Boolean{
        return etMessage.text.isNotEmpty()
    }

    private fun hasPhone(): Boolean {
        return etPhone.text.length == 10
    }

    private fun hasTime(): Boolean {
        return etTime.text.isNotEmpty()
    }

    private fun start() {
        val interval = (etTime.text.toString().toInt() * 60 * 1000).toLong()
        val time = System.currentTimeMillis()
        val intent = Intent(START)
        intent.putExtra(MESSAGE_KEY, etMessage.text.toString())
        intent.putExtra(PHONE_KEY, etPhone.text.toString())
        pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, pendingIntent)
        /* need to distinguish two intents; otherwise the intent received by onReceive does not change.
           Changing requestCode every time.
         */
        requestCode += 1
        btn.text = STOP
    }

    private fun end() {
        alarmManager.cancel(pendingIntent)
        btn.text = START
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            smsPermissionRequestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    start()
                }else {
                    Toast.makeText(this, permissionDelied, Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
}