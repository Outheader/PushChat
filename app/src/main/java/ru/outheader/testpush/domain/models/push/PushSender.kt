package ru.outheader.testpush.domain.models.push

import ru.outheader.testpush.data.entity.push.FireBaseEntity
import ru.outheader.testpush.data.entity.push.PushNotificationEntity
import ru.outheader.testpush.data.repository.PushRepository
import ru.outheader.testpush.domain.UseCase

open class PushSender /*@Inject constructor(
    private val pushRepository: PushRepository
)*/ : UseCase<PushNotificationEntity, Long>() {

    private val pushRepository = PushRepository()

    override suspend fun executeOnBackground(params: PushNotificationEntity): Long =
        pushRepository.sendPush(params.to, params.data, params.data?.data)


}