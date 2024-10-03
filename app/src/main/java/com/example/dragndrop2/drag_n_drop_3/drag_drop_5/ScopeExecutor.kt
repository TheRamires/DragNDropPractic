package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job

class ScopeExecutor(
    private val parentScope: CoroutineScope
) {
    private var currentJob: Job? = null

    fun newScope(): CoroutineScope {
        val newJob = Job(parent = parentScope.coroutineContext.job)
        currentJob = newJob
        return CoroutineScope(newJob)
    }

    fun stopScope() {
        currentJob?.cancel()
    }
}