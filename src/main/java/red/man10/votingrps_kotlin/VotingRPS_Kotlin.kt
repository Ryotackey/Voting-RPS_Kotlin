package red.man10.votingrps_kotlin

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import red.man10.kotlin.CustomConfig
import red.man10.kotlin.MySOLManager
import red.man10.kotlin.VaultManager
import java.sql.SQLException

import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

class VotingRPS_Kotlin : JavaPlugin() {

    var enable = true

    internal var vm: VaultManager? = null
    var config: CustomConfig? = null
    var mysql: MySOLManager? = null

    var battleinv1: Inventory? = null
    var battleinv2: Inventory? = null

    var voteinv: Inventory? = null

    var battleplayer = ArrayList<Player>()
    var voteitem = ArrayList<ItemStack>()
    var recorditem = ArrayList<ItemStack>()
    var voteplayer: ArrayList<Player> = ArrayList()
    var recordplayer: ArrayList<Player> = ArrayList()

    var item1: ArrayList<ItemStack> = ArrayList()
    var item2: ArrayList<ItemStack> = ArrayList()

    val prefex = "§l[§a§lVoting§e§lRPS§f§l]"

    var bal: Double? = null

    var setup = false
    var voting = false

    var timeout = false         //時間でキャンセル処理
    var gametimeout = false

    var game: Game? = null

    var battleitem1: ItemStack? = null
    var battleitem2: ItemStack? = null

    var uuid1: UUID? = null
    var uuid2: UUID? = null

    val rock = ItemStack(Material.STONE)
    val scissor = ItemStack(Material.SHEARS)
    val paper = ItemStack(Material.PAPER)

    var selection = HashMap<Int, Player>()
    var selection2 = HashMap<Int, Player>()

    var select: Int = 0
    var select2: Int = 0

    var rockm = rock.itemMeta
    var paperm = paper.itemMeta
    var scissorm = scissor.itemMeta

    override fun onEnable() {
        // Plugin startup logic

        vm = VaultManager(this)
        mysql = MySOLManager(this, "mv")
        config = CustomConfig(this)
        config!!.saveDefaultConfig()
        game = Game(this)

        getCommand("mv").setExecutor(Command(this))

        server.pluginManager.registerEvents(Event(this), this)

        rockm.displayName = "§e§lグー"
        paperm.displayName = "§e§lパー"
        scissorm.displayName = "§e§lチョキ"

        rock.itemMeta = rockm
        paper.itemMeta = paperm
        scissor.itemMeta = scissorm

        val mcr = MySQLCreate(this)
        mcr.start()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    internal fun jpnBalForm(`val`: Double): String {
        val val2 = `val`.toLong()

        var addition = ""
        var form = "万"
        var man = val2 / 10000
        if (`val` >= 100000000) {
            man = val2 / 100000000
            form = "億"
            val mann = (val2 - man * 100000000) / 10000
            addition = mann.toString() + "万"
        }
        return man.toString() + form + addition
    }

    fun cancelGame() {

        for (i in 0 until battleplayer.size) {
            battleplayer.get(i).closeInventory()
        }

        timeout = false
        gametimeout = false

        item1.clear()
        item2.clear()

        voteitem.clear()
        recorditem.clear()
        selection.clear()
        selection2.clear()

        recordplayer.clear()

        setup = false

        voting = false



        game!!.time = -1

        voteplayer.clear()

        battleitem1 = null
        battleitem2 = null

        val p1 = Bukkit.getOfflinePlayer(uuid1)

        if (p1 == null) {
            Bukkit.getLogger().info(uuid1.toString() + "は見つからない")
        } else {
            vm!!.deposit(this!!.uuid1!!, this!!.bal!!)
        }

        if (battleplayer.size == 2) {

            val p2 = Bukkit.getOfflinePlayer(uuid2)
            if (p2 == null) {
                Bukkit.getLogger().info(uuid2.toString() + "は見つからない")
            } else {
                vm!!.deposit(this!!.uuid2!!, this!!.bal!!)
            }

        }

        battleplayer.clear()

        bal = java.lang.Double.valueOf(0.0)

        uuid1 = null
        uuid2 = null

    }

    fun finishGame() {

        for (i in 0 until battleplayer.size) {
            battleplayer.get(i).closeInventory()
        }

        item1.clear()
        item2.clear()

        battleplayer.clear()

        voteitem.clear()
        recorditem.clear()
        recordplayer.clear()
        selection.clear()
        selection2.clear()

        setup = false

        voting = false

        bal = java.lang.Double.valueOf(0.0)

        game!!.time = -1

        voteplayer.clear()

        battleitem1 = null
        battleitem2 = null

        uuid1 = null
        uuid2 = null

    }

}
class MySQLexcute(private val winner_name: String, private val winner_uuid: String, private val winner_move: Int, private val loser_name: String, private val loser_uuid: String, private val loser_move: Int, private val bet: Int, private val vote_count: Int, private val plugin: VotingRPS_Kotlin) : Thread() {

