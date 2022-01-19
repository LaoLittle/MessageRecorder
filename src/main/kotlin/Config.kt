package org.laolittle.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority

object Config : AutoSavePluginConfig("MessageRecorderConfig") {
    @ValueDescription("监听器优先级")
    val priority by value(EventPriority.MONITOR)
}