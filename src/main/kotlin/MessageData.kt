package org.laolittle.plugin

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

class MessageData(groupId: Long) : Table("messages_$groupId") {
    val time = datetime("time")
    val content = varchar("content", 4500)
}

class ImageData(groupId: Long) : Table("images_$groupId") {
    val time = datetime("time")
    val images = varchar("images", 4500)
}