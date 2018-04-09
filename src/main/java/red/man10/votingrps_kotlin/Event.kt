package red.man10.votingrps_kotlin

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerQuitEvent

class Event(private val plugin: VotingRPS_Kotlin) : Listener {

    @EventHandler
    fun onClickInventory(e: InventoryClickEvent) {

        if (plugin.setup === false) {
            return
        }

        val p = e.whoClicked as Player

        if (p === plugin.battleplayer.get(0)) {

            if (e.inventory.name.equals("§a§lVoting §e§lRPS", ignoreCase = true)) {

                e.isCancelled = true

                if (e.currentItem.type == Material.STONE) {

                    plugin.battleitem1 = plugin.rock

                    p.sendMessage(plugin.prefex + "§a§lグーを選びました")

                    plugin.battleinv1!!.clear(e.slot)

                    plugin.battleplayer.get(0).closeInventory()

                }

                if (e.currentItem.type == Material.PAPER) {

                    plugin.battleitem1 = plugin.paper

                    p.sendMessage(plugin.prefex + "§a§lパーを選びました")

                    plugin.battleinv1!!.clear(e.slot)

                    plugin.battleplayer.get(0).closeInventory()

                }

                if (e.currentItem.type == Material.SHEARS) {

                    plugin.battleitem1 = plugin.scissor

                    p.sendMessage(plugin.prefex + "§a§lチョキを選びました")

                    plugin.battleinv1!!.clear(e.slot)

                    plugin.battleplayer.get(0).closeInventory()

                }

                if (plugin.battleitem1 != null && plugin.battleitem2 != null) {

                    plugin.gametimeout = false

                    plugin.game!!.result()

                }

                plugin.select = e.slot

                return

            }

        }

        if (p === plugin.battleplayer.get(1)) {

            if (e.inventory.name.equals("§a§lVoting §e§lRPS", ignoreCase = true)) {

                e.isCancelled = true

                if (e.currentItem.type == Material.STONE) {

                    plugin.battleitem2 = plugin.rock

                    p.sendMessage(plugin.prefex + "§a§lグーを選びました")

                    plugin.battleinv2!!.clear(e.slot)

                    plugin.battleplayer.get(1).closeInventory()

                }

                if (e.currentItem.type == Material.PAPER) {

                    plugin.battleitem2 = plugin.paper

                    p.sendMessage(plugin.prefex + "§a§lパーを選びました")

                    plugin.battleinv2!!.clear(e.slot)

                    plugin.battleplayer.get(1).closeInventory()

                }

                if (e.currentItem.type == Material.SHEARS) {

                    plugin.battleitem2 = plugin.scissor

                    p.sendMessage(plugin.prefex + "§a§lチョキを選びました")

                    plugin.battleinv2!!.clear(e.slot)

                    plugin.battleplayer.get(1).closeInventory()

                }

                if (plugin.battleitem1 != null && plugin.battleitem2 != null) {

                    plugin.gametimeout = false

                    plugin.game!!.result()

                }

                plugin.select2 = e.slot

                return

            }
        }

        if (e.inventory.name.equals("§a§lVoting Menu", ignoreCase = true)) {

            e.isCancelled = true

            if (e.currentItem.type == Material.STONE) {

                plugin.voteitem.add(plugin.rock)
                plugin.recorditem.add(plugin.rock)

                p.sendMessage(plugin.prefex + "§a§lグーに投票しました")

                Bukkit.broadcastMessage(plugin.prefex + "§e§l" + p.displayName + "§a§lが投票しました §f§l<§6§l現在" + plugin.voteitem.size + "枚です§f§l>")

                plugin.voteplayer.add(p)
                plugin.recordplayer.add(p)

                p.closeInventory()

                return

            }

            if (e.currentItem.type == Material.PAPER) {

                plugin.voteitem.add(plugin.paper)
                plugin.recorditem.add(plugin.paper)

                p.sendMessage(plugin.prefex + "§a§lパーに投票しました")

                Bukkit.broadcastMessage(plugin.prefex + "§e§l" + p.displayName + "§a§lが投票しました §f§l<§6§l現在" + plugin.voteitem.size + "枚です§f§l>")

                plugin.voteplayer.add(p)
                plugin.recordplayer.add(p)

                p.closeInventory()

                return

            }

            if (e.currentItem.type == Material.SHEARS) {

                plugin.voteitem.add(plugin.scissor)
                plugin.recorditem.add(plugin.scissor)

                p.sendMessage(plugin.prefex + "§a§lチョキに投票しました")

                Bukkit.broadcastMessage(plugin.prefex + "§e§l" + p.displayName + "§a§lが投票しました §f§l<§6§l現在" + plugin.voteitem.size + "枚です§f§l>")

                plugin.voteplayer.add(p)
                plugin.recordplayer.add(p)

                p.closeInventory()

                return

            }


        }


    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        if (plugin.battleplayer.contains(p)) {

            Bukkit.broadcastMessage(plugin.prefex + "§4§l" + p.displayName + "さんが退出したためキャンセルしました")

            plugin.cancelGame()

            return
        }

        if (plugin.recordplayer.contains(p)) {

            plugin.voteitem.removeAt(plugin.recordplayer.indexOf(p))
            plugin.recorditem.removeAt(plugin.recordplayer.indexOf(p))

            plugin.voteplayer.remove(p)
            plugin.recordplayer.remove(p)

            Bukkit.broadcastMessage("§e§l" + p.displayName + "§f§lさんが退出したため投票から外されました §f§l<§6§l現在" + plugin.voteitem.size + "枚です§f§l>")



        }

        return

    }

}
