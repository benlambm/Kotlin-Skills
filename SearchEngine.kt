package search

import java.io.File
import kotlin.system.exitProcess

class Search(fileName: String) {
    init {
        Corpus.loadInvertedIndex(fileName)
    }

    fun start() {
        while (true) {
            displayMenu()
            when (readln().toInt()) {
                0 -> exitProcess(0)
                1 -> newSearch()
                2 -> INPUT_LINES_LIST.forEach { println(it)}
                else -> println("Invalid choice. Please try again.")
            }
        }
    }

    //Singleton design pattern used for one global Inverted Index (probably overkill but good practice!)
    companion object Corpus {
        // temporary text builder used to remember the text input by line numbers
        private var tmp_text = mutableListOf<String>()

        // the Inverted Index data structure will be immutable once initialized
        private var _data: MutableMap<String, MutableSet<Int>> = mutableMapOf()
        val data: Map<String, Set<Int>>
            get() = _data.toMap()

        fun loadInvertedIndex(fileName: String) {
            File(fileName).useLines {
                it.forEachIndexed { index, line ->
                    line.split(" ").forEach { key ->
                        _data.computeIfAbsent(key) { mutableSetOf() } // this was new to me too!
                             .add(index)
                    }
                tmp_text += line
                }
            }
        }
        val INPUT_LINES_LIST: List<String> = tmp_text
    }

    //This enum contains functions per ANY, ALL, NONE that perform the search strategy
    enum class SearchStrategy {
        ANY {
            override fun find(query: String): List<Int> {
                val terms = query.split(" ")
                val lineNums = mutableSetOf<Int>()
                for (term in terms){
                    data.keys.forEach { k ->
                        if (k.equals(term, ignoreCase = true)) {
                            for (v in data[k]!!) {
                                lineNums += v
                            }
                        }
                    }
                }
                return lineNums.toList()
            }
        },
        ALL {
            override fun find(query: String): List<Int> {
                val terms = query.split(" ")
                val potentialMatches = ANY.find(query)
                val actualMatches = potentialMatches.toMutableList()
                for (int in potentialMatches){
                    terms.forEach {
                        if (!INPUT_LINES_LIST[int].contains(it)) {
                            actualMatches.remove(int)
                        }
                    }
                }
                return actualMatches.toList()
            }

        },
        NONE {
            override fun find(query: String): List<Int> {
                val hits = ANY.find(query).toSet()
                return INPUT_LINES_LIST.indices.toSet().subtract(hits).toList()
            }
        };
        abstract fun find(query: String): List<Int>
    }

    private fun newSearch() {
        println("Enter search strategy (ALL, ANY, NONE):")
        val strategy = SearchStrategy.valueOf(readln())
        println("Enter search term:")
        val query = readln()
        val searchResults = strategy.find(query)
        if (searchResults.isEmpty()) {
            println("No search results found.")
        } else {
            searchResults.forEach {
                println(INPUT_LINES_LIST[it])
            }
        }
    }

    private fun displayMenu() {
        println("""
            |
            |====== Menu ======
            |
            |   1. Find a person
            |   2. Print all people
            |   0. Exit
            |   
            """.trimMargin())
    }
}

fun main(args: Array<String>) = Search(args.last()).start()
