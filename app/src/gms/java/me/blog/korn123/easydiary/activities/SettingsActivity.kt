package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_settings_progress.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class SettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    lateinit var mDotIndicatorPager2Adapter: DotIndicatorPager2Adapter
    private var mCurrentPosition = 0

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.preferences_category_settings)
            setDisplayHomeAsUpEnabled(true)
        }

        val fragmentList = arrayListOf(
                SettingsBasicFragment(), SettingsFontFragment(),
                SettingsLockFragment(), SettingsGMSBackupFragment(),
                SettingsLocalBackupFragment(), SettingsScheduleFragment(), SettingsAppInfoFragment()
        )
        mDotIndicatorPager2Adapter = DotIndicatorPager2Adapter(supportFragmentManager, fragmentList)
        viewPager.adapter = mDotIndicatorPager2Adapter
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mCurrentPosition = position
                supportActionBar?.run {
                    when (position) {
                        0 -> {
                            title = getString(R.string.preferences_category_settings)
                            subtitle = ""
                        }
                        1 -> {
                            title = getString(R.string.preferences_category_font)
                            subtitle = ""
                        }
                        2 -> {
                            title = getString(R.string.preferences_category_lock)
                            subtitle = ""
                        }
                        3 -> {
                            title = getString(R.string.preferences_category_backup_restore)
                            subtitle = getString(R.string.preferences_category_backup_restore_sub)
                            pauseLock()
                            updateUI()
                        }
                        4 -> {
                            title = getString(R.string.preferences_category_backup_restore_device)
                            subtitle = getString(R.string.preferences_category_backup_restore_device_sub)
                        }
                        5 -> {
                            title = getString(R.string.preferences_category_schedule)
                            subtitle = ""

                        }
                        else -> {
                            title = getString(R.string.preferences_category_information)
                            subtitle = ""
                        }
                    }
                }
                invalidateOptionsMenu()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        dots_indicator.setViewPager(viewPager)
        progressContainer.setOnTouchListener { _, _ -> true }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_settings_schedule, menu)
        if (mCurrentPosition == 5) menu.findItem(R.id.addSchedule).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scheduleFragment = mDotIndicatorPager2Adapter.instantiateItem(viewPager, viewPager.currentItem)
        when (item.itemId) {
            R.id.addSchedule -> {
                if (scheduleFragment is SettingsScheduleFragment) {
                    scheduleFragment.openAlarmDialog(EasyDiaryDbHelper.createTemporaryAlarm())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun updateUI() {
        super.onResume()
    }
}