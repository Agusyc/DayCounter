package com.agusyc.daycounter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView

class AboutActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val dark_theme = settings!!.getBoolean("dark_theme", false)
        if (dark_theme) setTheme(R.style.AppDarkTheme)
        setContentView(R.layout.activity_about)

        // We set the title that is shown on the ActionBar
        title = getString(R.string.about_title)

        // We configure and set the text for the GitHub section
        var textView = findViewById(R.id.txtGithub) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='https://www.github.com/Agusyc/DayCounter'>" + getString(R.string.source_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='https://www.github.com/Agusyc/DayCounter'>" + getString(R.string.source_link) + "</a>")
        }

        // We configure and set the text for the Reddit section
        textView = findViewById(R.id.txtReddit) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='https://www.reddit.com/r/Android/comments/6knhlo/request_any_app_you_want_i_will_make_it_if_i_can/djnks5x/'>" + getString(R.string.reddit_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='https://www.reddit.com/r/Android/comments/6knhlo/request_any_app_you_want_i_will_make_it_if_i_can/djnks5x/'>" + getString(R.string.reddit_link) + "</a>")
        }

        // We configure and set the text for the Donation section
        textView = findViewById(R.id.txtDonate) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.text = Html.fromHtml("<a href='http://www.paypal.me/agezro'>" + getString(R.string.paypal_link) + "</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            textView.text = Html.fromHtml("<a href='http://www.paypal.me/agezro'>" + getString(R.string.paypal_link) + "</a>")
        }

        // We color all the icons depending on the theme:
        if (dark_theme) {
            (findViewById(R.id.imgGithub) as ImageView).setColorFilter(Color.WHITE)
            (findViewById(R.id.imgReddit) as ImageView).setColorFilter(Color.WHITE)
            (findViewById(R.id.imgDonate) as ImageView).setColorFilter(Color.WHITE)
        }
    }
}
