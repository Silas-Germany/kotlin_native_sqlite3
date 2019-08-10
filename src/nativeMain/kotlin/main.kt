import cnames.structs.sqlite3
import kotlinx.cinterop.*
import sqlite3.sqlite3_close
import sqlite3.sqlite3_exec
import sqlite3.sqlite3_open

fun main() {
  SqlCipherDatabaseTest()
}

class SqlCipherDatabaseTest {
  private val callback = staticCFunction { _: COpaquePointer?, _: Int, _: CPointer<CPointerVar<ByteVar>>?, _: CPointer<CPointerVar<ByteVar>>? ->
    0
  }

  init {
    val db = memScoped {
      val databasePointer = allocPointerTo<sqlite3>()
      sqlite3_open("/tmp/database.db", databasePointer.ptr)
              .also { println(it) }
      databasePointer.value
    }
    memScoped {
      val error = allocPointerTo<ByteVar>()
      sqlite3_exec(
              db,
              "CREATE TABLE user(id integer);",
              callback,
              intArrayOf(0).refTo(0),
              error.ptr
      ).toString()
              .also { println(it) }
              .also { println(error.value?.toKString()) }
    }
    sqlite3_close(db).toString()
            .also { println(it) }
  }
}
