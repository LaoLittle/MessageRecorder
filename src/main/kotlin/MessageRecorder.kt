package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.laolittle.plugin.MessageDatabase.database
import org.laolittle.plugin.MessageDatabase.isLocked
import java.time.LocalDateTime

object MessageRecorder : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.MessageRecorder",
        name = "MessageRecorder",
        version = "1.0",
    ) {
        author("LaoLittle")
    }
) {

    override fun onEnable() {
        init()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<GroupMessageEvent>(
            priority = MessageRecorderConfig.priority
        ) {
            if (!isLocked) {
                newSuspendedTransaction(Dispatchers.IO, database) {
                    val now = LocalDateTime.now()
                    addLogger(MiraiSqlLogger)
                    message.forEach { single ->
                        val filter =
                            (single is PlainText) && (!single.content.contains("请使用最新版手机QQ体验新功能")) && (single.content.isNotBlank())
                        if (filter) {
                            val messageData = MessageData(subject.id)
                            SchemaUtils.create(messageData)
                            messageData.insert { data ->
                                data[time] = now
                                data[content] = single.content
                            }
                        } else if (single is Image) {
                            val imageData = ImageData(subject.id)
                            SchemaUtils.create(imageData)
                            imageData.insert { data ->
                                data[time] = now
                                data[images] = single.imageId
                            }
                        }
                    }
                }
            }
        }
    }

    private fun init() {
        MessageRecorderConfig.reload()
    }
}