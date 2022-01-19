@file: Suppress("unused")

package org.laolittle.plugin

import com.alibaba.druid.pool.DruidDataSource
import kotlinx.coroutines.sync.Mutex
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import javax.sql.DataSource
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object MessageDatabase {
    val database by lazy {
        val dataSource = DruidDataSource()
        dataSource.url = "jdbc:sqlite:${MessageRecorder.dataFolder}/messageData.sqlite"
        dataSource.driverClassName = "org.sqlite.JDBC"
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
        Database.connect(dataSource as DataSource)
    }

    private val mutex = Mutex()
    private var locked = false

    val isLocked get() = locked

    suspend fun lock() {
        // while (isLocked) { }
        mutex.lock()
        locked = true
    }

    fun unlock() {
        locked = false
        mutex.unlock()
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <T> alsoLock(owner: Any? = null, action: () -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }

        mutex.lock(owner)
        locked = true
        try {
            return action()
        } finally {
            locked = false
            mutex.unlock(owner)
        }
    }
}

private class DataCannotAccessException(message: String? = null) : Exception(message)