import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    val data = readCsv("src/main/california_housing.csv")
    val cols: IntArray = intArrayOf(2, 3);
    val clusters = getClusters(data, cols, 5, true, epsilon = 0.0)
    clusters.forEach {
        it.forEach {
            print("$it  ")
        }
        println()
    }
}

fun readCsv(fileName: String): List<List<String>> {
    val csv = mutableListOf<List<String>>()
    File(fileName).useLines { lines -> lines.forEach {
        csv.add(it.split(","))
    }}
    return csv.toList()
}

// possible params: data, columns (index), hasHeader (true/false), k (# of clusters), maxIterations, epsilon (desired accuracy)
fun getClusters(
    data: List<List<String>>,
    columns: IntArray,
    k: Int,
    hasHeader: Boolean = true, // probably needed
    maxIterations: Int = 10000,
    epsilon: Double = 0.0,
): List<DoubleArray> {

    //initializing iteration count
    var iteration: Int = 1;

    //initializing difference
    var difference: Double = Double.MAX_VALUE

    // Filter data only pertinent columns and remove rows with NaN values
    val filteredData = filterData(data, columns, hasHeader)

    // Initialize k clusters from k random rows
    var clusterMeans = filteredData.shuffled().take(k).toList()

    while (difference > epsilon && iteration < maxIterations) {

        // Add each row to the closest cluster
        var clusters = assignRowsToClosestCluster(filteredData, clusterMeans)

        // Calculate new mean of each cluster
        var newClusterMeans = getClusterMeans(filteredData, clusters)

        // Calc change
        difference = calcChange(clusterMeans, newClusterMeans)

        // Iterate until convergence of epsilon distance or maxIterations
        iteration++;

        //updating new clusters as clusters for next iteration
        clusterMeans = newClusterMeans
    }
    return clusterMeans
}

fun calcChange(clusterMeans: List<DoubleArray>, newClusterMeans: List<DoubleArray>): Double {
    var distance = 0.0
    for (index in clusterMeans.indices) {
        distance += calcDistance(clusterMeans[index], newClusterMeans[index])
    }
    return distance
}

fun filterData(data: List<List<String>>, columns: IntArray, hasHeader: Boolean = true): List<DoubleArray> {
    val start: Int = if (hasHeader) 1 else 0
    val filtered = mutableListOf<DoubleArray>()
    for (i in start until data.size) {
        try {
            filtered.add(DoubleArray(columns.size) { data[i][columns[it]].toDouble() })
        } catch (e: NumberFormatException) {
            println("Removing row $i, due to invalid or missing values")
        }
    }
    return filtered.toList()
}

fun assignRowsToClosestCluster(data: List<DoubleArray>, clusterMeans: List<DoubleArray>): List<MutableList<Int>> {
    // Each cluster list contains the row index of the row closest to it
    val clusters: List<MutableList<Int>> = List(clusterMeans.size) { mutableListOf() }
    for (rowIndex in data.indices) {
        var closestDistance = Double.MAX_VALUE
        var closestClusterIndex = 0
        for (clusterIndex in clusterMeans.indices) {
            val distance = calcDistance(clusterMeans[clusterIndex], data[rowIndex])
            if (distance < closestDistance) {
                closestDistance = distance
                closestClusterIndex = clusterIndex
            }
        }
        clusters[closestClusterIndex].add(rowIndex)
    }
    return clusters
}

fun calcDistance(row1: DoubleArray, row2: DoubleArray): Double {
    // Distance Formula sqrt((x1-x2)^2 + (y1-y2)^2 + ... )
    var calc = 0.0
    for (i in row1.indices) {
        calc += (row1[i] - row2[i]).pow(2)
    }
    return sqrt(calc)
}

fun getClusterMeans(data: List<DoubleArray>, clusters: List<MutableList<Int>>): List<DoubleArray> {
    var clusterMeans = List(clusters.size) { DoubleArray(data[0].size) {0.0} }
    for (clusterIndex in clusters.indices) {
        for (colIndex in data[0].indices) {

            for (rowIndex in clusters[clusterIndex].indices) {
                val row = data[clusters[clusterIndex][rowIndex]]
                clusterMeans[clusterIndex][colIndex] += row[colIndex]
            }
            clusterMeans[clusterIndex][colIndex] /= clusters[clusterIndex].size.toDouble()
        }
    }
    return clusterMeans
}

fun normalize(value: Double, min: Double, max: Double): Double {
    return 1 - (value - min) / (max - min)
}