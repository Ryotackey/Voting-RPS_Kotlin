package red.man10.votingrps_kotlin

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.Double

import java.util.UUID

class Command(private val plugin: VotingRPS_Kotlin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        if (sender !is Player) return false
        val puuid = sender.uniqueId

        when (args.size) {

            0 -> {

                sender.sendMessage("§f§l--------§a§lVoting §e§lRPS§f§l--------")
                sender.sendMessage("§6§l/mv new [金額]§f§r:ゲームを開く")
                sender.sendMessage("§6§l/mv join§f§r:ゲームに参加する")
                sender.sendMessage("§6§l/mv vote§f§r:グーチョキパーのどれかに投票する")
                sender.sendMessage("§6§l/mv game§f§r:インベントリ間違えて閉じた場合開く")
                sender.sendMessage("§l--------------------------------------------")
                if (plugin.battleplayer.size === 2) {
                    sender.sendMessage("§e現在の状況 : §f(§c" + plugin.battleplayer.get(0).getDisplayName() + " §f§lVS §r§b" + plugin.battleplayer.get(1).getDisplayName() + "§f) §6§l掛け金 §f§l" + plugin.bal + "円")
                }
                if (plugin.battleplayer.size === 1) {
                    sender.sendMessage("§e§l" + plugin.battleplayer.get(0).getDisplayName() + "§f§lさんが §6§l掛け金 §f§l" + plugin.bal + "円§f§lで募集してます")
                }

                if (!plugin.voting) {
                    sender.sendMessage(plugin.prefex + "§4§lまだ投票の受付を開始していません")
                    return true
                }

                if (plugin.battleplayer.contains(sender)) {
                    sender.sendMessage(plugin.prefex + "§4§lあなたは参加者なので投票できません")
                    return true
                }

                if (plugin.voteplayer.contains(sender)) {
                    sender.sendMessage(plugin.prefex + "§4§lあなたはすでに投票しています")
                    return true
                }

                plugin.voteinv = Bukkit.createInventory(null, 45, "§a§lVoting Menu")

                for (i in 0..44) {

                    plugin!!.voteinv!!.setItem(i, ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()))

                }

                plugin!!.voteinv!!.setItem(20, plugin.rock)
                plugin!!.voteinv!!.setItem(22, plugin.scissor)
                plugin!!.voteinv!!.setItem(24, plugin.paper)

                sender.openInventory(plugin.voteinv)

                return true
            }

            1 -> {

                if (args[0].equals("join", ignoreCase = true)) {

                    if (!plugin.setup) {
                        sender.sendMessage(plugin.prefex + "§4§lまだ始まってません")
                        return true
                    }

                    if (plugin.battleplayer.size > 1) {
                        sender.sendMessage(plugin.prefex + "§4§l定員オーバーです")
                        return true
                    }

                    if (plugin.battleplayer.contains(sender)) {
                        sender.sendMessage(plugin.prefex + "§4§lあなたはすでに参加しています")
                        return true
                    }

                    if (plugin.vm!!.getBalance(puuid) < plugin.bal!!) {
                        sender.sendMessage(plugin.prefex + "§4§lお金が足りません")
                        return true
                    }

                    plugin.vm!!.withdraw(puuid, plugin.bal!!)

                    plugin.battleplayer.add(sender)

                    plugin.uuid2 = sender.uniqueId

                    Bukkit.broadcastMessage(plugin.prefex + "§e§l" + sender.displayName + "§f§lさん§a§lが参加しました")

                    plugin.timeout = false

                    plugin.game!!.votingTime()

                    return true
                }

                if (args[0].equals("cancel", ignoreCase = true)) {

                    if (plugin.setup === false) {
                        sender.sendMessage(plugin.prefex + "§4§lまだ始まっていません")
                        return true
                    }

                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§lあなたは権限がありません")
                        return true
                    }

                    Bukkit.broadcastMessage(plugin.prefex + "§4§lキャンセルしました")
                    plugin.cancelGame()

                    return true

                }

                if (args[0].equals("view", ignoreCase = true)) {

                    var rock = 0
                    var scissors = 0
                    var paper = 0

                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§lあなたには権限がありません")
                        return true
                    }

                    if (plugin.setup === false) {
                        sender.sendMessage(plugin.prefex + "§4§lまだ始まっていません")
                        return true
                    }

                    for (i in 0 until plugin.recorditem.size) {
                        sender.sendMessage(plugin.prefex + "§c§l" + plugin.recordplayer.get(i).getDisplayName() + "§f§l:" + plugin.recorditem.get(i).getItemMeta().getDisplayName())

                        if (plugin.recorditem.get(i) === plugin.rock) {
                            rock++
                        }
                        if (plugin.recorditem.get(i) === plugin.scissor) {
                            scissors++
                        }
                        if (plugin.recorditem.get(i) === plugin.paper) {
                            paper++
                        }
                    }

                    sender.sendMessage(plugin.prefex + "§7§lグー§f§l:" + rock + "/§c§lチョキ§f§l:" + scissors + "/§b§lパー§f§l:" + paper)

                    rock = 0
                    scissors = 0
                    paper = 0

                    return true
                }

                if (args[0].equals("vote", ignoreCase = true)) {

                    if (!plugin.voting) {
                        sender.sendMessage(plugin.prefex + "§4§lまだ投票の受付を開始していません")
                        return true
                    }

                    if (plugin.battleplayer.contains(sender)) {
                        sender.sendMessage(plugin.prefex + "§4§lあなたは参加者なので投票できません")
                        return true
                    }

                    if (plugin.voteplayer.contains(sender)) {
                        sender.sendMessage(plugin.prefex + "§4§lあなたはすでに投票しています")
                        return true
                    }

                    plugin.voteinv = Bukkit.createInventory(null, 45, "§a§lVoting Menu")

                    for (i in 0..44) {

                        plugin!!.voteinv!!.setItem(i, ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()))

                    }

                    plugin!!.voteinv!!.setItem(20, plugin.rock)
                    plugin!!.voteinv!!.setItem(22, plugin.scissor)
                    plugin!!.voteinv!!.setItem(24, plugin.paper)

                    sender.openInventory(plugin.voteinv)

                    return true

                }

                if (args[0].equals("game", ignoreCase = true)) {

                    if (plugin.setup === false || plugin.voting === true) {
                        return true
                    }

                    if (plugin.battleitem1 != null && plugin.battleitem2 != null) {
                        return true
                    }

                    if (sender === plugin.battleplayer.get(0)) {

                        sender.openInventory(plugin.battleinv1)

                        return true

                    }

                    if (sender === plugin.battleplayer.get(1)) {

                        sender.openInventory(plugin.battleinv2)

                        return true

                    }

                }

                if (args[0].equals("on", ignoreCase = true)) {
                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§l権限がありません")
                        return true
                    }
                    plugin.enable = true
                    sender.sendMessage(plugin.prefex + "§a§l起動しました")
                    return true
                }

                if (args[0].equals("off", ignoreCase = true)) {
                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§l権限がありません")
                        return true
                    }
                    plugin.enable = false
                    if (plugin.setup === true) {
                        plugin.cancelGame()
                    }
                    sender.sendMessage(plugin.prefex + "§a§lオフしました")
                    return true
                }

                if (args[0].equals("reload", ignoreCase = true)) {
                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§l権限がありません")
                        return true
                    }
                    plugin.config!!.reloadConfig()
                    sender.sendMessage(plugin.prefex + "§aReload Complite")
                    return true
                }

                if (args[0].equals("add", ignoreCase = true)) {
                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§l権限がありません")
                        return true
                    }

                    if (!plugin.voting) {
                        sender.sendMessage(plugin.prefex + "§4§lまだ投票の受付を開始していません")
                        return true
                    }

                    plugin.voteinv = Bukkit.createInventory(null, 45, "§a§lVoting Menu")

                    for (i in 0..44) {

                        plugin!!.voteinv!!.setItem(i, ItemStack(Material.STAINED_GLASS_PANE, 1, 15.toShort()))

                    }

                    plugin!!.voteinv!!.setItem(20, plugin.rock)
                    plugin!!.voteinv!!.setItem(22, plugin.scissor)
                    plugin!!.voteinv!!.setItem(24, plugin.paper)

                    sender.openInventory(plugin.voteinv)

                }
            }

            2 -> {
                if (args[0].equals("new", ignoreCase = true)) {

                    if (plugin.enable === false) {

                        sender.sendMessage(plugin.prefex + "§4§l今起動できません")

                        return true

                    }

                    if (plugin.setup) {
                        sender.sendMessage(plugin.prefex + "§4§lすでに始まっています!")

                        return true
                    }

                    try {
                        plugin.bal = Double.parseDouble(args[1])
                        if (plugin.config!!.getConfig()!!.getInt("minbal") > plugin.bal!!) {
                            sender.sendMessage(plugin.prefex + "§4§l最低金額は" + plugin.config!!.getConfig()!!.getInt("minbal") + "円です")
                            return false
                        }

                    } catch (vrps: NumberFormatException) {
                        sender.sendMessage(plugin.prefex + "§4§l金額を指定してください.")
                        return false

                    }

                    if (plugin.vm!!.getBalance(puuid) < plugin.bal!!) {
                        sender.sendMessage(plugin.prefex + "§4§lお金が足りません")
                        return true
                    }

                    plugin.vm!!.withdraw(puuid, plugin.bal!!)

                    plugin.setup = true

                    plugin.battleplayer.add(sender)

                    plugin.uuid1 = sender.uniqueId

                    Bukkit.broadcastMessage(plugin.prefex + "§e§l" + sender.name + "§f§rさんが§6§l" + plugin.jpnBalForm(plugin.bal!!) + "§f§l円の§a§l投票じゃんけん§f§rを開始しました:§l/mv")

                    plugin.timeout = true

                    plugin.game!!.timeOut()

                    return true

                }

                if (args[0].equals("time", ignoreCase = true)) {
                    if (!sender.hasPermission("votingrps.op")) {
                        sender.sendMessage(plugin.prefex + "§4§l権限がありません")
                        return true
                    }

                    val time: Int

                    try {

                        time = Integer.parseInt(args[1])

                    } catch (vrps: NumberFormatException) {
                        sender.sendMessage(plugin.prefex + "§4§l時間を指定してください.")
                        return false

                    }

                    plugin.config!!.getConfig()!!.set("time", time)
                    plugin.config!!.saveConfig()

                    sender.sendMessage(plugin.prefex + "§a§l時間を設定しました")

                    return true

                }
            }
        }



        return false
    }
}
