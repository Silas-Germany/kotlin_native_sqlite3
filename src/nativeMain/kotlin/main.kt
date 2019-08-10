import kotlinx.cinterop.*
import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import sqlite3.*

fun main() {
  SqlCipherDatabaseTest()
}

class SqlCipherDatabaseTest {
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
              "CREATE TABLE 'user'(" +
                      "'id' BLOB PRIMARY KEY," +
                      "'name' TEXT," +
                      "'phone' INTEGER" +
                      ");",
              null,
              intArrayOf(0).refTo(0),
              error.ptr
      ).toString()
              .also { println(it) }
              .also { println(error.value?.toKString()) }
    }
    memScoped {
      val error = allocPointerTo<ByteVar>()
      sqlite3_exec(
              db,
              "INSERT INTO android_app(id, name, phone) VALUES " +
                      "(x'080a', 'HI THERE', null)," +
                      "(x'110305', 'HI THERE!', 234803)," +
                      "(null, 'HI? THERE!', 3)," +
                      "(x'', ' THERE!', 23);",
              null,
              intArrayOf(0).refTo(0),
              error.ptr
      ).toString()
              .also { println(it) }
              .also { println(error.value?.toKString()) }
    }
    val cursor = memScoped {
      val cursorPointer = allocPointerTo<sqlite3_stmt>()
      sqlite3_prepare_v2(
              db,
              "SELECT * FROM android_app;",
              -1,
              cursorPointer.ptr,
              null
      ).toString()
              .also { println(it) }
      cursorPointer.value
    }
    var stepStatus: Int = sqlite3_step(cursor)
    while (stepStatus == SQLITE_ROW) {
      repeat(sqlite3_column_count(cursor)) {
        when (sqlite3_column_type(cursor, it)) {
          SQLITE_NULL -> println("null type")
          SQLITE_INTEGER -> println(sqlite3_column_int(cursor, it))
          SQLITE_FLOAT -> println(sqlite3_column_double(cursor, it))
          SQLITE_TEXT -> println(getText(cursor, it))
          SQLITE_BLOB -> println(getBlob(cursor, it).contentToString())
          else -> println("Unknown column type")
        }
      }
      println("READ")
      stepStatus = sqlite3_step(cursor)
    }
    if (stepStatus != SQLITE_DONE) {
      println(sqlite3_errmsg(db)?.toKString())
    }
    sqlite3_finalize(cursor)
    sqlite3_close(db).toString()
            .also { println(it) }
  }

  private fun getBlob(cursor: CPointer<sqlite3_stmt>?, column: Int): ByteArray {
    cursor ?: return byteArrayOf()
    val size = sqlite3_column_bytes(cursor, column)
    return sqlite3_column_blob(cursor, column)?.readBytes(size) ?: byteArrayOf()
  }

  private fun getText(cursor: CPointer<sqlite3_stmt>?, column: Int): String {
    cursor ?: return ""
    val size = sqlite3_column_bytes(cursor, column)
    return sqlite3_column_text(cursor, column)?.readBytes(size)?.stringFromUtf8() ?: ""
  }
}
