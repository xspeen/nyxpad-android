package com.nyxpad.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nyxpad.app.data.preferences.NyxPreferences
import com.nyxpad.app.domain.repository.NoteRepository
import com.nyxpad.app.utils.PreferencesKeys
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class TrashPurgeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val preferences: NyxPreferences,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        val settings = preferences.getSettingsSnapshot()
        if (settings.autoPurgeDays != PreferencesKeys.NEVER_PURGE) {
            val cutoff = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(settings.autoPurgeDays.toLong())
            noteRepository.purgeOldDeletedNotes(cutoff)
        }
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "nyxpad_trash_auto_purge"
        const val WORK_TAG = "nyxpad_maintenance"
    }
}