    var host: String = ""
    var user: String = ""
    var pass: String = ""
    var port: String = ""
    var db: String = ""

    init {

        if (plugin.config!!.getConfig()!!.getString("mysql")  != null) {

            host = plugin.config!!.getConfig()!!.getString("mysql.host")
            user = plugin.config!!.getConfig()!!.getString("mysql.user")
            pass = plugin.config!!.getConfig()!!.getString("mysql.pass")
            port = plugin.config!!.getConfig()!!.getString("mysql.port")
            db = plugin.config!!.getConfig()!!.getString("mysql.db")
        }

    }

    override fun run() {

        if (plugin.config!!.getConfig()!!.getString("mysql")  == null){
            Bukkit.getLogger().info("mysql is null")
            return
        }

        if (plugin.mysql!!.Connect(host, db, user, pass, port) === false) {

            Bukkit.getLogger().info("Failed Conected MySQL")

            return

        }

        plugin.mysql!!.execute("insert into vrps_record(winner, winner_uuid, winner_move, loser, loser_uuid, loser_move, player_bet, vote_count) values('$winner_name', '$winner_uuid', $winner_move, '$loser_name', '$loser_uuid', $loser_move, $bet, $vote_count);")

        val rs = plugin.mysql!!.query("SELECT * FROM vrps_record ORDER BY id desc limit 1;")

        try {
            while (rs!!.next()) {
                for (i in plugin.recorditem.indices) {

                    var move = -1

                    when (plugin.recorditem.get(i).getItemMeta().getDisplayName()) {

                        "§e§lグー" ->

                            move = 0

                        "§e§lチョキ" ->

                            move = 1

                        "§e§lパー" ->

                            move = 2
                    }

                    plugin.mysql!!.execute("insert into vrps_vote(gameid, name, uuid, vote) values(" + rs.getInt("id") + ", '" + plugin.recordplayer.get(i).getName() + "', '" + plugin.recordplayer.get(i).getUniqueId().toString() + "', " + move + ");")


                }

            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

}

internal class MySQLCreate(private val plugin: VotingRPS_Kotlin) : Thread() {
    override fun run() {

        plugin.mysql!!.execute("CREATE TABLE `vrps_vote` (\n" +
                "  `id` int(32) NOT NULL AUTO_INCREMENT,\n" +
                "  `gameid` int(32) DEFAULT NULL,\n" +
                "  `name` varchar(50) DEFAULT NULL,\n" +
                "  `uuid` varchar(50) DEFAULT NULL,\n" +
                "  `vote` int(32) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`));")


        plugin!!.mysql!!.execute("CREATE TABLE `vrps_record` (\n" +
                "  `id` int(32) NOT NULL AUTO_INCREMENT,\n" +
                "  `winner` varchar(50) DEFAULT NULL,\n" +
                "  `winner_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `winner_move` int(32) DEFAULT NULL,\n" +
                "  `loser` varchar(50) DEFAULT NULL,\n" +
                "  `loser_uuid` varchar(50) DEFAULT NULL,\n" +
                "  `loser_move` int(32) DEFAULT NULL,\n" +
                "  `player_bet` int(32) DEFAULT NULL,\n" +
                "  `vote_count` int(32) DEFAULT NULL,\n" +
                "  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`id`));")


        Bukkit.getLogger().info("Create Record Table")

    }


}