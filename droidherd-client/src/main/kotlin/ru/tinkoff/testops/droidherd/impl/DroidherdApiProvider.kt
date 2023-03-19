package ru.tinkoff.testops.droidherd.impl

import ru.tinkoff.testops.droidherd.api.DroidherdClientMetric
import ru.tinkoff.testops.droidherd.api.DroidherdSessionStatus
import ru.tinkoff.testops.droidherd.api.EmulatorRequest
import ru.tinkoff.testops.droidherd.api.SessionRequest


interface DroidherdApiProvider {

    enum class Version {
        v1
    }

    fun apiVersion(): Version

    fun login()

    fun release()

    fun status(): DroidherdSessionStatus

    fun request(request: SessionRequest): List<EmulatorRequest>

    fun ping()

    fun postMetrics(metrics: List<DroidherdClientMetric>)
}
