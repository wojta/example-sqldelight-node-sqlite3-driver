import com.example.Database
import com.example.sqldelight.hockey.data.HockeyPlayer
import com.example.sqldelight.hockey.data.PlayerQueries
import cz.sazel.sqldelight.node.sqlite3.SQLite3JsException
import cz.sazel.sqldelight.node.sqlite3.executeSuspendingAsList
import cz.sazel.sqldelight.node.sqlite3.initSqlite3SqlDriver

suspend fun main() {
    val driver = initSqlite3SqlDriver(filename = "test.db", schema = Database.Schema)

    val database = Database(driver)

    val playerQueries: PlayerQueries = database.playerQueries


    val selectAll = playerQueries.selectAll()

    println(selectAll.executeSuspendingAsList())
    // Prints [HockeyPlayer(15, "Ryan Getzlaf")]

    database.transaction {
        playerQueries.insert(player_number = 10, full_name = "Corey Perry")
    }

    println(selectAll.executeSuspendingAsList())
    // Prints [HockeyPlayer(15, "Ryan Getzlaf"), HockeyPlayer(10, "Corey Perry")]

    val player = HockeyPlayer(20, "Ronald McDonald")
    database.transaction {
        playerQueries.insertFullPlayerObject(player)
    }

    //rollback
    try {
        database.transaction {
            playerQueries.insertFullPlayerObject(player)
        }
    } catch (e: SQLite3JsException) {
        println("Rollback $e")
    }

    println(selectAll.executeSuspendingAsList())
    // Prints [HockeyPlayer(15, "Ryan Getzlaf"), HockeyPlayer(10, "Corey Perry"), HockeyPlayer(20, "Ronald McDonald")]

}
