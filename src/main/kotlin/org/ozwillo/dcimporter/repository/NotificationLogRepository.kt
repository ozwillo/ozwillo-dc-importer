package org.ozwillo.dcimporter.repository

import java.util.*
import org.ozwillo.dcimporter.model.NotificationLog
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationLogRepository : ReactiveMongoRepository<NotificationLog, UUID>
