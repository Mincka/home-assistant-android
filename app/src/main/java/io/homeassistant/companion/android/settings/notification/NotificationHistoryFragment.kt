package io.homeassistant.companion.android.settings.notification

import android.app.AlertDialog
import android.os.Bundle
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.database.AppDatabase
import io.homeassistant.companion.android.database.notification.NotificationDao
import io.homeassistant.companion.android.database.notification.NotificationItem
import java.util.Calendar
import java.util.GregorianCalendar

class NotificationHistoryFragment : PreferenceFragmentCompat() {

    companion object {
        val listCommands = listOf(
            "request_location_update",
            "TTS",
            "command_dnd",
            "command_ringer_mode",
            "clear_notification"
        )
        fun newInstance(): NotificationHistoryFragment {
            return NotificationHistoryFragment()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notifications, rootKey)
    }

    override fun onResume() {
        super.onResume()

        val notificationDao = AppDatabase.getInstance(requireContext()).notificationDao()
        val notificationList = notificationDao.getLast25()

        val prefCategory = findPreference<PreferenceCategory>("list_notifications")
        if (!notificationList.isNullOrEmpty()) {
            prefCategory?.isVisible = true
            prefCategory?.removeAll()
            reloadNotifications(notificationList, prefCategory)
//            for (item in notificationList) {
//                val pref = Preference(preferenceScreen.context)
//                val cal: Calendar = GregorianCalendar()
//                cal.timeInMillis = item.received
//                pref.key = item.id.toString()
//                pref.title = cal.time.toString()
//                pref.summary = item.message
//                pref.isIconSpaceReserved = false
//
//                pref.setOnPreferenceClickListener {
//                    parentFragmentManager
//                        .beginTransaction()
//                        .replace(
//                            R.id.content,
//                            NotificationDetailFragment.newInstance(
//                                item
//                            )
//                        )
//                        .addToBackStack("Notification Detail")
//                        .commit()
//                    return@setOnPreferenceClickListener true
//                }
//
//                prefCategory?.addPreference(pref)
//            }

            findPreference<PreferenceCategory>("manage_notifications")?.let {
                it.isVisible = true
            }

            findPreference<DropDownPreference>("filter_notifications")?.let {
                it.isVisible = true
                it.setOnPreferenceChangeListener { preference, newValue ->
                    when (newValue) {
                        "no_commands" -> {
                            val newList = notificationDao.getLast25NoCommands(listCommands)
                            prefCategory?.removeAll()
                            reloadNotifications(newList, prefCategory)
                        }
                        "last25" -> {
                            prefCategory?.removeAll()
                            reloadNotifications(notificationList, prefCategory)
                        }
                        else -> {
                            prefCategory?.removeAll()
                            reloadNotifications(notificationList, prefCategory)
                        }
                    }
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("delete_all")?.let {
                it.isVisible = true
                it.setOnPreferenceClickListener { _ ->
                    deleteAllConfirmation(notificationDao)
                    return@setOnPreferenceClickListener true
                }
            }
        } else {
            findPreference<PreferenceCategory>("manage_notifications")?.let {
                it.isVisible = false
            }
            findPreference<PreferenceCategory>("list_notifications")?.let {
                it.isVisible = false
            }
            findPreference<Preference>("no_notifications")?.let {
                it.isVisible = true
            }
        }
    }

    private fun deleteAllConfirmation(notificationDao: NotificationDao) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.confirm_delete_all_notification_title)
        builder.setMessage(R.string.confirm_delete_all_notification_message)

        builder.setPositiveButton(
            R.string.confirm_positive
        ) { dialog, _ ->
            notificationDao.deleteAll()
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        builder.setNegativeButton(
            R.string.confirm_negative
        ) { dialog, _ -> // Do nothing
            dialog.dismiss()
        }

        val alert: AlertDialog? = builder.create()
        alert?.show()
    }

    private fun reloadNotifications(notificationList: Array<NotificationItem>?, prefCategory: PreferenceCategory?) {
        if (notificationList != null) {
            for (item in notificationList) {
                val pref = Preference(preferenceScreen.context)
                val cal: Calendar = GregorianCalendar()
                cal.timeInMillis = item.received
                pref.key = item.id.toString()
                pref.title = cal.time.toString()
                pref.summary = item.message
                pref.isIconSpaceReserved = false

                pref.setOnPreferenceClickListener {
                    parentFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.content,
                            NotificationDetailFragment.newInstance(
                                item
                            )
                        )
                        .addToBackStack("Notification Detail")
                        .commit()
                    return@setOnPreferenceClickListener true
                }

                prefCategory?.addPreference(pref)
            }
        }
    }
}
