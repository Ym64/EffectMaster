package me.m64diamondstar.effectmaster.shows.utils

import me.m64diamondstar.effectmaster.EffectMaster
import me.m64diamondstar.effectmaster.shows.EffectShow
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import java.io.File
import java.util.*

object ShowUtils {

    private val droppedItems = HashSet<UUID>()
    private val runningShows = HashMap<Pair<String, String>, HashSet<EffectShow>>()

    fun getCategories(): ArrayList<File> {
        val file = File(EffectMaster.plugin().dataFolder, "shows")

        val files = ArrayList<File>()
        file.listFiles()?.let { files.addAll(it) }
        files.remove(File(EffectMaster.plugin().dataFolder, "shows/.DS_Store"))

        return files
    }

    fun getShows(category: String): ArrayList<File> {
        val file = File(EffectMaster.plugin().dataFolder, "shows/$category")

        val files = ArrayList<File>()
        file.listFiles()?.forEach {
            if(!it.name.contains(".DS_Store"))
                files.add(it)
        }

        return files
    }

    fun getAllShows(): List<Pair<File, File>> =
        getCategories().flatMap { category ->
            getShows(category.nameWithoutExtension).map { show ->
                category to show
            }
        }


    fun getCategory(string: String): File{
        return File(EffectMaster.plugin().dataFolder, "shows/$string")
    }

    private fun existsCategory(category: File): Boolean{
        File(EffectMaster.plugin().dataFolder, "shows").mkdirs()
        return File(EffectMaster.plugin().dataFolder, "shows").listFiles()!!.contains(category)
    }

    fun existsCategory(string: String): Boolean{
        return existsCategory(File(EffectMaster.plugin().dataFolder, "shows/$string"))
    }

    private fun existsShow(category: File, show: File): Boolean{
        return if(category.listFiles() == null)
            false
        else
            category.listFiles()!!.contains(show)
    }

    fun existsShow(category: String, show: String): Boolean{
        return existsShow(
            File(EffectMaster.plugin().dataFolder, "shows/$category"), File(
                EffectMaster.plugin().dataFolder,
                if(show.contains(".yml")) "shows/$category/$show" else "shows/$category/$show.yml")
        )
    }

    /**
     * Get all shows which are currently running
     * @return a set of the running shows
     */
    fun getRunningShows(): HashSet<EffectShow>{
        return runningShows.values.flatten().toHashSet()
    }

    /**
     * Get all shows which are currently running within a specific category
     * @param category the category to search in
     * @return a set of all the currently running shows within the given category
     */
    fun getRunningShows(category: String): HashSet<EffectShow>{
        return runningShows
            .filter { it.key.first == category }
            .values
            .flatten()
            .toHashSet()
    }

    /**
     * Get all shows which are currently running within a specific category and with a specific name
     * @param category the category to search in
     * @param name the name to search for
     * @return a set of all the currently running shows within the given category
     */
    fun getRunningShows(category: String, name: String): HashSet<EffectShow> {
        return runningShows
            .filter { it.key.first == category && it.key.second == name.replace(".yml", "") } // Filter by category and name
            .values // Get the corresponding HashSets
            .flatten() // Flatten them into a single list
            .toHashSet() // Convert to a HashSet
    }

    /**
     * Add a show to the currently running shows
     */
    fun addRunningShow(category: String, name: String, effectShow: EffectShow) {
        if(runningShows.containsKey(Pair(category, name.replace(".yml", ""))))
            runningShows[Pair(category, name.replace(".yml", ""))]?.add(effectShow)
        else
            runningShows[Pair(category, name.replace(".yml", ""))] = hashSetOf(effectShow)
    }

    /**
     * Remove a show from the currently running shows
     */
    fun removeRunningShow(category: String, name: String, effectShow: EffectShow) {
        runningShows[Pair(category, name.replace(".yml", ""))]?.remove(effectShow)
    }

    fun getDroppedItems(): HashSet<Item>{
        return droppedItems.mapNotNull { Bukkit.getEntity(it) as Item? }.toHashSet()
    }

    fun addDroppedItem(item: Item){
        droppedItems.add(item.uniqueId)
    }

    fun removeDroppedItem(item: Item){
        droppedItems.remove(item.uniqueId)
    }

    fun containsDroppedItem(itemUUID: UUID): Boolean {
        return droppedItems.contains(itemUUID)
    }

}