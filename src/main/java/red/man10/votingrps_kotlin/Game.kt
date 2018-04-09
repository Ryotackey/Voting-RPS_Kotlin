package red.man10.votingrps_kotlin

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

import java.util.Random

class Game(private val plugin: VotingRPS_Kotlin) {

    internal var time: Int = 0

    internal var timer: Int = 0

    internal var finalp: Player? = null

    internal var canceltime: Int = 0

    internal var votereward: Double = 0.0

    fun votingTime() {       //投票フェーズ

        if (!plugin.setup) {
            return
        }

        time = plugin.config!!.getConfig()!!.getInt("time")

        plugin.voting = true

        votereward = plugin.bal!! * plugin.config!!.getConfig()!!.getDouble("rewardmoney")

        Bukkit.broadcastMessage(plugin.prefex + "§a§l投票フェーズが始まりました")
        Bukkit.broadcastMessage(plugin.prefex + "§f§r投票に参加する人は§b§l/mv§fからどうぞ")

        object : BukkitRunnable() {
            override fun run() {

                if (time == 0) {

                    if (plugin.voteitem.size < plugin.config!!.getConfig()!!.getInt("minvote")) {

                        Bukkit.broadcastMessage(plugin.prefex + "§4§l投票数が足りないのでキャンセルします")

                        cancel()

                        plugin.cancelGame()

                        return

                    }

                    Bukkit.broadcastMessage(plugin.prefex + "§c受付終了しました")
                    time = plugin.config!!.getConfig()!!.getInt("time")
                    plugin.voting = false
                    cancel()
                    game()
                    return
                }

                if (time == -1 ){

                    cancel()
                    return

                }

                if (time % 10 == 0 || time <= 5) {
                    Bukkit.broadcastMessage(plugin.prefex + "§a投票受付終了まであと§f§l" + time + "秒")
                }

                time--

            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun game() {         //じゃんけんgui開く

        plugin.battleinv1 = Bukkit.createInventory(null, 45, "§a§lVoting §e§lRPS")
        plugin.battleinv2 = Bukkit.createInventory(null, 45, "§a§lVoting §e§lRPS")

        val slot = intArrayOf(20, 22, 24)

        for (i in 0..44) {

            plugin.battleinv1!!.setItem(i, ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()))
            plugin.battleinv2!!.setItem(i, ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()))

        }

        for (i in 0..2) {

            val r = Random()

            val random = r.nextInt(plugin.voteitem.size)

            val ritem = plugin.voteitem.get(random)

            plugin.battleinv1!!.setItem(slot[i], ritem)

            plugin.item1.add(ritem)

            plugin.voteitem.removeAt(random)

            plugin.selection[slot[i]] = plugin.voteplayer[random]

            plugin.voteplayer.removeAt(random)

        }

        for (i in 0..2) {

            val r = Random()

            val random = r.nextInt(plugin.voteitem.size)

            val ritem = plugin.voteitem.get(random)

            plugin.battleinv2!!.setItem(slot[i], plugin.voteitem.get(random))

            plugin.item2.add(ritem)

            plugin.voteitem.removeAt(random)

            plugin.selection2[slot[i]] = plugin.voteplayer[random]

            plugin.voteplayer.removeAt(random)

        }

        plugin.battleplayer.get(0).openInventory(plugin.battleinv1)
        plugin.battleplayer.get(1).openInventory(plugin.battleinv2)

        plugin.gametimeout = true
        gameTimeOut()

        return

    }

    fun result() {

        if (!plugin.setup) {
            return
        }

        timer = 3

        var finalmove: ItemStack? = null

        Bukkit.broadcastMessage(plugin.prefex + "§e§lジャンケン！...§f§k§laaa")

        object : BukkitRunnable() {
            override fun run() {

                if (timer == 0) {

                    Bukkit.broadcastMessage(plugin.prefex + "§e§lポン！")

                    val judge = judge()

                    when (judge) {

                        0 -> {

                            Bukkit.broadcastMessage(plugin.prefex + "§a§lあいこでした")

                            cancel()
                            reGame()

                            return
                        }

                        1 -> {

                            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer[0].displayName + "§f§lさんの§6§l勝利！")

                            val p1 = Bukkit.getOfflinePlayer(plugin.uuid1)
                            if (p1 == null) {
                                Bukkit.getLogger().info(plugin.uuid1.toString() + "は見つからない")
                            } else {
                                plugin.vm!!.deposit(plugin.uuid1!!, plugin.bal!! * 2 - votereward)
                            }

                            finalp = plugin.selection[plugin.select]

                            finalmove = plugin.recorditem[plugin.recordplayer.indexOf(finalp!!)]


                        }

                        2 -> {

                            Bukkit.broadcastMessage(plugin.prefex + "§b§l" + plugin.battleplayer[1].displayName + "§f§lさんの§6§l勝利！")

                            val p2 = Bukkit.getOfflinePlayer(plugin.uuid2)
                            if (p2 == null) {
                                Bukkit.getLogger().info(plugin.uuid2.toString() + "は見つからない")
                            } else {
                                plugin.vm!!.deposit(plugin.uuid2!!, plugin.bal!! * 2 - votereward)
                            }

                            finalp = plugin.selection2[plugin.select2]

                            finalmove = plugin.recorditem[plugin.recordplayer.indexOf(finalp!!)]

                        }
                    }

                    Bukkit.broadcastMessage(plugin.prefex + "§7§l" + finalmove!!.itemMeta.displayName + "§f§lを出して§e§l" + finalp!!.displayName + "§f§lさん§a§lは投票者の中から選ばれ§6§l" + plugin.jpnBalForm(votereward) + "円§f§l支払われました")

                    val p = Bukkit.getOfflinePlayer(finalp!!.uniqueId)
                    if (p == null) {
                        Bukkit.getLogger().info(finalp!!.uniqueId.toString() + "は見つからない")
                    } else {
                        plugin.vm!!.deposit(finalp!!.uniqueId, votereward)
                    }


                    cancel()

                    plugin.finishGame()

                }

                timer--

            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun judge(): Int {

        val me: MySQLexcute

        if (plugin.battleitem1 === plugin.battleitem2) {
            return 0
        }

        if (plugin.battleitem1 === plugin.rock && plugin.battleitem2 === plugin.paper) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lグー §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lパー")

            me = MySQLexcute(plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 2, plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 0, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()
            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 2
        }

        if (plugin.battleitem1 === plugin.rock && plugin.battleitem2 === plugin.scissor) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lグー §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lチョキ")

            me = MySQLexcute(plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 0, plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 1, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()

            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 1
        }

        if (plugin.battleitem1 === plugin.paper && plugin.battleitem2 === plugin.scissor) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lパー §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lチョキ")

            me = MySQLexcute(plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 1, plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 2, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()

            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 2
        }

        if (plugin.battleitem1 === plugin.paper && plugin.battleitem2 === plugin.rock) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lパー §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lグー")

            me = MySQLexcute(plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 2, plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 0, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()

            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 1
        }

        if (plugin.battleitem1 === plugin.scissor && plugin.battleitem2 === plugin.paper) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lチョキ §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lパー")

            me = MySQLexcute(plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 1, plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 2, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()

            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 1
        }

        if (plugin.battleitem1 === plugin.scissor && plugin.battleitem2 === plugin.rock) {

            Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + " §f§l: §6§lチョキ §f§lVS§b§l" + plugin.battleplayer.get(1).getDisplayName() + " §f§l: §6§lグー")

            me = MySQLexcute(plugin.battleplayer.get(1).getName(), plugin.battleplayer.get(1).getUniqueId().toString(), 0, plugin.battleplayer.get(0).getName(), plugin.battleplayer.get(0).getUniqueId().toString(), 1, plugin.bal!!.toInt(), plugin.recordplayer.size, plugin)
            me.start()

            try {
                me.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return 2
        }

        return -1
    }

    fun reGame() {

        if (!plugin.battleinv1!!.contains(plugin.rock) && !plugin.battleinv1!!.contains(plugin.paper) && !plugin.battleinv1!!.contains(plugin.scissor) && !plugin.battleinv2!!.contains(plugin.rock) && !plugin.battleinv2!!.contains(plugin.paper) && !plugin.battleinv2!!.contains(plugin.scissor)) {

            Bukkit.broadcastMessage(plugin.prefex + "§4§lすべてあいこのためキャンセルされました")

            plugin.cancelGame()
            return

        }

        plugin.battleitem1 = null
        plugin.battleitem2 = null

        plugin.battleplayer.get(0).openInventory(plugin.battleinv1)
        plugin.battleplayer.get(1).openInventory(plugin.battleinv2)

        return

    }

    fun timeOut() {

        canceltime = plugin.config!!.getConfig()!!.getInt("canceltime")

        object : BukkitRunnable() {
            override fun run() {

                if (canceltime == 0) {

                    Bukkit.broadcastMessage(plugin.prefex + "§4§lゲームを募集してから一定時間たったためキャンセルされました")

                    cancel()

                    plugin.cancelGame()



                    return

                }

                if (plugin.timeout === false) {

                    cancel()
                    return

                }

                canceltime--


            }
        }.runTaskTimer(plugin, 0, 20)

    }

    fun gameTimeOut() {

        canceltime = plugin.config!!.getConfig()!!.getInt("canceltime")

        object : BukkitRunnable() {

            override fun run() {

                if (canceltime == 0) {

                    if (plugin.battleitem1 != null && plugin.battleitem2 == null) {
                        Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(1).getDisplayName() + "§r§fがゲームを放棄したため§e§l" + plugin.battleplayer.get(0).getDisplayName() + "§aの不戦勝です")

                        plugin.vm!!.deposit(plugin.uuid1!!, plugin.bal!! * 2 - votereward)
                    }

                    if (plugin.battleitem1 == null && plugin.battleitem2 != null) {
                        Bukkit.broadcastMessage(plugin.prefex + "§c§l" + plugin.battleplayer.get(0).getDisplayName() + "§r§fがゲームを放棄したため§e§l" + plugin.battleplayer.get(1).getDisplayName() + "§aの不戦勝です")

                        plugin.vm!!.deposit(plugin.uuid2!!, plugin.bal!! * 2 - votereward)

                    }

                    if (plugin.battleitem1 == null && plugin.battleitem2 == null) {

                        cancel()

                        Bukkit.broadcastMessage(plugin.prefex + "§r§f二人ともがゲームを放棄したため§a§lキャンセルされました")

                        plugin.cancelGame()
                        return

                    }

                    cancel()
                    plugin.finishGame()

                    return

                }

                if (plugin.gametimeout === false) {

                    cancel()
                    return

                }

                canceltime--
            }
        }.runTaskTimer(plugin, 0, 20)

    }

}
