package com.agusyc.daycounter

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        title = getString(R.string.about_title)

        if (supportActionBar != null) {
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        var textView = findViewById(R.id.txtGithub) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='https://www.github.com/Agusyc/DayCounter'>" + getString(R.string.source_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='https://www.github.com/Agusyc/DayCounter'>" + getString(R.string.source_link) + "</a>")
        }

        textView = findViewById(R.id.txtReddit) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='https://www.reddit.com/r/Android/comments/6knhlo/request_any_app_you_want_i_will_make_it_if_i_can/djnks5x/'>" + getString(R.string.reddit_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='https://www.reddit.com/r/Android/comments/6knhlo/request_any_app_you_want_i_will_make_it_if_i_can/djnks5x/'>" + getString(R.string.reddit_link) + "</a>")
        }

        textView = findViewById(R.id.txtDonate) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='http://www.paypal.me/agezro'>" + getString(R.string.paypal_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='http://www.paypal.me/agezro'>" + getString(R.string.paypal_link) + "</a>")
        }
    }
}
