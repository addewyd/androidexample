package com.addewyd.kot002

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.content.Context
import android.content.SharedPreferences
import android.support.constraint.ConstraintLayout
import android.widget.TextView
import android.widget.EditText

class SettingsActivity : AppCompatActivity()  {

    val APP_PREFERENCES:String = "ballsettings"
    val APP_PREFERENCES_GF = "gravfact"
    var mSettings:SharedPreferences? = null
    var tv:TextView? = null
    var et:EditText? = null
    var gf:Float? = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        tv = findViewById(R.id.textViewInfo)
        et = findViewById(R.id.editText)

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                //navigateUpTo(supportParentActivityIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (mSettings?.contains(APP_PREFERENCES_GF) ?: false) {

            gf = mSettings?.getFloat(APP_PREFERENCES_GF, 1f)
            tv?.setText("GF $gf")
            et?.setText("$gf")
        }
    }

    override fun onPause() {
         var editor = mSettings?.edit()
            var s = et?.text.toString()
            var f = try { s.toFloat() } catch(e:Exception) {1f}
            if (f < 1f) f = 1f
            editor?.putFloat(APP_PREFERENCES_GF,f)
            editor?.apply()

        super.onPause()
    }
}

